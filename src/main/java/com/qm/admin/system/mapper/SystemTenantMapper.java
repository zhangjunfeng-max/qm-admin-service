package com.qm.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qm.admin.system.dto.TenantPageItemResponse;
import com.qm.admin.system.dto.TenantPageQuery;
import com.qm.admin.system.entity.SystemTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemTenantMapper extends BaseMapper<SystemTenant> {

    @Select("""
            <script>
            SELECT t.id, t.tenant_code AS tenantCode, t.tenant_name AS tenantName,
                   t.tenant_type AS tenantType, t.contact_name AS contactName,
                   t.contact_phone AS contactPhone, t.status, t.expire_time AS expireTime,
                   t.remark, t.create_time AS createTime, t.update_time AS updateTime
            FROM sys_tenant t WHERE t.deleted = 0
            <if test="query.keyword != null and query.keyword != ''">
              AND (t.tenant_code LIKE CONCAT('%', #{query.keyword}, '%') OR t.tenant_name LIKE CONCAT('%', #{query.keyword}, '%')
                   OR t.contact_name LIKE CONCAT('%', #{query.keyword}, '%') OR t.remark LIKE CONCAT('%', #{query.keyword}, '%'))
            </if>
            <if test="query.status != null">AND t.status = #{query.status}</if>
            <if test="query.startTime != null">AND t.create_time &gt;= #{query.startTime}</if>
            <if test="query.endTime != null">AND t.create_time &lt;= #{query.endTime}</if>
            ORDER BY
            <choose>
              <when test="query.sortBy == 'id'">t.id</when><when test="query.sortBy == 'tenantCode'">t.tenant_code</when>
              <when test="query.sortBy == 'tenantName'">t.tenant_name</when><when test="query.sortBy == 'tenantType'">t.tenant_type</when>
              <when test="query.sortBy == 'status'">t.status</when><when test="query.sortBy == 'updateTime'">t.update_time</when>
              <otherwise>t.create_time</otherwise>
            </choose>
            <choose><when test="query.sortOrder == 'asc'">ASC</when><otherwise>DESC</otherwise></choose>, t.id DESC
            </script>
            """)
    Page<TenantPageItemResponse> selectTenantPage(Page<TenantPageItemResponse> page, @Param("query") TenantPageQuery query);

    @Select("""
            SELECT t.id, t.tenant_code AS tenantCode, t.tenant_name AS tenantName,
                   t.tenant_type AS tenantType, t.contact_name AS contactName,
                   t.contact_phone AS contactPhone, t.status, t.expire_time AS expireTime,
                   t.remark, t.create_time AS createTime, t.update_time AS updateTime
            FROM sys_tenant t WHERE t.id = #{tenantId} AND t.deleted = 0 LIMIT 1
            """)
    TenantPageItemResponse selectTenant(@Param("tenantId") Long tenantId);
}
