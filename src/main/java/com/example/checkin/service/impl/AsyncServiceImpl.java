package com.example.checkin.service.impl;

import com.example.checkin.exception.AppException;
import com.example.checkin.model.constant.ErrorCode;
import com.example.checkin.model.dto.UserDTO;
import com.example.checkin.model.entity.CheckIn;
import com.example.checkin.model.entity.PointHistory;
import com.example.checkin.model.entity.User;
import com.example.checkin.model.mapper.PointHistoryMapper;
import com.example.checkin.model.mapper.UserMapper;
import com.example.checkin.repository.CheckInRepository;
import com.example.checkin.repository.PointHistoryRepository;
import com.example.checkin.repository.UserRepository;
import com.example.checkin.service.AsyncService;
import com.example.checkin.utils.RedisKeyUtils;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service

public class AsyncServiceImpl implements AsyncService {
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final RedissonClient redissonClient;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, UserDTO> redisTemplateUser;
    private final DefaultRedisScript<Long> rollbackCheckinScript;

    public AsyncServiceImpl(UserRepository userRepository, CheckInRepository checkInRepository, PointHistoryRepository pointHistoryRepository, RedissonClient redissonClient, UserMapper userMapper, RedisTemplate<String, String> redisTemplate, RedisTemplate<String, UserDTO> redisTemplateUser) throws IOException {
        this.userRepository = userRepository;
        this.checkInRepository = checkInRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.redissonClient = redissonClient;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.redisTemplateUser = redisTemplateUser;
        String scriptContent = StreamUtils.copyToString(
                new ClassPathResource("rollbackCheckIn.lua").getInputStream(),
                StandardCharsets.UTF_8
        );

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(scriptContent);
        script.setResultType(Long.class);

        this.rollbackCheckinScript = script;
    }

    @Async("taskExecutor")
    @Retryable(
            value = { AppException.class }, // Loại exception sẽ retry
            maxAttempts = 3,                // Tổng cộng thử 3 lần
            backoff = @Backoff(delay = 2000) // Mỗi lần cách nhau 2 giây
    )
    @Transactional
    public void saveCheckInAsync(Long userId, LocalDate checkInDate, int points, int checkInCountToday, String redisSetKey, String redisCountKey, String today) {
        String lockKey = RedisKeyUtils.getPointsLockKeySaveData(userId);
        RLock lock = redissonClient.getFairLock(lockKey);
        try {
            if (!lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                throw new AppException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }try{
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            long newTotalPoints = user.getTotalPoints() + points;
            CheckIn checkIn = new CheckIn();
            checkIn.setUserId(userId);
            checkIn.setCheckInDate(new Date());
            checkIn.setCheckInSequence(checkInCountToday);
            checkIn.setCheckInMonth(checkInDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            checkIn.setPoints((long) points);
            checkIn.setTotalPointsAfter(newTotalPoints);
            checkInRepository.save(checkIn);


            user.setTotalPoints(newTotalPoints);
            userRepository.save(user);

            PointHistory history = new PointHistory();
            history.setUserId(userId);
            history.setObjectId(checkIn.getId());
            history.setPoints((long) points);
            history.setTransactionType("ADD");
            history.setCreatedAt(new Date());
            history.setTotalPointsAfter(newTotalPoints);
            pointHistoryRepository.save(history);

            // Cập nhật cache UserDTO
            String cacheKey = RedisKeyUtils.getProfileCacheKey(userId);
            UserDTO userDTO = userMapper.responseToRequest(user);
            redisTemplateUser.opsForValue().set(cacheKey, userDTO, 1, TimeUnit.DAYS);
        } catch (RuntimeException e) {
              redisTemplate.execute(rollbackCheckinScript,
                        Arrays.asList(redisCountKey, redisSetKey),
                        today.toString());
            throw new AppException(ErrorCode.CHECKIN_INTERRUPTED);
        }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }
}
