package me.critiq.backend.util;

import me.critiq.backend.constant.SystemConstant;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtil {
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static Jwt getJwt() {
        return (Jwt) SecurityUtil.getAuthentication().getPrincipal();
    }

    public static Long getUserId() {
        Jwt jwt = SecurityUtil.getJwt();
        return jwt.getClaim(SystemConstant.ID);
    }

    private SecurityUtil() {
    }
}
