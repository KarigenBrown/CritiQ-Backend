package me.critiq.backend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.mapper.ShopTypeMapper;
import me.critiq.backend.domain.entity.ShopType;
import me.critiq.backend.service.ShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * (ShopType)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Slf4j
@Service("shopTypeService")
@RequiredArgsConstructor
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements ShopTypeService {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> getList() {
        var shopTypeJson = stringRedisTemplate.opsForValue().get(SystemConstant.CACHE_SHOP_TYPE_KEY);
        if (StringUtils.hasText(shopTypeJson)) {
            log.info("redis查询shop type");
            return JSONUtil.toList(shopTypeJson, ShopType.class);
        }

        log.info("db查询shop type");
        var shopTypeList = this.list();

        stringRedisTemplate.opsForValue().set(SystemConstant.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopTypeList));

        return shopTypeList;
    }
}

