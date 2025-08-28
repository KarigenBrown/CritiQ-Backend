package me.critiq.backend.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.bo.RedisBo;
import me.critiq.backend.enums.ResponseStatusEnum;
import me.critiq.backend.exception.SystemException;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public void set(String key, Object value, Duration duration) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), duration);
    }

    public void setWithLogicExpire(String key, Object value, Duration duration) {
        // 设置逻辑过期
        RedisBo redisBo = RedisBo.builder()
                .data(value)
                .expiration(Instant.now().plus(duration))
                .build();
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisBo));
    }

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Duration duration) {
        var cacheKey = keyPrefix + id;
        // 1.从redis查询缓存
        var json = stringRedisTemplate.opsForValue().get(cacheKey);

        // 2.判断是否存在
        if (StringUtils.hasText(json)) {
            // 3.存在,直接返回
            return JSONUtil.toBean(json, type);
        }

        // 判断命中是否是空字符串,应对缓存穿透
        if (json != null) {
            // 之前查询过数据库,没有该数据,已经被记录到redis,返回一个错误信息
            throw new SystemException(ResponseStatusEnum.SHOP_NOT_FOUND);
        }

        // 4.不存在,根据id查询数据库
        var data = dbFallback.apply(id);

        // 5.不存在,返回错误
        if (data == null) {
            // 将空字符串写入redis,应对缓存穿透
            stringRedisTemplate.opsForValue().set(cacheKey, SystemConstant.CACHE_NULL_VALUE, Duration.ofMinutes(SystemConstant.CACHE_NULL_TTL));
            // 第一次查询数据库,没有该数据,返回错误信息
            throw new SystemException(ResponseStatusEnum.NOT_FOUND);
        }

        // 6.存在,写入redis
        this.set(cacheKey, data, duration);

        // 7.返回
        return data;
    }

    public <R, ID> R queryWithLogicExpire(String cacheKeyPrefix, String LockKeyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Duration duration) {
        var cacheKey = cacheKeyPrefix + id;
        // 1.从redis查询缓存
        var json = stringRedisTemplate.opsForValue().get(cacheKey);

        // 2.判断是否存在
        // 因为逻辑过期理论上app开始运行就会把所有东西加载进来,所以如果没有说明数据库也没有
        if (!StringUtils.hasText(json)) {
            // 3.不存在,直接返回
            throw new SystemException(ResponseStatusEnum.NOT_FOUND);
        }

        // 4.命中,需要先把json反序列化成对象
        RedisBo redisBo = JSONUtil.toBean(json, RedisBo.class);
        var data = JSONUtil.toBean((JSONObject) redisBo.getData(), type);
        var expiration = redisBo.getExpiration();

        // 5.判断是否过期
        if (expiration.isAfter(Instant.now())) {
            // 5.1未过期,直接返回信息
            return data;
        }

        // 5.2已过期,需要缓存重建
        // 6.缓存重建
        // 6.1获取互斥锁
        var lockKey = LockKeyPrefix + id;
        var lock = redissonClient.getLock(lockKey);
        /*
        todo redission提供的锁都是可重入锁,凭依是线程ID
        本身的请求线程在将任务提交到线程池之后结束,可能在锁没被释放的时候,这个线程被二次调用导致两次进入db读取逻辑
        并且因为实在线程池内的线程释放锁,不是同一个线程导致锁不会真正被释放
         */
        boolean isLock = lock.tryLock();

        // 6.2判断是否获取锁成功
        // if (isLock) {
        if (isLock && lock.getHoldCount() == 1) {
            log.info("获取{}", Thread.currentThread().getName());
            log.info("{}", lock.getHoldCount());
            // 6.3成功,开启独立线程,实现缓存重建
            Runnable task = () -> {
                try {
                    // 查询数据库
                    log.info("db查询{}", Thread.currentThread().getName());
                    Thread.sleep(500);
                    var result = dbFallback.apply(id);
                    // 写入redis
                    this.setWithLogicExpire(cacheKey, result, duration);
                } catch (Exception ex) {
                    throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
                } finally {
                    // 释放锁
                    // 大坑,线程池吞异常,unlock抛异常导致后面不输出
                    // lock.unlock();
                    lock.forceUnlock();
                    log.info("释放锁{}", Thread.currentThread().getName());
                    log.info("{}", lock.getHoldCount());
                }
            };
            CACHE_REBUILD_EXECUTOR.submit(task);
            // new Thread(task).start();
        }

        // 6.4返回过期的信息
        return data;
    }

    // 使用 setnx 命令生成锁,不可重入
    private Boolean tryLock(String key) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(10));
    }

    private Boolean unLock(String key) {
        return stringRedisTemplate.delete(key);
    }
}
