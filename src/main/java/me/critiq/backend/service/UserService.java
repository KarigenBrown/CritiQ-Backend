package me.critiq.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpSession;
import me.critiq.backend.domain.dto.LoginFormDto;
import me.critiq.backend.domain.dto.RegisterFormDto;
import me.critiq.backend.domain.entity.User;

/**
 * (User)表服务接口
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
public interface UserService extends IService<User> {

    void getCode(String email);

    void register(RegisterFormDto registerForm);

    String login(LoginFormDto loginForm);
}

