package com.razorpay.aifinance.exception;

public class CsvIngestionException extends RuntimeException {
    public CsvIngestionException(String message) {
        super(message);
    }

    public CsvIngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
