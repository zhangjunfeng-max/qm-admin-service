package com.qm.admin.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zjf
 */
@ConfigurationProperties(prefix = "qm.security")
public class SecurityPermitProperties {

    private List<String> permitUrls = new ArrayList<>();

    public List<String> getPermitUrls() {
        return permitUrls;
    }

    public void setPermitUrls(List<String> permitUrls) {
        this.permitUrls = permitUrls;
    }
}
