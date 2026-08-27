package com.razorpay.aifinance.domain.model;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Represents the final reconciliation result for a single payment.
 * Serves strictly as a data transfer/domain object, holding no business or matching logic.
 */
public class ReconciliationResult {

    private String paymentId;
    private String orderId;

    // Order information
    private BigDecimal orderAmount;
    private String orderStatus;

    // Payment information
    private BigDecimal paymentAmount;
    private String paymentStatus;
    private Instant paymentDate;

    // Settlement information (supports absence/nulls for MISSING_SETTLEMENT)
    private Boolean settlementPresent;
    private BigDecimal settlementGrossAmount;
    private BigDecimal settlementFee;
    private BigDecimal settlementNetAmount;
    private String settlementStatus;
    private Instant settlementDate;

    // Bank information
    private Integer bankTransactionCount;
    // Primary/Aggregated bank information
    private BigDecimal bankTransactionAmount;
    private String bankTransactionStatus;
    private Instant bankTransactionDate;

    // Supports multiple bank transactions (e.g., DUPLICATE_TRANSACTION)
    private List<BankTransactionDetail> bankTransactions;

    // Reconciliation information
    private ReconciliationStatus overallStatus;
    private ExceptionType exceptionType;
    private ExpectedResult expectedResult;
    private String explanation;
    private Double confidenceScore;

    public ReconciliationResult() {
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Instant getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Instant paymentDate) { this.paymentDate = paymentDate; }

    public Boolean getSettlementPresent() { return settlementPresent; }
    public void setSettlementPresent(Boolean settlementPresent) { this.settlementPresent = settlementPresent; }

    public BigDecimal getSettlementGrossAmount() { return settlementGrossAmount; }
    public void setSettlementGrossAmount(BigDecimal settlementGrossAmount) { this.settlementGrossAmount = settlementGrossAmount; }

    public BigDecimal getSettlementFee() { return settlementFee; }
    public void setSettlementFee(BigDecimal settlementFee) { this.settlementFee = settlementFee; }

    public BigDecimal getSettlementNetAmount() { return settlementNetAmount; }
    public void setSettlementNetAmount(BigDecimal settlementNetAmount) { this.settlementNetAmount = settlementNetAmount; }

    public String getSettlementStatus() { return settlementStatus; }
    public void setSettlementStatus(String settlementStatus) { this.settlementStatus = settlementStatus; }

    public Instant getSettlementDate() { return settlementDate; }
    public void setSettlementDate(Instant settlementDate) { this.settlementDate = settlementDate; }

    public Integer getBankTransactionCount() { return bankTransactionCount; }
    public void setBankTransactionCount(Integer bankTransactionCount) { this.bankTransactionCount = bankTransactionCount; }

    public BigDecimal getBankTransactionAmount() { return bankTransactionAmount; }
    public void setBankTransactionAmount(BigDecimal bankTransactionAmount) { this.bankTransactionAmount = bankTransactionAmount; }

    public String getBankTransactionStatus() { return bankTransactionStatus; }
    public void setBankTransactionStatus(String bankTransactionStatus) { this.bankTransactionStatus = bankTransactionStatus; }

    public Instant getBankTransactionDate() { return bankTransactionDate; }
    public void setBankTransactionDate(Instant bankTransactionDate) { this.bankTransactionDate = bankTransactionDate; }

    public List<BankTransactionDetail> getBankTransactions() { return bankTransactions; }
    public void setBankTransactions(List<BankTransactionDetail> bankTransactions) { this.bankTransactions = bankTransactions; }

    public ReconciliationStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(ReconciliationStatus overallStatus) { this.overallStatus = overallStatus; }

    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }

    public ExpectedResult getExpectedResult() { return expectedResult; }
    public void setExpectedResult(ExpectedResult expectedResult) { this.expectedResult = expectedResult; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
}
