package com.razorpay.aifinance.reconciliation.reporting;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReconciliationReporter {

    public ReconciliationReport generateReport(List<ReconciliationResult> results) {
        ReconciliationReport report = new ReconciliationReport();
        
        if (results == null || results.isEmpty()) {
            return report; // Defaults handle 0 appropriately
        }

        // Sort by PaymentId to ensure deterministic reporting order
        List<ReconciliationResult> sortedResults = results.stream()
                .sorted(Comparator.comparing(ReconciliationResult::getPaymentId))
                .collect(Collectors.toList());

        report.setReconciliationResults(sortedResults);
        report.setTotalRecords(sortedResults.size());

        int matched = 0;
        int exceptions = 0;
        
        Map<ExceptionType, Integer> breakdown = new EnumMap<>(ExceptionType.class);

        // Initialize map with all exception types for completeness, except NONE
        for (ExceptionType type : ExceptionType.values()) {
            if (type != ExceptionType.NONE) {
                breakdown.put(type, 0);
            }
        }

        for (ReconciliationResult result : sortedResults) {
            if (result.getOverallStatus() == ReconciliationStatus.MATCH) {
                matched++;
            } else {
                exceptions++;
                ExceptionType type = result.getExceptionType();
                if (type != null && type != ExceptionType.NONE) {
                    breakdown.put(type, breakdown.getOrDefault(type, 0) + 1);
                }
            }
        }

        report.setMatchedRecords(matched);
        report.setExceptionRecords(exceptions);
        report.setExceptionBreakdown(breakdown);

        // Calculate rates using BigDecimal for precise financial percentages
        BigDecimal total = BigDecimal.valueOf(report.getTotalRecords());
        BigDecimal matches = BigDecimal.valueOf(matched);
        BigDecimal ex = BigDecimal.valueOf(exceptions);
        BigDecimal hundred = BigDecimal.valueOf(100);

        // Calculate (matched / total) * 100 with 2 decimal places
        BigDecimal matchRate = matches.divide(total, 4, RoundingMode.HALF_UP).multiply(hundred).setScale(2, RoundingMode.HALF_UP);
        BigDecimal exRate = ex.divide(total, 4, RoundingMode.HALF_UP).multiply(hundred).setScale(2, RoundingMode.HALF_UP);

        report.setMatchRate(matchRate);
        report.setExceptionRate(exRate);

        return report;
    }
}
