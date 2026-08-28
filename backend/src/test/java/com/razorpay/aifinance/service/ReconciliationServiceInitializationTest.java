package com.razorpay.aifinance.service;

import com.razorpay.aifinance.ingestion.model.FinancialDataset;
import com.razorpay.aifinance.ingestion.service.CsvIngestionService;
import com.razorpay.aifinance.reconciliation.engine.DeterministicReconciliationEngine;
import com.razorpay.aifinance.domain.entity.ReconciliationRunEntity;
import com.razorpay.aifinance.reconciliation.reporting.ReconciliationReporter;
import com.razorpay.aifinance.repository.ReconciliationResultRepository;
import com.razorpay.aifinance.repository.ReconciliationRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class ReconciliationServiceInitializationTest {

    @Mock
    private CsvIngestionService csvIngestionService;
    @Mock
    private DeterministicReconciliationEngine engine;
    @Mock
    private ReconciliationReporter reporter;
    @Mock
    private ReconciliationResultRepository repository;
    @Mock
    private ReconciliationRunRepository runRepository;

    private ReconciliationService service;
    private java.util.concurrent.Executor executor = Runnable::run;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReconciliationService(csvIngestionService, engine, reporter, repository, runRepository, executor);
    }

    @Test
    void testExecuteReconciliationRun_ThrowsIfInProgress() {
        when(runRepository.existsByStatus(com.razorpay.aifinance.domain.enums.RunStatus.IN_PROGRESS)).thenReturn(true);
        try {
            service.executeReconciliationRun();
            org.junit.jupiter.api.Assertions.fail("Expected ConcurrentExecutionException");
        } catch (com.razorpay.aifinance.exception.ConcurrentExecutionException e) {
            // expected
        }
    }

    @Test
    void testExecuteReconciliationRun_Success() {
        when(runRepository.existsByStatus(any())).thenReturn(false);
        when(runRepository.save(any(ReconciliationRunEntity.class))).thenAnswer(i -> {
            ReconciliationRunEntity run = (ReconciliationRunEntity) i.getArguments()[0];
            if (run.getId() == null) run.setId("test-run-id");
            return run;
        });
        when(runRepository.findById("test-run-id")).thenReturn(java.util.Optional.of(new ReconciliationRunEntity()));
        when(csvIngestionService.loadDataset(any())).thenReturn(new FinancialDataset());
        when(engine.reconcile(any())).thenReturn(List.of(new com.razorpay.aifinance.domain.model.ReconciliationResult()));

        service.executeReconciliationRun();

        verify(csvIngestionService, times(1)).loadDataset(any());
        verify(engine, times(1)).reconcile(any());
        verify(repository, times(1)).saveAll(any());
        verify(runRepository, times(2)).save(any(ReconciliationRunEntity.class));
    }

    @Test
    void testExecuteReconciliationRun_FailsGracefully() {
        when(runRepository.existsByStatus(any())).thenReturn(false);
        when(runRepository.save(any(ReconciliationRunEntity.class))).thenAnswer(i -> {
            ReconciliationRunEntity run = (ReconciliationRunEntity) i.getArguments()[0];
            if (run.getId() == null) run.setId("test-run-id");
            return run;
        });
        when(runRepository.findById("test-run-id")).thenReturn(java.util.Optional.of(new ReconciliationRunEntity()));
        when(csvIngestionService.loadDataset(any())).thenThrow(new RuntimeException("Ingestion failed"));

        service.executeReconciliationRun();

        verify(csvIngestionService, times(1)).loadDataset(any());
        verify(engine, never()).reconcile(any());
        verify(repository, never()).saveAll(any());
        verify(runRepository, times(2)).save(any(ReconciliationRunEntity.class)); // Saved as IN_PROGRESS then FAILED
    }
}
