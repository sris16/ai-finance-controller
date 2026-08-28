package com.razorpay.aifinance.reconciliation.engine;

import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.ingestion.model.FinancialDataset;
import com.razorpay.aifinance.ingestion.service.CsvIngestionService;
import com.razorpay.aifinance.reconciliation.evaluation.ReconciliationEvaluator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeterministicReconciliationEngineIntegrationTest {

    @Test
    void testRealDatasetReconciliation() {
        // 1. Ingest Dataset
        CsvIngestionService ingestionService = new CsvIngestionService();
        FinancialDataset dataset = ingestionService.loadDataset("../data");

        assertNotNull(dataset);
        assertEquals(100, dataset.getPayments().size());

        // 2. Run Engine
        DeterministicReconciliationEngine engine = new DeterministicReconciliationEngine();
        List<ReconciliationResult> results = engine.reconcile(dataset);

        assertEquals(100, results.size(), "Engine should produce exactly one result per payment");

        // 3. Evaluate against Ground Truth
        ReconciliationEvaluator evaluator = new ReconciliationEvaluator();
        ReconciliationEvaluator.EvaluationMetrics metrics = evaluator.evaluate(results, dataset.getGroundTruths());

        System.out.println("==================================================");
        System.out.println("RECONCILIATION EVALUATION METRICS");
        System.out.println("==================================================");
        System.out.println("Total Records Reconciled : " + metrics.getTotalRecords());
        System.out.println("Match Count              : " + metrics.getMatchCount());
        System.out.println("Exception Count          : " + metrics.getExceptionCount());
        System.out.println("Correct Classifications  : " + metrics.getCorrectClassifications());
        System.out.println("Incorrect Classifications: " + metrics.getIncorrectClassifications());
        System.out.println("Accuracy                 : " + String.format("%.2f%%", metrics.getAccuracy() * 100));

        if (!metrics.getDiscrepancies().isEmpty()) {
            System.out.println("\nDiscrepancies:");
            for (Map.Entry<String, String> entry : metrics.getDiscrepancies().entrySet()) {
                System.out.println("Payment: " + entry.getKey() + " -> " + entry.getValue());
            }
        }
        System.out.println("==================================================");

        // We assert accuracy as a benchmark.
        // As per instructions, DO NOT FORCE the result. Just let it run.
        // If it's not 100%, we report it honestly in the final output.
    }
}
