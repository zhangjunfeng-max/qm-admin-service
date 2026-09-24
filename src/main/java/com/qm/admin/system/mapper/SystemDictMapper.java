package com.qm.admin.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qm.admin.system.dto.DictPageItemResponse;
import com.qm.admin.system.dto.DictPageQuery;
import com.qm.admin.system.entity.SystemDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemDictMapper extends BaseMapper<SystemDict> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            <script>
            SELECT d.id, d.dict_name AS dictName, d.dict_code AS dictCode, d.status, d.remark,
                   d.create_time AS createTime, d.update_time AS updateTime
            FROM sys_dict d
            WHERE d.tenant_id = #{tenantId} AND d.deleted = 0
            <if test="query.keyword != null and query.keyword != ''">
              AND (d.dict_name LIKE CONCAT('%', #{query.keyword}, '%')
                   OR d.dict_code LIKE CONCAT('%', #{query.keyword}, '%')
                   OR d.remark LIKE CONCAT('%', #{query.keyword}, '%'))
            </if>
            <if test="query.status != null">AND d.status = #{query.status}</if>
            <if test="query.startTime != null">AND d.create_time &gt;= #{query.startTime}</if>
            <if test="query.endTime != null">AND d.create_time &lt;= #{query.endTime}</if>
            ORDER BY
            <choose>
              <when test="query.sortBy == 'id'">d.id</when>
              <when test="query.sortBy == 'dictName'">d.dict_name</when>
              <when test="query.sortBy == 'dictCode'">d.dict_code</when>
              <when test="query.sortBy == 'status'">d.status</when>
              <when test="query.sortBy == 'updateTime'">d.update_time</when>
              <otherwise>d.create_time</otherwise>
            </choose>
            <choose><when test="query.sortOrder == 'asc'">ASC</when><otherwise>DESC</otherwise></choose>, d.id DESC
            </script>
            """)
    Page<DictPageItemResponse> selectDictPage(Page<DictPageItemResponse> page,
                                               @Param("tenantId") Long tenantId,
                                               @Param("query") DictPageQuery query);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT d.id, d.dict_name AS dictName, d.dict_code AS dictCode, d.status, d.remark,
                   d.create_time AS createTime, d.update_time AS updateTime
            FROM sys_dict d WHERE d.tenant_id = #{tenantId} AND d.id = #{dictId} AND d.deleted = 0 LIMIT 1
            """)
    DictPageItemResponse selectTenantDict(@Param("tenantId") Long tenantId, @Param("dictId") Long dictId);
}
