package me.critiq.backend.handler.oauth2;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    @Lazy
    @Autowired
    private UserService userService;
    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Lazy
    @Autowired
    private JwtEncoder jwtEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (authentication.getPrincipal() instanceof OidcUser oidcUser){
            log.info("oidc user");
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 获取用户信息
        String username = oAuth2User.getAttribute("login"); // GitHub用户名
        String email = oAuth2User.getAttribute("email");
        String third = "GitHub";
        var user = User.builder()
                .email(email)
                .password("")
                .nickName(SystemConstant.USER_ + RandomUtil.randomString(10))
                .third(third)
                .build();
        /*Optional<User> result = userService.lambdaQuery()
                .eq(User::getEmail, email)
                .eq(User::getThird, third)
                .oneOpt();
        if (result.isEmpty()) {
            log.info("查无此人");
            userService.save(user);
        }*/

        log.info("OAuth2 login success for user: {}", username);
        log.info("{}", request.getParameter("code"));

        // 构建响应数据
        /*var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(SystemConstant.SELF)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(Duration.ofHours(7).toSeconds()))
                .subject(authentication.getName())
                .claim(SystemConstant.ID, user.getId())
                // 大坑,authority必须是字符串
                .claim(SystemConstant.LEVEL, user.getLevel().toString())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();*/
        String token = "token";

        // 设置响应头
        response.setContentType(MimeTypeUtils.TEXT_PLAIN_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        // 写入响应
        response.getWriter().write(token);
        response.getWriter().flush();
    }
}