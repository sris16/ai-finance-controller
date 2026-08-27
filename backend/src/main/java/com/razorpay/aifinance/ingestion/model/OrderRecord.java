package com.razorpay.aifinance.ingestion.model;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderRecord {
    private String orderId;
    private String customerId;
    private Instant orderDate;
    private BigDecimal amount;
    private String currency;
    private String status;

    public OrderRecord() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Instant getOrderDate() { return orderDate; }
    public void setOrderDate(Instant orderDate) { this.orderDate = orderDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
