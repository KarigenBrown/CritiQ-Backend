package me.critiq.backend.runner;

import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.Shop;
import me.critiq.backend.service.SeckillVoucherService;
import me.critiq.backend.service.ShopService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisRunner implements CommandLineRunner {
    private final ShopService shopService;
    private final SeckillVoucherService seckillVoucherService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) throws Exception {
        // shopService.saveShop2Redis(1L, 10L);
        saveVouchers2Redis();
        loadShopData();
    }

    private void saveVouchers2Redis() {
        var vouchers = seckillVoucherService.list();
        vouchers.forEach(voucher -> stringRedisTemplate.opsForValue().set(SystemConstant.SECKILL_STOCK_KEY + voucher.getVoucherId(), voucher.getStock().toString()));
    }

    private void loadShopData() {
        // 1.查询店铺信息
        var list = shopService.list();
        // 2.把店铺分组,按照typeId分组,id一致的放到一个集合
        var map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 3.分批完成写入redis
        for (var entry : map.entrySet()) {
            // 3.1获取类型id
            var typeId = entry.getKey();
            var key = SystemConstant.SHOP_GEO_KEY + typeId;
            // 3.2获取同类型的店铺的集合
            var shops = entry.getValue();
            var locations = shops.stream().map(
                    shop -> new RedisGeoCommands.GeoLocation<>(
                            shop.getId().toString(),
                            new Point(shop.getX(), shop.getY())
                    )
            ).toList();

            // 3.3写入redis GEOADD key 经度 维度 member
            /*for (var shop : shops) {
                stringRedisTemplate.opsForGeo().add(
                        key,
                        new Point(shop.getX(), shop.getY()),
                        shop.getId().toString()
                );
            }*/
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }
}
