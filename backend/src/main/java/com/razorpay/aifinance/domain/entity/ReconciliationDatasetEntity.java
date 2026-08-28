package com.razorpay.aifinance.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reconciliation_datasets")
public class ReconciliationDatasetEntity {

    @Id
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
