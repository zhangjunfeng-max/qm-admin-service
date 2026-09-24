package com.qm.admin.system.service;

import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.util.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Spring Security 方法鉴权的统一入口。
 * 权限必须同时绑定用户和当前令牌中的租户，避免多租户账号串用其他租户的授权结果。
 */
@Component("ss")
public class SystemAccessService {

    private final SystemMenuService menuService;
    private final SystemAccessCache accessCache;

    @Autowired
    public SystemAccessService(SystemMenuService menuService, SystemAccessCache accessCache) {
        this.menuService = menuService;
        this.accessCache = accessCache;
    }

    /** 兼容纯单元测试构造方式；生产环境始终注入版本化缓存。 */
    public SystemAccessService(SystemMenuService menuService) {
        this.menuService = menuService;
        this.accessCache = null;
    }

    public boolean hasPermission(String permission) {
        return SecurityUtils.getSystemPrincipalOptions()
                .map(principal -> getPermissions(principal).contains(permission))
                .orElse(false);
    }

    /**
     * 所有权限读取统一经过此方法，后续接入缓存时使用 userId + tenantId 作为缓存键，
     * 并由用户角色、角色菜单变更入口统一清理对应缓存。
     */
    public List<String> getPermissions(SystemPrincipal principal) {
        if (accessCache == null) {
            return menuService.getAccessCodes(principal.userId(), principal.tenantId());
        }
        return accessCache.getOrLoadPermissions(principal.tenantId(), principal.userId(),
                () -> menuService.loadAccessCodesFromDatabase(principal.userId(), principal.tenantId()));
    }
}
