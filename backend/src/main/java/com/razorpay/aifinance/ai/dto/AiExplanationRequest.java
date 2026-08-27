package com.razorpay.aifinance.ai.dto;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;

import java.math.BigDecimal;

public class AiExplanationRequest {
    private String paymentId;
    private ReconciliationStatus overallStatus;
    private ExceptionType exceptionType;
    private BigDecimal paymentAmount;
    private BigDecimal settlementGrossAmount;
    private Boolean settlementPresent;
    private Integer bankTransactionCount;
    private String explanation;

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public ReconciliationStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(ReconciliationStatus overallStatus) { this.overallStatus = overallStatus; }

    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }

    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }

    public BigDecimal getSettlementGrossAmount() { return settlementGrossAmount; }
    public void setSettlementGrossAmount(BigDecimal settlementGrossAmount) { this.settlementGrossAmount = settlementGrossAmount; }

    public Boolean getSettlementPresent() { return settlementPresent; }
    public void setSettlementPresent(Boolean settlementPresent) { this.settlementPresent = settlementPresent; }

    public Integer getBankTransactionCount() { return bankTransactionCount; }
    public void setBankTransactionCount(Integer bankTransactionCount) { this.bankTransactionCount = bankTransactionCount; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
