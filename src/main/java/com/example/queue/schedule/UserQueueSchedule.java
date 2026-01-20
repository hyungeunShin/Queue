package com.example.queue.schedule;

import com.example.queue.Constant;
import com.example.queue.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuples;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserQueueSchedule {
    private final UserQueueService userQueueService;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void scheduleAllowUser() {
        log.info("called scheduling...");

        Long maxAllowUserCount = 1L;

        reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                                              .match(Constant.USER_QUEUE_WAIT_KEY_FOR_SCAN)
                                              .count(100).build())
                             .map(key -> key.split(":")[2])
                             .flatMap(queue -> userQueueService.allowUser(queue, maxAllowUserCount).map(allowed -> Tuples.of(queue, allowed)))
                             .doOnNext(tuple -> log.info("Attempted to allow {} users in the '{}' queue. Successfully allowed {} users.", maxAllowUserCount, tuple.getT1(), tuple.getT2()))
                             .subscribe();
    }
}
