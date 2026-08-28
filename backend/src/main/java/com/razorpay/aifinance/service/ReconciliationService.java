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
import com.razorpay.aifinance.exception.ConcurrentExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@SuppressWarnings("null")
public class ReconciliationService {

    private final CsvIngestionService csvIngestionService;
    private final DeterministicReconciliationEngine engine;
    private final ReconciliationReporter reporter;

    private final ReconciliationResultRepository repository;
    private final ReconciliationRunRepository runRepository;
    private final com.razorpay.aifinance.repository.ReconciliationDatasetRepository datasetRepository;
    private final String dataPath;
    private final String datasetsPath;
    private final Executor executor;
    private static final Logger logger = LoggerFactory.getLogger(ReconciliationService.class);

    public ReconciliationService(
            CsvIngestionService csvIngestionService,
            DeterministicReconciliationEngine engine,
            ReconciliationReporter reporter,
            ReconciliationRunRepository runRepository,
            ReconciliationResultRepository repository,
            com.razorpay.aifinance.repository.ReconciliationDatasetRepository datasetRepository,
            @Value("${app.data.path}") String dataPath,
            @Value("${app.datasets.path:data/datasets}") String datasetsPath,
            @Qualifier("reconciliationTaskExecutor") Executor executor) {
        this.csvIngestionService = csvIngestionService;
        this.engine = engine;
        this.reporter = reporter;
        this.runRepository = runRepository;
        this.repository = repository;
        this.datasetRepository = datasetRepository;
        this.dataPath = dataPath;
        this.datasetsPath = datasetsPath;
        this.executor = executor;
    }

    public synchronized ReconciliationRunEntity executeReconciliationRun(String datasetId) {
        if (runRepository.existsByStatus(RunStatus.IN_PROGRESS)) {
            throw new ConcurrentExecutionException("A reconciliation batch is already IN_PROGRESS. Please wait for it to complete.");
        }

        if (datasetId != null && !datasetRepository.existsById(datasetId)) {
            throw new com.razorpay.aifinance.exception.ResourceNotFoundException("Dataset not found with ID: " + datasetId);
        }

        ReconciliationRunEntity run = new ReconciliationRunEntity();
        run.setExecutionTime(Instant.now());
        run.setStatus(RunStatus.IN_PROGRESS);
        run.setDatasetId(datasetId);
        run = runRepository.save(run);

        final String runId = run.getId();
        executor.execute(() -> processReconciliationAsync(runId, datasetId));

        return run;
    }

    @Async
    public void processReconciliationAsync(String runId, String datasetId) {
        logger.info("Starting Async Reconciliation for Run [{}] using Dataset [{}]", runId, datasetId != null ? datasetId : "LEGACY");
        ReconciliationRunEntity run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalStateException("Run not found: " + runId));

        try {
            String targetPath = datasetId == null ? dataPath : java.nio.file.Paths.get(datasetsPath).toAbsolutePath().normalize().resolve(datasetId).toString();
            FinancialDataset dataset = csvIngestionService.loadDataset(targetPath);
            List<ReconciliationResult> results = engine.reconcile(dataset);

            final ReconciliationRunEntity finalRun = run;
            List<ReconciliationResultEntity> entities = results.stream()
                    .map(r -> {
                        ReconciliationResultEntity entity = ReconciliationResultEntity.fromDomain(r);
                        entity.setRun(finalRun);
                        return entity;
                    })
                    .collect(Collectors.toList());

            repository.saveAll(entities);

            run.setTotalRecords(results.size());
            run.setStatus(RunStatus.COMPLETED);
            runRepository.save(run);

            logger.info("Completed Async Run [{}]", runId);
        } catch (Exception e) {
            logger.error("Async Run [{}] failed", runId, e);
            run.setStatus(RunStatus.FAILED);
            runRepository.save(run);
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
