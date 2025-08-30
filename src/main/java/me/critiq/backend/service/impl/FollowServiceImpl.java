package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.mapper.FollowMapper;
import me.critiq.backend.domain.entity.Follow;
import me.critiq.backend.service.FollowService;
import me.critiq.backend.service.UserService;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * (Follow)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("followService")
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {
    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;

    @Override
    public void follow(Long followUserId, Boolean isFollow) {
        // 获取登录用户id
        var userId = SecurityUtil.getUserId();
        var key = SystemConstant.FOLLOWS_KEY + userId;
        // 1.判断是否是关注还是取关
        if (isFollow) {
            // 2.关注,新增数据
            var follow = Follow.builder()
                    .userId(userId)
                    .followUserId(followUserId)
                    .build();
            boolean isSuccess = this.save(follow);
            if (isSuccess) {
                // 把关注用户的id,放入redis的set集合 SADD userId followUserId
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 3.取关,删除 DELETE FROM follow WHERE user_id = ? AND follow_user_id = ?
            boolean isSuccess = this.lambdaUpdate()
                    .eq(Follow::getUserId, userId)
                    .eq(Follow::getFollowUserId, followUserId)
                    .remove();
            if (isSuccess) {
                // 一般情况下只要谨记,操作redis在操作数据库之后即可
                // 把关注用户的id从redis集合中移除
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
    }

    @Override
    public Boolean isFollow(Long followUserId) {
        // 获取登录用户id
        var userId = SecurityUtil.getUserId();
        // 查询是否关注 SELECT * FROM follow WHERE user_id = ? AND follow_user_id = ?
        var follow = this.lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId)
                .oneOpt();
        // 判断
        return follow.isPresent();
    }

    @Override
    public List<User> followCommons(Long peerId) {
        // 1.获取当前用户id
        var userId = SecurityUtil.getUserId();
        // 2.求交集
        var intersect = stringRedisTemplate.opsForSet().intersect(
                SystemConstant.FOLLOWS_KEY + userId,
                SystemConstant.FOLLOWS_KEY + peerId
        );
        if (CollectionUtils.isEmpty(intersect)) {
            return Collections.emptyList();
        }
        // 3.解析id集合
        var ids = intersect.stream().map(Long::valueOf).toList();
        // 4.查询用户
        var users = userService.listByIds(ids);
        return users;
    }
}

