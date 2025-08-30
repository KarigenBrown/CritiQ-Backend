package me.critiq.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.critiq.backend.domain.entity.Follow;
import me.critiq.backend.domain.entity.User;

import java.util.List;

/**
 * (Follow)表服务接口
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
public interface FollowService extends IService<Follow> {

    void follow(Long followUserId, Boolean isFollow);

    Boolean isFollow(Long followUserId);

    List<User> followCommons(Long peerId);
}

