package me.critiq.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nimbusds.jwt.JWTClaimsSet;
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
import me.critiq.backend.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.BitFieldSubCommands;
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
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        // 因为issuedAt和expiresAt接口不兼容,暂时放弃
        var claims = JwtClaimsSet.builder()
                .issuer(SystemConstant.SELF)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(Duration.ofHours(7).toSeconds()))
                .subject(authentication.getName())
                .claim(SystemConstant.ID, user.getId())
                // 大坑,authority必须是字符串
                .claim(SystemConstant.LEVEL, user.getLevel().toString())
                .build();

        /*var claims = new JWTClaimsSet.Builder()
                .issuer(SystemConstant.SELF)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(Duration.ofHours(7).toSeconds())))
                .subject(authentication.getName())
                .claim(SystemConstant.ID, user.getId())
                .claim(SystemConstant.LEVEL, user.getLevel().toString())
                .build();*/
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public void sign() {
        // 1.获取当前登录的用户
        var userId = SecurityUtil.getUserId();
        // 2.获取日期
        var now = LocalDate.now();
        // 3.拼接key
        var keyInfix = now.format(DateTimeFormatter.ofPattern("yyyy:MM:"));
        var key = SystemConstant.SIGN_KEY + keyInfix + userId;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.写入redis SET key offset 1
        stringRedisTemplate.opsForValue().setBit(
                key,
                dayOfMonth - 1,
                true
        );
    }

    @Override
    public Integer signCount() {
        // 1.获取当前登录的用户
        var userId = SecurityUtil.getUserId();
        // 2.获取日期
        var now = LocalDate.now();
        // 3.拼接key
        var keyInfix = now.format(DateTimeFormatter.ofPattern("yyyy:MM:"));
        var key = SystemConstant.SIGN_KEY + keyInfix + userId;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.获取本月截至今天为止的所有签到记录 BITFIELD key GET type index
        var result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (CollectionUtils.isEmpty(result)) {
            // 没有签到结果
            return 0;
        }
        var number = result.getFirst();
        if (number == null || number == 0) {
            return 0;
        }
        // 6.循环遍历
        var counter = 0;
        while (true) {
            // 6.1让这个数字与1做与运算,得到数字的最后一个bit位,判断这个bit位是否为0
            if ((number & 1) == 0) {
                // 6.2如果为0,说明未签到,结束
                break;
            } else {
                // 6.3如果不为0,说明已签到,计数器+1
                counter++;
            }
            // 6.5把数字右移一位,抛弃最后一个bit位,继续下一个bit位
            number >>>= 1;
        }
        return counter;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.lambdaQuery()
                .eq(User::getEmail, username)
                .one();
    }
}

