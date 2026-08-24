# Project Scope Specification

## 1. Objective

The goal of the **AI Finance Controller** is to build a reliable, high-performance financial reconciliation agent that closes one finance-operations loop across multi-source synthetic datasets (Orders, Payments, Refunds, Settlements) while providing clear accuracy metrics and AI-assisted exception investigations.

---

## 2. In Scope

### 2.1 Multi-Source Financial Reconciliation
- Ingestion and matching of financial records across 4 primary domains:
  1. **Orders**: Merchant sales records (Order ID, Customer, Gross Amount, Tax, Currency, Status, Timestamp)
  2. **Payments**: Payment gateway captured records (Payment ID, Order ID, Captured Amount, Gateway Fee, GST, Timestamp)
  3. **Refunds**: Merchant/Gateway refund requests (Refund ID, Payment ID, Refund Amount, Reason, Status, Timestamp)
  4. **Settlements**: Bank payout settlements (Settlement ID, Payment/Refund IDs, Net Amount, Payout Date, Bank Ref)

### 2.2 Deterministic Reconciliation Engine
- Exact match algorithms on transaction identifiers.
- Tolerance-aware amount matching (accounting for rounded fees/taxes).
- Status cross-verification (e.g. Captured vs Paid, Refunded vs Settled).
- Metric tracking: Records Processed, Processing Time (ms), Throughput (records/sec), Reconciliation Accuracy (%), Matched Count, Exception Count.

### 2.3 AI-Powered Exception Agent
- Automated root-cause classification for exceptions (e.g., Uncaptured Payment, Gateway Fee Miscalculation, Missing Refund Settlement, Duplicate Charge, Currency Rounding Discrepancy).
- Natural language explanation generation summarizing evidence.
- Actionable recommendation for finance operations team.
- Immutable audit log entry creation.

### 2.4 User Interface Dashboard
- Executive KPI summary tiles.
- Transaction batch reconciliation trigger & file upload.
- Discrepancy explorer with filterable exception table.
- AI Investigation detail drawer showing root cause & advice.

---

## 3. Out of Scope

To maintain laser focus on the Razorpay Track 04 Buildathon requirements, the following areas are **explicitly out of scope**:

- **Live Banking & Payment Gateway API Integrations**: No live calls to real payment gateways or bank APIs; all operations run against synthetic financial record batches.
- **Actual Financial Money Movement**: No actual fund transfer, bank wire, or payout execution.
- **Production User Management & SSO**: Authentication infrastructure is scaffolded, but full production OAuth/SSO workflows are omitted.
- **Real Customer PII Handling**: Datasets contain purely synthetic, randomized identifiers and names.
- **Complex Multi-Currency Conversion Engines**: Live FX rate integration is out of scope; transactions focus on fixed currency pairs (INR).
