package com.qm.admin.system.converter;

import com.qm.admin.system.dto.UserCreateRequest;
import com.qm.admin.system.dto.UserUpdateRequest;
import com.qm.admin.system.entity.SystemUser;
import com.qm.admin.system.entity.SystemUserTenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/** 用户和租户成员请求的字段映射。密码加密、关联 ID 和默认值由 Service 负责。 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserStructMapper {

    UserStructMapper INSTANCE = Mappers.getMapper(UserStructMapper.class);

    @Mapping(source = "accountStatus", target = "status")
    SystemUser toUser(UserCreateRequest request);

    @Mapping(source = "accountStatus", target = "status")
    SystemUser toUser(UserUpdateRequest request);

    @Mapping(source = "memberStatus", target = "status")
    SystemUserTenant toMembership(UserCreateRequest request);

    @Mapping(source = "memberStatus", target = "status")
    SystemUserTenant toMembership(UserUpdateRequest request);

    void updateMembership(UserUpdateRequest request, @MappingTarget SystemUserTenant membership);
}
