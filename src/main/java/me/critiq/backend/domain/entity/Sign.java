package me.critiq.backend.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * (Sign)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Builder
@TableName("sign")
@EqualsAndHashCode(callSuper = false)
public class Sign {
    //主键
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    //用户id
    private Long userId;
    //签到的年
    private Integer year;
    //签到的月
    private Integer month;
    //签到的日期
    private LocalDateTime date;
    //是否补签
    private Boolean isBackup;
}

