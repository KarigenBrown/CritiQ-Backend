package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.enums.ResponseStatusEnum;
import me.critiq.backend.exception.SystemException;
import me.critiq.backend.mapper.BlogMapper;
import me.critiq.backend.domain.entity.Blog;
import me.critiq.backend.mapper.UserMapper;
import me.critiq.backend.service.BlogService;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * (Blog)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:15
 */
@Service("blogService")
@RequiredArgsConstructor
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<Blog> queryHotBlog(Integer current) {
        // 根据用户查询
        var records = this.lambdaQuery()
                .orderByDesc(Blog::getLiked)
                .page(Page.of(current, SystemConstant.MAX_PAGE_SIZE))
                // 获取当前页数据
                .getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return List.of();
    }

    @Override
    public Blog queryByBlogId(Long id) {
        // 1.查询blog
        var blog = this.getById(id);
        if (Objects.isNull(blog)) {
            throw new SystemException(ResponseStatusEnum.NOT_FOUND);
        }
        // 2.查询blog有关的用户
        queryBlogUser(blog);
        // 3.查询blog是否被点赞
        isBlogLiked(blog);
        return blog;
    }

    private void isBlogLiked(Blog blog) {
        if (!SecurityUtil.getAuthentication().isAuthenticated()) {
            // 用户未登录,无需查询是否点赞
            return;
        }
        // 1.获取登录用户
        var userId = SecurityUtil.getUserId();
        // 2.判断当前登录用户是否已经点赞
        var score = stringRedisTemplate.opsForZSet().score(SystemConstant.BLOG_LIKED_KEY + blog.getId(), userId.toString());
        blog.setIsLiked(Objects.nonNull(score));
    }

    @Override
    public void likeBlog(Long id) {
        // 1.获取登录用户
        var userId = SecurityUtil.getUserId();
        // 2.判断当前登录用户是否已经点赞
        var key = SystemConstant.BLOG_LIKED_KEY + id;
        var score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (Objects.isNull(score)) {
            // 3.如果未点赞,可以点赞
            // 3.1数据库点赞数+1
            var isSuccess = this.lambdaUpdate()
                    .setSql("liked = liked + 1")
                    .eq(Blog::getId, id)
                    .update();
            // 3.2保存用户到redis的set集合 zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4.如果已点赞,取消点赞
            // 4.1数据库点赞数-1
            var isSuccess = this.lambdaUpdate()
                    .setSql("liked = liked - 1")
                    .eq(Blog::getId, id)
                    .update();
            // 4.2把用户从redis的set集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
    }

    @Override
    public List<User> queryBlogLikes(Long id) {
        var key = SystemConstant.BLOG_LIKED_KEY + id;
        // 1.查询top5的点赞用户 zrange key 0 4
        var top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (CollectionUtils.isEmpty(top5)) {
            return Collections.emptyList();
        }
        // 2.解析出其中用户id
        var ids = top5.stream().map(Long::valueOf).toList();
        String idStr = String.join(",", top5);
        // 3.根据用户id查询用户 WHERE id IN (5, 1) ORDER BY FIELD(id, 5, 1)
        var condition = Wrappers.<User>lambdaQuery()
                .in(User::getId, ids)
                .last("ORDER BY FIELD(id, " + idStr + ")");
        var users = userMapper.selectList(condition);
        // 4.返回
        return users;
    }

    private void queryBlogUser(Blog blog) {
        var userId = blog.getUserId();
        var user = userMapper.selectById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}

