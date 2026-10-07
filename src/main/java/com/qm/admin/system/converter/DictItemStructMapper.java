package com.qm.admin.system.converter;

import com.qm.admin.system.dto.DictItemCreateRequest;
import com.qm.admin.system.dto.DictItemResponse;
import com.qm.admin.system.dto.DictItemUpdateRequest;
import com.qm.admin.system.entity.SystemDictItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/** 字典项请求、实体和响应之间的字段映射。关联字段和默认值由 Service 负责。 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictItemStructMapper {

    DictItemStructMapper INSTANCE = Mappers.getMapper(DictItemStructMapper.class);

    SystemDictItem toEntity(DictItemCreateRequest request);

    void updateEntity(DictItemUpdateRequest request, @MappingTarget SystemDictItem item);

    DictItemResponse toResponse(SystemDictItem item);
}
