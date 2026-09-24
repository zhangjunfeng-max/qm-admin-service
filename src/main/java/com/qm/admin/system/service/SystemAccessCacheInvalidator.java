package com.qm.admin.system.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 只在数据库事务提交后失效，回滚不会误删有效缓存。 */
@Component
public class SystemAccessCacheInvalidator {

    private final SystemAccessCache cache;

    public SystemAccessCacheInvalidator(SystemAccessCache cache) { this.cache = cache; }

    public void user(Long tenantId, Long userId) { afterCommit(() -> cache.evictUser(tenantId, userId)); }
    public void tenant(Long tenantId) { afterCommit(() -> cache.evictTenant(tenantId)); }
    public void all() { afterCommit(cache::evictAll); }

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { action.run(); return; }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { action.run(); }
        });
    }
}
