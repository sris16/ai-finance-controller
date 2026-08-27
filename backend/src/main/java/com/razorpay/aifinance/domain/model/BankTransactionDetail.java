package com.razorpay.aifinance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents an individual bank transaction detail.
 * Allows support for multiple transactions (e.g., DUPLICATE_TRANSACTION exception cases).
 */
public class BankTransactionDetail {
    private BigDecimal amount;
    private String status;
    private Instant date;

    public BankTransactionDetail() {
    }

    public BankTransactionDetail(BigDecimal amount, String status, Instant date) {
        this.amount = amount;
        this.status = status;
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }
}
