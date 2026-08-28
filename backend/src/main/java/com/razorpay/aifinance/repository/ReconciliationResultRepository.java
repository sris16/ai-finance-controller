package com.razorpay.aifinance.repository;

import com.razorpay.aifinance.domain.entity.ReconciliationResultEntity;
import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResultEntity, String> {

    Page<ReconciliationResultEntity> findByOverallStatus(ReconciliationStatus status, Pageable pageable);

    Page<ReconciliationResultEntity> findByExceptionType(ExceptionType exceptionType, Pageable pageable);

    Page<ReconciliationResultEntity> findByOverallStatusAndExceptionType(ReconciliationStatus status, ExceptionType exceptionType, Pageable pageable);

    long countByOverallStatus(ReconciliationStatus status);

    @Query("SELECT r.exceptionType, COUNT(r) FROM ReconciliationResultEntity r WHERE r.overallStatus = 'EXCEPTION' AND r.exceptionType IS NOT NULL GROUP BY r.exceptionType")
    List<Object[]> countExceptionsByType();
}
