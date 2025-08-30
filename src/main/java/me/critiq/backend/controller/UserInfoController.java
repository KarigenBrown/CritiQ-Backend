package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.domain.entity.UserInfo;
import me.critiq.backend.service.UserInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * (UserInfo)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@RestController
@RequestMapping("/user-info")
@RequiredArgsConstructor
public class UserInfoController {
    // 服务对象
    private final UserInfoService userInfoService;

    @GetMapping("/{id}")
    public ResponseEntity<UserInfo> info(@PathVariable("id") Long userId){
        // 查询详情
        var result = userInfoService.getOptById(userId);
        if (result.isEmpty()) {
            // 没有详情，应该是第一次查看详情
            return ResponseEntity.ok().build();
        }
        var info = result.get();
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return ResponseEntity.ok(info);
    }
}

