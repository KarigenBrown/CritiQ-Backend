package me.critiq.backend.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.service.UserService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> getCode(@RequestParam("email") String email, HttpSession session) {
        var succeed = userService.getCode(email, session);

        if (succeed) {
            return ResponseEntity
                    .ok()
                    .build();
        }

        return ResponseEntity.badRequest()
                .body("手机号格式错误");
    }
}

