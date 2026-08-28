package com.razorpay.aifinance.dto;

import java.time.Instant;

public class DatasetResponse {
    private String id;
    private String name;
    private Instant uploadedAt;

    public DatasetResponse(String id, String name, Instant uploadedAt) {
        this.id = id;
        this.name = name;
        this.uploadedAt = uploadedAt;
    }

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
