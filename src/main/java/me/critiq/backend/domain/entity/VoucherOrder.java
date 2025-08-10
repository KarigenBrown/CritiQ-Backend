package me.critiq.backend.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * (VoucherOrder)表实体类
 *
 * @author Karigen Brown
 * @since 2025-08-10 15:31:22
 */
@Data
@Builder
@TableName("voucher_order")
@EqualsAndHashCode(callSuper = false)
public class VoucherOrder {
    // 主键
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    // 下单的用户id
    private Long userId;
    // 购买的代金券id
    private Long voucherId;
    // 支付方式,1:余额支付,2:支付宝,3:微信
    @Max(3)
    @Min(1)
    private Integer payType;
    // 订单状态,1:未支付,2:已支付,3:已核销,4:已取消,5:退款中,6:已退款
    @Max(6)
    @Min(1)
    private Integer status;
    // 下单时间
    private LocalDateTime createTime;
    // 支付时间
    private LocalDateTime payTime;
    // 核销时间
    private LocalDateTime useTime;
    // 退款时间
    private LocalDateTime refundTime;
    // 更新时间
    private LocalDateTime updateTime;
}

