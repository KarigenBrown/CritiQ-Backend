package me.critiq.backend.mq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import me.critiq.backend.domain.entity.VoucherOrder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherOrderProducer {
    private final StreamBridge streamBridge;

    public void produce(VoucherOrder voucherOrder) {
        log.info("发送至mq");
        streamBridge.send(SystemConstant.QUEUE_NAME, voucherOrder);
    }
}
