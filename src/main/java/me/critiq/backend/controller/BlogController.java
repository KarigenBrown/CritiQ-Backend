package me.critiq.backend.controller;


import cn.hutool.core.bean.BeanUtil;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.Blog;
import me.critiq.backend.domain.vo.UserVo;
import me.critiq.backend.service.BlogService;
import me.critiq.backend.util.PathUtil;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
            // 获取原始文件名
            var originalFilename = file.getOriginalFilename();
            // 生成新文件名
            var filename = PathUtil.dateUuidPath(originalFilename);
            // 保存文件
            var url = s3Template.upload(SystemConstant.BUCKET_NAME, filename, is).getURL().toString();
            // 返回结果
            return ResponseEntity.ok(url);
        }
    }

    @PostMapping
    public ResponseEntity<Long> saveBlog(@RequestBody Blog blog) {
        // 获取登录用户
        var userId = SecurityUtil.getUserId();
        blog.setUserId(userId);
        // 保存探店博文
        blogService.save(blog);
        // 返回id
        return ResponseEntity.ok(blog.getId());
    }

    @PutMapping("/like/{id}")
    public ResponseEntity<Void> likeBlog(@PathVariable("id") Long id) {
        blogService.likeBlog(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hot")
    public ResponseEntity<List<Blog>> queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        var hotBlog = blogService.queryHotBlog(current);
        return ResponseEntity.ok(hotBlog);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> queryBlogById(@PathVariable("id") Long id) {
        var blog = blogService.queryByBlogId(id);
        return ResponseEntity.ok(blog);
    }

    @GetMapping("/likes/{id}")
    public ResponseEntity<List<UserVo>> queryBlogLikes(@PathVariable("id") Long id) {
        var users = blogService.queryBlogLikes(id);
        var userVos = BeanUtil.copyToList(users, UserVo.class);
        return ResponseEntity.ok(userVos);
    }
}

