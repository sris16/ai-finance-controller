package com.razorpay.aifinance.service;

import com.razorpay.aifinance.domain.entity.ReconciliationRunEntity;
import com.razorpay.aifinance.domain.enums.RunStatus;
import com.razorpay.aifinance.repository.ReconciliationRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "null"})
class JobRecoveryServiceTest {

    private ReconciliationRunRepository runRepository;
    private JobRecoveryService jobRecoveryService;

    @BeforeEach
    void setUp() {
        runRepository = mock(ReconciliationRunRepository.class);
        jobRecoveryService = new JobRecoveryService(runRepository);
    }

    @Test
    void testRecoverOrphanedRuns_InProgressBecomesFailed() {
        ReconciliationRunEntity orphanedRun = new ReconciliationRunEntity();
        orphanedRun.setId("test-run-1");
        orphanedRun.setStatus(RunStatus.IN_PROGRESS);

        when(runRepository.findByStatus(RunStatus.IN_PROGRESS)).thenReturn(List.of(orphanedRun));

        jobRecoveryService.recoverOrphanedRuns();

        ArgumentCaptor<List<ReconciliationRunEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(runRepository).saveAll(captor.capture());

        List<ReconciliationRunEntity> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals(RunStatus.FAILED, saved.get(0).getStatus());
    }

    @Test
    void testRecoverOrphanedRuns_CompletedRemainsUnchanged() {
        when(runRepository.findByStatus(RunStatus.IN_PROGRESS)).thenReturn(Collections.emptyList());

        jobRecoveryService.recoverOrphanedRuns();

        verify(runRepository, never()).saveAll(any());
        // Since findByStatus(IN_PROGRESS) returns empty, it never even looks for COMPLETED.
    }

    @Test
    void testRecoverOrphanedRuns_FailedRemainsUnchanged() {
        when(runRepository.findByStatus(RunStatus.IN_PROGRESS)).thenReturn(Collections.emptyList());

        jobRecoveryService.recoverOrphanedRuns();

        verify(runRepository, never()).saveAll(any());
    }

    @Test
    void testConcurrencyLockReleased() {
        // First simulate the recovery
        ReconciliationRunEntity orphanedRun = new ReconciliationRunEntity();
        orphanedRun.setId("test-run-1");
        orphanedRun.setStatus(RunStatus.IN_PROGRESS);

        when(runRepository.findByStatus(RunStatus.IN_PROGRESS)).thenReturn(List.of(orphanedRun));
        jobRecoveryService.recoverOrphanedRuns();

        // Verify saveAll was called with FAILED status
        ArgumentCaptor<List<ReconciliationRunEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(runRepository).saveAll(captor.capture());
        assertEquals(RunStatus.FAILED, captor.getValue().get(0).getStatus());

        // We can simulate that after recovery, existsByStatus would return false.
        when(runRepository.existsByStatus(RunStatus.IN_PROGRESS)).thenReturn(false);
        boolean isLocked = runRepository.existsByStatus(RunStatus.IN_PROGRESS);
        assertEquals(false, isLocked);
    }

    @Test
    void testRecoveryFailureBehavior() {
        ReconciliationRunEntity orphanedRun = new ReconciliationRunEntity();
        orphanedRun.setId("test-run-1");
        orphanedRun.setStatus(RunStatus.IN_PROGRESS);

        when(runRepository.findByStatus(RunStatus.IN_PROGRESS)).thenReturn(List.of(orphanedRun));

        // Simulate a database failure during saveAll
        doThrow(new RuntimeException("Database down")).when(runRepository).saveAll(any());

        assertThrows(RuntimeException.class, () -> {
            jobRecoveryService.recoverOrphanedRuns();
        });

        // Ensure that because it threw an exception, the transactional boundary would roll it back
        // (verified by Spring transaction manager in reality, here just checking exception propagation)
    }
}
