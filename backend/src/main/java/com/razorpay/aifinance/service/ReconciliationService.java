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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
        int total = (int) repository.count();
        int matched = (int) repository.countByOverallStatus(ReconciliationStatus.MATCH);
        int exceptions = (int) repository.countByOverallStatus(ReconciliationStatus.EXCEPTION);

        Map<ExceptionType, Integer> breakdown = new EnumMap<>(ExceptionType.class);
        List<Object[]> exceptionCounts = repository.countExceptionsByType();
        for (Object[] row : exceptionCounts) {
            ExceptionType type = (ExceptionType) row[0];
            Number count = (Number) row[1];
            breakdown.put(type, count.intValue());
        }

        return reporter.generateReport(total, matched, exceptions, breakdown);
    }

    public Page<ReconciliationResult> getAllResults(ReconciliationStatus status, ExceptionType exceptionType, Pageable pageable) {
        Page<ReconciliationResultEntity> page;

        if (status != null && exceptionType != null) {
            page = repository.findByOverallStatusAndExceptionType(status, exceptionType, pageable);
        } else if (status != null) {
            page = repository.findByOverallStatus(status, pageable);
        } else if (exceptionType != null) {
            page = repository.findByExceptionType(exceptionType, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.map(ReconciliationResultEntity::toDomain);
    }

    public ReconciliationResult getResultByPaymentId(String paymentId) {
        return repository.findById(paymentId)
                .map(ReconciliationResultEntity::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("No reconciliation result found for payment " + paymentId));
    }
}
