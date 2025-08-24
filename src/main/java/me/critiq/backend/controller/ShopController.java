package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import me.critiq.backend.domain.entity.Shop;
import me.critiq.backend.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * (Shop)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {
    // 服务对象
    private final ShopService shopService;

    @GetMapping("/{id}")
    public ResponseEntity<Shop> getById(@PathVariable("id") Long id) {
        var shop = shopService.queryById(id);
        return ResponseEntity.ok(shop);
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody Shop shop){
        shopService.update(shop);
        return ResponseEntity.ok().build();
    }
}

