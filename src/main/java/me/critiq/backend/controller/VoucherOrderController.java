package me.critiq.backend.controller;


import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.service.VoucherOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * (VoucherOrder)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@RestController
@RequestMapping("/voucher-order")
@RequiredArgsConstructor
public class VoucherOrderController {
    // 服务对象
    private final VoucherOrderService voucherOrderService;

    @RateLimiter(name = "seckill")
    @PostMapping("/seckill/{id}")
    public ResponseEntity<Long> seckillVoucher(@PathVariable("id") Long id) {
        var orderId = voucherOrderService.seckillVoucher(id);
        return ResponseEntity.ok(orderId);
    }
}

