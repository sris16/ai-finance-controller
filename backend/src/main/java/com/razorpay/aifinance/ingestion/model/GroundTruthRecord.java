package com.razorpay.aifinance.ingestion.model;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;

public class GroundTruthRecord {
    private String paymentId;
    private ExpectedResult expectedResult;
    private ExceptionType exceptionType;

    public GroundTruthRecord() {}

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public ExpectedResult getExpectedResult() { return expectedResult; }
    public void setExpectedResult(ExpectedResult expectedResult) { this.expectedResult = expectedResult; }

    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }
}
