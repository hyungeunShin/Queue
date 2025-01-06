package com.example.queue.service;

import com.example.queue.exception.QueueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(EmbeddedRedis.class)
class UserQueueServiceImplTest {
    @Autowired
    private UserQueueService userQueueService;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    //데이터 초기화
    @BeforeEach
    public void beforeEach() {
        ReactiveRedisConnection connection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        connection.serverCommands().flushAll().subscribe();
    }

    @Test
    void registerWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L))
                    .expectNext(1L)
                    .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 102L))
                    .expectNext(2L)
                    .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 103L))
                    .expectNext(3L)
                    .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 103L))
                    .expectError(QueueException.class)
                    .verify();
    }

    @Test
    void emptyAllowUser() {
        StepVerifier.create(userQueueService.allowUser("default", 1L))
                    .expectNext(0L)
                    .verifyComplete();
    }

    @Test
    void allowUser1() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L)
                            .then(userQueueService.registerWaitQueue("default", 102L))
                            .then(userQueueService.registerWaitQueue("default", 103L))
                            .then(userQueueService.allowUser("default", 2L)))
                    .expectNext(2L)
                    .verifyComplete();
    }

    @Test
    void allowUser2() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L)
                            .then(userQueueService.registerWaitQueue("default", 102L))
                            .then(userQueueService.registerWaitQueue("default", 103L))
                            .then(userQueueService.allowUser("default", 5L)))
                    .expectNext(3L)
                    .verifyComplete();
    }

    @Test
    void allowUser3() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L)
                            .then(userQueueService.registerWaitQueue("default", 102L))
                            .then(userQueueService.registerWaitQueue("default", 103L))
                            .then(userQueueService.allowUser("default", 5L))
                            .then(userQueueService.registerWaitQueue("default", 200L)))
                    .expectNext(1L)
                    .verifyComplete();
    }

    @Test
    void isNotAllowedUser1() {
        StepVerifier.create(userQueueService.isAllowedUser("default", 101L))
                    .expectNext(false)
                    .verifyComplete();
    }

    @Test
    void isNotAllowedUser2() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L)
                            .then(userQueueService.allowUser("default", 1L))
                            .then(userQueueService.isAllowedUser("default", 102L)))
                    .expectNext(false)
                    .verifyComplete();
    }

    @Test
    void isAllowedUser() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L)
                            .then(userQueueService.allowUser("default", 1L))
                            .then(userQueueService.isAllowedUser("default", 101L)))
                    .expectNext(true)
                    .verifyComplete();
    }
}