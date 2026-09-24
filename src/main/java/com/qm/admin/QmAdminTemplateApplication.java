package com.qm.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * @author zjf
 */
// 项目使用数据库账号 + 自有 Bearer Token 认证，不使用 Spring Boot 默认内存用户。
// 显式排除默认用户自动配置，避免启动时生成无效的随机密码并误导部署人员。
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@MapperScan("com.qm.admin.**.mapper")
public class QmAdminTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(QmAdminTemplateApplication.class, args);
    }

}
