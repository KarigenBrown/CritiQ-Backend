package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.SeckillVoucherMapper;
import me.critiq.backend.domain.entity.SeckillVoucher;
import me.critiq.backend.service.SeckillVoucherService;
import org.springframework.stereotype.Service;

/**
 * 秒杀优惠券表,与优惠券是一对一关系(SeckillVoucher)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("seckillVoucherService")
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements SeckillVoucherService {

}

