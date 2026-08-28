package com.razorpay.aifinance.repository;

import com.razorpay.aifinance.domain.entity.ReconciliationResultEntity;
import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResultEntity, String> {

    List<ReconciliationResultEntity> findByOverallStatus(ReconciliationStatus status);
    
    List<ReconciliationResultEntity> findByExceptionType(ExceptionType exceptionType);
    
    long countByOverallStatus(ReconciliationStatus status);

}
