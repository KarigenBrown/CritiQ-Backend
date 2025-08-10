package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.service.BlogCommentsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * (BlogComments)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:06
 */
@RestController
@RequestMapping("/blogComments")
@RequiredArgsConstructor
public class BlogCommentsController {
    // 服务对象
    private final BlogCommentsService blogCommentsService;
}

