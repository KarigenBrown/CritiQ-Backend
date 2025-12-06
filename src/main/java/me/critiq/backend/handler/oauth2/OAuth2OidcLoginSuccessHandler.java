package me.critiq.backend.handler.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2OidcLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        // 获取用户信息
        String username = oidcUser.getName(); // GitHub用户名
        String email = oidcUser.getEmail();

        log.info("OAuth2 login success for user: {}", username);
        log.info("{}", request.getParameter("code"));

        // 构建响应数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "OAuth2 登录成功！");
        responseData.put("user", Map.of(
                "username", username != null ? username : "unknown",
                "email", email != null ? email : "unknown"
        ));
        responseData.put("timestamp", System.currentTimeMillis());

        // 设置响应头
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(responseData));
        response.getWriter().flush();
    }
}