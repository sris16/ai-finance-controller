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
@SuppressWarnings("null")
public class ReconciliationReporter {

    public ReconciliationReport generateReport(int totalRecords, int matchedRecords, int exceptionRecords, Map<ExceptionType, Integer> exceptionBreakdown) {
        ReconciliationReport report = new ReconciliationReport();

        if (totalRecords == 0) {
            return report; // Defaults handle 0 appropriately
        }

        report.setTotalRecords(totalRecords);
        report.setMatchedRecords(matchedRecords);
        report.setExceptionRecords(exceptionRecords);

        Map<ExceptionType, Integer> breakdown = new EnumMap<>(ExceptionType.class);

        // Initialize map with all exception types for completeness, except NONE
        for (ExceptionType type : ExceptionType.values()) {
            if (type != ExceptionType.NONE) {
                breakdown.put(type, exceptionBreakdown.getOrDefault(type, 0));
            }
        }

        report.setExceptionBreakdown(breakdown);

        // Calculate rates using BigDecimal for precise financial percentages
        BigDecimal total = BigDecimal.valueOf(report.getTotalRecords());
        BigDecimal matches = BigDecimal.valueOf(matchedRecords);
        BigDecimal ex = BigDecimal.valueOf(exceptionRecords);
        BigDecimal hundred = BigDecimal.valueOf(100);

        // Calculate (matched / total) * 100 with 2 decimal places
        BigDecimal matchRate = matches.divide(total, 4, RoundingMode.HALF_UP).multiply(hundred).setScale(2, RoundingMode.HALF_UP);
        BigDecimal exRate = ex.divide(total, 4, RoundingMode.HALF_UP).multiply(hundred).setScale(2, RoundingMode.HALF_UP);

        report.setMatchRate(matchRate);
        report.setExceptionRate(exRate);

        return report;
    }
}
