package com.example.queue.controller;

import com.example.queue.Constant;
import com.example.queue.dto.AllowUserResponse;
import com.example.queue.dto.IsAllowedUserResponse;
import com.example.queue.dto.RankNumberResponse;
import com.example.queue.dto.RegisterUserResponse;
import com.example.queue.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {
    private final UserQueueService userQueueService;

    @PostMapping("/register")
    public Mono<RegisterUserResponse> registerUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                   @RequestParam(name = "userId") Long userId) {
        return userQueueService.registerWaitQueue(queue, userId).map(RegisterUserResponse::new);
    }

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                             @RequestParam(name = "count") Long count) {
        return userQueueService.allowUser(queue, count).map(allowed -> new AllowUserResponse(count, allowed));
    }

//    @GetMapping("/allowed")
    public Mono<IsAllowedUserResponse> isAllowedUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                     @RequestParam(name = "userId") Long userId) {
        return userQueueService.isAllowedUser(queue, userId).map(IsAllowedUserResponse::new);
    }

    @GetMapping("/allowed")
    public Mono<IsAllowedUserResponse> isAllowedUserByToken(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                            @RequestParam(name = "userId") Long userId, @RequestParam(name = "token") String token) {
        return userQueueService.isAllowedUserByToken(queue, userId, token).map(IsAllowedUserResponse::new);
    }

    @GetMapping("/getRank")
    public Mono<RankNumberResponse> getRank(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                            @RequestParam(name = "userId") Long userId) {
        return userQueueService.getRank(queue, userId).map(RankNumberResponse::new);
    }

    @GetMapping("/touch")
    public Mono<?> touch(@RequestParam(name = "queue", defaultValue = "default") String queue,
                         @RequestParam(name = "userId") Long userId, ServerWebExchange exchange) {
        return Mono.defer(() -> userQueueService.generateToken(queue, userId))
                   .map(token -> {
                        exchange.getResponse()
                                .addCookie(ResponseCookie.from(Constant.COOKIE_NAME.formatted(queue), token)
                                                         .maxAge(Duration.ofSeconds(300))
                                                         .path("/")
                                                         .build());

                        return token;
                   });
    }
}
