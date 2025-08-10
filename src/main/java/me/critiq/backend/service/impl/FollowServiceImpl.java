package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.FollowMapper;
import me.critiq.backend.domain.entity.Follow;
import me.critiq.backend.service.FollowService;
import org.springframework.stereotype.Service;

/**
 * (Follow)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("followService")
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

}

