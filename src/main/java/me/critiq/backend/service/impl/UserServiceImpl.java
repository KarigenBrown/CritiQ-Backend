package me.critiq.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.domain.dto.LoginFormDto;
import me.critiq.backend.domain.dto.RegisterFormDto;
import me.critiq.backend.domain.vo.UserVo;
import me.critiq.backend.enums.ResponseStatusEnum;
import me.critiq.backend.exception.SystemException;
import me.critiq.backend.mapper.UserMapper;
import me.critiq.backend.domain.entity.User;
import me.critiq.backend.service.EmailService;
import me.critiq.backend.service.UserService;
import me.critiq.backend.util.RegexUtil;
import me.critiq.backend.constant.SystemConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * (User)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Slf4j
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, UserDetailsService {
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void getCode(String email) {
        // 1.校验手机号
        if (RegexUtil.isEmailInvalid(email)) {
            // 2.如果不符合,返回错误信息
            throw new SystemException(ResponseStatusEnum.INVALID_EMAIL);
        }
        // 3.符合,生成验证码
        var code = RandomUtil.randomNumbers(6);
        log.info("code: {}", code);
        // 4.保存验证码到redis
        stringRedisTemplate.opsForValue().set(SystemConstant.LOGIN_CODE_KEY + email, code, Duration.ofMinutes(SystemConstant.LOGIN_CODE_TTL));
        // 5.发送验证码
        // 已经删除授权码
        // emailService.sendCode(email, code);
    }

    @Override
    public void register(RegisterFormDto registerForm) {
        // 1.校验手机号
        var email = registerForm.getEmail();
        if (RegexUtil.isEmailInvalid(email)) {
            throw new SystemException(ResponseStatusEnum.INVALID_EMAIL);
        }
        // 2.校验验证码
        var cacheCode = stringRedisTemplate.opsForValue().get(SystemConstant.LOGIN_CODE_KEY + email);
        log.info("{}", cacheCode);
        // var code = registerForm.getCode();
        // 3.不一致,报错
        // if (!Objects.equals(code, cacheCode)) {
        //     throw new SystemException(ResponseStatusEnum.CODE_ERROR);
        // }
        // 4.新建用户
        var user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(registerForm.getPassword()))
                .nickName(SystemConstant.USER_ + RandomUtil.randomString(10))
                .build();
        // 5.保存用户
        this.save(user);
    }

    @Override
    public String login(LoginFormDto loginForm) {
        // 1.校验手机号
        var email = loginForm.getEmail();
        if (RegexUtil.isEmailInvalid(email)) {
            throw new SystemException(ResponseStatusEnum.INVALID_EMAIL);
        }
        // 2.一致,根据手机号查询用户
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword());
        var authentication = authenticationManager.authenticate(authenticationToken);
        var user = (User) authentication.getPrincipal();

        // 3.将用户信息存入redis
        var userVo = BeanUtil.copyProperties(user, UserVo.class);
        Map<String, Object> userHash = BeanUtil.beanToMap(
                userVo,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString())
        );
        String redisKey = SystemConstant.LOGIN_USER_KEY + user.getId();
        stringRedisTemplate.opsForHash().putAll(redisKey, userHash);
        stringRedisTemplate.expire(redisKey, Duration.ofMinutes(SystemConstant.LOGIN_USER_TTL));

        // 4.生成token并返回
        // 通过jwt+spring security+security context holder的实现等效于session+thread local+interceptor
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(SystemConstant.SELF)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(Duration.ofHours(7).toSeconds()))
                .subject(authentication.getName())
                .claim(SystemConstant.ID, user.getId())
                // 大坑,authority必须是字符串
                .claim(SystemConstant.LEVEL, user.getLevel().toString())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.lambdaQuery()
                .eq(User::getEmail, username)
                .one();
    }
}

