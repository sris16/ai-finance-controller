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
import com.razorpay.aifinance.domain.entity.ReconciliationRunEntity;
import com.razorpay.aifinance.domain.enums.RunStatus;
import com.razorpay.aifinance.repository.ReconciliationResultRepository;
import com.razorpay.aifinance.repository.ReconciliationRunRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
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
    private final ReconciliationRunRepository runRepository;

    @Value("${app.data.path}")
    private String dataPath;

    public ReconciliationService(CsvIngestionService csvIngestionService,
                                 DeterministicReconciliationEngine engine,
                                 ReconciliationReporter reporter,
                                 ReconciliationResultRepository repository,
                                 ReconciliationRunRepository runRepository) {
        this.csvIngestionService = csvIngestionService;
        this.engine = engine;
        this.reporter = reporter;
        this.repository = repository;
        this.runRepository = runRepository;
    }

    public synchronized ReconciliationRunEntity executeReconciliationRun() {
        ReconciliationRunEntity run = new ReconciliationRunEntity();
        run.setExecutionTime(Instant.now());
        run.setStatus(RunStatus.IN_PROGRESS);
        run = runRepository.save(run);

        try {
            FinancialDataset dataset = csvIngestionService.loadDataset(dataPath);
            List<ReconciliationResult> results = engine.reconcile(dataset);

            run.setTotalRecords(results.size());
            run.setStatus(RunStatus.COMPLETED);
            run = runRepository.save(run);

            final ReconciliationRunEntity finalRun = run;
            List<ReconciliationResultEntity> entities = results.stream()
                    .map(r -> {
                        ReconciliationResultEntity entity = ReconciliationResultEntity.fromDomain(r);
                        entity.setRun(finalRun);
                        return entity;
                    })
                    .collect(Collectors.toList());

            repository.saveAll(entities);
            return run;
        } catch (Exception e) {
            run.setStatus(RunStatus.FAILED);
            runRepository.save(run);
            throw new RuntimeException("Reconciliation run failed", e);
        }
    }

    private ReconciliationRunEntity resolveRun(String runId) {
        if (runId != null && !runId.isEmpty()) {
            return runRepository.findById(runId)
                    .orElseThrow(() -> new ResourceNotFoundException("No reconciliation run found for id " + runId));
        }
        return runRepository.findFirstByStatusOrderByExecutionTimeDesc(RunStatus.COMPLETED)
                .orElseThrow(() -> new ResourceNotFoundException("No completed reconciliation runs found"));
    }

    public List<ReconciliationRunEntity> getAllRuns() {
        return runRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "executionTime"));
    }

    public ReconciliationReport getReport(String runId) {
        ReconciliationRunEntity run = resolveRun(runId);

        int total = (int) repository.countByRun(run);
        int matched = (int) repository.countByRunAndOverallStatus(run, ReconciliationStatus.MATCH);
        int exceptions = total - matched;

        Map<ExceptionType, Integer> breakdown = new EnumMap<>(ExceptionType.class);
        List<Object[]> exceptionCounts = repository.countExceptionsByType(run);
        for (Object[] row : exceptionCounts) {
            ExceptionType type = (ExceptionType) row[0];
            Number count = (Number) row[1];
            breakdown.put(type, count.intValue());
        }

        return reporter.generateReport(total, matched, exceptions, breakdown);
    }

    public Page<ReconciliationResult> getAllResults(String runId, ReconciliationStatus status, ExceptionType exceptionType, Pageable pageable) {
        ReconciliationRunEntity run = resolveRun(runId);
        Page<ReconciliationResultEntity> page;

        if (status != null && exceptionType != null) {
            page = repository.findByRunAndOverallStatusAndExceptionType(run, status, exceptionType, pageable);
        } else if (status != null) {
            page = repository.findByRunAndOverallStatus(run, status, pageable);
        } else if (exceptionType != null) {
            page = repository.findByRunAndExceptionType(run, exceptionType, pageable);
        } else {
            page = repository.findByRun(run, pageable);
        }

        return page.map(ReconciliationResultEntity::toDomain);
    }

    public ReconciliationResult getResultByPaymentId(String runId, String paymentId) {
        ReconciliationRunEntity run = resolveRun(runId);
        return repository.findByRunAndPaymentId(run, paymentId)
                .map(ReconciliationResultEntity::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("No reconciliation result found for payment " + paymentId + " in run " + run.getId()));
    }
}
