package com.example.queue.service;

import com.example.queue.Constant;
import com.example.queue.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserQueueServiceImpl implements UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
        /*
        redis 의 sortedSet
        - key : userId
        - value : timestamp
        */
        long second = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet()
                                    .add(Constant.USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), second)
                                    .filter(i -> i) //이미 대기열 존재하는 사용자 제외
                                    .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()))
                                    .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(Constant.USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
                                    .map(i -> i + 1);    //대기열 순번 반환
    }

    //진입 허용
    @Override
    public Mono<Long> allowUser(final String queue, final Long count) {
        //진입을 허용하는 단계
        //1. wait queue 에서 사용자 제거
        //2. proceed queue 에 사용자 추가
        return reactiveRedisTemplate.opsForZSet()
                                    .popMin(Constant.USER_QUEUE_WAIT_KEY.formatted(queue), count)
                                    .flatMap(user -> reactiveRedisTemplate.opsForZSet().add(Constant.USER_QUEUE_PROCEED_KEY.formatted(queue), user.getValue(), Instant.now().getEpochSecond()))
                                    .count();

    }

    //진입 가능 상태 조회
    @Override
    public Mono<Boolean> isAllowedUser(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet()
                                    .rank(Constant.USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
                                    .defaultIfEmpty(-1L)
                                    .map(rank -> rank >= 0);
    }

    @Override
    public Mono<Boolean> isAllowedUserByToken(final String queue, final Long userId, final String token) {
        return generateToken(queue, userId).filter(t -> t.equalsIgnoreCase(token))
                                           .map(i -> true)
                                           .defaultIfEmpty(false);
    }

    @Override
    public Mono<Long> getRank(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet()
                                    .rank(Constant.USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
                                    .defaultIfEmpty(-1L)
                                    .map(rank -> rank >= 0 ? rank + 1 : rank);
    }

    @Override
    public Mono<String> generateToken(final String queue, final Long userId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = Constant.TOKEN_VALUE.formatted(queue, userId);
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for(byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }
            return Mono.just(hexString.toString());
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
