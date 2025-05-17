package com.example.checkin.service.impl;


import com.example.checkin.exception.AppException;
import com.example.checkin.model.constant.ErrorCode;
import com.example.checkin.model.dto.CheckInStatusDTO;
import com.example.checkin.model.dto.PointHistoryDTO;
import com.example.checkin.model.dto.PointHistoryDTOList;
import com.example.checkin.model.dto.UserDTO;
import com.example.checkin.model.entity.CheckIn;
import com.example.checkin.model.entity.PointHistory;
import com.example.checkin.model.entity.User;
import com.example.checkin.model.mapper.PointHistoryMapper;
import com.example.checkin.model.mapper.UserMapper;
import com.example.checkin.model.request.DeductPointsRequest;
import com.example.checkin.repository.CheckInRepository;
import com.example.checkin.repository.PointHistoryRepository;
import com.example.checkin.repository.UserRepository;
import com.example.checkin.service.AsyncService;
import com.example.checkin.service.CheckInService;
import com.example.checkin.utils.RedisKeyUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;


@Service
public class CheckInServiceImpl implements CheckInService {
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final RedissonClient redissonClient;
    private final PointHistoryMapper pointHistoryMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<List> checkInScript;
    private final RedisTemplate<String, PointHistoryDTOList> redisTemplatePointHistory;
    private final RedisTemplate<String, UserDTO> redisTemplateUser;
    private final AsyncService asyncService;

    @Value("${app.checkin.points.day1}")
    private int day1Points;
    @Value("${app.checkin.points.day2}")
    private int day2Points;
    @Value("${app.checkin.points.day3}")
    private int day3Points;
    @Value("${app.checkin.points.day4}")
    private int day4Points;
    @Value("${app.checkin.points.day5}")
    private int day5Points;
    @Value("${app.checkin.points.day6}")
    private int day6Points;
    @Value("${app.checkin.points.day7}")
    private int day7Points;

    // Constructor để load Lua Script
    public CheckInServiceImpl(UserRepository userRepository, CheckInRepository checkInRepository,
                              PointHistoryRepository pointHistoryRepository, RedissonClient redissonClient, PointHistoryMapper pointHistoryMapper, UserMapper userMapper,
                              RedisTemplate<String, String> redisTemplate, RedisTemplate<String, UserDTO> redisTemplateUser, RedisTemplate<String, PointHistoryDTOList> redisTemplatePointHistory, AsyncService asyncService) throws IOException {
        this.userRepository = userRepository;
        this.checkInRepository = checkInRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.redissonClient = redissonClient;
        this.pointHistoryMapper = pointHistoryMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.redisTemplateUser = redisTemplateUser;
        this.redisTemplatePointHistory = redisTemplatePointHistory;
        this.asyncService = asyncService;

        // Load Lua Script


        String scriptContent = StreamUtils.copyToString(
                new ClassPathResource("checkin.lua").getInputStream(),
                StandardCharsets.UTF_8
        );

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(scriptContent);
        script.setResultType(List.class);

        this.checkInScript = script;

    }

    public void checkIn(Long userId) {
        // Bước 1: Kiểm tra khung giờ hợp lệ
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        if (!isValidCheckInTime(time)) {
            throw new AppException(ErrorCode.INVALID_CHECKIN_TIME);
        }

        LocalDate today = now.toLocalDate();
        String redisSetKey = RedisKeyUtils.getCheckInSetKey(userId, today);
        String redisCountKey = RedisKeyUtils.getCheckInCountKey(userId, today);


        // Bước 2: Khóa phân tán
        String lockKey = RedisKeyUtils.getPointsLockKey(userId);
        RLock lock = redissonClient.getFairLock(lockKey);
        try {
            if (!lock.tryLock(1, 3, TimeUnit.SECONDS)) {
                throw new AppException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            // Bước 3: Dùng Lua Script
            List<Long> result = redisTemplate.execute(checkInScript,
                    Arrays.asList(redisSetKey, redisCountKey),
                    today.toString());
            if (ObjectUtils.isEmpty(result)){
                throw new AppException(ErrorCode.CHECKIN_INTERRUPTED);
            }

            boolean isCheckedIn = result.get(0) == 1;
            int checkInCount = result.get(1).intValue();

            if (isCheckedIn) {
                throw new AppException(ErrorCode.ALREADY_CHECKED_IN);
            }
            if (checkInCount >= 7) {
                throw new AppException(ErrorCode.MAX_CHECKINS_REACHED);
            }

            redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
                setExpireToEndOfMonth(redisCountKey, now.toLocalDate()); // EXPIRE đến cuối tháng
                setExpireToEndOfMonth(redisSetKey, now.toLocalDate()); // EXPIRE đến cuối tháng
                return null;
            });


            // Bước 4: Ghi cơ sở dữ liệu bất đồng bộ
            int checkInCountToday =checkInCount + 1;
            int points = getPointsForCheckIn(checkInCountToday);

            asyncService.saveCheckInAsync(userId, today, points,checkInCountToday,redisSetKey,redisCountKey,today.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.CHECKIN_INTERRUPTED);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }






