package com.razorpay.aifinance.ingestion.model;

import java.math.BigDecimal;
import java.time.Instant;

public class BankTransactionRecord {
    private String transactionId;
    private String paymentId;
    private Instant transactionDate;
    private BigDecimal amount;
    private String transactionType;
    private String status;

    public BankTransactionRecord() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Instant getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Instant transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
