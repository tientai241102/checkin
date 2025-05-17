local setKey = KEYS[1]
local countKey = KEYS[2]
local checkInDate = ARGV[1]

-- Kiểm tra đã điểm danh hôm nay chưa
local isCheckedIn = redis.call('SISMEMBER', setKey, checkInDate)
if isCheckedIn == 1 then
    local count = redis.call('GET', countKey) or '0'
    return {1, tonumber(count)}
end

-- Kiểm tra số lần điểm danh
local count = redis.call('GET', countKey) or '0'
count = tonumber(count)
if count >= 7 then
    return {0, count}
end

-- Thêm ngày điểm danh và tăng count
redis.call('SADD', setKey, checkInDate)
redis.call('INCR', countKey)
return {0, count}