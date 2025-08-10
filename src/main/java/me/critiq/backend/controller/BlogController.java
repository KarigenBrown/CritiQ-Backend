package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.service.BlogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * (Blog)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:06
 */
@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {
    // 服务对象
    private final BlogService blogService;
}

