package me.critiq.backend.controller;


import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.service.BlogService;
import me.critiq.backend.util.PathUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    private final S3Template s3Template;

    @PostMapping("/img")
    public ResponseEntity<String> upload(@RequestParam("img") MultipartFile file) throws IOException {
        try (var is = file.getInputStream()) {
            var originalFilename = file.getOriginalFilename();
            var filename = PathUtil.dateUuidPath(originalFilename);
            var url = s3Template.upload(SystemConstant.BUCKET_NAME, filename, is).getURL().toString();
            return ResponseEntity.ok(url);
        }
    }
}

