package com.yeebotech.yeeboai.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许所有路径
                .allowedOrigins("*")  // 允许所有来源
                .allowedMethods("*")  // 允许所有 HTTP 方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(false)  // 不需要携带凭据（如 Cookie）
                .maxAge(3600);  // 缓存跨域请求的有效期，单位秒
    }

}
