package com.razorpay.aifinance.ingestion.model;

import java.math.BigDecimal;
import java.time.Instant;

public class SettlementRecord {
    private String settlementId;
    private String paymentId;
    private Instant settlementDate;
    private BigDecimal grossAmount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String status;

    public SettlementRecord() {}

    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Instant getSettlementDate() { return settlementDate; }
    public void setSettlementDate(Instant settlementDate) { this.settlementDate = settlementDate; }

    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
