package com.razorpay.aifinance.controller;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.reconciliation.reporting.ReconciliationReport;
import com.razorpay.aifinance.service.ReconciliationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final com.razorpay.aifinance.ai.service.AiIntegrationService aiIntegrationService;

    public ReconciliationController(ReconciliationService reconciliationService, com.razorpay.aifinance.ai.service.AiIntegrationService aiIntegrationService) {
        this.reconciliationService = reconciliationService;
        this.aiIntegrationService = aiIntegrationService;
    }

    @GetMapping("/report")
    public ReconciliationReport getReport() {
        return reconciliationService.getReport();
    }

    @GetMapping("/results")
    public Page<ReconciliationResult> getResults(
            @RequestParam(required = false) ReconciliationStatus status,
            @RequestParam(required = false) ExceptionType exceptionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        return reconciliationService.getAllResults(status, exceptionType, pageable);
    }

    @GetMapping("/results/{paymentId}")
    public ReconciliationResult getResultByPaymentId(@PathVariable String paymentId) {
        return reconciliationService.getResultByPaymentId(paymentId);
    }

    @GetMapping("/exceptions")
    public Page<ReconciliationResult> getExceptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) throw new IllegalArgumentException("Page index must not be less than zero");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");

        return reconciliationService.getAllResults(ReconciliationStatus.EXCEPTION, null, PageRequest.of(page, size));
    }

    @GetMapping("/exceptions/{exceptionType}")
    public Page<ReconciliationResult> getExceptionsByType(
            @PathVariable ExceptionType exceptionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) throw new IllegalArgumentException("Page index must not be less than zero");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");

        return reconciliationService.getAllResults(ReconciliationStatus.EXCEPTION, exceptionType, PageRequest.of(page, size));
    }

    @GetMapping("/results/{paymentId}/explanation")
    public com.razorpay.aifinance.ai.dto.ReconciliationExplanationResponse getExplanation(@PathVariable String paymentId) {
        ReconciliationResult result = reconciliationService.getResultByPaymentId(paymentId);
        return aiIntegrationService.generateExplanation(result);
    }
}
