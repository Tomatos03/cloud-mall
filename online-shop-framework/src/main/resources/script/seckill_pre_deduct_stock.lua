if redis.call('EXISTS', KEYS[2]) == 1 then
    return -2
end

local stock = redis.call('GET', KEYS[1])
if not stock then
    return -3
end

local remain = tonumber(stock)
local need = tonumber(ARGV[1])
if remain < need then
    return -1
end

local left = redis.call('DECRBY', KEYS[1], need)
local ttl = tonumber(ARGV[2])
if ttl and ttl > 0 then
    redis.call('SET', KEYS[2], '1', 'EX', ttl)
else
    redis.call('SET', KEYS[2], '1')
end

return left
