package me.critiq.backend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.Blog;
import me.critiq.backend.domain.entity.Shop;
import me.critiq.backend.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    public ResponseEntity<Void> update(@RequestBody Shop shop) {
        shopService.update(shop);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/of/type")
    public ResponseEntity<List<Shop>> queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y
    ) {
        List<Shop> shops;
        // 判断是否需要根据坐标来查询
        if (x != null && y != null) {
            shops = shopService.queryShopByType(typeId, current, x, y);
        } else {
            shops = shopService.queryShopByType(typeId, current);
        }
        return ResponseEntity.ok(shops);
    }

    @PostMapping
    public ResponseEntity<Long> saveShop(@RequestBody Shop shop) {
        // 写入数据库
        shopService.save(shop);
        // 返回店铺id
        return ResponseEntity.ok(shop.getId());
    }

    @GetMapping("/of/name")
    public ResponseEntity<List<Shop>> queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        var shops = shopService.lambdaQuery()
                .like(StringUtils.hasText(name), Shop::getName, name)
                .page(Page.of(current, SystemConstant.MAX_PAGE_SIZE))
                .getRecords();
        // 返回数据
        return ResponseEntity.ok(shops);
    }
}

