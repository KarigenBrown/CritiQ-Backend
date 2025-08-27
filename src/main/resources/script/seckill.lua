-- 1.参数列表
-- 1.1优惠券id
local voucherId = ARGV[1]
-- 1.2用户id
local userId = ARGV[2]

-- XGROUP CREATE stream.orders g1 0 MKSTREAM
-- 1.3订单id
local orderId = ARGV[3]

-- 2.数据key
-- 2.1库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2订单key
local orderKey = 'seckill:order:' .. voucherId

-- 3.脚本业务
-- 3.1判断库存是否充足 GET key
if (tonumber(redis.call('GET', stockKey)) <= 0) then
    -- 3.2库存不足返回1
    return 1
end
-- 3.3判断用户是否下单 SISMEMBER orderKey userId
if (redis.call('SISMEMBER', orderKey, userId) == 1) then
    -- 3.4存在,说明是重复下单,返回2
    return 2
end
-- 3.5扣库存 INCRBY stockKey -1
redis.call('INCRBY', stockKey, -1)
-- 3.6下单(保存用户) SADD orderKey userId
redis.call('SADD', orderKey, userId)
-- 3.7发送消息到队列中,XADD stream.orders * k1 v1 k2 v2
redis.call('XADD', 'stream.orders', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0