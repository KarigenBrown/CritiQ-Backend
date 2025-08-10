package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.SignMapper;
import me.critiq.backend.domain.entity.Sign;
import me.critiq.backend.service.SignService;
import org.springframework.stereotype.Service;

/**
 * (Sign)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("signService")
public class SignServiceImpl extends ServiceImpl<SignMapper, Sign> implements SignService {

}

