package com.qm.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qm.admin.common.constants.Constants;
import com.qm.admin.common.exception.BusinessException;
import com.qm.admin.common.model.PageResult;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.common.util.ValueUtils;
import com.qm.admin.system.dto.DictCreateRequest;
import com.qm.admin.system.dto.DictItemBatchDeleteRequest;
import com.qm.admin.system.dto.DictItemCreateRequest;
import com.qm.admin.system.dto.DictItemResponse;
import com.qm.admin.system.dto.DictItemUpdateRequest;
import com.qm.admin.system.dto.DictPageItemResponse;
import com.qm.admin.system.dto.DictPageQuery;
import com.qm.admin.system.dto.DictUpdateRequest;
import com.qm.admin.system.converter.DictItemStructMapper;
import com.qm.admin.system.converter.DictStructMapper;
import com.qm.admin.system.entity.SystemDict;
import com.qm.admin.system.entity.SystemDictItem;
import com.qm.admin.system.mapper.SystemDictItemMapper;
import com.qm.admin.system.mapper.SystemDictMapper;
import com.qm.admin.system.security.SystemPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemDictService {

    private final SystemDictMapper dictMapper;
    private final SystemDictItemMapper itemMapper;

    public SystemDictService(SystemDictMapper dictMapper, SystemDictItemMapper itemMapper) {
        this.dictMapper = dictMapper;
        this.itemMapper = itemMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<DictPageItemResponse> getDictPage(DictPageQuery query, SystemPrincipal principal) {
        Page<DictPageItemResponse> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(dictMapper.selectDictPage(page, principal.tenantId(), query));
    }

    @Transactional(readOnly = true)
    public DictPageItemResponse getDict(Long dictId, SystemPrincipal principal) {
        DictPageItemResponse result = dictMapper.selectTenantDict(principal.tenantId(), dictId);
        if (result == null) throw notFound("字典不存在");
        return result;
    }

    @Transactional
    public DictPageItemResponse createDict(DictCreateRequest request, SystemPrincipal principal) {
        ensureDictCodeAvailable(request.dictCode(), principal.tenantId(), null);
        SystemDict dict = DictStructMapper.INSTANCE.toEntity(request);
        dict.setTenantId(principal.tenantId());
        dict.setStatus(ValueUtils.defaultIfNull(request.status(), Constants.YES));
        dict.setRemark(ValueUtils.defaultString(request.remark()));
        dictMapper.insert(dict);
        return getDict(dict.getId(), principal);
    }

    @Transactional
    public DictPageItemResponse updateDict(Long dictId, DictUpdateRequest request, SystemPrincipal principal) {
        DictPageItemResponse current = getDict(dictId, principal);
        SystemDict dict = DictStructMapper.INSTANCE.toEntity(request);
        dict.setId(dictId);
        dict.setStatus(ValueUtils.defaultIfNull(request.status(), current.getStatus()));
        dict.setRemark(ValueUtils.defaultString(request.remark()));
        dictMapper.updateById(dict);
        return getDict(dictId, principal);
    }

    @Transactional
    public void deleteDict(Long dictId, SystemPrincipal principal) {
        getDict(dictId, principal);
        itemMapper.delete(Wrappers.<SystemDictItem>lambdaQuery()
                .eq(SystemDictItem::getTenantId, principal.tenantId())
                .eq(SystemDictItem::getDictId, dictId));
        dictMapper.delete(Wrappers.<SystemDict>lambdaQuery()
                .eq(SystemDict::getTenantId, principal.tenantId()).eq(SystemDict::getId, dictId));
    }

    @Transactional(readOnly = true)
    public List<DictItemResponse> getItems(Long dictId, SystemPrincipal principal) {
        getDict(dictId, principal);
        return itemMapper.selectList(Wrappers.<SystemDictItem>lambdaQuery()
                        .eq(SystemDictItem::getTenantId, principal.tenantId())
                        .eq(SystemDictItem::getDictId, dictId)
                        .orderByAsc(SystemDictItem::getSort)
                        .orderByAsc(SystemDictItem::getId))
                .stream().map(this::toItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public DictItemResponse getItem(Long dictId, Long itemId, SystemPrincipal principal) {
        getDict(dictId, principal);
        return toItemResponse(requireItem(dictId, itemId, principal.tenantId()));
    }

    @Transactional
    public DictItemResponse createItem(Long dictId, DictItemCreateRequest request, SystemPrincipal principal) {
        getDict(dictId, principal);
        ensureItemCodeAvailable(dictId, request.itemCode(), principal.tenantId(), null);
        SystemDictItem item = DictItemStructMapper.INSTANCE.toEntity(request);
        item.setTenantId(principal.tenantId());
        item.setDictId(dictId);
        normalizeItem(item, request.sort(), request.color(), request.icon(), request.status());
        itemMapper.insert(item);
        return getItem(dictId, item.getId(), principal);
    }

    @Transactional
    public DictItemResponse updateItem(Long dictId, Long itemId, DictItemUpdateRequest request, SystemPrincipal principal) {
        SystemDictItem current = requireItem(dictId, itemId, principal.tenantId());
        ensureItemCodeAvailable(dictId, request.itemCode(), principal.tenantId(), itemId);
        DictItemStructMapper.INSTANCE.updateEntity(request, current);
        normalizeItem(current, request.sort(), request.color(), request.icon(), request.status());
        itemMapper.updateById(current);
        return getItem(dictId, itemId, principal);
    }

    @Transactional
    public void deleteItems(Long dictId, List<Long> itemIds, SystemPrincipal principal) {
        getDict(dictId, principal);
        List<Long> ids = itemIds.stream().distinct().toList();
        if (ids.isEmpty()) return;
        long count = itemMapper.selectCount(Wrappers.<SystemDictItem>lambdaQuery()
                .eq(SystemDictItem::getTenantId, principal.tenantId())
                .eq(SystemDictItem::getDictId, dictId).in(SystemDictItem::getId, ids));
        if (count != ids.size()) throw notFound("存在不属于当前字典的字典项");
        itemMapper.delete(Wrappers.<SystemDictItem>lambdaQuery()
                .eq(SystemDictItem::getTenantId, principal.tenantId())
                .eq(SystemDictItem::getDictId, dictId).in(SystemDictItem::getId, ids));
    }

    private SystemDictItem requireItem(Long dictId, Long itemId, Long tenantId) {
        SystemDictItem item = itemMapper.selectOne(Wrappers.<SystemDictItem>lambdaQuery()
                .eq(SystemDictItem::getTenantId, tenantId).eq(SystemDictItem::getDictId, dictId)
                .eq(SystemDictItem::getId, itemId).last("LIMIT 1"));
        if (item == null) throw notFound("字典项不存在");
        return item;
    }

    private void ensureDictCodeAvailable(String code, Long tenantId, Long excludedId) {
        var query = Wrappers.<SystemDict>lambdaQuery().eq(SystemDict::getTenantId, tenantId).eq(SystemDict::getDictCode, code);
        if (excludedId != null) query.ne(SystemDict::getId, excludedId);
        if (dictMapper.selectCount(query) > 0) throw new BusinessException(CommonResultCode.CONFLICT, "字典编码已存在");
    }

    private void ensureItemCodeAvailable(Long dictId, String code, Long tenantId, Long excludedId) {
        var query = Wrappers.<SystemDictItem>lambdaQuery().eq(SystemDictItem::getTenantId, tenantId)
                .eq(SystemDictItem::getDictId, dictId).eq(SystemDictItem::getItemCode, code);
        if (excludedId != null) query.ne(SystemDictItem::getId, excludedId);
        if (itemMapper.selectCount(query) > 0) throw new BusinessException(CommonResultCode.CONFLICT, "同一字典下字典项编码已存在");
    }

    private void normalizeItem(SystemDictItem item, Integer sort, String color, String icon, Integer status) {
        item.setSort(sort == null ? 0 : sort);
        item.setColor(ValueUtils.defaultString(color)); item.setIcon(ValueUtils.defaultString(icon));
        item.setStatus(ValueUtils.defaultIfNull(status, Constants.YES));
    }

    private DictItemResponse toItemResponse(SystemDictItem item) {
        return DictItemStructMapper.INSTANCE.toResponse(item);
    }

    private BusinessException notFound(String message) { return new BusinessException(CommonResultCode.NOT_FOUND, message); }
}
