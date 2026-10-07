package com.qm.admin.system.controller;

import com.qm.admin.common.model.PageResult;
import com.qm.admin.system.dto.TenantCreateRequest;
import com.qm.admin.system.dto.TenantPageItemResponse;
import com.qm.admin.system.dto.TenantPageQuery;
import com.qm.admin.system.dto.TenantUpdateRequest;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.service.SystemTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SystemTenant", description = "系统租户")
@RestController
@RequestMapping("/system/tenant")
public class TenantController {
    private final SystemTenantService tenantService;
    public TenantController(SystemTenantService tenantService) { this.tenantService = tenantService; }
    @Operation(summary = "分页查询租户")
    @PreAuthorize("@ss.hasPermission('system:tenant:view')")
    @GetMapping("/page") public PageResult<TenantPageItemResponse> getPage(@Valid TenantPageQuery query) { return tenantService.getPage(query); }
    @Operation(summary = "获取租户详情")
    @PreAuthorize("@ss.hasPermission('system:tenant:view')")
    @GetMapping("/{tenantId}") public TenantPageItemResponse get(@PathVariable Long tenantId) { return tenantService.get(tenantId); }
    @Operation(summary = "新增租户，编码由后台生成")
    @PreAuthorize("@ss.hasPermission('system:tenant:create')")
    @PostMapping public TenantPageItemResponse create(@Valid @RequestBody TenantCreateRequest request, @AuthenticationPrincipal SystemPrincipal principal) { return tenantService.create(request, principal); }
    @Operation(summary = "编辑租户，编码不可修改")
    @PreAuthorize("@ss.hasPermission('system:tenant:update')")
    @PutMapping("/{tenantId}") public TenantPageItemResponse update(@PathVariable Long tenantId, @Valid @RequestBody TenantUpdateRequest request, @AuthenticationPrincipal SystemPrincipal principal) { return tenantService.update(tenantId, request, principal); }
    @Operation(summary = "删除租户")
    @PreAuthorize("@ss.hasPermission('system:tenant:delete')")
    @DeleteMapping("/{tenantId}") public void delete(@PathVariable Long tenantId, @AuthenticationPrincipal SystemPrincipal principal) { tenantService.delete(tenantId, principal); }
}
