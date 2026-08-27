package com.razorpay.aifinance.reconciliation.engine;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import com.razorpay.aifinance.ingestion.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicReconciliationEngineTest {

    private DeterministicReconciliationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DeterministicReconciliationEngine();
    }

    private FinancialDataset createBaseDataset(String paymentId) {
        OrderRecord order = new OrderRecord();
        order.setOrderId("ord_1");
        order.setAmount(new BigDecimal("100.00"));
        order.setOrderDate(Instant.parse("2026-08-27T10:00:00Z"));
        order.setStatus("PAID");

        PaymentRecord payment = new PaymentRecord();
        payment.setPaymentId(paymentId);
        payment.setOrderId("ord_1");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentDate(Instant.parse("2026-08-27T10:05:00Z"));
        payment.setStatus("CAPTURED");

        SettlementRecord settlement = new SettlementRecord();
        settlement.setSettlementId("set_1");
        settlement.setPaymentId(paymentId);
        settlement.setGrossAmount(new BigDecimal("100.00"));
        settlement.setFee(new BigDecimal("2.00"));
        settlement.setNetAmount(new BigDecimal("98.00"));
        settlement.setSettlementDate(Instant.parse("2026-08-27T10:10:00Z"));
        settlement.setStatus("SETTLED");

        BankTransactionRecord bank = new BankTransactionRecord();
        bank.setTransactionId("bt_1");
        bank.setPaymentId(paymentId);
        bank.setAmount(new BigDecimal("98.00"));
        bank.setTransactionDate(Instant.parse("2026-08-27T10:15:00Z"));
        bank.setStatus("SUCCESS");

        FinancialDataset dataset = new FinancialDataset();
        dataset.setOrders(Collections.singletonList(order));
        dataset.setPayments(Collections.singletonList(payment));
        dataset.setSettlements(Collections.singletonList(settlement));
        dataset.setBankTransactions(Collections.singletonList(bank));
        
        return dataset;
    }

    @Test
    void testNormalMatch() {
        FinancialDataset ds = createBaseDataset("pay_match");
        List<ReconciliationResult> results = engine.reconcile(ds);
        
        assertEquals(1, results.size());
        ReconciliationResult res = results.get(0);
        assertEquals(ReconciliationStatus.MATCH, res.getOverallStatus());
        assertEquals(ExceptionType.NONE, res.getExceptionType());
    }

    @Test
    void testAmountMismatch() {
        FinancialDataset ds = createBaseDataset("pay_amt");
        ds.getBankTransactions().get(0).setAmount(new BigDecimal("95.00")); // Mismatch

        List<ReconciliationResult> results = engine.reconcile(ds);
        ReconciliationResult res = results.get(0);
        assertEquals(ReconciliationStatus.EXCEPTION, res.getOverallStatus());
        assertEquals(ExceptionType.AMOUNT_MISMATCH, res.getExceptionType());
    }

    @Test
    void testMissingSettlement() {
        FinancialDataset ds = createBaseDataset("pay_missing_set");
        ds.setSettlements(Collections.emptyList()); // Remove settlement

        List<ReconciliationResult> results = engine.reconcile(ds);
        ReconciliationResult res = results.get(0);
        assertEquals(ReconciliationStatus.EXCEPTION, res.getOverallStatus());
        assertEquals(ExceptionType.MISSING_SETTLEMENT, res.getExceptionType());
    }

    @Test
    void testDuplicateTransaction() {
        FinancialDataset ds = createBaseDataset("pay_dup");
        BankTransactionRecord extraBank = new BankTransactionRecord();
        extraBank.setTransactionId("bt_2");
        extraBank.setPaymentId("pay_dup");
        extraBank.setAmount(new BigDecimal("98.00"));
        extraBank.setTransactionDate(Instant.parse("2026-08-27T10:16:00Z"));
        extraBank.setStatus("SUCCESS");

        ds.setBankTransactions(Arrays.asList(ds.getBankTransactions().get(0), extraBank));

        List<ReconciliationResult> results = engine.reconcile(ds);
        ReconciliationResult res = results.get(0);
        assertEquals(ReconciliationStatus.EXCEPTION, res.getOverallStatus());
        assertEquals(ExceptionType.DUPLICATE_TRANSACTION, res.getExceptionType());
        assertEquals(2, res.getBankTransactionCount());
        assertEquals(2, res.getBankTransactions().size());
    }

    @Test
    void testDateAnomaly() {
        FinancialDataset ds = createBaseDataset("pay_date");
        // Set settlement date before payment date
        ds.getSettlements().get(0).setSettlementDate(Instant.parse("2026-08-27T10:01:00Z"));

        List<ReconciliationResult> results = engine.reconcile(ds);
        ReconciliationResult res = results.get(0);
        assertEquals(ReconciliationStatus.EXCEPTION, res.getOverallStatus());
        assertEquals(ExceptionType.DATE_ANOMALY, res.getExceptionType());
    }

    @Test
    void testStatusMismatch() {
        FinancialDataset ds = createBaseDataset("pay_status");
        ds.getPayments().get(0).setStatus("FAILED"); // But settlement exists

        List<ReconciliationResult> results = engine.reconcile(ds);
        ReconciliationResult res = results.get(0);
        assertEquals(ReconciliationStatus.EXCEPTION, res.getOverallStatus());
        assertEquals(ExceptionType.STATUS_MISMATCH, res.getExceptionType());
    }
}
