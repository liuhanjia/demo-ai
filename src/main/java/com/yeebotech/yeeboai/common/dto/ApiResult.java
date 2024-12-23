package com.yeebotech.yeeboai.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    // 已有的方法
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(8200, "SUCCESS", data);
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(8200, "SUCCESS", null);
    }

    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    // 新增的方法：支持自定义状态码和消息
    public static <T> ApiResult<T> success(T data, int code, String message) {
        return new ApiResult<>(code, message, data);
    }
}
