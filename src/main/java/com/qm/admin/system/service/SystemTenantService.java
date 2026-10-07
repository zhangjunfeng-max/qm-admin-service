package com.qm.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qm.admin.common.constants.Constants;
import com.qm.admin.common.exception.BusinessException;
import com.qm.admin.common.model.PageResult;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.common.util.ValueUtils;
import com.qm.admin.system.dto.TenantCreateRequest;
import com.qm.admin.system.dto.TenantPageItemResponse;
import com.qm.admin.system.dto.TenantPageQuery;
import com.qm.admin.system.dto.TenantUpdateRequest;
import com.qm.admin.system.converter.TenantStructMapper;
import com.qm.admin.system.entity.SystemTenant;
import com.qm.admin.system.mapper.SystemTenantMapper;
import com.qm.admin.system.security.SystemPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemTenantService {
    private final SystemTenantMapper tenantMapper;

    public SystemTenantService(SystemTenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<TenantPageItemResponse> getPage(TenantPageQuery query) {
        return PageResult.of(tenantMapper.selectTenantPage(new Page<>(query.getPageNum(), query.getPageSize()), query));
    }

    @Transactional(readOnly = true)
    public TenantPageItemResponse get(Long tenantId) {
        TenantPageItemResponse tenant = tenantMapper.selectTenant(tenantId);
        if (tenant == null) throw notFound("租户不存在");
        return tenant;
    }

    @Transactional
    public TenantPageItemResponse create(TenantCreateRequest request, SystemPrincipal principal) {
        SystemTenant tenant = TenantStructMapper.INSTANCE.toEntity(request);
        tenant.setTenantCode(generateTenantCode());
        tenant.setTenantType(ValueUtils.defaultIfNull(request.tenantType(), "SINGLE"));
        tenant.setContactName(ValueUtils.defaultString(request.contactName()));
        tenant.setContactPhone(ValueUtils.defaultString(request.contactPhone()));
        tenant.setStatus(ValueUtils.defaultIfNull(request.status(), Constants.YES));
        tenant.setExpireTime(request.expireTime());
        tenant.setRemark(ValueUtils.defaultString(request.remark()));
        tenant.setCreateBy(principal.userId());
        tenant.setUpdateBy(principal.userId());
        tenantMapper.insert(tenant);
        return get(tenant.getId());
    }

    @Transactional
    public TenantPageItemResponse update(Long tenantId, TenantUpdateRequest request, SystemPrincipal principal) {
        get(tenantId);
        SystemTenant tenant = TenantStructMapper.INSTANCE.toEntity(request);
        tenant.setId(tenantId);
        tenant.setTenantType(ValueUtils.defaultIfNull(request.tenantType(), "SINGLE"));
        tenant.setContactName(ValueUtils.defaultString(request.contactName()));
        tenant.setContactPhone(ValueUtils.defaultString(request.contactPhone()));
        tenant.setStatus(ValueUtils.defaultIfNull(request.status(), Constants.YES));
        tenant.setExpireTime(request.expireTime());
        tenant.setRemark(ValueUtils.defaultString(request.remark()));
        tenant.setUpdateBy(principal.userId());
        tenantMapper.updateById(tenant);
        return get(tenantId);
    }

    @Transactional
    public void delete(Long tenantId, SystemPrincipal principal) {
        get(tenantId);
        if (tenantId.equals(principal.tenantId())) {
            throw new BusinessException(CommonResultCode.CONFLICT, "不能删除当前登录租户");
        }
        tenantMapper.deleteById(tenantId);
    }

    private String generateTenantCode() {
        // 使用 MyBatis-Plus 雪花 ID 生成全局唯一编码，避免创建租户时额外查询数据库。
        return IdWorker.getIdStr();
    }

    private BusinessException notFound(String message) {
        return new BusinessException(CommonResultCode.NOT_FOUND, message);
    }
}
