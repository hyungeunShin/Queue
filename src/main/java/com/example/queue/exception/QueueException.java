package com.example.queue.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class QueueException extends RuntimeException {
    private HttpStatus httpStatus;
    private String code;
    private String reason;
}
