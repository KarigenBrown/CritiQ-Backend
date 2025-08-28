package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.Voucher;
import me.critiq.backend.mapper.SeckillVoucherMapper;
import me.critiq.backend.domain.entity.SeckillVoucher;
import me.critiq.backend.mapper.VoucherMapper;
import me.critiq.backend.service.SeckillVoucherService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 秒杀优惠券表,与优惠券是一对一关系(SeckillVoucher)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("seckillVoucherService")
@RequiredArgsConstructor
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements SeckillVoucherService {
    private final VoucherMapper voucherMapper;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void addSeckillVoucher(Voucher voucher) {
        voucherMapper.insert(voucher);
        var seckillVoucher = SeckillVoucher.builder()
                .voucherId(voucher.getId())
                .stock(voucher.getStock())
                .beginTime(voucher.getBeginTime())
                .endTime(voucher.getEndTime())
                .build();
        this.save(seckillVoucher);
        // 保存秒杀库存到redis中
        stringRedisTemplate.opsForValue().set(SystemConstant.SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }

    @Override
    public void saveVouchers2Redis() {
        var vouchers = this.list();
        vouchers.forEach(voucher -> stringRedisTemplate.opsForValue().set(SystemConstant.SECKILL_STOCK_KEY + voucher.getVoucherId(), voucher.getStock().toString()));
    }
}

