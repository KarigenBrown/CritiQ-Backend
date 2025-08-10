package me.critiq.backend.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀优惠券表,与优惠券是一对一关系(SeckillVoucher)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Builder
@TableName("seckill_voucher")
@EqualsAndHashCode(callSuper = false)
public class SeckillVoucher {
    // 关联的优惠券的id
    @TableId(value = "voucher_id", type = IdType.INPUT)
    private Long voucherId;
    // 库存
    private Integer stock;
    // 创建时间
    private LocalDateTime createTime;
    // 生效时间
    private LocalDateTime beginTime;
    // 失效时间
    private LocalDateTime endTime;
    // 更新时间
    private LocalDateTime updateTime;
}

