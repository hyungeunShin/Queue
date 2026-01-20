package com.example.queue.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {
    QUEUE_ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "UQ-0001", "Already registered in queue");

    private final HttpStatus httpStatus;
    private final String code;
    private final String reason;

    public QueueException build() {
        return new QueueException(httpStatus, code, reason);
    }

    public QueueException build(Object ...args) {
        return new QueueException(httpStatus, code, reason.formatted(args));
    }
}
