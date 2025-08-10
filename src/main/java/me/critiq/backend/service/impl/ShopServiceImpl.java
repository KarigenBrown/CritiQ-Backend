package me.critiq.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.critiq.backend.mapper.ShopMapper;
import me.critiq.backend.domain.entity.Shop;
import me.critiq.backend.service.ShopService;
import org.springframework.stereotype.Service;

/**
 * (Shop)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("shopService")
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

}

