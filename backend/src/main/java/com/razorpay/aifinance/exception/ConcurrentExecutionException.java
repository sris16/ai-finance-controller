package com.razorpay.aifinance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConcurrentExecutionException extends RuntimeException {
    public ConcurrentExecutionException(String message) {
        super(message);
    }
}
