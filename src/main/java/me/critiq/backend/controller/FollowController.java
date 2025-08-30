package me.critiq.backend.controller;


import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.domain.entity.Follow;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.domain.vo.UserVo;
import me.critiq.backend.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * (Follow)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {
    // 服务对象
    private final FollowService followService;

    @PutMapping("/{followUserId}/{isFollow}")
    public ResponseEntity<Void> follow(
            @PathVariable("followUserId") Long followUserId,
            @PathVariable("isFollow") Boolean isFollow
    ) {
        followService.follow(followUserId, isFollow);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/or/not/{followUserId}")
    public ResponseEntity<Boolean> isFollow(@PathVariable("followUserId") Long followUserId) {
        var isFollow = followService.isFollow(followUserId);
        return ResponseEntity.ok(isFollow);
    }

    @GetMapping("/common/{peerId}")
    public ResponseEntity<List<UserVo>> followCommons(@PathVariable("peerId") Long peerId) {
        var commons = followService.followCommons(peerId);
        var userVos = BeanUtil.copyToList(commons, UserVo.class);
        return ResponseEntity.ok(userVos);
    }
}