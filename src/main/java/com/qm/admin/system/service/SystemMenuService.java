package com.qm.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qm.admin.system.dto.MenuMetaResponse;
import com.qm.admin.system.dto.MenuResponse;
import com.qm.admin.system.dto.MenuCreateRequest;
import com.qm.admin.system.dto.MenuPageItemResponse;
import com.qm.admin.system.dto.MenuPageQuery;
import com.qm.admin.system.dto.MenuUpdateRequest;
import com.qm.admin.common.constants.Constants;
import com.qm.admin.common.exception.BusinessException;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.system.entity.SystemMenu;
import com.qm.admin.system.entity.SystemRoleMenu;
import com.qm.admin.system.mapper.SystemMenuMapper;
import com.qm.admin.system.mapper.SystemRoleMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemMenuService {

    private static final String BUTTON_TYPE = "BUTTON";

    private final SystemMenuMapper menuMapper;
    private final SystemRoleMenuMapper roleMenuMapper;
    private final SystemAccessCache accessCache;
    private final SystemAccessCacheInvalidator cacheInvalidator;

    public SystemMenuService(SystemMenuMapper menuMapper, SystemRoleMenuMapper roleMenuMapper,
                             SystemAccessCache accessCache,
                             SystemAccessCacheInvalidator cacheInvalidator) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.accessCache = accessCache;
        this.cacheInvalidator = cacheInvalidator;
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenus(Long userId, Long tenantId) {
        return accessCache.getOrLoadMenus(tenantId, userId, () -> {
            List<SystemMenu> menus = getAuthorizedMenus(userId, tenantId).stream()
                    .filter(menu -> !BUTTON_TYPE.equals(menu.getMenuType()))
                    .filter(menu -> Integer.valueOf(1).equals(menu.getVisible()))
                    .toList();
            Map<Long, List<SystemMenu>> childrenByParent = menus.stream()
                    .collect(Collectors.groupingBy(SystemMenu::getParentId));
            return childrenByParent.getOrDefault(0L, List.of()).stream()
                    .map(menu -> toMenuResponse(menu, childrenByParent))
                    .toList();
        });
    }

    @Transactional(readOnly = true)
    public List<String> getAccessCodes(Long userId, Long tenantId) {
        return loadAccessCodesFromDatabase(userId, tenantId);
    }

    public List<String> loadAccessCodesFromDatabase(Long userId, Long tenantId) {
        return menuMapper.selectAccessCodes(userId, tenantId);
    }

    @Transactional(readOnly = true)
    public List<MenuPageItemResponse> getChildren(MenuPageQuery query) {
        return menuMapper.selectChildren(query.getParentId() == null ? 0L : query.getParentId(), query.getStatus());
    }

    @Transactional(readOnly = true)
    public MenuPageItemResponse getMenu(Long menuId) {
        SystemMenu menu = requireMenu(menuId);
        return toPageItem(menu);
    }

    @Transactional
    public MenuPageItemResponse createMenu(MenuCreateRequest request) {
        Long parentId = defaultParentId(request.parentId());
        ensureParentExists(parentId);
        SystemMenu menu = new SystemMenu();
        apply(menu, parentId, request.menuType(), request.name(), request.path(), request.component(), request.redirect(),
                request.icon(), request.title(), request.sort(), request.status(), request.visible(), request.keepAlive(),
                request.affixTab(), request.authCode());
        menuMapper.insert(menu);
        cacheInvalidator.all();
        return getMenu(menu.getId());
    }

    @Transactional
    public MenuPageItemResponse updateMenu(Long menuId, MenuUpdateRequest request) {
        SystemMenu current = requireMenu(menuId);
        Long parentId = defaultParentId(request.parentId());
        if (menuId.equals(parentId)) {
            throw new BusinessException(CommonResultCode.CONFLICT, "菜单不能将自身设置为父节点");
        }
        ensureParentExists(parentId);
        if (isDescendant(menuId, parentId)) {
            throw new BusinessException(CommonResultCode.CONFLICT, "菜单不能移动到自己的子节点下");
        }
        apply(current, parentId, request.menuType(), request.name(), request.path(), request.component(), request.redirect(),
                request.icon(), request.title(), request.sort(), request.status(), request.visible(), request.keepAlive(),
                request.affixTab(), request.authCode());
        menuMapper.updateById(current);
        cacheInvalidator.all();
        return getMenu(menuId);
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        requireMenu(menuId);
        Long childCount = menuMapper.selectCount(Wrappers.<SystemMenu>lambdaQuery().eq(SystemMenu::getParentId, menuId));
        if (childCount > 0) {
            throw new BusinessException(CommonResultCode.CONFLICT, "请先删除子菜单");
        }
        roleMenuMapper.delete(Wrappers.<SystemRoleMenu>lambdaQuery().eq(SystemRoleMenu::getMenuId, menuId));
        menuMapper.deleteById(menuId);
        cacheInvalidator.all();
    }

    private List<SystemMenu> getAuthorizedMenus(Long userId, Long tenantId) {
        return menuMapper.selectAuthorizedMenus(userId, tenantId);
    }

    private SystemMenu requireMenu(Long menuId) {
        SystemMenu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "菜单不存在");
        }
        return menu;
    }

    private void ensureParentExists(Long parentId) {
        if (parentId != 0L && menuMapper.selectById(parentId) == null) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "父菜单不存在");
        }
    }

    private boolean isDescendant(Long menuId, Long parentId) {
        Long current = parentId;
        while (current != 0L) {
            SystemMenu parent = menuMapper.selectById(current);
            if (parent == null) return false;
            if (menuId.equals(parent.getParentId())) return true;
            current = parent.getParentId();
        }
        return false;
    }

    private void apply(SystemMenu menu, Long parentId, String menuType, String name, String path, String component,
                       String redirect, String icon, String title, Integer sort, Integer status, Integer visible,
                       Integer keepAlive, Integer affixTab, String authCode) {
        menu.setParentId(parentId);
        menu.setMenuType(menuType);
        menu.setName(name);
        menu.setPath(defaultString(path));
        menu.setComponent(defaultString(component));
        menu.setRedirect(defaultString(redirect));
        menu.setIcon(defaultString(icon));
        menu.setTitle(title);
        menu.setSort(sort == null ? 0 : sort);
        menu.setStatus(status == null ? Constants.YES : status);
        menu.setVisible(visible == null ? Constants.YES : visible);
        menu.setKeepAlive(keepAlive == null ? Constants.NO : keepAlive);
        menu.setAffixTab(affixTab == null ? Constants.NO : affixTab);
        menu.setAuthCode(defaultString(authCode));
    }

    private MenuPageItemResponse toPageItem(SystemMenu menu) {
        MenuPageItemResponse response = new MenuPageItemResponse();
        response.setId(menu.getId()); response.setParentId(menu.getParentId()); response.setMenuType(menu.getMenuType());
        response.setName(menu.getName()); response.setPath(menu.getPath()); response.setComponent(menu.getComponent());
        response.setRedirect(menu.getRedirect()); response.setIcon(menu.getIcon()); response.setTitle(menu.getTitle());
        response.setSort(menu.getSort()); response.setStatus(menu.getStatus()); response.setVisible(menu.getVisible());
        response.setKeepAlive(menu.getKeepAlive()); response.setAffixTab(menu.getAffixTab()); response.setAuthCode(menu.getAuthCode());
        response.setHasChildren(menuMapper.selectCount(Wrappers.<SystemMenu>lambdaQuery().eq(SystemMenu::getParentId, menu.getId())) > 0);
        response.setCreateTime(menu.getCreateTime()); response.setUpdateTime(menu.getUpdateTime());
        return response;
    }

    private Long defaultParentId(Long parentId) { return parentId == null ? 0L : parentId; }
    private String defaultString(String value) { return value == null ? "" : value; }

    private MenuResponse toMenuResponse(SystemMenu menu, Map<Long, List<SystemMenu>> childrenByParent) {
        List<MenuResponse> children = new ArrayList<>();
        for (SystemMenu child : childrenByParent.getOrDefault(menu.getId(), List.of())) {
            children.add(toMenuResponse(child, childrenByParent));
        }
        MenuMetaResponse meta = new MenuMetaResponse(
                menu.getTitle(),
                menu.getIcon(),
                menu.getSort(),
                toBoolean(menu.getKeepAlive()),
                toBoolean(menu.getAffixTab())
        );
        return new MenuResponse(
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getRedirect(),
                meta,
                children.isEmpty() ? null : children
        );
    }

    private Boolean toBoolean(Integer value) {
        return value == null ? null : value == 1;
    }
}
