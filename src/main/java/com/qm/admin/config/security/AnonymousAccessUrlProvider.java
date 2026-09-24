package com.qm.admin.config.security;

import com.qm.admin.common.annotation.AnonymousAccess;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class AnonymousAccessUrlProvider {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public AnonymousAccessUrlProvider(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    public Set<String> getAnonymousAccessUrls() {
        Set<String> urls = new LinkedHashSet<>();
        requestMappingHandlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {
            // Class-level annotation is treated as a shortcut for all endpoints in that controller.
            boolean anonymousAccess = AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), AnonymousAccess.class)
                    || AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), AnonymousAccess.class);
            if (anonymousAccess) {
                urls.addAll(getPatternValues(mappingInfo));
            }
        });
        return urls;
    }

    private Set<String> getPatternValues(RequestMappingInfo mappingInfo) {
        if (mappingInfo.getPathPatternsCondition() != null) {
            return mappingInfo.getPathPatternsCondition().getPatternValues();
        }
        if (mappingInfo.getPatternsCondition() != null) {
            return mappingInfo.getPatternsCondition().getPatterns();
        }
        return Set.of();
    }
}
