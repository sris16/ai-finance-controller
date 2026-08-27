package com.razorpay.aifinance.ingestion.model;

import java.util.List;

public class FinancialDataset {
    private List<OrderRecord> orders;
    private List<PaymentRecord> payments;
    private List<SettlementRecord> settlements;
    private List<BankTransactionRecord> bankTransactions;
    private List<GroundTruthRecord> groundTruths;

    public FinancialDataset() {}

    public List<OrderRecord> getOrders() { return orders; }
    public void setOrders(List<OrderRecord> orders) { this.orders = orders; }

    public List<PaymentRecord> getPayments() { return payments; }
    public void setPayments(List<PaymentRecord> payments) { this.payments = payments; }

    public List<SettlementRecord> getSettlements() { return settlements; }
    public void setSettlements(List<SettlementRecord> settlements) { this.settlements = settlements; }

    public List<BankTransactionRecord> getBankTransactions() { return bankTransactions; }
    public void setBankTransactions(List<BankTransactionRecord> bankTransactions) { this.bankTransactions = bankTransactions; }

    public List<GroundTruthRecord> getGroundTruths() { return groundTruths; }
    public void setGroundTruths(List<GroundTruthRecord> groundTruths) { this.groundTruths = groundTruths; }
}
