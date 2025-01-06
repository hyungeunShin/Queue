package com.example.queue.controller;

import com.example.queue.dto.AllowUserResponse;
import com.example.queue.dto.IsAllowedUserResponse;
import com.example.queue.dto.RegisterUserResponse;
import com.example.queue.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {
    private final UserQueueService userQueueService;

    @PostMapping("/register")
    public Mono<RegisterUserResponse> registerUser(@RequestParam(name = "queue", defaultValue = "default") String queue, @RequestParam(name = "userId") Long userId) {
        return userQueueService.registerWaitQueue(queue, userId).map(RegisterUserResponse::new);
    }

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(name = "queue", defaultValue = "default") String queue, @RequestParam(name = "count") Long count) {
        return userQueueService.allowUser(queue, count).map(allowed -> new AllowUserResponse(count, allowed));
    }

    @GetMapping("/allowed")
    public Mono<IsAllowedUserResponse> isAllowedUser(@RequestParam(name = "queue", defaultValue = "default") String queue, @RequestParam(name = "userId") Long userId) {
        return userQueueService.isAllowedUser(queue, userId).map(IsAllowedUserResponse::new);
    }
}
