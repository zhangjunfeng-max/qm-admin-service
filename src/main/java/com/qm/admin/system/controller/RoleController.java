package com.qm.admin.system.controller;

import com.qm.admin.common.model.PageResult;
import com.qm.admin.system.dto.RoleBatchDeleteRequest;
import com.qm.admin.system.dto.RoleCreateRequest;
import com.qm.admin.system.dto.RolePageItemResponse;
import com.qm.admin.system.dto.RolePageQuery;
import com.qm.admin.system.dto.RoleUpdateRequest;
import com.qm.admin.system.dto.IdsAssignmentRequest;
import com.qm.admin.system.dto.RoleMenuAssignmentResponse;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.service.SystemRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SystemRole", description = "系统角色")
@RestController
@RequestMapping("/system/role")
public class RoleController {

    private final SystemRoleService roleService;

    public RoleController(SystemRoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "分页查询当前租户角色")
    @PreAuthorize("@ss.hasPermission('system:role:view')")
    @GetMapping("/page")
    public PageResult<RolePageItemResponse> getRolePage(@Valid RolePageQuery query,
                                                        @AuthenticationPrincipal SystemPrincipal principal) {
        return roleService.getRolePage(query, principal);
    }

    @Operation(summary = "获取当前租户角色详情")
    @PreAuthorize("@ss.hasPermission('system:role:view')")
    @GetMapping("/{roleId}")
    public RolePageItemResponse getRole(@PathVariable Long roleId,
                                        @AuthenticationPrincipal SystemPrincipal principal) {
        return roleService.getRole(roleId, principal);
    }

    @Operation(summary = "获取角色菜单分配数据")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    @GetMapping("/{roleId}/menus")
    public RoleMenuAssignmentResponse getMenuAssignment(@PathVariable Long roleId,
                                                        @AuthenticationPrincipal SystemPrincipal principal) {
        return roleService.getMenuAssignment(roleId, principal);
    }

    @Operation(summary = "保存角色菜单分配")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    @PutMapping("/{roleId}/menus")
    public void assignMenus(@PathVariable Long roleId, @Valid @RequestBody IdsAssignmentRequest request,
                            @AuthenticationPrincipal SystemPrincipal principal) {
        roleService.assignMenus(roleId, request, principal);
    }

    @Operation(summary = "新增当前租户角色")
    @PreAuthorize("@ss.hasPermission('system:role:create')")
    @PostMapping
    public RolePageItemResponse createRole(@Valid @RequestBody RoleCreateRequest request,
                                           @AuthenticationPrincipal SystemPrincipal principal) {
        return roleService.createRole(request, principal);
    }

    @Operation(summary = "编辑当前租户角色")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    @PutMapping("/{roleId}")
    public RolePageItemResponse updateRole(@PathVariable Long roleId,
                                           @Valid @RequestBody RoleUpdateRequest request,
                                           @AuthenticationPrincipal SystemPrincipal principal) {
        return roleService.updateRole(roleId, request, principal);
    }

    @Operation(summary = "删除当前租户角色")
    @PreAuthorize("@ss.hasPermission('system:role:delete')")
    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable Long roleId,
                           @AuthenticationPrincipal SystemPrincipal principal) {
        roleService.deleteRoles(List.of(roleId), principal);
    }

    @Operation(summary = "批量删除当前租户角色")
    @PreAuthorize("@ss.hasPermission('system:role:delete')")
    @PostMapping("/batch-delete")
    public void batchDeleteRoles(@Valid @RequestBody RoleBatchDeleteRequest request,
                                 @AuthenticationPrincipal SystemPrincipal principal) {
        roleService.deleteRoles(request.roleIds(), principal);
    }
}
