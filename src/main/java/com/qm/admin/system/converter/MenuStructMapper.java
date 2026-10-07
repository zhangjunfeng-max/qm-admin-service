package com.qm.admin.system.converter;

import com.qm.admin.system.dto.MenuCreateRequest;
import com.qm.admin.system.dto.MenuPageItemResponse;
import com.qm.admin.system.dto.MenuUpdateRequest;
import com.qm.admin.system.entity.SystemMenu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/** 菜单请求、实体和列表响应之间的字段映射。树关系和默认值由 Service 负责。 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuStructMapper {

    MenuStructMapper INSTANCE = Mappers.getMapper(MenuStructMapper.class);

    SystemMenu toEntity(MenuCreateRequest request);

    SystemMenu toEntity(MenuUpdateRequest request);

    void updateEntity(MenuUpdateRequest request, @MappingTarget SystemMenu menu);

    @Mapping(target = "hasChildren", ignore = true)
    MenuPageItemResponse toPageItem(SystemMenu menu);
}
