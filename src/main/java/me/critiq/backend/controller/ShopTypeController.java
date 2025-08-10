package me.critiq.backend.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.domain.entity.ShopType;
import me.critiq.backend.service.ShopTypeService;
import me.critiq.backend.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * (ShopType)表控制层
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:20:10
 */
@Slf4j
@RestController
@RequestMapping("/shopType")
@RequiredArgsConstructor
public class ShopTypeController {
    // 服务对象
    private final ShopTypeService shopTypeService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('level_0')")
    public ResponseEntity<List<ShopType>> getShopTypeList() {
        var shopTypeList = shopTypeService.lambdaQuery()
                .orderByAsc(ShopType::getSort)
                .list();
        return ResponseEntity.ok(shopTypeList);
    }
}

