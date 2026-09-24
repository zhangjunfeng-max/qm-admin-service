package com.qm.admin.config.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @description:
 * @author: ZhangJunFeng
 * @date: 2026/9/19 21:35
 */

@Data
@ConfigurationProperties(prefix = "qm.tenant")
public class QmTenantProperties {

    private List<String> ignoreTables = List.of("sys_tenant", "sys_user", "sys_menu");
}
