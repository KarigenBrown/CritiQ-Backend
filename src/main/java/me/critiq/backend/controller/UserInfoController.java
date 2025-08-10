package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * (UserInfo)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@RestController
@RequestMapping("/userInfo")
@RequiredArgsConstructor
public class UserInfoController {
    // 服务对象
    private final UserInfoService userInfoService;
}

