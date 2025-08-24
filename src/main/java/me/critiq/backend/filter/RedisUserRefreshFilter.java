package me.critiq.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUserRefreshFilter extends OncePerRequestFilter {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authentication = SecurityUtil.getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            var userId = SecurityUtil.getUserId();
            stringRedisTemplate.expire(SystemConstant.LOGIN_USER_KEY + userId, Duration.ofMinutes(SystemConstant.LOGIN_USER_TTL));
        }
        filterChain.doFilter(request, response);
    }
}
