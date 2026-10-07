package com.qm.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qm.admin.system.dto.MenuMetaResponse;
import com.qm.admin.system.dto.MenuResponse;
import com.qm.admin.system.dto.MenuCreateRequest;
import com.qm.admin.system.dto.MenuPageItemResponse;
import com.qm.admin.system.dto.MenuPageQuery;
import com.qm.admin.system.dto.MenuUpdateRequest;
import com.qm.admin.system.converter.MenuStructMapper;
import com.qm.admin.common.constants.Constants;
import com.qm.admin.common.exception.BusinessException;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.common.util.ValueUtils;
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
        SystemMenu menu = MenuStructMapper.INSTANCE.toEntity(request);
        normalizeMenu(menu, parentId);
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
        MenuStructMapper.INSTANCE.updateEntity(request, current);
        normalizeMenu(current, parentId);
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

    private void normalizeMenu(SystemMenu menu, Long parentId) {
        menu.setParentId(parentId);
        menu.setPath(ValueUtils.defaultString(menu.getPath()));
        menu.setComponent(ValueUtils.defaultString(menu.getComponent()));
        menu.setRedirect(ValueUtils.defaultString(menu.getRedirect()));
        menu.setIcon(ValueUtils.defaultString(menu.getIcon()));
        menu.setSort(menu.getSort() == null ? 0 : menu.getSort());
        menu.setStatus(menu.getStatus() == null ? Constants.YES : menu.getStatus());
        menu.setVisible(menu.getVisible() == null ? Constants.YES : menu.getVisible());
        menu.setKeepAlive(menu.getKeepAlive() == null ? Constants.NO : menu.getKeepAlive());
        menu.setAffixTab(menu.getAffixTab() == null ? Constants.NO : menu.getAffixTab());
        menu.setAuthCode(ValueUtils.defaultString(menu.getAuthCode()));
    }

    private MenuPageItemResponse toPageItem(SystemMenu menu) {
        MenuPageItemResponse response = MenuStructMapper.INSTANCE.toPageItem(menu);
        response.setHasChildren(menuMapper.selectCount(Wrappers.<SystemMenu>lambdaQuery().eq(SystemMenu::getParentId, menu.getId())) > 0);
        response.setCreateTime(menu.getCreateTime()); response.setUpdateTime(menu.getUpdateTime());
        return response;
    }

    private Long defaultParentId(Long parentId) { return parentId == null ? 0L : parentId; }
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
