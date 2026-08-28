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
import com.razorpay.aifinance.domain.entity.ReconciliationResultEntity;
import com.razorpay.aifinance.repository.ReconciliationResultRepository;
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

    private final ReconciliationResultRepository repository;

    @Value("${app.data.path}")
    private String dataPath;

    public ReconciliationService(CsvIngestionService csvIngestionService,
                                 DeterministicReconciliationEngine engine,
                                 ReconciliationReporter reporter,
                                 ReconciliationResultRepository repository) {
        this.csvIngestionService = csvIngestionService;
        this.engine = engine;
        this.reporter = reporter;
        this.repository = repository;
    }

    @PostConstruct
    public void initializeDataset() {
        if (repository.count() == 0) {
            // Load data and perform reconciliation at startup
            FinancialDataset dataset = csvIngestionService.loadDataset(dataPath);
            List<ReconciliationResult> results = engine.reconcile(dataset);
            List<ReconciliationResultEntity> entities = results.stream()
                    .map(ReconciliationResultEntity::fromDomain)
                    .collect(Collectors.toList());
            repository.saveAll(entities);
        }
    }

    public ReconciliationReport getReport() {
        List<ReconciliationResult> allResults = repository.findAll().stream()
                .map(ReconciliationResultEntity::toDomain)
                .collect(Collectors.toList());
        return reporter.generateReport(allResults);
    }

    public List<ReconciliationResult> getAllResults(ReconciliationStatus status, ExceptionType exceptionType) {
        List<ReconciliationResultEntity> entities;

        if (status != null && exceptionType != null) {
            entities = repository.findByOverallStatus(status).stream()
                    .filter(e -> e.getExceptionType() == exceptionType)
                    .collect(Collectors.toList());
        } else if (status != null) {
            entities = repository.findByOverallStatus(status);
        } else if (exceptionType != null) {
            entities = repository.findByExceptionType(exceptionType);
        } else {
            entities = repository.findAll();
        }

        return entities.stream().map(ReconciliationResultEntity::toDomain).collect(Collectors.toList());
    }

    public ReconciliationResult getResultByPaymentId(String paymentId) {
        return repository.findById(paymentId)
                .map(ReconciliationResultEntity::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("No reconciliation result found for payment " + paymentId));
    }
}
