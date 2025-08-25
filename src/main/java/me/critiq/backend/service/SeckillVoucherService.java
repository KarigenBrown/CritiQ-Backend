package me.critiq.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.critiq.backend.domain.entity.SeckillVoucher;
import me.critiq.backend.domain.entity.Voucher;

/**
 * 秒杀优惠券表,与优惠券是一对一关系(SeckillVoucher)表服务接口
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
public interface SeckillVoucherService extends IService<SeckillVoucher> {

    void addSeckillVoucher(Voucher voucher);
}

