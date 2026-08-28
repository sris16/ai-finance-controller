package com.razorpay.aifinance.reconciliation.evaluation;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.ingestion.model.GroundTruthRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            boolean isOverallMatchCorrect = true;

            // Confusion Matrix and Overall Accuracy
            if (result.getOverallStatus() == ReconciliationStatus.MATCH && gt.getExpectedResult() == ExpectedResult.MATCH) {
                metrics.incrementTrueMatches();
            } else if (result.getOverallStatus() == ReconciliationStatus.MATCH && gt.getExpectedResult() == ExpectedResult.EXCEPTION) {
                metrics.incrementFalseMatches();
                isOverallMatchCorrect = false;
            } else if (result.getOverallStatus() == ReconciliationStatus.EXCEPTION && gt.getExpectedResult() == ExpectedResult.MATCH) {
                metrics.incrementFalseExceptions();
                isOverallMatchCorrect = false;
            } else if (result.getOverallStatus() == ReconciliationStatus.EXCEPTION && gt.getExpectedResult() == ExpectedResult.EXCEPTION) {
                metrics.incrementTrueExceptions();
            }

            if (isOverallMatchCorrect) {
                metrics.incrementCorrectClassifications();
            } else {
                metrics.incrementIncorrectClassifications();
                metrics.addMismatch(result.getPaymentId(), result.getExceptionType(), gt.getExceptionType());
            }

            // Exception Type Evaluation (only applies to records expected to be exceptions)
            if (gt.getExpectedResult() == ExpectedResult.EXCEPTION) {
                if (result.getOverallStatus() == ReconciliationStatus.EXCEPTION && result.getExceptionType() == gt.getExceptionType()) {
                    metrics.incrementCorrectExceptionTypeClassifications();
                } else {
                    metrics.incrementIncorrectExceptionTypeClassifications();
                    // We only add mismatch if we didn't already add it above
                    if (isOverallMatchCorrect) {
                         metrics.addMismatch(result.getPaymentId(), result.getExceptionType(), gt.getExceptionType());
                    }
                }
            }
        }

        if (metrics.getTotalRecords() > 0) {
            metrics.setAccuracy((double) metrics.getCorrectClassifications() / metrics.getTotalRecords());
        } else {
            metrics.setAccuracy(0.0);
        }

        int totalExpectedExceptions = metrics.getTrueExceptions() + metrics.getFalseMatches();
        if (totalExpectedExceptions > 0) {
            metrics.setExceptionTypeAccuracy((double) metrics.getCorrectExceptionTypeClassifications() / totalExpectedExceptions);
        } else {
            metrics.setExceptionTypeAccuracy(0.0);
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

        // Exception specific metrics
        private int correctExceptionTypeClassifications = 0;
        private int incorrectExceptionTypeClassifications = 0;
        private double exceptionTypeAccuracy = 0.0;

        // Confusion Matrix style counts
        private int trueMatches = 0; // Expected MATCH -> Actual MATCH
        private int falseExceptions = 0; // Expected MATCH -> Actual EXCEPTION
        private int falseMatches = 0; // Expected EXCEPTION -> Actual MATCH
        private int trueExceptions = 0; // Expected EXCEPTION -> Actual EXCEPTION

        public void incrementMatchCount() { matchCount++; }
        public void incrementExceptionCount() { exceptionCount++; }
        public void incrementCorrectClassifications() { correctClassifications++; }
        public void incrementIncorrectClassifications() { incorrectClassifications++; }

        public void incrementCorrectExceptionTypeClassifications() { correctExceptionTypeClassifications++; }
        public void incrementIncorrectExceptionTypeClassifications() { incorrectExceptionTypeClassifications++; }

        public void incrementTrueMatches() { trueMatches++; }
        public void incrementFalseExceptions() { falseExceptions++; }
        public void incrementFalseMatches() { falseMatches++; }
        public void incrementTrueExceptions() { trueExceptions++; }

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

        public int getCorrectExceptionTypeClassifications() { return correctExceptionTypeClassifications; }
        public int getIncorrectExceptionTypeClassifications() { return incorrectExceptionTypeClassifications; }
        public double getExceptionTypeAccuracy() { return exceptionTypeAccuracy; }
        public void setExceptionTypeAccuracy(double exceptionTypeAccuracy) { this.exceptionTypeAccuracy = exceptionTypeAccuracy; }

        public int getTrueMatches() { return trueMatches; }
        public int getFalseExceptions() { return falseExceptions; }
        public int getFalseMatches() { return falseMatches; }
        public int getTrueExceptions() { return trueExceptions; }
    }
}
