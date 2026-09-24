package com.qm.admin.system.controller;

import com.qm.admin.system.dto.MenuResponse;
import com.qm.admin.system.dto.MenuCreateRequest;
import com.qm.admin.system.dto.MenuPageItemResponse;
import com.qm.admin.system.dto.MenuPageQuery;
import com.qm.admin.system.dto.MenuUpdateRequest;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.service.SystemMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "SystemMenu", description = "系统菜单")
@RestController
@RequestMapping("/system/menu")
public class MenuController {

    private final SystemMenuService menuService;

    public MenuController(SystemMenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "获取当前用户所有菜单")
    @GetMapping("/all")
    public List<MenuResponse> getAllMenus(@AuthenticationPrincipal SystemPrincipal principal) {
        return menuService.getMenus(principal.userId(), principal.tenantId());
    }

    @Operation(summary = "查询菜单直属子节点")
    @PreAuthorize("@ss.hasPermission('system:menu:view')")
    @GetMapping("/children")
    public List<MenuPageItemResponse> getChildren(@Valid MenuPageQuery query) {
        return menuService.getChildren(query);
    }

    @Operation(summary = "获取菜单详情")
    @PreAuthorize("@ss.hasPermission('system:menu:view')")
    @GetMapping("/{menuId}")
    public MenuPageItemResponse getMenu(@PathVariable Long menuId) { return menuService.getMenu(menuId); }

    @Operation(summary = "新增菜单")
    @PreAuthorize("@ss.hasPermission('system:menu:create')")
    @PostMapping
    public MenuPageItemResponse createMenu(@Valid @RequestBody MenuCreateRequest request) { return menuService.createMenu(request); }

    @Operation(summary = "编辑菜单")
    @PreAuthorize("@ss.hasPermission('system:menu:update')")
    @PutMapping("/{menuId}")
    public MenuPageItemResponse updateMenu(@PathVariable Long menuId, @Valid @RequestBody MenuUpdateRequest request) {
        return menuService.updateMenu(menuId, request);
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("@ss.hasPermission('system:menu:delete')")
    @DeleteMapping("/{menuId}")
    public void deleteMenu(@PathVariable Long menuId) { menuService.deleteMenu(menuId); }
}
