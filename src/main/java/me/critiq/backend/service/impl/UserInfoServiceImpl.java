package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.UserInfoMapper;
import me.critiq.backend.domain.entity.UserInfo;
import me.critiq.backend.service.UserInfoService;
import org.springframework.stereotype.Service;

/**
 * (UserInfo)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("userInfoService")
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

}