    public List<CheckInStatusDTO> getCheckInStatus(Long userId) {
        LocalDate today = LocalDate.now();
        String month = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String redisKey = RedisKeyUtils.getCheckInSetKey(userId, today);

        // Thử lấy từ Redis Set trước
        Set<String> checkedInDates = redisTemplate.opsForSet().members(redisKey);
        List<CheckInStatusDTO> statusList = new ArrayList<>();

        if (checkedInDates != null && !checkedInDates.isEmpty()) {
            // Dữ liệu có trong Redis
            for (int day = 1; day <= today.getDayOfMonth(); day++) {
                LocalDate date = LocalDate.of(today.getYear(), today.getMonth(), day);
                CheckInStatusDTO status = new CheckInStatusDTO();
                status.setDate(date);
                status.setCheckedIn(checkedInDates.contains(date.toString()));
                statusList.add(status);
            }
        } else {
            // Fallback về cơ sở dữ liệu
            List<CheckIn> checkIns = checkInRepository.findByUserIdAndCheckInMonth(userId, month);
            for (int day = 1; day <= today.getDayOfMonth(); day++) {
                LocalDate date = LocalDate.of(today.getYear(), today.getMonth(), day);
                CheckInStatusDTO status = new CheckInStatusDTO();
                status.setDate(date);
                status.setCheckedIn(checkIns.stream().anyMatch(c -> c.getCheckInDate().equals(date)));
                statusList.add(status);
            }
            // Lưu lại vào Redis Set
            for (CheckIn checkIn : checkIns) {
                redisTemplate.opsForSet().add(redisKey, checkIn.getCheckInDate().toString());
            }
            redisTemplate.expire(redisKey, 30, TimeUnit.DAYS);
        }
        return statusList;
    }


    public Page<PointHistoryDTO> getPointHistory(Long userId, int page, int size) {
        String cacheKey = RedisKeyUtils.getPointHistoryCacheKey(userId, page, size);
        PointHistoryDTOList cachedResult = redisTemplatePointHistory.opsForValue().get(cacheKey);

        if (cachedResult != null && cachedResult.getPointHistoryList() != null) {
            return new PageImpl<>(cachedResult.getPointHistoryList(), Pageable.ofSize(size).withPage(page),
                    cachedResult.getTotalElements());
        }

        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<PointHistory> data = pointHistoryRepository.findByUserId(userId,"ADD", pageable);
        List<PointHistoryDTO> dtoList = pointHistoryMapper.toDTOList(data.getContent());

        PointHistoryDTOList toCache = new PointHistoryDTOList();
        toCache.setPointHistoryList(dtoList);
        toCache.setTotalElements(data.getTotalElements());
        redisTemplatePointHistory.opsForValue().set(cacheKey, toCache, 5, TimeUnit.MINUTES);

        return new PageImpl<>(dtoList, pageable, data.getTotalElements());
    }

    @Transactional
    public void deductPoints(Long userId, DeductPointsRequest request) {
        String lockKey = RedisKeyUtils.getPointsLockKeySaveData(userId);
        RLock lock = redissonClient.getFairLock(lockKey);
        try {
            if (!lock.tryLock(1, 3, TimeUnit.SECONDS)) {
                throw new AppException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getTotalPoints() < request.getPoints()) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }
        long newTotalPoints = user.getTotalPoints() - request.getPoints();
        user.setTotalPoints(newTotalPoints);
        userRepository.save(user);
        PointHistory history = new PointHistory();
        history.setUserId(userId);
        history.setPoints(request.getPoints());
        history.setTransactionType("DEDUCT");
        history.setCreatedAt(new Date());
        history.setTotalPointsAfter(newTotalPoints);
        pointHistoryRepository.save(history);
        String cacheKey = RedisKeyUtils.getProfileCacheKey(userId);
        UserDTO userDTO = userMapper.responseToRequest(user);
        redisTemplateUser.opsForValue().set(cacheKey, userDTO, 1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.CHECKIN_INTERRUPTED);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean isValidCheckInTime(LocalTime time) {
        int hour = time.getHour();
        return (hour >= 9 && hour < 11) || (hour >= 19 && hour < 24);
    }

    private int getPointsForCheckIn(int checkInCount) {
        return switch (checkInCount) {
            case 1 -> day1Points;
            case 2 -> day2Points;
            case 3 -> day3Points;
            case 4 -> day4Points;
            case 5 -> day5Points;
            case 6 -> day6Points;
            case 7 -> day7Points;
            default -> throw new AppException(ErrorCode.INVALID_CHECKIN_COUNT);
        };


    }
    // Phương thức tính và đặt EXPIRE đến cuối tháng
    private void setExpireToEndOfMonth(String key, LocalDate currentDate) {
        LocalDate lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth());
        long daysUntilEndOfMonth = java.time.temporal.ChronoUnit.DAYS.between(currentDate, lastDayOfMonth) + 1;
        redisTemplate.expire(key, daysUntilEndOfMonth, TimeUnit.DAYS);
    }
}
