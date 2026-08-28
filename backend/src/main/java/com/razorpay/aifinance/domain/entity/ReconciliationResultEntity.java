package com.razorpay.aifinance.domain.entity;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.BankTransactionDetail;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "reconciliation_results", indexes = {
    @Index(name = "idx_overall_status", columnList = "overall_status"),
    @Index(name = "idx_exception_type", columnList = "exception_type"),
    @Index(name = "idx_run_payment", columnList = "run_id, payment_id"),
    @Index(name = "idx_run_overall_status", columnList = "run_id, overall_status"),
    @Index(name = "idx_run_exception_type", columnList = "run_id, exception_type")
})
public class ReconciliationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private ReconciliationRunEntity run;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "order_amount")
    private BigDecimal orderAmount;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_date")
    private Instant paymentDate;

    @Column(name = "settlement_present")
    private Boolean settlementPresent;

    @Column(name = "settlement_gross_amount")
    private BigDecimal settlementGrossAmount;

    @Column(name = "settlement_fee")
    private BigDecimal settlementFee;

    @Column(name = "settlement_net_amount")
    private BigDecimal settlementNetAmount;

    @Column(name = "settlement_status")
    private String settlementStatus;

    @Column(name = "settlement_date")
    private Instant settlementDate;

    @Column(name = "bank_transaction_count")
    private Integer bankTransactionCount;

    @Column(name = "bank_transaction_amount")
    private BigDecimal bankTransactionAmount;

    @Column(name = "bank_transaction_status")
    private String bankTransactionStatus;

    @Column(name = "bank_transaction_date")
    private Instant bankTransactionDate;

    @Column(name = "bank_transactions", columnDefinition = "TEXT")
    @Convert(converter = BankTransactionDetailConverter.class)
    private List<BankTransactionDetail> bankTransactions;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status")
    private ReconciliationStatus overallStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type")
    private ExceptionType exceptionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "expected_result")
    private ExpectedResult expectedResult;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    public ReconciliationResultEntity() {
    }

    // --- Mappers ---

    public static ReconciliationResultEntity fromDomain(ReconciliationResult result) {
        ReconciliationResultEntity entity = new ReconciliationResultEntity();
        entity.paymentId = result.getPaymentId();
        entity.orderId = result.getOrderId();
        entity.orderAmount = result.getOrderAmount();
        entity.orderStatus = result.getOrderStatus();
        entity.paymentAmount = result.getPaymentAmount();
        entity.paymentStatus = result.getPaymentStatus();
        entity.paymentDate = result.getPaymentDate();
        entity.settlementPresent = result.getSettlementPresent();
        entity.settlementGrossAmount = result.getSettlementGrossAmount();
        entity.settlementFee = result.getSettlementFee();
        entity.settlementNetAmount = result.getSettlementNetAmount();
        entity.settlementStatus = result.getSettlementStatus();
        entity.settlementDate = result.getSettlementDate();
        entity.bankTransactionCount = result.getBankTransactionCount();
        entity.bankTransactionAmount = result.getBankTransactionAmount();
        entity.bankTransactionStatus = result.getBankTransactionStatus();
        entity.bankTransactionDate = result.getBankTransactionDate();
        entity.bankTransactions = result.getBankTransactions();
        entity.overallStatus = result.getOverallStatus();
        entity.exceptionType = result.getExceptionType();
        entity.expectedResult = result.getExpectedResult();
        entity.explanation = result.getExplanation();
        entity.confidenceScore = result.getConfidenceScore();
        return entity;
    }

    public ReconciliationResult toDomain() {
        ReconciliationResult result = new ReconciliationResult();
        result.setPaymentId(paymentId);
        result.setOrderId(orderId);
        result.setOrderAmount(orderAmount);
        result.setOrderStatus(orderStatus);
        result.setPaymentAmount(paymentAmount);
        result.setPaymentStatus(paymentStatus);
        result.setPaymentDate(paymentDate);
        result.setSettlementPresent(settlementPresent);
        result.setSettlementGrossAmount(settlementGrossAmount);
        result.setSettlementFee(settlementFee);
        result.setSettlementNetAmount(settlementNetAmount);
        result.setSettlementStatus(settlementStatus);
        result.setSettlementDate(settlementDate);
        result.setBankTransactionCount(bankTransactionCount);
        result.setBankTransactionAmount(bankTransactionAmount);
        result.setBankTransactionStatus(bankTransactionStatus);
        result.setBankTransactionDate(bankTransactionDate);
        result.setBankTransactions(bankTransactions);
        result.setOverallStatus(overallStatus);
        result.setExceptionType(exceptionType);
        result.setExpectedResult(expectedResult);
        result.setExplanation(explanation);
        result.setConfidenceScore(confidenceScore);
        return result;
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ReconciliationRunEntity getRun() { return run; }
    public void setRun(ReconciliationRunEntity run) { this.run = run; }

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
