package com.razorpay.aifinance.domain.entity;

import com.razorpay.aifinance.domain.enums.RunStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "reconciliation_runs", indexes = {
    @Index(name = "idx_run_execution_time", columnList = "execution_time"),
    @Index(name = "idx_run_status", columnList = "status"),
    @Index(name = "idx_run_dataset_id", columnList = "dataset_id")
})
public class ReconciliationRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "execution_time", nullable = false)
    private Instant executionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RunStatus status;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "dataset_id")
    private String datasetId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Instant getExecutionTime() { return executionTime; }
    public void setExecutionTime(Instant executionTime) { this.executionTime = executionTime; }
    
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }
    
    public Integer getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }

    public String getDatasetId() { return datasetId; }
    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
}
