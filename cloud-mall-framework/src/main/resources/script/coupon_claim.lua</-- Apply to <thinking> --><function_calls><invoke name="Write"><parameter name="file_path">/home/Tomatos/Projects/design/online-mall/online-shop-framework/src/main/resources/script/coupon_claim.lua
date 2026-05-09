-- KEYS[1] = coupon:stock:{templateId} (库存计数器)
-- KEYS[2] = coupon:user:claim:{userId}:{templateId} (用户领取计数)
-- ARGV[1] = perUserLimit (每人限领数)

local userCount = tonumber(redis.call('GET', KEYS[2]) or '0')
local limit = tonumber(ARGV[1])
if userCount >= limit then
    return -2
end

local stock = redis.call('GET', KEYS[1])
if not stock then
    return -1
end

local remain = tonumber(stock)
if remain <= 0 then
    return -1
end

redis.call('DECRBY', KEYS[1], 1)
redis.call('INCR', KEYS[2])

return remain - 1
