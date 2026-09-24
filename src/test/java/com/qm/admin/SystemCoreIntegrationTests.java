package com.qm.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qm.admin.system.service.SystemAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemCoreIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemAuthService authService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void supportsCoreLoginUserMenuCodesRefreshAndLogout() throws Exception {
        String loginBody = mockMvc.perform(post("/system/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(header().string("Set-Cookie", containsString("qm_refresh_token=")))
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginBody).path("data").path("accessToken").asText();

        mockMvc.perform(get("/system/user/info").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.realName").value("管理员"))
                .andExpect(jsonPath("$.data.homePath").value("/system/user"))
                .andExpect(jsonPath("$.data.roles[0]").value("admin"))
                .andExpect(jsonPath("$.data.token").doesNotExist());

        mockMvc.perform(get("/system/menu/all").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("SystemManagement"))
                .andExpect(jsonPath("$.data[0].children[0].component").value("/system/user/index"))
                .andExpect(jsonPath("$.data[0].children[1].component").value("/system/role/index"))
                .andExpect(jsonPath("$.data[0].children[2].component").value("/system/menu/index"))
                .andExpect(jsonPath("$.data[0].children[3].component").value("/system/dict/index"));

        mockMvc.perform(get("/system/auth/codes").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("system:cache:clear"))
                .andExpect(jsonPath("$.data[1]").value("system:dict:create"))
                .andExpect(jsonPath("$.data[2]").value("system:dict:delete"))
                .andExpect(jsonPath("$.data[3]").value("system:dict:update"))
                .andExpect(jsonPath("$.data[4]").value("system:dict:view"))
                .andExpect(jsonPath("$.data[5]").value("system:menu:create"))
                .andExpect(jsonPath("$.data[6]").value("system:menu:delete"))
                .andExpect(jsonPath("$.data[7]").value("system:menu:update"))
                .andExpect(jsonPath("$.data[8]").value("system:menu:view"))
                .andExpect(jsonPath("$.data[9]").value("system:role:create"))
                .andExpect(jsonPath("$.data[10]").value("system:role:delete"))
                .andExpect(jsonPath("$.data[11]").value("system:role:update"))
                .andExpect(jsonPath("$.data[12]").value("system:role:view"))
                .andExpect(jsonPath("$.data[13]").value("system:user:create"))
                .andExpect(jsonPath("$.data[14]").value("system:user:delete"))
                .andExpect(jsonPath("$.data[15]").value("system:user:update"))
                .andExpect(jsonPath("$.data[16]").value("system:user:view"));

        mockMvc.perform(delete("/system/auth/cache")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scope\":\"CURRENT_USER\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/system/auth/cache")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scope\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresTenantSelectionForMultiTenantUserAndIsolatesRbacBySelectedTenant() throws Exception {
        mockMvc.perform(post("/system/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"multiadmin\",\"password\":\"admin123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("账号属于多个租户，请选择租户后登录"));

        MockHttpServletResponse loginResponse = mockMvc.perform(post("/system/auth/login")
                        .header("tenant-id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"multiadmin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn().getResponse();
        String tenantBLoginBody = loginResponse.getContentAsString();
        String tenantBToken = objectMapper.readTree(tenantBLoginBody).path("data").path("accessToken").asText();

        mockMvc.perform(get("/system/user/info").header("Authorization", "Bearer " + tenantBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("chain_admin"));

        mockMvc.perform(get("/system/auth/codes").header("Authorization", "Bearer " + tenantBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("system:role:view"))
                .andExpect(jsonPath("$.data[1]").value("system:user:view"))
                .andExpect(jsonPath("$.data[2]").doesNotExist());

        mockMvc.perform(get("/system/user/info")
                        .header("Authorization", "Bearer " + tenantBToken)
                        .header("tenant-id", "1"))
                .andExpect(status().isUnauthorized());

        String refreshToken = cookieValue(loginResponse.getHeader("Set-Cookie"), "qm_refresh_token");
        String refreshBody = mockMvc.perform(post("/system/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("qm_refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String refreshedAccessToken = objectMapper.readTree(refreshBody).path("data").asText();
        org.junit.jupiter.api.Assertions.assertEquals(2L,
                authService.resolvePrincipal(refreshedAccessToken).orElseThrow().tenantId());
    }

    @Test
    void rejectsTenantThatUserDoesNotBelongTo() throws Exception {
        mockMvc.perform(post("/system/auth/login")
                        .header("tenant-id", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("无权访问指定租户"));
    }

    @Test
    void rejectsInvalidCredentialsAndUnauthorizedCoreCalls() throws Exception {
        mockMvc.perform(post("/system/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"not-a-valid-rsa-ciphertext\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/system/user/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void logoutWithExpiredAccessTokenStillWritesNonNullAuditOperator() throws Exception {
        MockHttpServletResponse loginResponse = mockMvc.perform(post("/system/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        String refreshToken = cookieValue(loginResponse.getHeader("Set-Cookie"), "qm_refresh_token");

        mockMvc.perform(post("/system/auth/logout")
                        .header("Authorization", "Bearer expired-access-token")
                        .cookie(new jakarta.servlet.http.Cookie("qm_refresh_token", refreshToken)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(1L, jdbcTemplate.queryForObject(
                "SELECT update_by FROM sys_auth_token WHERE refresh_token_hash = ?",
                Long.class, sha256(refreshToken)));
    }

    @Test
    void pagesFiltersSortsAndIsolatesUsersByCurrentTenant() throws Exception {
        String tenantAToken = login("admin", null);

        mockMvc.perform(get("/system/user/page")
                        .header("Authorization", "Bearer " + tenantAToken)
                        .queryParam("pageNum", "1")
                        .queryParam("pageSize", "20")
                        .queryParam("keyword", "运营")
                        .queryParam("accountStatus", "0")
                        .queryParam("isTenantAdmin", "0")
                        .queryParam("sortBy", "username")
                        .queryParam("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.records[0].username").value("operator"))
                .andExpect(jsonPath("$.data.records[0].accountStatus").value(0))
                .andExpect(jsonPath("$.data.records[0].memberStatus").value(0));

        String tenantBToken = login("multiadmin", 2L);
        mockMvc.perform(get("/system/user/page")
                        .header("Authorization", "Bearer " + tenantBToken)
                        .queryParam("pageNum", "1")
                        .queryParam("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("multiadmin"));

        mockMvc.perform(get("/system/user/page")
                        .header("Authorization", "Bearer " + tenantAToken)
                        .queryParam("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("20、50、100、200")));
    }

    @Test
    void createsReadsUpdatesAndDeletesTenantUsers() throws Exception {
        String adminToken = login("admin", null);

        String createBody = mockMvc.perform(post("/system/user")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"newuser",
                                  "password":"newuser123",
                                  "realName":"新用户",
                                  "description":"用户管理测试",
                                  "accountStatus":1,
                                  "memberStatus":1,
                                  "isTenantAdmin":0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.realName").value("新用户"))
                .andReturn().getResponse().getContentAsString();
        String userId = objectMapper.readTree(createBody).path("data").path("id").asText();

        mockMvc.perform(get("/system/user/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("newuser"));

        mockMvc.perform(put("/system/user/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "realName":"新用户已编辑",
                                  "description":"已更新",
                                  "accountStatus":1,
                                  "memberStatus":1,
                                  "isTenantAdmin":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("新用户已编辑"))
                .andExpect(jsonPath("$.data.isTenantAdmin").value(1));

        mockMvc.perform(delete("/system/user/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/system/user/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void batchDeletesUsersRejectsSelfDeletionAndEnforcesPermission() throws Exception {
        String adminToken = login("admin", null);

        String firstId = createTestUser(adminToken, "batch-user-1");
        String secondId = createTestUser(adminToken, "batch-user-2");
        mockMvc.perform(post("/system/user/batch-delete")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[" + firstId + "," + secondId + "]}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/system/user/{userId}", firstId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/system/user/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("不能删除当前登录用户"));

        String tenantBToken = login("multiadmin", 2L);
        mockMvc.perform(post("/system/user")
                        .header("Authorization", "Bearer " + tenantBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"forbidden-user",
                                  "password":"password123",
                                  "realName":"无权限用户",
                                  "accountStatus":1,
                                  "memberStatus":1,
                                  "isTenantAdmin":0
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void pagesFiltersSortsAndIsolatesRolesByCurrentTenant() throws Exception {
        String tenantAToken = login("admin", null);

        mockMvc.perform(get("/system/role/page")
                        .header("Authorization", "Bearer " + tenantAToken)
                        .queryParam("pageNum", "1")
                        .queryParam("pageSize", "20")
                        .queryParam("keyword", "租户 A")
                        .queryParam("status", "1")
                        .queryParam("sortBy", "roleCode")
                        .queryParam("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].roleCode").value("admin"))
                .andExpect(jsonPath("$.data.records[0].roleName").value("租户 A 管理员"));

        String tenantBToken = login("multiadmin", 2L);
        mockMvc.perform(get("/system/role/page")
                        .header("Authorization", "Bearer " + tenantBToken)
                        .queryParam("pageNum", "1")
                        .queryParam("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].roleCode").value("chain_admin"));

        mockMvc.perform(get("/system/role/page")
                        .header("Authorization", "Bearer " + tenantAToken)
                        .queryParam("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("20、50、100、200")));
    }

    @Test
    void createsReadsUpdatesAndDeletesTenantRoles() throws Exception {
        String adminToken = login("admin", null);

        String createBody = mockMvc.perform(post("/system/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleCode":"operator_manager",
                                  "roleName":"运营管理员",
                                  "status":1,
                                  "remark":"角色管理测试"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("operator_manager"))
                .andExpect(jsonPath("$.data.roleName").value("运营管理员"))
                .andReturn().getResponse().getContentAsString();
        String roleId = objectMapper.readTree(createBody).path("data").path("id").asText();

        mockMvc.perform(get("/system/role/{roleId}", roleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("operator_manager"));

        mockMvc.perform(put("/system/role/{roleId}", roleId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName":"运营负责人",
                                  "status":1,
                                  "remark":"已更新"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleName").value("运营负责人"))
                .andExpect(jsonPath("$.data.remark").value("已更新"));

        mockMvc.perform(delete("/system/role/{roleId}", roleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/system/role/{roleId}", roleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void batchDeletesRolesProtectsCurrentUserRoleAndEnforcesPermission() throws Exception {
        String adminToken = login("admin", null);
        String firstId = createTestRole(adminToken, "batch_role_1");
        String secondId = createTestRole(adminToken, "batch_role_2");

        mockMvc.perform(post("/system/role/batch-delete")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[" + firstId + "," + secondId + "]}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/system/role/{roleId}", firstId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/system/role/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("不能删除当前登录用户绑定的角色"));

        mockMvc.perform(put("/system/role/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"租户 A 管理员\",\"status\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("不能禁用当前登录用户绑定的角色"));

        String tenantBToken = login("multiadmin", 2L);
        mockMvc.perform(post("/system/role")
                        .header("Authorization", "Bearer " + tenantBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"forbidden\",\"roleName\":\"无权限角色\",\"status\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignsUserRolesAndRoleMenusWithExistingSelections() throws Exception {
        String adminToken = login("admin", null);

        mockMvc.perform(get("/system/user/1/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.selectedRoleIds[0]").value("1"));

        String userId = createTestUser(adminToken, "assignment-user");
        String roleId = createTestRole(adminToken, "assignment-role");
        mockMvc.perform(put("/system/user/{userId}/roles", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[1," + roleId + "," + roleId + "]}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/system/user/{userId}/roles", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectedRoleIds").isArray())
                .andExpect(jsonPath("$.data.selectedRoleIds", org.hamcrest.Matchers.hasSize(2)));

        mockMvc.perform(get("/system/role/{roleId}/menus", roleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menus[0].children").isArray())
                .andExpect(jsonPath("$.data.menus[0].parentId").doesNotExist())
                .andExpect(jsonPath("$.data.menus[0].status").doesNotExist())
                .andExpect(jsonPath("$.data.selectedMenuIds").isEmpty());

        mockMvc.perform(put("/system/role/{roleId}/menus", roleId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[1,2,3,5,5]}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/system/role/{roleId}/menus", roleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectedMenuIds").value(org.hamcrest.Matchers.containsInAnyOrder("1", "2", "3", "5")));

        mockMvc.perform(put("/system/user/{userId}/roles", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[2]}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/system/role/{roleId}/menus", roleId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[999999999]}"))
                .andExpect(status().isNotFound());
    }

    private String createTestUser(String accessToken, String username) throws Exception {
        String body = mockMvc.perform(post("/system/user")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"%s",
                                  "password":"password123",
                                  "realName":"批量测试用户",
                                  "accountStatus":1,
                                  "memberStatus":1,
                                  "isTenantAdmin":0
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asText();
    }

    private String createTestRole(String accessToken, String roleCode) throws Exception {
        String body = mockMvc.perform(post("/system/role")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleCode":"%s",
                                  "roleName":"批量测试角色",
                                  "status":1
                                }
                                """.formatted(roleCode)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asText();
    }

    private String login(String username, Long tenantId) throws Exception {
        var request = post("/system/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"admin123\"}");
        if (tenantId != null) {
            request.header("tenant-id", tenantId);
        }
        String body = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private String cookieValue(String setCookieHeader, String name) {
        String prefix = name + "=";
        return java.util.Arrays.stream(setCookieHeader.split(";"))
                .map(String::trim)
                .filter(value -> value.startsWith(prefix))
                .map(value -> value.substring(prefix.length()))
                .findFirst()
                .orElseThrow();
    }

    private String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

}
