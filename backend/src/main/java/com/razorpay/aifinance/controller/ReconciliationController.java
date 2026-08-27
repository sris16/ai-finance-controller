package com.razorpay.aifinance.controller;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.reconciliation.reporting.ReconciliationReport;
import com.razorpay.aifinance.service.ReconciliationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/report")
    public ReconciliationReport getReport() {
        return reconciliationService.getReport();
    }

    @GetMapping("/results")
    public List<ReconciliationResult> getResults(
            @RequestParam(required = false) ReconciliationStatus status,
            @RequestParam(required = false) ExceptionType exceptionType) {
        return reconciliationService.getAllResults(status, exceptionType);
    }

    @GetMapping("/results/{paymentId}")
    public ReconciliationResult getResultByPaymentId(@PathVariable String paymentId) {
        return reconciliationService.getResultByPaymentId(paymentId);
    }

    @GetMapping("/exceptions")
    public List<ReconciliationResult> getExceptions() {
        return reconciliationService.getAllResults(ReconciliationStatus.EXCEPTION, null);
    }

    @GetMapping("/exceptions/{exceptionType}")
    public List<ReconciliationResult> getExceptionsByType(@PathVariable ExceptionType exceptionType) {
        return reconciliationService.getAllResults(ReconciliationStatus.EXCEPTION, exceptionType);
    }
}
