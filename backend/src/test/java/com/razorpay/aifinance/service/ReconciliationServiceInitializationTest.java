package com.razorpay.aifinance.service;

import com.razorpay.aifinance.ingestion.model.FinancialDataset;
import com.razorpay.aifinance.ingestion.service.CsvIngestionService;
import com.razorpay.aifinance.reconciliation.engine.DeterministicReconciliationEngine;
import com.razorpay.aifinance.reconciliation.reporting.ReconciliationReporter;
import com.razorpay.aifinance.repository.ReconciliationResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReconciliationServiceInitializationTest {

    @Mock
    private CsvIngestionService csvIngestionService;
    @Mock
    private DeterministicReconciliationEngine engine;
    @Mock
    private ReconciliationReporter reporter;
    @Mock
    private ReconciliationResultRepository repository;

    private ReconciliationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReconciliationService(csvIngestionService, engine, reporter, repository);
    }

    @Test
    void testInitializeDataset_EmptyDatabase() {
        when(repository.count()).thenReturn(0L);
        when(csvIngestionService.loadDataset(any())).thenReturn(new FinancialDataset());
        when(engine.reconcile(any())).thenReturn(List.of(new com.razorpay.aifinance.domain.model.ReconciliationResult()));

        service.initializeDataset();

        verify(csvIngestionService, times(1)).loadDataset(any());
        verify(engine, times(1)).reconcile(any());
        verify(repository, times(1)).saveAll(any());
    }

    @Test
    void testInitializeDataset_ExistingDatabaseDoesNotDuplicate() {
        when(repository.count()).thenReturn(100L); // Data already exists

        service.initializeDataset();

        // Engine and ingestion should NOT be triggered
        verify(csvIngestionService, never()).loadDataset(anyString());
        verify(engine, never()).reconcile(any());
        verify(repository, never()).saveAll(any());
    }
}
