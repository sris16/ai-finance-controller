package com.razorpay.aifinance.ingestion.model;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentRecord {
    private String paymentId;
    private String orderId;
    private Instant paymentDate;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;

    public PaymentRecord() {}

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Instant getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Instant paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
