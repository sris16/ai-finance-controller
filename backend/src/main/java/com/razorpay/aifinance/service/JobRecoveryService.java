package com.razorpay.aifinance.service;

import com.razorpay.aifinance.domain.entity.ReconciliationRunEntity;
import com.razorpay.aifinance.domain.enums.RunStatus;
import com.razorpay.aifinance.repository.ReconciliationRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Recovers orphaned IN_PROGRESS runs on application startup.
 * Since this application runs in a single JVM with a bounded @Async executor
 * (no distributed workers, no job resumption), any IN_PROGRESS run present
 * in the database at startup was necessarily orphaned by a previous system crash or restart.
 */
@Service
public class JobRecoveryService {

    private static final Logger logger = LoggerFactory.getLogger(JobRecoveryService.class);

    private final ReconciliationRunRepository runRepository;

    public JobRecoveryService(ReconciliationRunRepository runRepository) {
        this.runRepository = runRepository;
    }

    /**
     * Executes immediately after the application has fully started up,
     * but before it can begin processing new batch jobs, effectively
     * acting as a boot-time barrier to recover corrupted database state.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recoverOrphanedRuns() {
        logger.info("Scanning for orphaned IN_PROGRESS reconciliation runs...");

        List<ReconciliationRunEntity> orphanedRuns = runRepository.findByStatus(RunStatus.IN_PROGRESS);

        if (orphanedRuns.isEmpty()) {
            logger.info("No orphaned runs detected.");
            return;
        }

        for (ReconciliationRunEntity run : orphanedRuns) {
            logger.warn("Recovering orphaned run [{}]. Marking as FAILED.", run.getId());
            run.setStatus(RunStatus.FAILED);
            // We set totalRecords to 0 if null, but it's okay to just mark as failed.
            if (run.getTotalRecords() == null) {
                run.setTotalRecords(0);
            }
        }

        runRepository.saveAll(orphanedRuns);
        logger.info("Successfully recovered {} orphaned run(s). Run orphaned due to system restart.", orphanedRuns.size());
    }
}
