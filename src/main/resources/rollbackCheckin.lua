 local countKey = KEYS[1]
 local setKey = KEYS[2]
 local data = ARGV[1]

 if (redis.call('GET', countKey) and tonumber(redis.call('GET', countKey)) > 0) then
        redis.call('DECR', countKey)
    end
    redis.call('SREM', setKey, data)
    return 1