package me.critiq.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.critiq.backend.domain.entity.Shop;

import java.util.List;

/**
 * (Shop)表服务接口
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
public interface ShopService extends IService<Shop> {

    Shop queryById(Long id);

    void update(Shop shop);

    void saveShop2Redis(Long id, Long expireSeconds);

    List<Shop> queryShopByType(Integer typeId, Integer current, Double x, Double y);

    List<Shop> queryShopByType(Integer typeId, Integer current);
}

