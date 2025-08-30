package me.critiq.backend.util.lock;

import cn.hutool.core.lang.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;

@RequiredArgsConstructor
public class SimpleRedisLock implements AppLock {
    private final String lockName;
    private final StringRedisTemplate stringRedisTemplate;

    private final static String LOCK_KEY_PREFIX = "lock:";
    private final static String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private final static DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("script/unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Boolean tryLock(Long seconds) {
        // 获取锁
        return stringRedisTemplate.opsForValue().setIfAbsent(
                LOCK_KEY_PREFIX + lockName,
                // 获取线程标识
                ID_PREFIX + Thread.currentThread().threadId(),
                Duration.ofSeconds(seconds)
        );
    }

    @Override
    public void unlock() {
        // 调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(LOCK_KEY_PREFIX + lockName),
                ID_PREFIX + Thread.currentThread().threadId()
        );
    }

    /*@Override
    public void unlock() {
        // 获取线程标识
        var threadId = ID_PREFIX + Thread.currentThread().threadId();
        // 获取锁中的标识
        var id = stringRedisTemplate.opsForValue().get(LOCK_KEY_PREFIX + lockName);
        if (threadId.equals(id)) {
            // 释放锁
            stringRedisTemplate.delete(LOCK_KEY_PREFIX + lockName);
        }
    }*/
}
