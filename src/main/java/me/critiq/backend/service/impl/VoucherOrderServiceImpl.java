package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.VoucherOrderMapper;
import me.critiq.backend.domain.entity.VoucherOrder;
import me.critiq.backend.service.VoucherOrderService;
import org.springframework.stereotype.Service;

/**
 * (VoucherOrder)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("voucherOrderService")
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

}

