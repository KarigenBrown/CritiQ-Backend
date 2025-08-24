package me.critiq.backend.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * (Voucher)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Builder
@TableName("voucher")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Voucher {
    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 商铺id
    private Long shopId;
    // 代金券标题
    private String title;
    // 副标题
    private String subTitle;
    // 使用规则
    private String rules;
    // 支付金额,单位是分.例如200代表2元
    private Long payValue;
    // 抵扣金额,单位是分.例如200代表2元
    private Long actualValue;
    // 0:普通券,1:秒杀券
    @Max(1)
    @Min(0)
    private Integer type;
    @Max(3)
    @Min(1)
    // 1:上架,2:下架,3:过期
    private Integer status;
    // 库存
    @TableField(exist = false)
    private Integer stock;
    // 生效时间
    @TableField(exist = false)
    private LocalDateTime beginTime;
    // 失效时间
    @TableField(exist = false)
    private LocalDateTime endTime;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
}

