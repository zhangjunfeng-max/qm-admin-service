package com.qm.admin.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qm.admin.common.response.ApiResponse;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.common.web.TraceIdFilter;
import com.qm.admin.system.config.SystemAuthProperties;
import com.qm.admin.system.config.SystemAccessCacheProperties;
import com.qm.admin.system.security.TokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({SecurityPermitProperties.class, SystemAuthProperties.class,
        SystemAccessCacheProperties.class})
public class SecurityConfig {

    private static final String[] OPENAPI_WHITELIST = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final ObjectMapper objectMapper;
    private final SecurityPermitProperties securityPermitProperties;
    private final AnonymousAccessUrlProvider anonymousAccessUrlProvider;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    public SecurityConfig(ObjectMapper objectMapper, SecurityPermitProperties securityPermitProperties,
                          AnonymousAccessUrlProvider anonymousAccessUrlProvider,
                          TokenAuthenticationFilter tokenAuthenticationFilter) {
        this.objectMapper = objectMapper;
        this.securityPermitProperties = securityPermitProperties;
        this.anonymousAccessUrlProvider = anonymousAccessUrlProvider;
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] permitAllUrls = getPermitAllUrls();
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(permitAllUrls).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, CommonResultCode.UNAUTHORIZED, getTraceId()))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, CommonResultCode.FORBIDDEN, getTraceId()))
                )
                .build();
    }

    private String[] getPermitAllUrls() {
        // Merge all anonymous access sources before Spring Security builds request matchers.
        Set<String> permitAllUrls = new LinkedHashSet<>();
        permitAllUrls.addAll(Set.of(OPENAPI_WHITELIST));
        permitAllUrls.addAll(securityPermitProperties.getPermitUrls());
        permitAllUrls.addAll(anonymousAccessUrlProvider.getAnonymousAccessUrls());
        return permitAllUrls.toArray(String[]::new);
    }

    private void writeError(HttpServletResponse response, CommonResultCode resultCode, String traceId)
            throws IOException {
        response.setStatus(resultCode.httpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(resultCode, traceId));
    }

    private String getTraceId() {
        return MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
    }
}
