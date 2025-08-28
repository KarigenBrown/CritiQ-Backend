package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.domain.entity.Voucher;
import me.critiq.backend.service.SeckillVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀优惠券表,与优惠券是一对一关系(SeckillVoucher)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@RestController
@RequestMapping("/seckill-voucher")
@RequiredArgsConstructor
public class SeckillVoucherController {
    // 服务对象
    private final SeckillVoucherService seckillVoucherService;

    @PostMapping
    public ResponseEntity<Long> addVoucher(@RequestBody Voucher voucher) {
        seckillVoucherService.addSeckillVoucher(voucher);
        return ResponseEntity.ok(voucher.getId());
    }
}

