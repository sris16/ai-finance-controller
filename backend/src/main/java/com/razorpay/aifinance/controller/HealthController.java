package com.razorpay.aifinance.controller;

import com.razorpay.aifinance.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = new HealthResponse("UP", "ai-finance-controller-backend", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
