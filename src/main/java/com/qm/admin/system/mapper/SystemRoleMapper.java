package com.qm.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qm.admin.system.dto.RolePageItemResponse;
import com.qm.admin.system.dto.RolePageQuery;
import com.qm.admin.system.dto.RoleAssignOption;
import com.qm.admin.system.entity.SystemRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SystemRoleMapper extends BaseMapper<SystemRole> {

    @Select("SELECT id, role_code AS roleCode, role_name AS roleName, status FROM sys_role WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY role_name ASC, id ASC")
    List<RoleAssignOption> selectTenantRoleOptions(@Param("tenantId") Long tenantId);

    @Select("<script>SELECT COUNT(1) FROM sys_role WHERE tenant_id = #{tenantId} AND deleted = 0 AND id IN <foreach collection='roleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    long countTenantRoles(@Param("tenantId") Long tenantId, @Param("roleIds") List<Long> roleIds);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            <script>
            SELECT role.id AS id,
                   role.role_code AS roleCode,
                   role.role_name AS roleName,
                   role.status AS status,
                   role.remark AS remark,
                   role.create_time AS createTime,
                   role.update_time AS updateTime
            FROM sys_role role
            WHERE role.tenant_id = #{tenantId}
              AND role.deleted = 0
            <if test="query.keyword != null and query.keyword != ''">
                AND (role.role_code LIKE CONCAT('%', #{query.keyword}, '%')
                     OR role.role_name LIKE CONCAT('%', #{query.keyword}, '%')
                     OR role.remark LIKE CONCAT('%', #{query.keyword}, '%'))
            </if>
            <if test="query.status != null">
                AND role.status = #{query.status}
            </if>
            <if test="query.startTime != null">
                AND role.create_time &gt;= #{query.startTime}
            </if>
            <if test="query.endTime != null">
                AND role.create_time &lt;= #{query.endTime}
            </if>
            ORDER BY
            <choose>
                <when test="query.sortBy == 'id'">role.id</when>
                <when test="query.sortBy == 'roleCode'">role.role_code</when>
                <when test="query.sortBy == 'roleName'">role.role_name</when>
                <when test="query.sortBy == 'status'">role.status</when>
                <when test="query.sortBy == 'updateTime'">role.update_time</when>
                <otherwise>role.create_time</otherwise>
            </choose>
            <choose>
                <when test="query.sortOrder == 'asc'">ASC</when>
                <otherwise>DESC</otherwise>
            </choose>, role.id DESC
            </script>
            """)
    Page<RolePageItemResponse> selectRolePage(Page<RolePageItemResponse> page,
                                              @Param("tenantId") Long tenantId,
                                              @Param("query") RolePageQuery query);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT role.id AS id,
                   role.role_code AS roleCode,
                   role.role_name AS roleName,
                   role.status AS status,
                   role.remark AS remark,
                   role.create_time AS createTime,
                   role.update_time AS updateTime
            FROM sys_role role
            WHERE role.tenant_id = #{tenantId}
              AND role.id = #{roleId}
              AND role.deleted = 0
            LIMIT 1
            """)
    RolePageItemResponse selectTenantRole(@Param("tenantId") Long tenantId,
                                          @Param("roleId") Long roleId);
}
