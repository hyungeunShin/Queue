package com.example.queue.service;

import reactor.core.publisher.Mono;

public interface UserQueueService {
    Mono<Long> registerWaitQueue(final String queue, final Long userId);
    Mono<Long> allowUser(final String queue, final Long count);
    Mono<Boolean> isAllowedUser(final String queue, final Long userId);
    Mono<Long> getRank(final String queue, final Long userId);
    Mono<String> generateToken(final String queue, final Long userId);
    Mono<Boolean> isAllowedUserByToken(final String queue, final Long userId, final String token);
}
