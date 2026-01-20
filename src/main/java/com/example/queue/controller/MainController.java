package com.example.queue.controller;

import com.example.queue.Constant;
import com.example.queue.dto.IsAllowedUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final WebClient.Builder webClientBuilder;

    @GetMapping("/")
    public Mono<Rendering> mainPage(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                    @RequestParam(name = "userId") Long userId, ServerWebExchange exchange) {
        WebClient webClient = webClientBuilder.baseUrl("http://localhost").build();

        String key = Constant.COOKIE_NAME.formatted(queue);
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(key);
        String token = cookie == null ? "" : cookie.getValue();

        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/v1/queue/allowed")
                                                     .queryParam("queue", queue)
                                                     .queryParam("userId", userId)
                                                     .queryParam("token", token)
                                                     .build())
                        .retrieve()
                        .bodyToMono(IsAllowedUserResponse.class)
                        .flatMap(response -> {
                            if(response == null || !response.isAllowedUser()) {
                                return Mono.just(Rendering.redirectTo("waitingRoom?userId=%d&redirectUrl=%s".formatted(userId, "http://localhost?userId=%d".formatted(userId))).build());
                            }

                            return Mono.just(Rendering.view("index").build());
                        });
    }
}
