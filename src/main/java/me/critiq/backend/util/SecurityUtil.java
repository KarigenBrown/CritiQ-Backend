package me.critiq.backend.util;

import me.critiq.backend.constant.SystemConstant;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtil {
    private static Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Long getUserId() {
        Jwt jwt = SecurityUtil.getJwt();
        return jwt.getClaim(SystemConstant.ID);
    }

    private SecurityUtil() {
    }
}
