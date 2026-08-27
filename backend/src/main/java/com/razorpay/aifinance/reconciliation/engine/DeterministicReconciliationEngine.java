package com.razorpay.aifinance.reconciliation.engine;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.BankTransactionDetail;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.ingestion.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeterministicReconciliationEngine {

    public List<ReconciliationResult> reconcile(FinancialDataset dataset) {
        // 1. Build Lookup Maps for efficient querying O(1)
        Map<String, OrderRecord> orderMap = new HashMap<>();
        if (dataset.getOrders() != null) {
            for (OrderRecord order : dataset.getOrders()) {
                orderMap.put(order.getOrderId(), order);
            }
        }

        Map<String, List<SettlementRecord>> settlementMap = new HashMap<>();
        if (dataset.getSettlements() != null) {
            for (SettlementRecord sr : dataset.getSettlements()) {
                settlementMap.computeIfAbsent(sr.getPaymentId(), k -> new ArrayList<>()).add(sr);
            }
        }

        Map<String, List<BankTransactionRecord>> bankMap = new HashMap<>();
        if (dataset.getBankTransactions() != null) {
            for (BankTransactionRecord btr : dataset.getBankTransactions()) {
                bankMap.computeIfAbsent(btr.getPaymentId(), k -> new ArrayList<>()).add(btr);
            }
        }

        List<ReconciliationResult> results = new ArrayList<>();

        if (dataset.getPayments() == null) {
            return results;
        }

        // 2. Reconcile every payment
        for (PaymentRecord payment : dataset.getPayments()) {
            OrderRecord order = orderMap.get(payment.getOrderId());
            List<SettlementRecord> settlements = settlementMap.getOrDefault(payment.getPaymentId(), new ArrayList<>());
            List<BankTransactionRecord> bankTransactions = bankMap.getOrDefault(payment.getPaymentId(), new ArrayList<>());

            ReconciliationResult result = buildReconciliationResult(payment, order, settlements, bankTransactions);
            results.add(result);
        }

        return results;
    }

    private ReconciliationResult buildReconciliationResult(
            PaymentRecord payment,
            OrderRecord order,
            List<SettlementRecord> settlements,
            List<BankTransactionRecord> bankTransactions) {

        ReconciliationResult result = new ReconciliationResult();

        // Populate basic payment data
        result.setPaymentId(payment.getPaymentId());
        result.setPaymentAmount(payment.getAmount());
        result.setPaymentStatus(payment.getStatus());
        result.setPaymentDate(payment.getPaymentDate());
        
        // Populate Order Data
        if (order != null) {
            result.setOrderId(order.getOrderId());
            result.setOrderAmount(order.getAmount());
            result.setOrderStatus(order.getStatus());
        }

        // Populate Settlement Data
        if (!settlements.isEmpty()) {
            SettlementRecord sr = settlements.get(0); // Take first
            result.setSettlementPresent(true);
            result.setSettlementGrossAmount(sr.getGrossAmount());
            result.setSettlementFee(sr.getFee());
            result.setSettlementNetAmount(sr.getNetAmount());
            result.setSettlementStatus(sr.getStatus());
            result.setSettlementDate(sr.getSettlementDate());
        } else {
            result.setSettlementPresent(false);
        }

        // Populate Bank Transaction Data
        result.setBankTransactionCount(bankTransactions.size());
        List<BankTransactionDetail> details = new ArrayList<>();
        BigDecimal totalBankAmount = BigDecimal.ZERO;
        
        for (BankTransactionRecord btr : bankTransactions) {
            BankTransactionDetail detail = new BankTransactionDetail();
            detail.setAmount(btr.getAmount());
            detail.setStatus(btr.getStatus());
            detail.setDate(btr.getTransactionDate());
            details.add(detail);
            
            if (btr.getAmount() != null) {
                totalBankAmount = totalBankAmount.add(btr.getAmount());
            }
        }
        result.setBankTransactions(details);
        
        if (!bankTransactions.isEmpty()) {
            BankTransactionRecord first = bankTransactions.get(0);
            result.setBankTransactionAmount(totalBankAmount);
            result.setBankTransactionStatus(first.getStatus());
            result.setBankTransactionDate(first.getTransactionDate());
        }

        // Apply rules
        applyReconciliationRules(result, payment, order, settlements, bankTransactions);

        return result;
    }

    private void applyReconciliationRules(
            ReconciliationResult result,
            PaymentRecord payment,
            OrderRecord order,
            List<SettlementRecord> settlements,
            List<BankTransactionRecord> bankTransactions) {

        // Precedence 1: MISSING_SETTLEMENT
        if (settlements.isEmpty()) {
            result.setOverallStatus(ReconciliationStatus.EXCEPTION);
            result.setExceptionType(ExceptionType.MISSING_SETTLEMENT);
            result.setExplanation("No settlement record was found for payment " + payment.getPaymentId() + ".");
            result.setConfidenceScore(1.0);
            return;
        }

        // Precedence 2: DUPLICATE_TRANSACTION
        if (bankTransactions.size() > 1) {
            result.setOverallStatus(ReconciliationStatus.EXCEPTION);
            result.setExceptionType(ExceptionType.DUPLICATE_TRANSACTION);
            result.setExplanation("Multiple bank transactions (" + bankTransactions.size() + ") were found for payment " + payment.getPaymentId() + ".");
            result.setConfidenceScore(1.0);
            return;
        }
        
        if (bankTransactions.isEmpty()) {
            // Unspecified by precedence list, but let's assume it's also a serious error. 
            // In a real system we might have MISSING_BANK_TRANSACTION, but we'll stick to rules.
            // Let's rely on AMOUNT_MISMATCH if 0 amounts don't match, or just continue checks.
        }

        // Precedence 3: AMOUNT_MISMATCH
        SettlementRecord sr = settlements.get(0);
        BankTransactionRecord btr = bankTransactions.isEmpty() ? null : bankTransactions.get(0);

        boolean amountMismatch = false;
        String amountMismatchExplanation = "";

        if (order != null && payment.getAmount().compareTo(order.getAmount()) != 0) {
            amountMismatch = true;
            amountMismatchExplanation = "Payment amount (" + payment.getAmount() + ") does not match order amount (" + order.getAmount() + ").";
        } else if (payment.getAmount().compareTo(sr.getGrossAmount()) != 0) {
            amountMismatch = true;
            amountMismatchExplanation = "Payment amount (" + payment.getAmount() + ") does not match settlement gross amount (" + sr.getGrossAmount() + ").";
        } else if (btr != null && btr.getAmount().compareTo(sr.getNetAmount()) != 0) {
            amountMismatch = true;
            amountMismatchExplanation = "Bank transaction amount (" + btr.getAmount() + ") does not match settlement net amount (" + sr.getNetAmount() + ").";
        } else if (sr.getGrossAmount().subtract(sr.getFee()).compareTo(sr.getNetAmount()) != 0) {
            amountMismatch = true;
            amountMismatchExplanation = "Settlement gross amount minus fee does not equal net amount.";
        }

        if (amountMismatch) {
            result.setOverallStatus(ReconciliationStatus.EXCEPTION);
            result.setExceptionType(ExceptionType.AMOUNT_MISMATCH);
            result.setExplanation(amountMismatchExplanation);
            result.setConfidenceScore(1.0);
            return;
        }

        // Precedence 4: DATE_ANOMALY
        boolean dateAnomaly = false;
        String dateAnomalyExplanation = "";

        if (order != null && payment.getPaymentDate().isBefore(order.getOrderDate())) {
            dateAnomaly = true;
            dateAnomalyExplanation = "Payment timestamp occurs before order timestamp.";
        } else if (sr.getSettlementDate().isBefore(payment.getPaymentDate())) {
            dateAnomaly = true;
            dateAnomalyExplanation = "Settlement timestamp occurs before payment timestamp.";
        } else if (btr != null && btr.getTransactionDate().isBefore(sr.getSettlementDate())) {
            dateAnomaly = true;
            dateAnomalyExplanation = "Bank transaction timestamp occurs before settlement timestamp.";
        }

        if (dateAnomaly) {
            result.setOverallStatus(ReconciliationStatus.EXCEPTION);
            result.setExceptionType(ExceptionType.DATE_ANOMALY);
            result.setExplanation(dateAnomalyExplanation);
            result.setConfidenceScore(1.0);
            return;
        }

        // Precedence 5: STATUS_MISMATCH
        // Typical success flow status logic (based on standard e-commerce flows):
        // Order = PAID or DELIVERED, Payment = CAPTURED, Settlement = SETTLED, Bank = COMPLETED
        // We'll define a simple rule: if it's supposed to be successful, they shouldn't be FAILED/PENDING in subsequent steps.
        // Let's implement a direct mapping checking if they are the "SUCCESS" equivalent.
        boolean statusMismatch = false;
        String statusMismatchExplanation = "";

        if (!"CAPTURED".equalsIgnoreCase(payment.getStatus())) {
            statusMismatch = true;
            statusMismatchExplanation = "Payment status is not CAPTURED (" + payment.getStatus() + ").";
        } else if (order != null && !"PAID".equalsIgnoreCase(order.getStatus())) {
            statusMismatch = true;
            statusMismatchExplanation = "Order status is not PAID (" + order.getStatus() + ").";
        } else if (!"SETTLED".equalsIgnoreCase(sr.getStatus())) {
            statusMismatch = true;
            statusMismatchExplanation = "Settlement status is not SETTLED (" + sr.getStatus() + ").";
        } else if (btr != null && !"SUCCESS".equalsIgnoreCase(btr.getStatus())) {
            statusMismatch = true;
            statusMismatchExplanation = "Bank transaction status is not SUCCESS (" + btr.getStatus() + ").";
        }

        if (statusMismatch) {
            result.setOverallStatus(ReconciliationStatus.EXCEPTION);
            result.setExceptionType(ExceptionType.STATUS_MISMATCH);
            result.setExplanation(statusMismatchExplanation);
            result.setConfidenceScore(1.0);
            return;
        }

        // Default: MATCH
        result.setOverallStatus(ReconciliationStatus.MATCH);
        result.setExceptionType(ExceptionType.NONE);
        result.setExplanation("Order amount, payment amount, settlement amount, and bank credit are consistent.");
        result.setConfidenceScore(1.0); // Deterministic certainty
    }
}
