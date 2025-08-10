package me.critiq.backend.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.domain.dto.LoginFormDto;
import me.critiq.backend.domain.dto.RegisterFormDto;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.service.UserService;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    // 服务对象
    private final UserService userService;

    @GetMapping("/code")
    // 原版使用手机号实现
    public ResponseEntity<String> getCode(
            // todo 变更为手机号
            @RequestParam("email") String email,
            HttpSession session
    ) {
        userService.getCode(email, session);
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody RegisterFormDto registerForm,
            HttpSession session
    ) {
        userService.register(registerForm, session);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginFormDto loginForm) {
        var token = userService.login(loginForm);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('level_0')")
    public ResponseEntity<User> me() {
        Authentication authentication = SecurityUtil.getAuthentication();
        log.info(authentication.getAuthorities().toString());
        var userid = SecurityUtil.getUserId();
        var user = userService.getById(userid);
        return ResponseEntity.ok(user);
    }
}

