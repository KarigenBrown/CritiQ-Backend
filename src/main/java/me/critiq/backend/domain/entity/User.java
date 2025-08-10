package me.critiq.backend.domain.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * (User)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Builder
@TableName("user")
@EqualsAndHashCode(callSuper = false)
public class User implements UserDetails {
    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 邮箱
    @Email
    private String email;
    // 手机号码
    private String phone;
    // 密码,加密存储
    private String password;
    // 会员级别,0~9级,0代表未开通会员
    @Max(9)
    @Min(10)
    private Integer level = 0;
    // 昵称,默认是用户id
    private String nickName;
    // 人物头像
    private String icon = "";
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.level.toString()));
    }

    // 将用户email作用用户名
    @Override
    public String getUsername() {
        return this.email;
    }
}

