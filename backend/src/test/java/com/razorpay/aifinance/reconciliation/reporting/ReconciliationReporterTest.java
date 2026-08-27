package com.razorpay.aifinance.reconciliation.reporting;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        ReconciliationReport report = reporter.generateReport(Collections.emptyList());
        assertEquals(0, report.getTotalRecords());
        assertEquals(0, report.getMatchedRecords());
        assertEquals(0, report.getExceptionRecords());
        assertEquals(BigDecimal.ZERO, report.getMatchRate());
        assertEquals(BigDecimal.ZERO, report.getExceptionRate());
    }

    @Test
    void testAllMatches() {
        List<ReconciliationResult> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ReconciliationResult res = new ReconciliationResult();
            res.setPaymentId("pay_" + i);
            res.setOverallStatus(ReconciliationStatus.MATCH);
            res.setExceptionType(ExceptionType.NONE);
            results.add(res);
        }

        ReconciliationReport report = reporter.generateReport(results);
        assertEquals(10, report.getTotalRecords());
        assertEquals(10, report.getMatchedRecords());
        assertEquals(0, report.getExceptionRecords());
        assertEquals(new BigDecimal("100.00"), report.getMatchRate());
        assertEquals(new BigDecimal("0.00"), report.getExceptionRate());
    }

    @Test
    void testMixedResultsAndBreakdown() {
        List<ReconciliationResult> results = new ArrayList<>();
        
        // 3 Matches
        for (int i = 0; i < 3; i++) {
            ReconciliationResult res = new ReconciliationResult();
            res.setPaymentId("pay_m" + i);
            res.setOverallStatus(ReconciliationStatus.MATCH);
            res.setExceptionType(ExceptionType.NONE);
            results.add(res);
        }
        
        // 1 AMOUNT_MISMATCH
        ReconciliationResult r1 = new ReconciliationResult();
        r1.setPaymentId("pay_e1");
        r1.setOverallStatus(ReconciliationStatus.EXCEPTION);
        r1.setExceptionType(ExceptionType.AMOUNT_MISMATCH);
        results.add(r1);

        // 1 DATE_ANOMALY
        ReconciliationResult r2 = new ReconciliationResult();
        r2.setPaymentId("pay_e2");
        r2.setOverallStatus(ReconciliationStatus.EXCEPTION);
        r2.setExceptionType(ExceptionType.DATE_ANOMALY);
        results.add(r2);

        ReconciliationReport report = reporter.generateReport(results);

        assertEquals(5, report.getTotalRecords());
        assertEquals(3, report.getMatchedRecords());
        assertEquals(2, report.getExceptionRecords());
        
        // Rates: 3/5 = 60%, 2/5 = 40%
        assertEquals(new BigDecimal("60.00"), report.getMatchRate());
        assertEquals(new BigDecimal("40.00"), report.getExceptionRate());

        Map<ExceptionType, Integer> breakdown = report.getExceptionBreakdown();
        assertNotNull(breakdown);
        assertEquals(1, breakdown.get(ExceptionType.AMOUNT_MISMATCH));
        assertEquals(1, breakdown.get(ExceptionType.DATE_ANOMALY));
        assertEquals(0, breakdown.get(ExceptionType.MISSING_SETTLEMENT));
    }
}
