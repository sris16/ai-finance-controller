package com.razorpay.aifinance.repository;

import com.razorpay.aifinance.domain.entity.ReconciliationRunEntity;
import com.razorpay.aifinance.domain.enums.RunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRunEntity, String> {
    
    Optional<ReconciliationRunEntity> findFirstByStatusOrderByExecutionTimeDesc(RunStatus status);
}
