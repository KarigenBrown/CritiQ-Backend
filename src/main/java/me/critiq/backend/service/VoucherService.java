package me.critiq.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.critiq.backend.domain.entity.Voucher;

import java.util.List;

/**
 * (Voucher)表服务接口
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
public interface VoucherService extends IService<Voucher> {

    List<Voucher> queryVoucherOfShop(Long shopId);
}

