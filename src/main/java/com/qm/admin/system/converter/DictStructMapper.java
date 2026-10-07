package com.qm.admin.system.converter;

import com.qm.admin.system.dto.DictCreateRequest;
import com.qm.admin.system.dto.DictUpdateRequest;
import com.qm.admin.system.entity.SystemDict;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/** 字典请求与实体之间的字段映射。租户字段和默认值由 Service 负责。 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictStructMapper {

    DictStructMapper INSTANCE = Mappers.getMapper(DictStructMapper.class);

    SystemDict toEntity(DictCreateRequest request);

    SystemDict toEntity(DictUpdateRequest request);
}
