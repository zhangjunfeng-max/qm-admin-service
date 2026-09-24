package com.qm.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qm.admin.system.dto.UserInfoRoleRow;
import com.qm.admin.system.dto.UserPageItemResponse;
import com.qm.admin.system.dto.UserPageQuery;
import com.qm.admin.system.entity.SystemUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SystemUserMapper extends BaseMapper<SystemUser> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT account.id AS userId,
                   account.username AS username,
                   account.real_name AS realName,
                   account.avatar AS avatar,
                   account.description AS description,
                   account.home_path AS homePath,
                   role.role_code AS roleCode
            FROM sys_user account
            INNER JOIN sys_user_tenant member
                    ON member.user_id = account.id
                   AND member.tenant_id = #{tenantId}
                   AND member.status = 1
            LEFT JOIN sys_user_role user_role
                   ON user_role.user_id = account.id
                  AND user_role.tenant_id = #{tenantId}
            LEFT JOIN sys_role role
                   ON role.id = user_role.role_id
                  AND role.tenant_id = #{tenantId}
                  AND role.status = 1
                  AND role.deleted = 0
            WHERE account.id = #{userId}
              AND account.status = 1
              AND account.deleted = 0
            ORDER BY role.id ASC
            """)
    List<UserInfoRoleRow> selectUserInfoWithRoles(@Param("userId") Long userId,
                                                  @Param("tenantId") Long tenantId);

    /**
     * sys_user 是平台表，成员关系才是租户边界。这里显式使用认证令牌中的 tenantId，避免列表查询跨租户。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            <script>
            SELECT account.id AS id,
                   account.username AS username,
                   account.real_name AS realName,
                   account.avatar AS avatar,
                   account.description AS description,
                   account.status AS accountStatus,
                   member.status AS memberStatus,
                   member.is_tenant_admin AS isTenantAdmin,
                   account.create_time AS createTime,
                   account.update_time AS updateTime
            FROM sys_user_tenant member
            INNER JOIN sys_user account
                    ON account.id = member.user_id
                   AND account.deleted = 0
            WHERE member.tenant_id = #{tenantId}
            <if test="query.keyword != null and query.keyword != ''">
                AND (account.username LIKE CONCAT('%', #{query.keyword}, '%')
                     OR account.real_name LIKE CONCAT('%', #{query.keyword}, '%')
                     OR account.description LIKE CONCAT('%', #{query.keyword}, '%'))
            </if>
            <if test="query.accountStatus != null">
                AND account.status = #{query.accountStatus}
            </if>
            <if test="query.memberStatus != null">
                AND member.status = #{query.memberStatus}
            </if>
            <if test="query.isTenantAdmin != null">
                AND member.is_tenant_admin = #{query.isTenantAdmin}
            </if>
            <if test="query.startTime != null">
                AND account.create_time &gt;= #{query.startTime}
            </if>
            <if test="query.endTime != null">
                AND account.create_time &lt;= #{query.endTime}
            </if>
            ORDER BY
            <choose>
                <when test="query.sortBy == 'id'">account.id</when>
                <when test="query.sortBy == 'username'">account.username</when>
                <when test="query.sortBy == 'realName'">account.real_name</when>
                <when test="query.sortBy == 'accountStatus'">account.status</when>
                <when test="query.sortBy == 'memberStatus'">member.status</when>
                <when test="query.sortBy == 'isTenantAdmin'">member.is_tenant_admin</when>
                <when test="query.sortBy == 'updateTime'">account.update_time</when>
                <otherwise>account.create_time</otherwise>
            </choose>
            <choose>
                <when test="query.sortOrder == 'asc'">ASC</when>
                <otherwise>DESC</otherwise>
            </choose>, account.id DESC
            </script>
            """)
    Page<UserPageItemResponse> selectUserPage(Page<UserPageItemResponse> page,
                                              @Param("tenantId") Long tenantId,
                                              @Param("query") UserPageQuery query);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT account.id AS id,
                   account.username AS username,
                   account.real_name AS realName,
                   account.avatar AS avatar,
                   account.description AS description,
                   account.status AS accountStatus,
                   member.status AS memberStatus,
                   member.is_tenant_admin AS isTenantAdmin,
                   account.create_time AS createTime,
                   account.update_time AS updateTime
            FROM sys_user_tenant member
            INNER JOIN sys_user account
                    ON account.id = member.user_id
                   AND account.deleted = 0
            WHERE member.tenant_id = #{tenantId}
              AND member.user_id = #{userId}
            LIMIT 1
            """)
    UserPageItemResponse selectTenantUser(@Param("tenantId") Long tenantId,
                                          @Param("userId") Long userId);
}
