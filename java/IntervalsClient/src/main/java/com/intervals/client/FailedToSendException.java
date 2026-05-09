package com.intervals.client;

public class FailedToSendException extends RuntimeException {
    public FailedToSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
