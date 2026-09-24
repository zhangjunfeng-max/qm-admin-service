package com.qm.admin.system.controller;

import com.qm.admin.common.model.PageResult;
import com.qm.admin.system.dto.DictCreateRequest;
import com.qm.admin.system.dto.DictItemBatchDeleteRequest;
import com.qm.admin.system.dto.DictItemCreateRequest;
import com.qm.admin.system.dto.DictItemResponse;
import com.qm.admin.system.dto.DictItemUpdateRequest;
import com.qm.admin.system.dto.DictPageItemResponse;
import com.qm.admin.system.dto.DictPageQuery;
import com.qm.admin.system.dto.DictUpdateRequest;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.service.SystemDictService;
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

@Tag(name = "SystemDict", description = "系统字典")
@RestController
@RequestMapping("/system/dict")
public class DictController {
    private final SystemDictService dictService;

    public DictController(SystemDictService dictService) { this.dictService = dictService; }

    @Operation(summary = "分页查询字典")
    @PreAuthorize("@ss.hasPermission('system:dict:view')")
    @GetMapping("/page")
    public PageResult<DictPageItemResponse> getPage(@Valid DictPageQuery query, @AuthenticationPrincipal SystemPrincipal principal) {
        return dictService.getDictPage(query, principal);
    }

    @Operation(summary = "获取字典详情")
    @PreAuthorize("@ss.hasPermission('system:dict:view')")
    @GetMapping("/{dictId}")
    public DictPageItemResponse get(@PathVariable Long dictId, @AuthenticationPrincipal SystemPrincipal principal) {
        return dictService.getDict(dictId, principal);
    }

    @Operation(summary = "新增字典")
    @PreAuthorize("@ss.hasPermission('system:dict:create')")
    @PostMapping
    public DictPageItemResponse create(@Valid @RequestBody DictCreateRequest request, @AuthenticationPrincipal SystemPrincipal principal) {
        return dictService.createDict(request, principal);
    }

    @Operation(summary = "编辑字典")
    @PreAuthorize("@ss.hasPermission('system:dict:update')")
    @PutMapping("/{dictId}")
    public DictPageItemResponse update(@PathVariable Long dictId, @Valid @RequestBody DictUpdateRequest request, @AuthenticationPrincipal SystemPrincipal principal) {
        return dictService.updateDict(dictId, request, principal);
    }

    @Operation(summary = "删除字典")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    @DeleteMapping("/{dictId}")
    public void delete(@PathVariable Long dictId, @AuthenticationPrincipal SystemPrincipal principal) { dictService.deleteDict(dictId, principal); }

    @Operation(summary = "查询字典项")
    @PreAuthorize("@ss.hasPermission('system:dict:view')")
    @GetMapping("/{dictId}/items")
    public List<DictItemResponse> getItems(@PathVariable Long dictId, @AuthenticationPrincipal SystemPrincipal principal) { return dictService.getItems(dictId, principal); }

    @Operation(summary = "获取字典项详情")
    @PreAuthorize("@ss.hasPermission('system:dict:view')")
    @GetMapping("/{dictId}/items/{itemId}")
    public DictItemResponse getItem(@PathVariable Long dictId, @PathVariable Long itemId, @AuthenticationPrincipal SystemPrincipal principal) { return dictService.getItem(dictId, itemId, principal); }

    @Operation(summary = "新增字典项")
    @PreAuthorize("@ss.hasPermission('system:dict:create')")
    @PostMapping("/{dictId}/items")
    public DictItemResponse createItem(@PathVariable Long dictId, @Valid @RequestBody DictItemCreateRequest request, @AuthenticationPrincipal SystemPrincipal principal) { return dictService.createItem(dictId, request, principal); }

    @Operation(summary = "编辑字典项")
    @PreAuthorize("@ss.hasPermission('system:dict:update')")
    @PutMapping("/{dictId}/items/{itemId}")
    public DictItemResponse updateItem(@PathVariable Long dictId, @PathVariable Long itemId, @Valid @RequestBody DictItemUpdateRequest request, @AuthenticationPrincipal SystemPrincipal principal) { return dictService.updateItem(dictId, itemId, request, principal); }

    @Operation(summary = "删除字典项")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    @DeleteMapping("/{dictId}/items/{itemId}")
    public void deleteItem(@PathVariable Long dictId, @PathVariable Long itemId, @AuthenticationPrincipal SystemPrincipal principal) { dictService.deleteItems(dictId, List.of(itemId), principal); }

    @Operation(summary = "批量删除字典项")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    @PostMapping("/{dictId}/items/batch-delete")
    public void deleteItems(@PathVariable Long dictId, @Valid @RequestBody DictItemBatchDeleteRequest request, @AuthenticationPrincipal SystemPrincipal principal) { dictService.deleteItems(dictId, request.itemIds(), principal); }
}
