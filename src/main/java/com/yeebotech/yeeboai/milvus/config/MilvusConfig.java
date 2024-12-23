package com.yeebotech.yeeboai.milvus.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {

    @Bean
    public MilvusClientV2 milvusClient() {
        // 使用官方代码创建 Milvus 客户端，配置 Milvus 的服务地址和端口
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri("http://47.113.103.241:19530")  // Milvus 服务地址
                .token("root:Milvus")                // Milvus 认证 token
                .build();

        // 创建并返回 Milvus 客户端实例
        return new MilvusClientV2(connectConfig);
    }
}
