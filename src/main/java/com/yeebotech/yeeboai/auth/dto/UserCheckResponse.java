package com.yeebotech.yeeboai.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCheckResponse {
    private int code;  // 与 JSON 中的 "code" 字段对应
    private String msg;  // 与 JSON 中的 "message" 字段对应
    private boolean success;  // 与 JSON 中的 "success" 字段对应
    private Data data;  // 包含的 Data 对象

    @Getter
    @Setter
    public static class Data {
        private String content;  // 添加这个字段
        @JsonProperty("expires_in")  // 添加这个注解来处理 JSON 中的 "expires_in"
        private Long expiresIn;  // 与 JSON 中的 "expires_in" 字段对应
    }
}
