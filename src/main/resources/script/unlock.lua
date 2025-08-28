-- 锁的key
local key = KEYS[1]
-- 当前线程的线程标识
local threadId = ARGV[1]

-- 获取锁中的线程标识 GET key
local id = redis.call('GET', key)
-- 比较线程标识与锁中的标识是否一致
if (threadId == id) then
    -- 释放锁 DEL key
    return redis.call('DEL', key)
end
return 0