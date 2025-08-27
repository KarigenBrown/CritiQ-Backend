package me.critiq.backend.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.VoucherOrder;
import me.critiq.backend.enums.ResponseStatusEnum;
import me.critiq.backend.exception.SystemException;
import me.critiq.backend.service.VoucherOrderService;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherOrderConsumer {
    private final RedissonClient redissonClient;
    private final VoucherOrderService voucherOrderService;

    @Bean
    public Consumer<VoucherOrder> voucherOrder() {
        return voucherOrder -> {
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
                voucherOrderService.createVoucherOrder(voucherOrder);
            } catch (IllegalStateException e) {
                throw new SystemException(ResponseStatusEnum.SYSTEM_ERROR);
            } finally {
                // 释放锁
                lock.unlock();
            }
        };
    }
}
