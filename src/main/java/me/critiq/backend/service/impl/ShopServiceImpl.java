package me.critiq.backend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.bo.RedisBo;
import me.critiq.backend.enums.ResponseStatusEnum;
import me.critiq.backend.exception.SystemException;
import me.critiq.backend.mapper.ShopMapper;
import me.critiq.backend.domain.entity.Shop;
import me.critiq.backend.service.ShopService;
import me.critiq.backend.util.CacheClient;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * (Shop)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Slf4j
@Service("shopService")
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final CacheClient cacheClient;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Override
    public Shop queryById(Long id) {
        // 缓存穿透
        // var shop = this.queryWithPassThrough(id);
        /*var shop = cacheClient.queryWithPassThrough(
                SystemConstant.CACHE_SHOP_KEY,
                id,
                Shop.class,
                this::getById,
                Duration.ofMinutes(SystemConstant.CACHE_SHOP_TTL)
        );*/

        // 互斥锁解决缓存击穿
        // var shop = this.queryWithMutex(id);

        // 逻辑过期解决缓存击穿
        // var shop = this.queryWithLogicExpire(id);
        var shop = cacheClient.queryWithLogicExpire(
                SystemConstant.CACHE_SHOP_KEY,
                SystemConstant.LOCK_SHOP_KEY,
                id,
                Shop.class,
                this::getById,
                Duration.ofMinutes(SystemConstant.CACHE_SHOP_TTL)
        );

        // 7.返回
        return shop;
    }

    public Shop queryWithLogicExpire(Long id) {
        var cacheKey = SystemConstant.CACHE_SHOP_KEY + id;
        // 1.从redis查询店铺缓存
        var shopJson = stringRedisTemplate.opsForValue().get(cacheKey);

        // 2.判断是否存在
        // 因为逻辑过期理论上app开始运行就会把所有东西加载进来,所以如果没有说明数据库也没有
        if (!StringUtils.hasText(shopJson)) {
            // 3.不存在,直接返回
            throw new SystemException(ResponseStatusEnum.SHOP_NOT_FOUND);
        }

        // 4.命中,需要先把json反序列化成对象
        RedisBo redisBo = JSONUtil.toBean(shopJson, RedisBo.class);
        var shop = JSONUtil.toBean((JSONObject) redisBo.getData(), Shop.class);
        log.info("shop: {}", shop);
        var expiration = redisBo.getExpiration();
        log.info("expiration: {}", expiration);

        // 5.判断是否过期
        if (expiration.isAfter(Instant.now())) {
            // 5.1未过期,直接返回店铺信息
            return shop;
        }

        // 5.2已过期,需要缓存重建
        // 6.缓存重建
        // 6.1获取互斥锁
        var lockKey = SystemConstant.LOCK_SHOP_KEY + id;
        var lock = redissonClient.getLock(lockKey);
        boolean isLock = lock.tryLock();

        // 6.2判断是否获取锁成功
        if (isLock && lock.getHoldCount() == 1) {
            // 6.3成功,开启独立线程,实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 重建缓存
                    this.saveShop2Redis(id, 20L);
                } catch (Exception ex) {
                    throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
                } finally {
                    // 释放锁
                    log.info("锁释放{}", Thread.currentThread().getName());
                    lock.forceUnlock();
                }
            });
        }

        // 6.4返回过期的店铺信息
        return shop;
    }

    public Shop queryWithMutex(Long id) {
        var cacheKey = SystemConstant.CACHE_SHOP_KEY + id;
        // 1.从redis查询店铺缓存
        var shopJson = stringRedisTemplate.opsForValue().get(cacheKey);

        // 2.判断是否存在
        if (StringUtils.hasText(shopJson)) {
            // log.info("redis查询shop{}", Thread.currentThread().getName());
            // 3.存在,直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // 判断命中是否是空字符串,应对缓存穿透
        if (shopJson != null) {
            // 之前查询过数据库,没有该数据,已经被记录到redis,返回一个错误信息
            throw new SystemException(ResponseStatusEnum.SHOP_NOT_FOUND);
        }

        // shopJson等于null的情况,对于第一次查询
        // 4.实现缓存重建
        var lockKey = SystemConstant.LOCK_SHOP_KEY + id;
        // 4.1获取互斥锁
        var lock = redissonClient.getLock(lockKey);

        Shop shop = null;
        try {
            // 此处因为判断是否是lock在try块里面所以所以无论是否获取到锁finally都会执行,需要判断是否为当前线程获取到锁
            boolean isLock = lock.tryLock();
            // 4.2判断是否获取成功
            if (!isLock) {
                log.info("获取锁失败{}", Thread.currentThread().getName());
                // 4.3失败,则休眠并重试,失败线程指挥等待成功线程将数据读取到redis,然后失败线程从redis读取,失败线程不会读取db
                // 理论上只有一个成功线程,且因为都是并行的所以不用考虑重入问题
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            // 4.4成功,根据id查询数据库
            log.info("获取锁成功,db查询shop{}", Thread.currentThread().getName());
            shop = this.getById(id);
            Thread.sleep(200);

            // 5.不存在,返回错误
            if (shop == null) {
                // 将空字符串写入redis,应对缓存穿透
                // 因为不管数据库中有没有数据,最后redis中都会有这个键,同样对应缓存击穿
                stringRedisTemplate.opsForValue().set(cacheKey, SystemConstant.CACHE_NULL_VALUE, Duration.ofMinutes(SystemConstant.CACHE_NULL_TTL));
                // 第一次查询数据库,没有该数据,返回错误信息
                throw new SystemException(ResponseStatusEnum.SHOP_NOT_FOUND);
            }

            // 6.存在,写入redis
            stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(shop), Duration.ofMinutes(SystemConstant.CACHE_SHOP_TTL));
        } catch (InterruptedException e) {
            throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                // 7.释放互斥锁
                lock.unlock();
            }
        }

        // 8.返回
        return shop;
    }

    public Shop queryWithPassThrough(Long id) {
        var cacheKey = SystemConstant.CACHE_SHOP_KEY + id;
        // 1.从redis查询店铺缓存
        var shopJson = stringRedisTemplate.opsForValue().get(cacheKey);

        // 2.判断是否存在
        if (StringUtils.hasText(shopJson)) {
            log.info("redis查询shop");
            // 3.存在,直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // 判断命中是否是空字符串,应对缓存穿透
        if (shopJson != null) {
            // 之前查询过数据库,没有该数据,已经被记录到redis,返回一个错误信息
            throw new SystemException(ResponseStatusEnum.SHOP_NOT_FOUND);
        }

        log.info("db查询shop");
        // 4.不存在,根据id查询数据库
        var shop = this.getById(id);

        // 5.不存在,返回错误
        if (shop == null) {
            // 将空字符串写入redis,应对缓存穿透
            stringRedisTemplate.opsForValue().set(cacheKey, SystemConstant.CACHE_NULL_VALUE, Duration.ofMinutes(SystemConstant.CACHE_NULL_TTL));
            // 第一次查询数据库,没有该数据,返回错误信息
            throw new SystemException(ResponseStatusEnum.SHOP_NOT_FOUND);
        }

        // 6.存在,写入redis
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(shop), Duration.ofMinutes(SystemConstant.CACHE_SHOP_TTL));

        // 7.返回
        return shop;
    }

    @Override
    @Transactional
    public void update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            throw new SystemException(ResponseStatusEnum.ID_NOT_NULL);
        }
        this.updateById(shop);
        stringRedisTemplate.delete(SystemConstant.CACHE_SHOP_KEY + id);
    }

    @Override
    public void saveShop2Redis(Long id, Long expireSeconds) {
        try {
            // 1.查询店铺数据
            var shop = this.getById(id);

            Thread.sleep(200);

            // 2.封装逻辑过期时间
            var redisBo = RedisBo.builder()
                    .data(shop)
                    .expiration(Instant.now().plusSeconds(expireSeconds))
                    .build();

            // 3.写入Redis
            stringRedisTemplate.opsForValue().set(SystemConstant.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisBo));

        } catch (InterruptedException e) {
            throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
        }
    }
}

