# AI Finance Controller - Synthetic Datasets

This directory contains the synthetic datasets and their definitions used for the Razorpay AI Buildathon 2026 - Track 04.

## Data Schemas

The reconciliation engine processes financial records from four distinct sources, plus a ground truth file for measuring accuracy and performance. All schemas are defined in `schemas/`.

### 1. Orders
Represents the merchant's storefront sales records.
- **File**: `orders.csv`
- **Schema**: `schemas/orders.schema.json`
- **Fields**: `order_id`, `customer_id`, `order_date`, `amount`, `currency`, `status`

### 2. Payments
Represents the captured payment records from the Payment Gateway (e.g., Razorpay).
- **File**: `payments.csv`
- **Schema**: `schemas/payments.schema.json`
- **Fields**: `payment_id`, `order_id`, `payment_date`, `amount`, `payment_method`, `status`

### 3. Settlements
Represents the payout settlements from the payment gateway breaking down fees and net amounts.
- **File**: `settlements.csv`
- **Schema**: `schemas/settlements.schema.json`
- **Fields**: `settlement_id`, `payment_id`, `settlement_date`, `gross_amount`, `fee`, `net_amount`, `status`

### 4. Bank Transactions
Represents the actual funds credited or debited to the merchant's bank account.
- **File**: `bank_transactions.csv`
- **Schema**: `schemas/bank_transactions.schema.json`
- **Fields**: `transaction_id`, `payment_id`, `transaction_date`, `amount`, `transaction_type`, `status`

### 5. Ground Truth
Provides the expected reconciliation result and specific exception types used to benchmark the deterministic engine and AI reasoning agent.
- **File**: `ground_truth.csv`
- **Schema**: `schemas/ground_truth.schema.json`
- **Fields**: `payment_id`, `expected_result`, `exception_type`

## Relationships
- `orders.order_id` -> `payments.order_id`
- `payments.payment_id` -> `settlements.payment_id`
- `payments.payment_id` -> `bank_transactions.payment_id`

## Allowed Exception Types
- `NONE`
- `AMOUNT_MISMATCH`
- `MISSING_SETTLEMENT`
- `DUPLICATE_TRANSACTION`
- `DATE_ANOMALY`
- `STATUS_MISMATCH`

## Expected Result Types
- `MATCH`
- `EXCEPTION`
