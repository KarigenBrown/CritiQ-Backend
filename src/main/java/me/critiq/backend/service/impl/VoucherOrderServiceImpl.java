package me.critiq.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.SeckillVoucher;
import me.critiq.backend.domain.entity.Voucher;
import me.critiq.backend.enums.ResponseStatusEnum;
import me.critiq.backend.exception.SystemException;
import me.critiq.backend.mapper.SeckillVoucherMapper;
import me.critiq.backend.mapper.VoucherOrderMapper;
import me.critiq.backend.domain.entity.VoucherOrder;
import me.critiq.backend.mq.producer.VoucherOrderProducer;
import me.critiq.backend.service.VoucherOrderService;
import me.critiq.backend.util.SecurityUtil;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * (VoucherOrder)表服务实现类
 *
 * @author Karigen Brown
 * @since 2025-08-10 16:15:27
 */
@Slf4j
@Service("voucherOrderService")
@RequiredArgsConstructor
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {
    private final SeckillVoucherMapper seckillVoucherMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final VoucherOrderProducer voucherOrderProducer;

    // private final BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);

    private final static DefaultRedisScript<Long> SECKILL_SCRIPT;
    private final static ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private VoucherOrderService proxy;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("script/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @PostConstruct
    public void init() {
        // SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderTask());
    }

    /*private class VoucherOrderTask implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // 1.获取队列中的订单信息
                    var voucherOrder = orderTasks.take();
                    // 2.创建订单
                    handleVoucherOrder(voucherOrder);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
                }
            }
        }
    }*/

    private class VoucherOrderTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.orders >
                    var orders = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(SystemConstant.STREAM_NAME, ReadOffset.lastConsumed())
                    );
                    // 2.判断消息获取是否成功
                    if (CollectionUtils.isEmpty(orders)) {
                        // 如果获取失败,说明没有消息,继续下一次执行
                        continue;
                    }
                    // 解析消息中的订单信息
                    var record = orders.get(0);
                    var values = record.getValue();
                    var voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    // 3.如果执行成功,可以下单
                    handleVoucherOrder(voucherOrder);
                    // 4.ACK确认 SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(
                            SystemConstant.STREAM_NAME,
                            "g1",
                            record.getId()
                    );
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    // 1.获取pending list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 STREAMS stream.orders 0
                    var orders = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(SystemConstant.STREAM_NAME, ReadOffset.from("0"))
                    );
                    // 2.判断消息获取是否成功
                    if (CollectionUtils.isEmpty(orders)) {
                        // 如果获取失败,说明pending list没有消息,结束循环
                        break;
                    }
                    // 解析消息中的订单信息
                    var record = orders.get(0);
                    var values = record.getValue();
                    var voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    // 3.如果执行成功,可以下单
                    handleVoucherOrder(voucherOrder);
                    // 4.ACK确认 SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(
                            SystemConstant.STREAM_NAME,
                            "g1",
                            record.getId()
                    );
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            // 1.获取用户
            var userId = voucherOrder.getUserId();
            // 2. 创建锁对象
            var lock = redissonClient.getLock(SystemConstant.LOCK_ORDER_KEY + userId);
            // 3.获取锁
            var isLock = lock.tryLock();
            // 4.判断是否获取锁成功
            if (!isLock) {
                // 获取锁失败,返回错误或重试
                log.error(ResponseStatusEnum.REPEAT_PURCHASE.getMessage());
                throw new SystemException(ResponseStatusEnum.REPEAT_PURCHASE);
            }
            try {
                // 8.返回订单id
                proxy.createVoucherOrder(voucherOrder);
            } catch (IllegalStateException e) {
                throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
    }

    @Override
    public Long seckillVoucher(Long voucherId) {
        // 获取用户id
        var userId = SecurityUtil.getUserId();
        // 获取订单id
        long orderId = IdUtil.getSnowflakeNextId();
        // 1.执行lua脚本
        var result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString() //, String.valueOf(orderId)
        );

        // 2.判断结果是否为0
        switch (result.intValue()) {
            // 2.1不为0,代表没有购买资格
            case 1 -> throw new SystemException(ResponseStatusEnum.STOCK_NOT_ENOUGH);
            case 2 -> throw new SystemException(ResponseStatusEnum.REPEAT_PURCHASE);
        }
        // 2.2为0,有购买资格,把下单信息保存到阻塞队列
        // 2.创建订单
        var voucherOrder = VoucherOrder.builder()
                // 2.1订单id
                .id(orderId)
                // 2.2用户id
                .userId(userId)
                // 2.3代金券id
                .voucherId(voucherId)
                .build();
        // @Transactional需要代理对象才有事务功能
        // proxy = (VoucherOrderService) AopContext.currentProxy();

        // 2.4放入阻塞队列
        // orderTasks.add(voucherOrder);
        voucherOrderProducer.produce(voucherOrder);

        // 3返回订单id
        return orderId;
    }

    /*@Override
    public Long seckillVoucher(Long voucherId) {
        // 获取用户id
        var userId = SecurityUtil.getUserId();
        // 1.执行lua脚本
        var result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );

        // 2.判断结果是否为0
        switch (result.intValue()) {
            // 2.1不为0,代表没有购买资格
            case 1 -> throw new SystemException(ResponseStatusEnum.STOCK_NOT_ENOUGH);
            case 2 -> throw new SystemException(ResponseStatusEnum.REPEAT_PURCHASE);
        }
        // 2.2为0,有购买资格,把下单信息保存到阻塞队列
        // 2.创建订单
        long orderId = IdUtil.getSnowflakeNextId();
        var voucherOrder = VoucherOrder.builder()
                // 2.1订单id
                .id(orderId)
                // 2.2用户id
                .userId(userId)
                // 2.3代金券id
                .voucherId(voucherId)
                .build();
        // @Transactional需要代理对象才有事务功能
        proxy = (VoucherOrderService) AopContext.currentProxy();

        // 2.4放入阻塞队列
        orderTasks.add(voucherOrder);

        // 3返回订单id
        return orderId;
    }*/

    /*@Override
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
    }*/

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
        var voucherOrder = VoucherOrder.builder()
                // 7.1订单id
                .id(orderId)
                // 7.2用户id
                .userId(userId)
                // 7.3代金券id
                .voucherId(voucherId)
                .build();
        this.save(voucherOrder);
        return orderId;
    }

    @Override
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 5.1查询订单
        var count = this.lambdaQuery()
                .eq(VoucherOrder::getUserId, voucherOrder.getUserId())
                .eq(VoucherOrder::getVoucherId, voucherOrder.getVoucherId())
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
                        .eq(SeckillVoucher::getVoucherId, voucherOrder.getVoucherId())
                        .gt(SeckillVoucher::getStock, 0)
        ) > 0;

        if (!success) {
            throw new SystemException(ResponseStatusEnum.STOCK_NOT_ENOUGH);
        }

        this.save(voucherOrder);
    }


}

