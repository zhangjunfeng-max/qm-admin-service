package com.qm.admin;

import com.qm.admin.common.annotation.AnonymousAccess;
import com.qm.admin.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = QmAdminTemplateApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ApiInfrastructureTests.TestControllerConfig.class)
class ApiInfrastructureTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void wrapsSuccessfulResponse() throws Exception {
        mockMvc.perform(get("/test-api/success").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.name").value("template"))
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.traceId", notNullValue()));
    }

    @Test
    @WithMockUser
    void handlesBusinessException() throws Exception {
        mockMvc.perform(get("/test-api/business-error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("business failed"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void handlesUnauthorizedResponse() throws Exception {
        mockMvc.perform(get("/secure-api").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("unauthorized"));
    }

    @Test
    void permitsAnonymousAccessAnnotation() throws Exception {
        mockMvc.perform(get("/test-api/anonymous").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.anonymous").value("ok"));
    }

    @TestConfiguration
    static class TestControllerConfig {

        @RestController
        static class TestController {

            @GetMapping("/test-api/success")
            Map<String, String> success() {
                return Map.of("name", "template");
            }

            @GetMapping("/test-api/business-error")
            Map<String, String> businessError() {
                throw new BusinessException("business failed");
            }

            @GetMapping("/secure-api")
            Map<String, String> secure() {
                return Map.of("secure", "ok");
            }

            @AnonymousAccess
            @GetMapping("/test-api/anonymous")
            Map<String, String> anonymous() {
                return Map.of("anonymous", "ok");
            }
        }
    }
}
