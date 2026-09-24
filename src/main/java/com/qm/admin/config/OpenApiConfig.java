package com.qm.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI qmAdminTemplateOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QM Admin Template API")
                        .description("Enterprise backend template interface documentation")
                        .version("0.0.1")
                        .contact(new Contact().name("QM"))
                        .license(new License().name("Private")));
    }
}
