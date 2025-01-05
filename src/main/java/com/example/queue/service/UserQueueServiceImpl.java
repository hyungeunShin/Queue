package com.example.queue.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserQueueServiceImpl implements UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";

    @Override
    public Mono<Long> registerWaitQueue(String queue, Long userId) {
        /*
        redis 의 sortedSet
        - key : userId
        - value : timestamp
        */
        long second = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), second)
                .filter(i -> i) //이미 대기열 존재하는 사용자 제외
                .switchIfEmpty(Mono.error(new RuntimeException("이미 등록된 사용자")))
                .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
                .map(i -> i + 1);    //대기열 순번 반환
    }
}
