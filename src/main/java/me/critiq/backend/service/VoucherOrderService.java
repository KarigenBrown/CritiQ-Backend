package me.critiq.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.critiq.backend.domain.entity.VoucherOrder;

/**
 * (VoucherOrder)表服务接口
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
public interface VoucherOrderService extends IService<VoucherOrder> {

    Long seckillVoucher(Long voucherId);

    Long createVoucherOrder(Long voucherId, Long userId);
}

