package com.qm.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qm.admin.common.constants.Constants;
import com.qm.admin.common.exception.BusinessException;
import com.qm.admin.common.model.PageResult;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.common.util.ValueUtils;
import com.qm.admin.system.dto.RoleCreateRequest;
import com.qm.admin.system.dto.RolePageItemResponse;
import com.qm.admin.system.dto.RolePageQuery;
import com.qm.admin.system.dto.RoleUpdateRequest;
import com.qm.admin.system.converter.RoleStructMapper;
import com.qm.admin.system.dto.IdsAssignmentRequest;
import com.qm.admin.system.dto.MenuAssignNode;
import com.qm.admin.system.dto.RoleMenuAssignmentResponse;
import com.qm.admin.system.entity.SystemRole;
import com.qm.admin.system.entity.SystemRoleMenu;
import com.qm.admin.system.entity.SystemMenu;
import com.qm.admin.system.entity.SystemUserRole;
import com.qm.admin.system.mapper.SystemRoleMapper;
import com.qm.admin.system.mapper.SystemRoleMenuMapper;
import com.qm.admin.system.mapper.SystemMenuMapper;
import com.qm.admin.system.mapper.SystemUserRoleMapper;
import com.qm.admin.system.security.SystemPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemRoleService {

    private final SystemRoleMapper roleMapper;
    private final SystemUserRoleMapper userRoleMapper;
    private final SystemRoleMenuMapper roleMenuMapper;
    private final SystemMenuMapper menuMapper;
    private final SystemAccessCacheInvalidator cacheInvalidator;

    public SystemRoleService(SystemRoleMapper roleMapper, SystemUserRoleMapper userRoleMapper,
                             SystemRoleMenuMapper roleMenuMapper, SystemMenuMapper menuMapper,
                             SystemAccessCacheInvalidator cacheInvalidator) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.cacheInvalidator = cacheInvalidator;
    }

    @Transactional(readOnly = true)
    public PageResult<RolePageItemResponse> getRolePage(RolePageQuery query, SystemPrincipal principal) {
        Page<RolePageItemResponse> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(roleMapper.selectRolePage(page, principal.tenantId(), query));
    }

    @Transactional(readOnly = true)
    public RolePageItemResponse getRole(Long roleId, SystemPrincipal principal) {
        RolePageItemResponse role = roleMapper.selectTenantRole(principal.tenantId(), roleId);
        if (role == null) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "当前租户下不存在该角色");
        }
        return role;
    }

    @Transactional(readOnly = true)
    public RoleMenuAssignmentResponse getMenuAssignment(Long roleId, SystemPrincipal principal) {
        getRole(roleId, principal);
        List<SystemMenu> menus = menuMapper.selectList(Wrappers.<SystemMenu>lambdaQuery()
                .orderByAsc(SystemMenu::getSort).orderByAsc(SystemMenu::getId));
        Map<Long, List<SystemMenu>> children = menus.stream().collect(Collectors.groupingBy(SystemMenu::getParentId));
        List<MenuAssignNode> roots = children.getOrDefault(0L, List.of()).stream()
                .map(menu -> toAssignNode(menu, children)).toList();
        List<Long> selected = roleMenuMapper.selectList(Wrappers.<SystemRoleMenu>lambdaQuery()
                        .eq(SystemRoleMenu::getTenantId, principal.tenantId())
                        .eq(SystemRoleMenu::getRoleId, roleId))
                .stream().map(SystemRoleMenu::getMenuId).toList();
        return new RoleMenuAssignmentResponse(roots, selected);
    }

    @Transactional
    public void assignMenus(Long roleId, IdsAssignmentRequest request, SystemPrincipal principal) {
        getRole(roleId, principal);
        List<Long> menuIds = request.ids().stream().distinct().toList();
        if (!menuIds.isEmpty() && menuMapper.selectCount(Wrappers.<SystemMenu>lambdaQuery()
                .in(SystemMenu::getId, menuIds)) != menuIds.size()) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "存在无效菜单");
        }
        roleMenuMapper.delete(Wrappers.<SystemRoleMenu>lambdaQuery()
                .eq(SystemRoleMenu::getTenantId, principal.tenantId())
                .eq(SystemRoleMenu::getRoleId, roleId));
        menuIds.forEach(menuId -> {
            SystemRoleMenu relation = new SystemRoleMenu();
            relation.setTenantId(principal.tenantId());
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            relation.setCreateBy(principal.userId());
            roleMenuMapper.insert(relation);
        });
        cacheInvalidator.tenant(principal.tenantId());
    }

    private MenuAssignNode toAssignNode(SystemMenu menu, Map<Long, List<SystemMenu>> children) {
        return new MenuAssignNode(menu.getId(), menu.getTitle(), menu.getMenuType(), menu.getAuthCode(),
                children.getOrDefault(menu.getId(), List.of()).stream()
                .map(child -> toAssignNode(child, children)).toList());
    }

    @Transactional
    public RolePageItemResponse createRole(RoleCreateRequest request, SystemPrincipal principal) {
        ensureRoleCodeAvailable(request.roleCode(), principal.tenantId());
        SystemRole role = RoleStructMapper.INSTANCE.toEntity(request);
        role.setTenantId(principal.tenantId());
        role.setStatus(ValueUtils.defaultIfNull(request.status(), Constants.YES));
        role.setRemark(ValueUtils.defaultString(request.remark()));
        roleMapper.insert(role);
        return getRole(role.getId(), principal);
    }

    @Transactional
    public RolePageItemResponse updateRole(Long roleId, RoleUpdateRequest request, SystemPrincipal principal) {
        RolePageItemResponse current = getRole(roleId, principal);
        int status = ValueUtils.defaultIfNull(request.status(), current.getStatus());
        if (status == Constants.NO && isCurrentUserRole(roleId, principal)) {
            throw new BusinessException(CommonResultCode.CONFLICT, "不能禁用当前登录用户绑定的角色");
        }

        SystemRole role = RoleStructMapper.INSTANCE.toEntity(request);
        role.setId(roleId);
        role.setStatus(status);
        role.setRemark(ValueUtils.defaultString(request.remark()));
        roleMapper.updateById(role);
        cacheInvalidator.tenant(principal.tenantId());
        return getRole(roleId, principal);
    }

    @Transactional
    public void deleteRoles(List<Long> roleIds, SystemPrincipal principal) {
        List<Long> distinctIds = roleIds.stream().distinct().toList();
        List<SystemRole> roles = roleMapper.selectList(Wrappers.<SystemRole>lambdaQuery()
                .in(SystemRole::getId, distinctIds));
        if (roles.size() != distinctIds.size()) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "部分角色不属于当前租户");
        }

        Long currentRoleCount = userRoleMapper.selectCount(Wrappers.<SystemUserRole>lambdaQuery()
                .eq(SystemUserRole::getUserId, principal.userId())
                .in(SystemUserRole::getRoleId, distinctIds));
        if (currentRoleCount > 0) {
            throw new BusinessException(CommonResultCode.CONFLICT, "不能删除当前登录用户绑定的角色");
        }

        userRoleMapper.delete(Wrappers.<SystemUserRole>lambdaQuery()
                .in(SystemUserRole::getRoleId, distinctIds));
        roleMenuMapper.delete(Wrappers.<SystemRoleMenu>lambdaQuery()
                .in(SystemRoleMenu::getRoleId, distinctIds));
        roleMapper.delete(Wrappers.<SystemRole>lambdaQuery()
                .in(SystemRole::getId, distinctIds));
        cacheInvalidator.tenant(principal.tenantId());
    }

    private void ensureRoleCodeAvailable(String roleCode, Long tenantId) {
        Long count = roleMapper.selectCount(Wrappers.<SystemRole>lambdaQuery()
                .eq(SystemRole::getTenantId, tenantId)
                .eq(SystemRole::getRoleCode, roleCode));
        if (count > 0) {
            throw new BusinessException(CommonResultCode.CONFLICT, "角色编码已存在");
        }
    }

    private boolean isCurrentUserRole(Long roleId, SystemPrincipal principal) {
        return userRoleMapper.selectCount(Wrappers.<SystemUserRole>lambdaQuery()
                .eq(SystemUserRole::getUserId, principal.userId())
                .eq(SystemUserRole::getRoleId, roleId)) > 0;
    }

}
