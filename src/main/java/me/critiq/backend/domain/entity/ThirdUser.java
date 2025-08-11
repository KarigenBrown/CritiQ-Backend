package me.critiq.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * (ThirdUser)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-11 22:37:00
 */
@Data
@Builder
@TableName("user")
@EqualsAndHashCode(callSuper = false)
public class ThirdUser {
    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 邮箱
    @Email
    private String email;
    // 手机号码
    private String phone;
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

    // 第三方主键
    private Long thirdId;
    // token
    private String credentials;
    // token过期时间
    private Instant credentialsExpiry;
    // 第三方网站名
    private String registrationId;
}
