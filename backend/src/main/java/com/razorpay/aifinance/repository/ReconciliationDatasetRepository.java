package com.razorpay.aifinance.repository;

import com.razorpay.aifinance.domain.entity.ReconciliationDatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationDatasetRepository extends JpaRepository<ReconciliationDatasetEntity, String> {
    List<ReconciliationDatasetEntity> findAllByOrderByUploadedAtDesc();
}
