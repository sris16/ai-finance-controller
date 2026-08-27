package com.razorpay.aifinance.ai.dto;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;

public class ReconciliationExplanationResponse {
    private String paymentId;
    private ReconciliationStatus overallStatus;
    private ExceptionType exceptionType;
    private AiExplanationResponse explanation;

    public ReconciliationExplanationResponse() {}

    public ReconciliationExplanationResponse(String paymentId, ReconciliationStatus overallStatus, ExceptionType exceptionType, AiExplanationResponse explanation) {
        this.paymentId = paymentId;
        this.overallStatus = overallStatus;
        this.exceptionType = exceptionType;
        this.explanation = explanation;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public ReconciliationStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(ReconciliationStatus overallStatus) { this.overallStatus = overallStatus; }

    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }

    public AiExplanationResponse getExplanation() { return explanation; }
    public void setExplanation(AiExplanationResponse explanation) { this.explanation = explanation; }
}
