package me.critiq.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.critiq.backend.domain.entity.ShopType;

import java.util.List;

/**
 * (ShopType)表服务接口
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
public interface ShopTypeService extends IService<ShopType> {
    List<ShopType> getList();
}

