package com.example.queue.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class QueueAdvice {
    @ExceptionHandler(QueueException.class)
    public Mono<ResponseEntity<QueueExceptionResponse>> queueExceptionHandler(QueueException e) {
        return Mono.just(ResponseEntity.status(e.getHttpStatus()).body(new QueueExceptionResponse(e.getCode(), e.getReason())));
    }

    public record QueueExceptionResponse(String code, String reason) {

    }
}
