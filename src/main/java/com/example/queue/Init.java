package com.example.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Init {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void event() {
        reactiveRedisTemplate.opsForValue().set("testKey", "testValue").subscribe();
    }
}
