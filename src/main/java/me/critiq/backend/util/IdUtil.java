package me.critiq.backend.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdUtil {
    private final StringRedisTemplate stringRedisTemplate;

    private static final Long BEGIN_TIMESTAMP = 1738713600L;
    private static final Integer SERIAL_BITS = 32;

    public Long nextId(String keyPrefix) {
        // 1.生成时间戳
        var now = LocalDateTime.now();
        var nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        var timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.生成序列号
        // 2.1获取当前日期,精确到天
        var date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2自增长
        var count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        // 3.拼接返回
        // 符号位正值默认为0,不要考虑
        // 实际用库里的雪花算法
        return (timestamp << SERIAL_BITS) | count;
    }

    /*public static void main(String[] args) {
        var time = LocalDateTime.of(2025, 2, 5, 0, 0);
        var second = time.toEpochSecond(ZoneOffset.UTC);
        log.info(String.valueOf(second));
    }*/
}
