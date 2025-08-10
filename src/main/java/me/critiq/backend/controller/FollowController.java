package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.service.FollowService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * (Follow)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {
    // 服务对象
    private final FollowService followService;
}

