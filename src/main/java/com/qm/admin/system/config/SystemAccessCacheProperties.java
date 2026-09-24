package com.qm.admin.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** 鉴权相关业务缓存配置。缓存不可用时所有服务必须自动回退数据库。 */
@ConfigurationProperties(prefix = "qm.security.access-cache")
public class SystemAccessCacheProperties {

    private boolean enabled = true;
    private Duration permissionTtl = Duration.ofMinutes(10);
    private Duration menuTtl = Duration.ofMinutes(30);
    private Duration userInfoTtl = Duration.ofMinutes(5);
    private Duration loadLockWaitTime = Duration.ofSeconds(2);

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Duration getPermissionTtl() { return permissionTtl; }
    public void setPermissionTtl(Duration permissionTtl) { this.permissionTtl = permissionTtl; }
    public Duration getMenuTtl() { return menuTtl; }
    public void setMenuTtl(Duration menuTtl) { this.menuTtl = menuTtl; }
    public Duration getUserInfoTtl() { return userInfoTtl; }
    public void setUserInfoTtl(Duration userInfoTtl) { this.userInfoTtl = userInfoTtl; }
    public Duration getLoadLockWaitTime() { return loadLockWaitTime; }
    public void setLoadLockWaitTime(Duration loadLockWaitTime) { this.loadLockWaitTime = loadLockWaitTime; }
}
