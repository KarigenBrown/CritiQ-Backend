package me.critiq.backend.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * (UserInfo)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Accessors(chain = true)
@TableName("user_info")
@EqualsAndHashCode(callSuper = false)
public class UserInfo {
    // 主键,用户id
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    // 城市名称
    private String city;
    // 个人介绍,不要超过128个字符
    private String introduce;
    // 粉丝数量
    private Integer fans;
    // 关注的人的数量
    private Integer followee;
    // 性别,0:男,1:女
    @Max(1)
    @Min(0)
    private Integer gender;
    // 生日
    private LocalDate birthday;
    // 积分
    private Integer credits;
    // 会员级别,0~9级,0代表未开通会员
    @Max(9)
    @Min(10)
    private Integer level;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
}

