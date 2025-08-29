package me.critiq.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.critiq.backend.domain.entity.Blog;
import me.critiq.backend.domain.entity.User;

import java.util.List;

/**
 * (Blog)表服务接口
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:15
 */
public interface BlogService extends IService<Blog> {

    List<Blog> queryHotBlog(Integer current);

    Blog queryByBlogId(Long id);

    void likeBlog(Long id);

    List<User> queryBlogLikes(Long id);
}

