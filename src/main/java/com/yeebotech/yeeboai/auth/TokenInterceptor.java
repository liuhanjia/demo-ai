package com.yeebotech.yeeboai.auth;

import com.yeebotech.yeeboai.auth.dto.TokenRequest;
import com.yeebotech.yeeboai.auth.dto.UserCheckResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final WebClient webClient;

    public TokenInterceptor(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://47.113.103.241:8083").build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是 OPTIONS 请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
            System.out.println("Token extracted: " + token);
            if (validateToken(token)) {
                System.out.println("Token valid: " + token);
                return true;
            } else {
                System.out.println("Token validation failed: " + token);
            }
        } else {
            System.out.println("No token or incorrect token format.");
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized: Invalid token");
        return false;
    }

    private boolean validateToken(String token) {
        try {
            // 去除 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            TokenRequest tokenRequest = new TokenRequest(token);
            UserCheckResponse userCheckResponse = webClient
                    .post()
                    .uri("/auth/check-token")
                    .bodyValue(tokenRequest)  // 将 TokenRequest 作为请求体，发送 JSON
                    .retrieve()
                    .bodyToMono(UserCheckResponse.class)
                    .block();

            // 根据返回的数据判断 token 是否有效
            return userCheckResponse != null
                    && userCheckResponse.getCode() == 200
                    && userCheckResponse.getData().getExpiresIn() != null // 确保不为空
                    && userCheckResponse.getData().getExpiresIn() > 0; // 确保大于 0

        } catch (Exception e) {
            e.printStackTrace();
            return false; // 如果出现任何异常，则认为 token 无效
        }
    }
}
