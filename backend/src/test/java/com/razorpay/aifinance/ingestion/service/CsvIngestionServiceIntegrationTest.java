package com.razorpay.aifinance.ingestion.service;

import com.razorpay.aifinance.ingestion.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
class CsvIngestionServiceIntegrationTest {

    private CsvIngestionService service;

    @BeforeEach
    void setUp() {
        service = new CsvIngestionService();
    }

    @Test
    void testLoadRealDataset() {
        // The data directory is located one level up from the backend directory
        String datasetPath = "../data";
        
        FinancialDataset dataset = service.loadDataset(datasetPath);

        assertNotNull(dataset);
        
        // 1. Verify exact counts based on requirements
        assertEquals(100, dataset.getOrders().size(), "Orders count should be 100");
        assertEquals(100, dataset.getPayments().size(), "Payments count should be 100");
        assertEquals(96, dataset.getSettlements().size(), "Settlements count should be 96 (missing 4 intentionally)");
        assertEquals(100, dataset.getBankTransactions().size(), "Bank Transactions count should be 100");
        assertEquals(100, dataset.getGroundTruths().size(), "Ground Truth count should be 100");

        // 2. Verify relationships (Structural Validation)
        Set<String> orderIds = dataset.getOrders().stream().map(OrderRecord::getOrderId).collect(Collectors.toSet());
        Set<String> paymentIds = dataset.getPayments().stream().map(PaymentRecord::getPaymentId).collect(Collectors.toSet());

        for (PaymentRecord payment : dataset.getPayments()) {
            assertTrue(orderIds.contains(payment.getOrderId()), "Payment refers to unknown order: " + payment.getOrderId());
        }

        for (SettlementRecord settlement : dataset.getSettlements()) {
            assertTrue(paymentIds.contains(settlement.getPaymentId()), "Settlement refers to unknown payment: " + settlement.getPaymentId());
        }

        for (BankTransactionRecord bankTx : dataset.getBankTransactions()) {
            assertTrue(paymentIds.contains(bankTx.getPaymentId()), "Bank transaction refers to unknown payment: " + bankTx.getPaymentId());
        }

        for (GroundTruthRecord gt : dataset.getGroundTruths()) {
            assertTrue(paymentIds.contains(gt.getPaymentId()), "Ground truth refers to unknown payment: " + gt.getPaymentId());
        }

        // 3. Ensure duplicate bank transactions exist (meaning paymentIds in bank transactions are not all unique)
        Set<String> uniqueBankTxPaymentIds = dataset.getBankTransactions().stream()
                .map(BankTransactionRecord::getPaymentId)
                .collect(Collectors.toSet());
        
        // There are 100 bank transactions, but 4 are duplicates for the same payment ID.
        // Therefore, there should be fewer than 100 unique payment IDs.
        assertTrue(uniqueBankTxPaymentIds.size() < 100, "There should be duplicate bank transactions (multiple rows for the same payment ID).");
    }
}
