package com.example.queue.service;

import reactor.core.publisher.Mono;

public interface UserQueueService {
    Mono<Long> registerWaitQueue(final String queue, final Long userId);
    Mono<Long> allowUser(final String queue, final Long count);
    Mono<Boolean> isAllowedUser(final String queue, final Long userId);
}
