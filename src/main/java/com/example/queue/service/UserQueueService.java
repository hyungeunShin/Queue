package com.example.queue.service;

import reactor.core.publisher.Mono;

public interface UserQueueService {
    Mono<Long> registerWaitQueue(String queue, Long userId);
}
