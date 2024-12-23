package com.yeebotech.yeeboai.auth.dto;
import lombok.Data;

@Data
public class UserInfo {

    private Long userId;
    private String email;
    private String username;

    // 构造函数
    public UserInfo(Long userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.username = username;
    }

    // 默认构造函数和 getter/setter 方法会由 Lombok 自动生成
}

