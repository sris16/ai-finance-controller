package com.razorpay.aifinance.reconciliation.reporting;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.ingestion.model.FinancialDataset;
import com.razorpay.aifinance.ingestion.service.CsvIngestionService;
import com.razorpay.aifinance.reconciliation.engine.DeterministicReconciliationEngine;
import com.razorpay.aifinance.reconciliation.evaluation.ReconciliationEvaluator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReconciliationReportingIntegrationTest {

    @Test
    void testRealDatasetReportingAndEvaluation() {
        // 1. Ingestion
        CsvIngestionService ingestionService = new CsvIngestionService();
        FinancialDataset dataset = ingestionService.loadDataset("../data");
        
        // 2. Engine
        DeterministicReconciliationEngine engine = new DeterministicReconciliationEngine();
        List<ReconciliationResult> results = engine.reconcile(dataset);

        // 3. Operational Reporting
        ReconciliationReporter reporter = new ReconciliationReporter();
        ReconciliationReport report = reporter.generateReport(results);

        // Verify Operational Report
        assertEquals(100, report.getTotalRecords());
        assertEquals(80, report.getMatchedRecords());
        assertEquals(20, report.getExceptionRecords());
        assertEquals(new BigDecimal("80.00"), report.getMatchRate());
        assertEquals(new BigDecimal("20.00"), report.getExceptionRate());

        Map<ExceptionType, Integer> breakdown = report.getExceptionBreakdown();
        assertEquals(4, breakdown.get(ExceptionType.AMOUNT_MISMATCH));
        assertEquals(4, breakdown.get(ExceptionType.MISSING_SETTLEMENT));
        assertEquals(4, breakdown.get(ExceptionType.DUPLICATE_TRANSACTION));
        assertEquals(4, breakdown.get(ExceptionType.DATE_ANOMALY));
        assertEquals(4, breakdown.get(ExceptionType.STATUS_MISMATCH));

        // 4. Ground-Truth Evaluation
        ReconciliationEvaluator evaluator = new ReconciliationEvaluator();
        ReconciliationEvaluator.EvaluationMetrics metrics = evaluator.evaluate(results, dataset.getGroundTruths());

        System.out.println("==================================================");
        System.out.println("OPERATIONAL RECONCILIATION REPORT");
        System.out.println("==================================================");
        System.out.println("Total Records : " + report.getTotalRecords());
        System.out.println("Matches       : " + report.getMatchedRecords() + " (" + report.getMatchRate() + "%)");
        System.out.println("Exceptions    : " + report.getExceptionRecords() + " (" + report.getExceptionRate() + "%)");
        System.out.println("Exception Breakdown:");
        for (Map.Entry<ExceptionType, Integer> entry : breakdown.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.println("  - " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        System.out.println("\n==================================================");
        System.out.println("GROUND-TRUTH EVALUATION METRICS");
        System.out.println("==================================================");
        System.out.println("Accuracy Overall       : " + String.format("%.2f%%", metrics.getAccuracy() * 100));
        System.out.println("Accuracy by Type       : " + String.format("%.2f%%", metrics.getExceptionTypeAccuracy() * 100));
        System.out.println("Correct Classifications: " + metrics.getCorrectClassifications());
        System.out.println("Incorrect              : " + metrics.getIncorrectClassifications());
        System.out.println("\nConfusion Matrix:");
        System.out.println("True Matches (Expected MATCH -> Actual MATCH): " + metrics.getTrueMatches());
        System.out.println("False Exceptions (Expected MATCH -> Actual EXCEPTION): " + metrics.getFalseExceptions());
        System.out.println("False Matches (Expected EXCEPTION -> Actual MATCH): " + metrics.getFalseMatches());
        System.out.println("True Exceptions (Expected EXCEPTION -> Actual EXCEPTION): " + metrics.getTrueExceptions());
        System.out.println("==================================================");

        // Verify Evaluation Accuracy
        assertEquals(1.0, metrics.getAccuracy(), "Overall accuracy should be 100%");
        assertEquals(1.0, metrics.getExceptionTypeAccuracy(), "Exception type accuracy should be 100%");
        assertEquals(100, metrics.getCorrectClassifications());
        assertEquals(0, metrics.getIncorrectClassifications());
    }
}
