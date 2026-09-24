package com.qm.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.qm.admin.system.dto.AuthenticatedSession;
import com.qm.admin.system.entity.SystemAuthToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface SystemAuthTokenMapper extends BaseMapper<SystemAuthToken> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT token.id AS tokenId,
                   token.tenant_id AS tenantId,
                   token.user_id AS userId,
                   account.username AS username,
                   token.access_token_hash AS accessTokenHash,
                   token.refresh_token_hash AS refreshTokenHash,
                   token.access_expires_time AS accessExpiresTime,
                   token.refresh_expires_time AS refreshExpiresTime
            FROM sys_auth_token token
            INNER JOIN sys_user account
                    ON account.id = token.user_id
                   AND account.deleted = 0
                   AND account.status = 1
            INNER JOIN sys_user_tenant member
                    ON member.tenant_id = token.tenant_id
                   AND member.user_id = token.user_id
                   AND member.status = 1
            INNER JOIN sys_tenant tenant
                    ON tenant.id = token.tenant_id
                   AND tenant.deleted = 0
                   AND tenant.status = 1
                   AND (tenant.expire_time IS NULL OR tenant.expire_time > #{now})
            WHERE token.deleted = 0
              AND token.access_token_hash = #{tokenHash}
              AND token.revoked_time IS NULL
              AND token.access_expires_time > #{now}
            LIMIT 1
            """)
    AuthenticatedSession selectActiveAccessSession(@Param("tokenHash") String tokenHash,
                                                   @Param("now") LocalDateTime now);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT token.id AS tokenId,
                   token.tenant_id AS tenantId,
                   token.user_id AS userId,
                   account.username AS username,
                   token.access_token_hash AS accessTokenHash,
                   token.refresh_token_hash AS refreshTokenHash,
                   token.access_expires_time AS accessExpiresTime,
                   token.refresh_expires_time AS refreshExpiresTime
            FROM sys_auth_token token
            INNER JOIN sys_user account
                    ON account.id = token.user_id
                   AND account.deleted = 0
                   AND account.status = 1
            INNER JOIN sys_user_tenant member
                    ON member.tenant_id = token.tenant_id
                   AND member.user_id = token.user_id
                   AND member.status = 1
            INNER JOIN sys_tenant tenant
                    ON tenant.id = token.tenant_id
                   AND tenant.deleted = 0
                   AND tenant.status = 1
                   AND (tenant.expire_time IS NULL OR tenant.expire_time > #{now})
            WHERE token.deleted = 0
              AND token.refresh_token_hash = #{tokenHash}
              AND token.revoked_time IS NULL
              AND token.refresh_expires_time > #{now}
            LIMIT 1
            """)
    AuthenticatedSession selectActiveRefreshSession(@Param("tokenHash") String tokenHash,
                                                    @Param("now") LocalDateTime now);

    @InterceptorIgnore(tenantLine = "true")
    @Delete("DELETE FROM sys_auth_token WHERE tenant_id = #{tenantId} AND user_id = #{userId}")
    int physicallyDeleteUserSessions(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
