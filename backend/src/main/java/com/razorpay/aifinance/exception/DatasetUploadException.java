package com.razorpay.aifinance.exception;

public class DatasetUploadException extends RuntimeException {
    public DatasetUploadException(String message) {
        super(message);
    }

    public DatasetUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
