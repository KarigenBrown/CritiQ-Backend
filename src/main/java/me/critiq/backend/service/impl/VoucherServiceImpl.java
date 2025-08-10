package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.VoucherMapper;
import me.critiq.backend.domain.entity.Voucher;
import me.critiq.backend.service.VoucherService;
import org.springframework.stereotype.Service;

/**
 * (Voucher)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("voucherService")
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {

}

