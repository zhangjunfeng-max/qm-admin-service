package com.qm.admin;

import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.service.SystemAccessService;
import com.qm.admin.system.service.SystemMenuService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemAccessServiceTests {

    private final SystemMenuService menuService = mock(SystemMenuService.class);
    private final SystemAccessService accessService = new SystemAccessService(menuService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsPermissionCheckWithoutAuthenticatedSystemPrincipal() {
        assertFalse(accessService.hasPermission("system:user:view"));
    }

    @Test
    void checksPermissionUsingAuthenticatedUserAndTenant() {
        SystemPrincipal principal = new SystemPrincipal(7L, 11L, "admin");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, "token", List.of()));
        when(menuService.getAccessCodes(7L, 11L))
                .thenReturn(List.of("system:user:view", "system:user:update"));

        assertTrue(accessService.hasPermission("system:user:view"));
        assertFalse(accessService.hasPermission("system:user:delete"));
        verify(menuService, times(2)).getAccessCodes(7L, 11L);
    }
}
