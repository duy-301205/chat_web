package com.example.chatWeb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String STATUS_PREFIX = "user:status:";

    public void updateStatus(Long userId, String status) {
        String key = STATUS_PREFIX + userId;
        if("ONLINE".equalsIgnoreCase(status)) {
            stringRedisTemplate.opsForValue().set(key, "ONLINE", Duration.ofMinutes(5));
        } else {
            stringRedisTemplate.delete(key);
        }
    }


    public String getStatus(Long userId) {
        String key = STATUS_PREFIX + userId;
        String val = stringRedisTemplate.opsForValue().get(key);
        return (val != null) ? "ONLINE" : "OFFLINE";
    }
}
