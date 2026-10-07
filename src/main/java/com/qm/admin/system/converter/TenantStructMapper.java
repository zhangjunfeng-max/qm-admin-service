package com.qm.admin.system.converter;

import com.qm.admin.system.dto.TenantCreateRequest;
import com.qm.admin.system.dto.TenantUpdateRequest;
import com.qm.admin.system.entity.SystemTenant;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/** 租户请求与实体之间的字段映射。业务默认值和审计字段由 Service 负责。 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantStructMapper {

    TenantStructMapper INSTANCE = Mappers.getMapper(TenantStructMapper.class);

    SystemTenant toEntity(TenantCreateRequest request);

    SystemTenant toEntity(TenantUpdateRequest request);
}
