package me.critiq.backend.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.Blog;
import me.critiq.backend.domain.vo.ScrollVo;
import me.critiq.backend.domain.vo.UserVo;
import me.critiq.backend.service.BlogService;
import me.critiq.backend.util.PathUtil;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.function.Tuple3;

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

    @DeleteMapping("/img/{name}")
    public ResponseEntity<Void> deleteBlogImg(@PathVariable("name") String filename) {
        s3Template.deleteObject(SystemConstant.BUCKET_NAME, filename);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Long> saveBlog(@RequestBody Blog blog) {
        blogService.saveBlog(blog);
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

    @GetMapping("/of/user")
    public ResponseEntity<List<Blog>> queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id
    ) {
        // 根据用户查询
        var records = blogService.lambdaQuery()
                .eq(Blog::getUserId, id)
                .page(Page.of(current, SystemConstant.MAX_PAGE_SIZE))
                // 获取当前页数据
                .getRecords();
        return ResponseEntity.ok(records);
    }

    /*
    动分页询参数:
    max:当前时间戳|上一次查询的最小时间戳
    min:0
    offset:0|在上一次的结果中,与最小值一样的元素的个数(如何保证时间戳一样的内部排序也一样?score一样会依据key的字典序排列)
    count:3
     */
    @GetMapping("/of/follow")
    public ResponseEntity<ScrollVo<Blog>> queryBlogOfFollow(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset
    ) {
        var result = blogService.queryBlogOfFollow(max, offset);
        var scrollVo = ScrollVo.<Blog>builder()
                .list(result.getT1())
                .minTime(result.getT2())
                .offset(result.getT3())
                .build();
        return ResponseEntity.ok(scrollVo);
    }

    @GetMapping("/of/me")
    public ResponseEntity<List<Blog>> queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        var userId = SecurityUtil.getUserId();
        // 根据用户查询
        var records = blogService.lambdaQuery()
                .eq(Blog::getUserId, userId)
                .page(Page.of(current, SystemConstant.MAX_PAGE_SIZE))
                // 获取当前页数据
                .getRecords();
        return ResponseEntity.ok(records);
    }
}

