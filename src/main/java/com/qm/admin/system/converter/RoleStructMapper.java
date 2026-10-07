package com.qm.admin.system.converter;

import com.qm.admin.system.dto.RoleCreateRequest;
import com.qm.admin.system.dto.RoleUpdateRequest;
import com.qm.admin.system.entity.SystemRole;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/** 角色请求与实体之间的字段映射。租户字段和默认值由 Service 负责。 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleStructMapper {

    RoleStructMapper INSTANCE = Mappers.getMapper(RoleStructMapper.class);

    SystemRole toEntity(RoleCreateRequest request);

    SystemRole toEntity(RoleUpdateRequest request);
}
