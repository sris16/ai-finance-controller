package com.razorpay.aifinance.domain.model;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReconciliationResultTest {

    @Test
    void testReconciliationResultConstruction() {
        BankTransactionDetail detail = new BankTransactionDetail();
        detail.setAmount(new BigDecimal("100.00"));
        detail.setStatus("COMPLETED");
        detail.setDate(Instant.parse("2026-08-27T10:00:00Z"));

        ReconciliationResult result = new ReconciliationResult();
        result.setPaymentId("pay_123");
        result.setOrderId("order_456");
        result.setOrderAmount(new BigDecimal("100.00"));
        result.setOrderStatus("PAID");
        result.setPaymentAmount(new BigDecimal("100.00"));
        result.setPaymentStatus("AUTHORIZED");
        result.setPaymentDate(Instant.parse("2026-08-27T09:00:00Z"));
        result.setSettlementPresent(false); // Simulating MISSING_SETTLEMENT
        result.setBankTransactionCount(1);
        result.setBankTransactionAmount(new BigDecimal("100.00"));
        result.setBankTransactionStatus("COMPLETED");
        result.setBankTransactionDate(Instant.parse("2026-08-27T10:00:00Z"));
        result.setBankTransactions(Collections.singletonList(detail));
        result.setOverallStatus(ReconciliationStatus.EXCEPTION);
        result.setExceptionType(ExceptionType.MISSING_SETTLEMENT);
        result.setExpectedResult(ExpectedResult.EXCEPTION);
        result.setExplanation("Settlement data is missing.");
        result.setConfidenceScore(0.99);

        assertNotNull(result);
        assertEquals("pay_123", result.getPaymentId());
        assertEquals("order_456", result.getOrderId());
        assertEquals(new BigDecimal("100.00"), result.getOrderAmount());
        assertEquals(Boolean.FALSE, result.getSettlementPresent());
        assertEquals(1, result.getBankTransactionCount());
        assertEquals(1, result.getBankTransactions().size());
        assertEquals(ReconciliationStatus.EXCEPTION, result.getOverallStatus());
        assertEquals(ExceptionType.MISSING_SETTLEMENT, result.getExceptionType());
    }
}
