package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.ThirdUserMapper;
import me.critiq.backend.domain.entity.ThirdUser;
import me.critiq.backend.service.ThirdUserService;
import org.springframework.stereotype.Service;

/**
 * (ThirdUser)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-11 22:35:28
 */
@Service("thirdUserService")
public class ThirdUserServiceImpl extends ServiceImpl<ThirdUserMapper, ThirdUser> implements ThirdUserService {

}

