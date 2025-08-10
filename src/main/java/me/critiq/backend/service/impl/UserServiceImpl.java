package me.critiq.backend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.mapper.UserMapper;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.service.EmailService;
import me.critiq.backend.service.UserService;
import me.critiq.backend.util.RegexUtil;
import org.springframework.stereotype.Service;

/**
 * (User)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final EmailService emailService;

    @Override
    public boolean getCode(String email, HttpSession session) {
        // 1.校验手机号
        if (RegexUtil.isEmailInvalid(email)) {
            // 2.如果不符合,返回错误信息
            return false;
        }
        // 3.符合,生成验证码
        var code = RandomUtil.randomNumbers(6);
        // 4.保存验证码到session
        session.setAttribute("code", code);
        // 5.发送验证码
        emailService.sendCode(email, code);
        return true;
    }
}

