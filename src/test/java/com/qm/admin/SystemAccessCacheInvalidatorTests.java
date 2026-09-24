package com.qm.admin;

import com.qm.admin.system.service.SystemAccessCache;
import com.qm.admin.system.service.SystemAccessCacheInvalidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SystemAccessCacheInvalidatorTests {

    private final SystemAccessCache cache = mock(SystemAccessCache.class);
    private final SystemAccessCacheInvalidator invalidator = new SystemAccessCacheInvalidator(cache);

    @AfterEach
    void cleanup() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void transactionRollbackDoesNotInvalidateCache() {
        TransactionSynchronizationManager.initSynchronization();
        invalidator.user(1L, 7L);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(cache, never()).evictUser(1L, 7L);
    }

    @Test
    void successfulCommitInvalidatesAfterCommitOnly() {
        TransactionSynchronizationManager.initSynchronization();
        invalidator.tenant(1L);
        verify(cache, never()).evictTenant(1L);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(cache).evictTenant(1L);
    }
}
