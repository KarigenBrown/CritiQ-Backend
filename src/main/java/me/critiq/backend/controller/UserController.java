package me.critiq.backend.controller;


import cn.hutool.core.bean.BeanUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.dto.LoginFormDto;
import me.critiq.backend.domain.dto.RegisterFormDto;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.domain.entity.UserInfo;
import me.critiq.backend.domain.vo.UserVo;
import me.critiq.backend.service.UserService;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * (User)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/code")
    // 原版使用手机号实现
    public ResponseEntity<String> getCode(
            // todo 变更为手机号
            @Valid @RequestParam("email") String email
    ) {
        userService.getCode(email);
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterFormDto registerForm) {
        userService.register(registerForm);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginFormDto loginForm) {
        var token = userService.login(loginForm);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('level_0')")
    public ResponseEntity<UserVo> me() {
        var userId = SecurityUtil.getUserId();
        var userMap = stringRedisTemplate.opsForHash().entries(SystemConstant.LOGIN_USER_KEY + userId);
        var userVo = BeanUtil.toBean(userMap, UserVo.class);
        return ResponseEntity.ok(userVo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserVo> queryUserById(@PathVariable("id") Long id) {
        // 查询详情
        var user = userService.getOptById(id);
        if (user.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        var userVo = BeanUtil.copyProperties(user.get(), UserVo.class);
        // 返回
        return ResponseEntity.ok(userVo);
    }

    @PostMapping("/sign")
    public ResponseEntity<Void> sign() {
        userService.sign();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sign/count")
    public ResponseEntity<Integer> signCount() {
        var count = userService.signCount();
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout() {
        // TODO 实现登出功能
        return ResponseEntity.ok().build();
    }
}

