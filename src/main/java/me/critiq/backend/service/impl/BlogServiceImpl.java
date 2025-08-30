package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.Follow;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.enums.ResponseStatusEnum;
import me.critiq.backend.exception.SystemException;
import me.critiq.backend.mapper.BlogMapper;
import me.critiq.backend.domain.entity.Blog;
import me.critiq.backend.mapper.FollowMapper;
import me.critiq.backend.mapper.UserMapper;
import me.critiq.backend.service.BlogService;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.*;

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
    private final FollowMapper followMapper;

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
        var idStr = String.join(",", top5);
        // 3.根据用户id查询用户 WHERE id IN (5, 1) ORDER BY FIELD(id, 5, 1)
        var condition = Wrappers.<User>lambdaQuery()
                .in(User::getId, ids)
                .last("ORDER BY FIELD(id, " + idStr + ")");
        var users = userMapper.selectList(condition);
        // 4.返回
        return users;
    }

    @Override
    public void saveBlog(Blog blog) {
        // 1.获取登录用户
        var userId = SecurityUtil.getUserId();
        blog.setUserId(userId);
        // 2.保存探店博文
        boolean isSuccess = this.save(blog);
        if (!isSuccess) {
            throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
        }
        // 3.查询笔记作者的所有粉丝 SELECT * FROM follow WHERE follow_user_id = ?
        var follows = followMapper.selectList(
                Wrappers.<Follow>lambdaQuery()
                        .eq(Follow::getFollowUserId, SecurityUtil.getUserId())
        );
        // 4.推送笔记id给所有粉丝
        for (var follow : follows) {
            // 4.1获取粉丝id
            var fanId = follow.getUserId();
            // 4.2推送
            var key = SystemConstant.FEED_KEY + fanId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
    }

    @Override
    public Tuple3<List<Blog>, Long, Integer> queryBlogOfFollow(Long max, Integer offset) {
        // 1.获取当前用户
        var userId = SecurityUtil.getUserId();
        // 2.查询收件箱 ZREVRANGEBYSCORE key max min LIMIT offset count
        var key = SystemConstant.FEED_KEY + userId;
        var result = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(
                key,
                0,
                max,
                offset,
                SystemConstant.MAX_PAGE_SIZE
        );
        // 3.非空判断
        if (CollectionUtils.isEmpty(result)) {
            return Tuples.of(null, null, null);
        }
        // 4.解析数据:blogId,minTime(时间戳),offset
        List<String> ids = new ArrayList<>(result.size());
        int nextOffset = 1;
        long minTime = 0;
        for (var tuple : result) {
            // 4.1获取id
            ids.add(tuple.getValue());
            // 4.2获取分数(时间戳)
            var time = tuple.getScore().longValue();
            if (time == minTime) {
                nextOffset++;
            } else {
                minTime = time;
                nextOffset = 1;
            }
        }
        // 5.根据id查询blog
        var idStr = String.join(",", ids);
        var idList = ids.stream().map(Long::valueOf).toList();
        var blogs = this.lambdaQuery()
                .in(Blog::getId, idList)
                .last("ORDER BY FIELD(id, " + idStr + ")")
                .list();

        for (var blog : blogs) {
            // 5.1查询blog有关的用户
            queryBlogUser(blog);
            // 5.2查询blog是否被点赞
            isBlogLiked(blog);
        }

        // 6.封装并返回
        return Tuples.of(blogs, minTime, nextOffset);
    }

    private void queryBlogUser(Blog blog) {
        var userId = blog.getUserId();
        var user = userMapper.selectById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}

