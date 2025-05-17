package com.example.checkin.service.impl;

import com.example.checkin.exception.AppException;
import com.example.checkin.model.constant.ErrorCode;
import com.example.checkin.model.dto.UserDTO;
import com.example.checkin.model.entity.User;
import com.example.checkin.model.mapper.UserMapper;
import com.example.checkin.repository.UserRepository;
import com.example.checkin.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, UserDTO> redisTemplate;
    private final UserMapper userMapper;

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTS);
        }
        User user = userMapper.requestToResponse(userDTO);
        user.setTotalPoints(0L);
        user = userRepository.save(user);
        return userMapper.responseToRequest(user);
    }


    public UserDTO getUserProfile(Long userId) {
        String key= "getProfile:user:"+ userId;
        UserDTO response = redisTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(response)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            response =userMapper.responseToRequest(user);
            redisTemplate.opsForValue().set(key,response );
            redisTemplate.expire(key,1, TimeUnit.DAYS );
        }
        return response;
    }

}
