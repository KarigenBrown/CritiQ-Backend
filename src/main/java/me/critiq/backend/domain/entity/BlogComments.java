package me.critiq.backend.domain.entity;

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
 * (BlogComments)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:28:38
 */
@Data
@Accessors(chain = true)
@TableName("blog_comments")
@EqualsAndHashCode(callSuper = false)
public class BlogComments {
    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 用户id
    private Long userId;
    // 探店id
    private Long blogId;
    // 关联的1级评论id,如果是一级评论,则值为0
    private Long parentId;
    // 回复的评论id
    private Long answerId;
    // 回复的内容
    private String content;
    // 点赞数
    private Integer liked;
    // 状态,0:正常,1:被举报,2:禁止查看
    @Max(2)
    @Min(0)
    private Integer status;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
}

