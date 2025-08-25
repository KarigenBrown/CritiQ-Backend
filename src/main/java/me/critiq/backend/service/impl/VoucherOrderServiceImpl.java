package me.critiq.backend.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.SeckillVoucher;
import me.critiq.backend.enums.ResponseStatusEnum;
import me.critiq.backend.exception.SystemException;
import me.critiq.backend.mapper.SeckillVoucherMapper;
import me.critiq.backend.mapper.VoucherOrderMapper;
import me.critiq.backend.domain.entity.VoucherOrder;
import me.critiq.backend.service.VoucherOrderService;
import me.critiq.backend.util.SecurityUtil;
import me.critiq.backend.util.SimpleRedisLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * (VoucherOrder)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Service("voucherOrderService")
@RequiredArgsConstructor
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {
    private final SeckillVoucherMapper seckillVoucherMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Override
    public Long seckillVoucher(Long voucherId) {
        // 1.查询优惠券
        var seckillVoucher = seckillVoucherMapper.selectById(voucherId);
        // 2.判断秒杀是否开始
        var now = LocalDateTime.now();
        if (seckillVoucher.getBeginTime().isAfter(now)) {
            throw new SystemException(ResponseStatusEnum.SECKILL_NOT_STARTED);
        }
        // 3.判断秒杀是否已经结束
        if (seckillVoucher.getEndTime().isBefore(now)) {
            throw new SystemException(ResponseStatusEnum.SECKILL_ENDED);
        }
        // 4.判断库存是否充足
        if (seckillVoucher.getStock() < 1) {
            throw new SystemException(ResponseStatusEnum.STOCK_NOT_ENOUGH);
        }

        // 5.一人一单
        var userId = SecurityUtil.getUserId();
        // 先获取锁,然后开启事务,等事务提交完之后在释放锁,保证数据库中有该记录,不然有可能在释放锁的时候事务没有成功提交
        // 单进程锁
        // synchronized (userId.toString().intern()) {

        // 创建锁对象(自定义分布式锁)
        // var lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        var lock = redissonClient.getLock(SystemConstant.LOCK_ORDER_KEY + userId);

        // 获取锁
        var isLock = lock.tryLock();
        // 判断是否获取锁成功
        if (!isLock) {
            // 获取锁失败,返回错误或重试
            throw new SystemException(ResponseStatusEnum.REPEAT_PURCHASE);
        }
        try {
            // 8.返回订单id
            // @Transactional需要代理对象才有事务功能
            var proxy = (VoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId, userId);
        } catch (IllegalStateException e) {
            throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
        } finally {
            // 释放锁
            lock.unlock();
        }
        // }
    }

    @Transactional
    public Long createVoucherOrder(Long voucherId, Long userId) {
        // 5.1查询订单
        var count = this.lambdaQuery()
                .eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getVoucherId, voucherId)
                .count();
        // 5.2判断是否存在
        if (count > 0) {
            // 用户已经购买过了
            throw new SystemException(ResponseStatusEnum.REPEAT_PURCHASE);
        }

        // 6.扣减库存
        // 修改的时候数据库会自动给该条记录上锁,所以数据库层面不会出现并发问题,update操作的行级锁
        // 老方法
        /*seckillVoucher.setStock(seckillVoucher.getStock() - 1);
        boolean success = seckillVoucherMapper.updateById(seckillVoucher) == 1;*/

        // 乐观锁CAS实现
        boolean success = seckillVoucherMapper.update(
                Wrappers.<SeckillVoucher>lambdaUpdate()
                        // 这里不是数据库保证原子性,框架只是单纯的设置这个字段的值,所以并不可靠
                        // .set(SeckillVoucher::getStock, seckillVoucher.getStock() - 1)
                        // 大坑,此处-1操作是由数据库保证的,且0固定.所以不会有并发问题
                        .setSql("stock = stock - 1")
                        .eq(SeckillVoucher::getVoucherId, voucherId)
                        .gt(SeckillVoucher::getStock, 0)
        ) > 0;

        if (!success) {
            throw new SystemException(ResponseStatusEnum.STOCK_NOT_ENOUGH);
        }

        // 7.创建订单
        long orderId = IdUtil.getSnowflakeNextId();
        var order = VoucherOrder.builder()
                // 7.1订单id
                .id(orderId)
                // 7.2用户id
                .userId(userId)
                // 7.3代金券id
                .voucherId(voucherId)
                .build();
        this.save(order);
        return orderId;
    }
}

