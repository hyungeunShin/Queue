package com.example.queue;

public interface Constant {
    String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    String USER_QUEUE_WAIT_KEY_FOR_SCAN = "users:queue:*:wait";
    String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";
    String TOKEN_VALUE = "user-queue-%s-%d";
    String COOKIE_NAME = "user-queue-%s-token";
}
