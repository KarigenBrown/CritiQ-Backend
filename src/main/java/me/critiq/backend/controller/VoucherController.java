package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.domain.entity.Voucher;
import me.critiq.backend.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

