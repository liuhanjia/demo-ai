package com.yeebotech.yeeboai.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 创建 SecurityScheme
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("请输入有效的 Token");

        // 添加全局 SecurityRequirement
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("Yeebo AI API")
                        .version("1.0")
                        .description("Yeebo AI API 文档"))
                .addSecurityItem(securityRequirement) // 应用到全局
                .schemaRequirement("Authorization", securityScheme);
    }
}
