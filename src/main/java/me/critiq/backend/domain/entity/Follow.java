package me.critiq.backend.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * (Follow)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Builder
@TableName("follow")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Follow {
    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 用户id
    private Long userId;
    // 关联的用户id
    private Long followUserId;
    // 创建时间
    private Date createTime;
}

