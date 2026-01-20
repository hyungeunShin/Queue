package com.example.queue.controller;

import com.example.queue.Constant;
import com.example.queue.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WaitingRoomController {
    private final UserQueueService userQueueService;

    @GetMapping("/waitingRoom")
    public Mono<Rendering> waitingRoomPage(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                           @RequestParam(name = "userId") Long userId,
                                           @RequestParam(name = "redirectUrl") String redirectUrl,
                                           ServerWebExchange exchange) {
        log.info("userId: {}", userId);
        log.info("queue: {}", queue);
        log.info("redirectUrl: {}", redirectUrl);

        String key = Constant.COOKIE_NAME.formatted(queue);
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(key);
        String token = cookie == null ? "" : cookie.getValue();

//        return userQueueService.isAllowedUser(queue, userId)
        return userQueueService.isAllowedUserByToken(queue, userId, token)
                               .filter(allowed -> allowed)
                               .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
                               .switchIfEmpty(
                                        userQueueService.registerWaitQueue(queue, userId)
                                                        .onErrorResume(ex -> userQueueService.getRank(queue, userId))
                                                        .map(rank -> Rendering.view("waitingRoom")
                                                                              .modelAttribute("number", rank)
                                                                              .modelAttribute("userId", userId)
                                                                              .modelAttribute("queue", queue)
                                                                              .build())
                               );
    }
}
