package com.razorpay.aifinance.service;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.exception.ResourceNotFoundException;
import com.razorpay.aifinance.ingestion.model.FinancialDataset;
import com.razorpay.aifinance.ingestion.service.CsvIngestionService;
import com.razorpay.aifinance.reconciliation.engine.DeterministicReconciliationEngine;
import com.razorpay.aifinance.reconciliation.reporting.ReconciliationReport;
import com.razorpay.aifinance.reconciliation.reporting.ReconciliationReporter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

    private final CsvIngestionService csvIngestionService;
    private final DeterministicReconciliationEngine engine;
    private final ReconciliationReporter reporter;

    @Value("${app.data.path}")
    private String dataPath;

    // Simple in-memory cache for the results and report to avoid re-parsing CSVs on every request.
    // Data is loaded exactly once on startup. To refresh data, the service must be restarted.
    private List<ReconciliationResult> cachedResults;
    private ReconciliationReport cachedReport;

    public ReconciliationService(CsvIngestionService csvIngestionService,
                                 DeterministicReconciliationEngine engine,
                                 ReconciliationReporter reporter) {
        this.csvIngestionService = csvIngestionService;
        this.engine = engine;
        this.reporter = reporter;
    }

    @PostConstruct
    public void initializeDataset() {
        // Load data and perform reconciliation at startup
        FinancialDataset dataset = csvIngestionService.loadDataset(dataPath);
        this.cachedResults = engine.reconcile(dataset);
        this.cachedReport = reporter.generateReport(cachedResults);
    }

    public ReconciliationReport getReport() {
        return cachedReport;
    }

    public List<ReconciliationResult> getAllResults(ReconciliationStatus status, ExceptionType exceptionType) {
        return cachedResults.stream()
                .filter(r -> status == null || r.getOverallStatus() == status)
                .filter(r -> exceptionType == null || r.getExceptionType() == exceptionType)
                .collect(Collectors.toList());
    }

    public ReconciliationResult getResultByPaymentId(String paymentId) {
        return cachedResults.stream()
                .filter(r -> r.getPaymentId().equals(paymentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No reconciliation result found for payment " + paymentId));
    }
}
