package me.critiq.backend.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * (User)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Accessors(chain = true)
@TableName("user")
@EqualsAndHashCode(callSuper = false)
public class User {
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
    // 昵称,默认是用户id
    private String nickName;
    // 人物头像
    private String icon = "";
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
}

