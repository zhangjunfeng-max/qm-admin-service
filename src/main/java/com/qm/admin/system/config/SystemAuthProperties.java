package com.qm.admin.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 系统认证令牌配置。访问令牌和刷新令牌均只以摘要形式保存到数据库。
 */
@ConfigurationProperties(prefix = "qm.security.auth")
public class SystemAuthProperties {

    private Duration accessTokenTtl = Duration.ofHours(2);
    private Duration refreshTokenTtl = Duration.ofDays(7);
    private String refreshCookieName = "qm_refresh_token";
    private boolean secureCookie;
    private boolean accessTokenCacheEnabled = true;
    private Duration accessTokenCacheTtl = Duration.ofMinutes(5);
    private String rsaPrivateKey;
    private boolean rsaPasswordEnabled;

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public String getRefreshCookieName() {
        return refreshCookieName;
    }

    public void setRefreshCookieName(String refreshCookieName) {
        this.refreshCookieName = refreshCookieName;
    }

    public boolean isSecureCookie() {
        return secureCookie;
    }

    public void setSecureCookie(boolean secureCookie) {
        this.secureCookie = secureCookie;
    }

    public boolean isAccessTokenCacheEnabled() {
        return accessTokenCacheEnabled;
    }

    public void setAccessTokenCacheEnabled(boolean accessTokenCacheEnabled) {
        this.accessTokenCacheEnabled = accessTokenCacheEnabled;
    }

    public Duration getAccessTokenCacheTtl() {
        return accessTokenCacheTtl;
    }

    public void setAccessTokenCacheTtl(Duration accessTokenCacheTtl) {
        this.accessTokenCacheTtl = accessTokenCacheTtl;
    }

    public String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }

    public boolean isRsaPasswordEnabled() {
        return rsaPasswordEnabled;
    }

    public void setRsaPasswordEnabled(boolean rsaPasswordEnabled) {
        this.rsaPasswordEnabled = rsaPasswordEnabled;
    }
}
