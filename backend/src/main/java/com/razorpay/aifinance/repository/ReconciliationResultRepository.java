package com.razorpay.aifinance.repository;

import com.razorpay.aifinance.domain.entity.ReconciliationResultEntity;
import com.razorpay.aifinance.domain.entity.ReconciliationRunEntity;
import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResultEntity, String> {

    Page<ReconciliationResultEntity> findByRun(ReconciliationRunEntity run, Pageable pageable);

    Optional<ReconciliationResultEntity> findByRunAndPaymentId(ReconciliationRunEntity run, String paymentId);

    Page<ReconciliationResultEntity> findByRunAndOverallStatus(ReconciliationRunEntity run, ReconciliationStatus status, Pageable pageable);

    Page<ReconciliationResultEntity> findByRunAndExceptionType(ReconciliationRunEntity run, ExceptionType exceptionType, Pageable pageable);

    Page<ReconciliationResultEntity> findByRunAndOverallStatusAndExceptionType(ReconciliationRunEntity run, ReconciliationStatus status, ExceptionType exceptionType, Pageable pageable);

    long countByRun(ReconciliationRunEntity run);

    long countByRunAndOverallStatus(ReconciliationRunEntity run, ReconciliationStatus status);

    @Query("SELECT r.exceptionType, COUNT(r) FROM ReconciliationResultEntity r WHERE r.run = :run AND r.overallStatus = 'EXCEPTION' AND r.exceptionType IS NOT NULL GROUP BY r.exceptionType")
    List<Object[]> countExceptionsByType(ReconciliationRunEntity run);
}
