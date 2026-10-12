package com.retailer.rewards.exception;

public class DownstreamDataException extends RuntimeException {

    public DownstreamDataException(String message) {
        super(message);
    }

    public DownstreamDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
