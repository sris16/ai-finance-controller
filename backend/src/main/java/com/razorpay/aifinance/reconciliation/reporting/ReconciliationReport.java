package com.razorpay.aifinance.reconciliation.reporting;

import com.razorpay.aifinance.domain.enums.ExceptionType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class ReconciliationReport {

    private int totalRecords;
    private int matchedRecords;
    private int exceptionRecords;

    private BigDecimal matchRate;
    private BigDecimal exceptionRate;

    private Map<ExceptionType, Integer> exceptionBreakdown;

    public ReconciliationReport() {
        this.exceptionBreakdown = Collections.emptyMap();
        this.matchRate = BigDecimal.ZERO;
        this.exceptionRate = BigDecimal.ZERO;
    }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getMatchedRecords() { return matchedRecords; }
    public void setMatchedRecords(int matchedRecords) { this.matchedRecords = matchedRecords; }

    public int getExceptionRecords() { return exceptionRecords; }
    public void setExceptionRecords(int exceptionRecords) { this.exceptionRecords = exceptionRecords; }

    public BigDecimal getMatchRate() { return matchRate; }
    public void setMatchRate(BigDecimal matchRate) { this.matchRate = matchRate; }

    public BigDecimal getExceptionRate() { return exceptionRate; }
    public void setExceptionRate(BigDecimal exceptionRate) { this.exceptionRate = exceptionRate; }

    public Map<ExceptionType, Integer> getExceptionBreakdown() { return exceptionBreakdown; }
    public void setExceptionBreakdown(Map<ExceptionType, Integer> exceptionBreakdown) { this.exceptionBreakdown = exceptionBreakdown; }
}
