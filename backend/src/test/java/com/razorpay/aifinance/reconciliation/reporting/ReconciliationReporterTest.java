package com.razorpay.aifinance.reconciliation.reporting;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReconciliationReporterTest {

    private ReconciliationReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new ReconciliationReporter();
    }

    @Test
    void testEmptyDataset() {
        ReconciliationReport report = reporter.generateReport(0, 0, 0, new EnumMap<>(ExceptionType.class));
        assertEquals(0, report.getTotalRecords());
        assertEquals(0, report.getMatchedRecords());
        assertEquals(0, report.getExceptionRecords());
        assertEquals(BigDecimal.ZERO, report.getMatchRate());
        assertEquals(BigDecimal.ZERO, report.getExceptionRate());
    }

    @Test
    void testAllMatches() {
        ReconciliationReport report = reporter.generateReport(10, 10, 0, new EnumMap<>(ExceptionType.class));
        assertEquals(10, report.getTotalRecords());
        assertEquals(10, report.getMatchedRecords());
        assertEquals(0, report.getExceptionRecords());
        assertEquals(new BigDecimal("100.00"), report.getMatchRate());
        assertEquals(new BigDecimal("0.00"), report.getExceptionRate());
    }

    @Test
    void testMixedResultsAndBreakdown() {
        Map<ExceptionType, Integer> breakdown = new EnumMap<>(ExceptionType.class);
        breakdown.put(ExceptionType.AMOUNT_MISMATCH, 1);
        breakdown.put(ExceptionType.DATE_ANOMALY, 1);

        ReconciliationReport report = reporter.generateReport(5, 3, 2, breakdown);

        assertEquals(5, report.getTotalRecords());
        assertEquals(3, report.getMatchedRecords());
        assertEquals(2, report.getExceptionRecords());

        // Rates: 3/5 = 60%, 2/5 = 40%
        assertEquals(new BigDecimal("60.00"), report.getMatchRate());
        assertEquals(new BigDecimal("40.00"), report.getExceptionRate());

        Map<ExceptionType, Integer> resultBreakdown = report.getExceptionBreakdown();
        assertNotNull(resultBreakdown);
        assertEquals(1, resultBreakdown.get(ExceptionType.AMOUNT_MISMATCH));
        assertEquals(1, resultBreakdown.get(ExceptionType.DATE_ANOMALY));
        assertEquals(0, resultBreakdown.get(ExceptionType.MISSING_SETTLEMENT));
    }
}
