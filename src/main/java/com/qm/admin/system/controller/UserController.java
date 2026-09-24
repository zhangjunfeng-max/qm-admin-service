package com.qm.admin.system.controller;

import com.qm.admin.common.model.PageResult;
import com.qm.admin.system.dto.UserBatchDeleteRequest;
import com.qm.admin.system.dto.UserCreateRequest;
import com.qm.admin.system.dto.UserInfoResponse;
import com.qm.admin.system.dto.UserPageItemResponse;
import com.qm.admin.system.dto.UserPageQuery;
import com.qm.admin.system.dto.UserUpdateRequest;
import com.qm.admin.system.dto.IdsAssignmentRequest;
import com.qm.admin.system.dto.UserRoleAssignmentResponse;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.service.SystemUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zjf
 */
@Tag(name = "SystemUser", description = "系统用户")
@RestController
@RequestMapping("/system/user")
public class UserController {

    private final SystemUserService userService;

    public UserController(SystemUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public UserInfoResponse getUserInfo(@AuthenticationPrincipal SystemPrincipal principal) {
        return userService.getUserInfo(principal);
    }

    @Operation(summary = "分页查询当前租户用户")
    @PreAuthorize("@ss.hasPermission('system:user:view')")
    @GetMapping("/page")
    public PageResult<UserPageItemResponse> getUserPage(@Valid UserPageQuery query,
                                                        @AuthenticationPrincipal SystemPrincipal principal) {
        return userService.getUserPage(query, principal);
    }

    @Operation(summary = "获取当前租户用户详情")
    @PreAuthorize("@ss.hasPermission('system:user:view')")
    @GetMapping("/{userId}")
    public UserPageItemResponse getUser(@PathVariable Long userId,
                                        @AuthenticationPrincipal SystemPrincipal principal) {
        return userService.getUser(userId, principal);
    }

    @Operation(summary = "获取用户角色分配数据")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    @GetMapping("/{userId}/roles")
    public UserRoleAssignmentResponse getRoleAssignment(@PathVariable Long userId,
                                                        @AuthenticationPrincipal SystemPrincipal principal) {
        return userService.getRoleAssignment(userId, principal);
    }

    @Operation(summary = "保存用户角色分配")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    @PutMapping("/{userId}/roles")
    public void assignRoles(@PathVariable Long userId, @Valid @RequestBody IdsAssignmentRequest request,
                            @AuthenticationPrincipal SystemPrincipal principal) {
        userService.assignRoles(userId, request, principal);
    }

    @Operation(summary = "新增当前租户用户")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    @PostMapping
    public UserPageItemResponse createUser(@Valid @RequestBody UserCreateRequest request,
                                           @AuthenticationPrincipal SystemPrincipal principal) {
        return userService.createUser(request, principal);
    }

    @Operation(summary = "编辑当前租户用户")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    @PutMapping("/{userId}")
    public UserPageItemResponse updateUser(@PathVariable Long userId,
                                           @Valid @RequestBody UserUpdateRequest request,
                                           @AuthenticationPrincipal SystemPrincipal principal) {
        return userService.updateUser(userId, request, principal);
    }

    @Operation(summary = "从当前租户移除用户")
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId,
                           @AuthenticationPrincipal SystemPrincipal principal) {
        userService.deleteUsers(List.of(userId), principal);
    }

    @Operation(summary = "批量从当前租户移除用户")
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    @PostMapping("/batch-delete")
    public void batchDeleteUsers(@Valid @RequestBody UserBatchDeleteRequest request,
                                 @AuthenticationPrincipal SystemPrincipal principal) {
        userService.deleteUsers(request.userIds(), principal);
    }
}
