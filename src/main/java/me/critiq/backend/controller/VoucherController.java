package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.domain.entity.Voucher;
import me.critiq.backend.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * (Voucher)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@RestController
@RequestMapping("/voucher")
@RequiredArgsConstructor
public class VoucherController {
    // 服务对象
    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<Long> addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return ResponseEntity.ok(voucher.getId());
    }

    @GetMapping("/list/{shopId}")
    public ResponseEntity<List<Voucher>> queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        var vouchers = voucherService.queryVoucherOfShop(shopId);
        return ResponseEntity.ok(vouchers);
    }
}

