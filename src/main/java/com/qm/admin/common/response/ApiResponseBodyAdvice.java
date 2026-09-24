package com.qm.admin.common.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qm.admin.common.web.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final String[] EXCLUDED_PATHS = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiResponseBodyAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> parameterType = returnType.getParameterType();
        return !ApiResponse.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResponse<?> || isExcludedPath(request)) {
            return body;
        }

        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return writeStringResponse(ApiResponse.success(body, getTraceId(request)));
        }

        if (!isJsonResponse(selectedContentType)) {
            return body;
        }

        return ApiResponse.success(body, getTraceId(request));
    }

    private boolean isJsonResponse(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.includes(mediaType)
                || mediaType.includes(MediaType.APPLICATION_JSON);
    }

    private boolean isExcludedPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        for (String excludedPath : EXCLUDED_PATHS) {
            if (pathMatcher.match(excludedPath, path)) {
                return true;
            }
        }
        return false;
    }

    private String getTraceId(ServerHttpRequest request) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        return request.getHeaders().getFirst(TraceIdFilter.TRACE_ID_HEADER);
    }

    private String writeStringResponse(ApiResponse<Object> apiResponse) {
        try {
            return objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write api response", e);
        }
    }
}
