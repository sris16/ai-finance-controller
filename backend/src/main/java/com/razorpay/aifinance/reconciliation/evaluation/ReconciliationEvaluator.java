package com.razorpay.aifinance.reconciliation.evaluation;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.ingestion.model.GroundTruthRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReconciliationEvaluator {

    public EvaluationMetrics evaluate(List<ReconciliationResult> engineResults, List<GroundTruthRecord> groundTruths) {
        Map<String, GroundTruthRecord> gtMap = new HashMap<>();
        if (groundTruths != null) {
            for (GroundTruthRecord gt : groundTruths) {
                gtMap.put(gt.getPaymentId(), gt);
            }
        }

        EvaluationMetrics metrics = new EvaluationMetrics();
        metrics.setTotalRecords(engineResults.size());

        for (ReconciliationResult result : engineResults) {
            GroundTruthRecord gt = gtMap.get(result.getPaymentId());
            if (gt == null) {
                continue; // Skip if no ground truth for this payment
            }

            result.setExpectedResult(gt.getExpectedResult());

            if (result.getOverallStatus() == ReconciliationStatus.MATCH) {
                metrics.incrementMatchCount();
            } else {
                metrics.incrementExceptionCount();
            }

            boolean isCorrect = true;

            // Check overall MATCH / EXCEPTION equality
            if ((result.getOverallStatus() == ReconciliationStatus.MATCH && gt.getExpectedResult() != ExpectedResult.MATCH) ||
                (result.getOverallStatus() == ReconciliationStatus.EXCEPTION && gt.getExpectedResult() != ExpectedResult.EXCEPTION)) {
                isCorrect = false;
            }

            // Check specific exception type if it's an exception
            if (result.getOverallStatus() == ReconciliationStatus.EXCEPTION) {
                if (result.getExceptionType() != gt.getExceptionType()) {
                    isCorrect = false;
                }
            }

            if (isCorrect) {
                metrics.incrementCorrectClassifications();
            } else {
                metrics.incrementIncorrectClassifications();
                metrics.addMismatch(result.getPaymentId(), result.getExceptionType(), gt.getExceptionType());
            }
        }

        if (metrics.getTotalRecords() > 0) {
            metrics.setAccuracy((double) metrics.getCorrectClassifications() / metrics.getTotalRecords());
        } else {
            metrics.setAccuracy(0.0);
        }

        return metrics;
    }

    public static class EvaluationMetrics {
        private int totalRecords = 0;
        private int correctClassifications = 0;
        private int incorrectClassifications = 0;
        private int matchCount = 0;
        private int exceptionCount = 0;
        private double accuracy = 0.0;
        private Map<String, String> discrepancies = new HashMap<>();

        public void incrementMatchCount() { matchCount++; }
        public void incrementExceptionCount() { exceptionCount++; }
        public void incrementCorrectClassifications() { correctClassifications++; }
        public void incrementIncorrectClassifications() { incorrectClassifications++; }

        public void addMismatch(String paymentId, ExceptionType engineException, ExceptionType gtException) {
            String engineStr = engineException != null ? engineException.name() : "MATCH";
            String gtStr = gtException != null ? gtException.name() : "MATCH";
            discrepancies.put(paymentId, "Engine=" + engineStr + " vs GT=" + gtStr);
        }

        // Getters and Setters
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public int getCorrectClassifications() { return correctClassifications; }
        public int getIncorrectClassifications() { return incorrectClassifications; }
        public int getMatchCount() { return matchCount; }
        public int getExceptionCount() { return exceptionCount; }
        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
        public Map<String, String> getDiscrepancies() { return discrepancies; }
    }
}
