# AI Finance Controller — API Documentation

## 1. API Overview
The Reconciliation REST API provides a programmatic interface to the **Deterministic Reconciliation Engine**. It allows users to ingest synthetic financial datasets and retrieve structured reports, matched records, and classified exceptions. 

## 2. Base URL
When running via Docker Compose:
- **Backend API Base URL**: `http://localhost:8080`

## 3. Architecture Flow
The operational REST API follows a strict architectural pipeline designed for deterministic execution:
1. **Client**: Submits an HTTP GET request.
2. **REST Controller** (`ReconciliationController`): Handles routing, path variables, and payload structuring.
3. **Reconciliation Service** (`ReconciliationService`): Initializes once at application startup. Proxies retrieval operations.
4. **CSV Ingestion Service** (`CsvIngestionService`): Parses the `orders.csv`, `payments.csv`, `settlements.csv`, and `bank_transactions.csv` synthetic files.
5. **Deterministic Reconciliation Engine** (`DeterministicReconciliationEngine`): Executes math and rule-based matching across all parsed records.
6. **Reconciliation Reporter** (`ReconciliationReporter`): Aggregates results into high-level metrics.
7. **JSON Response**: Returns the processed models securely.

## 4. Ground Truth Separation
**Important Note:** The operational API exclusively utilizes the deterministic reconciliation engine. The `ground_truth.csv` file provided in the dataset serves **only** as benchmark evaluation data. It is never invoked or referenced by the operational reconciliation flow to determine matches or exceptions. 

---

## 5. Endpoints

### 5.1 Health Endpoint
Checks the operational status of the backend API.
- **HTTP Method**: `GET`
- **URL**: `/api/health`
- **Parameters**: None
- **Successful Status**: `200 OK`
- **Response Structure**:
```json
{
  "status": "UP",
  "service": "ai-finance-controller-backend",
  "version": "1.0.0"
}
```

### 5.2 Report Endpoint
Retrieves the aggregated high-level reconciliation metrics and exception breakdown.
- **HTTP Method**: `GET`
- **URL**: `/api/reconciliation/report`
- **Parameters**: None
- **Successful Status**: `200 OK`
- **Response Structure** (`ReconciliationReport`):
```json
{
  "totalRecords": 100,
  "matchedRecords": 80,
  "exceptionRecords": 20,
  "matchRate": 80.00,
  "exceptionRate": 20.00,
  "exceptionBreakdown": {
    "AMOUNT_MISMATCH": 4,
    "MISSING_SETTLEMENT": 4,
    "DUPLICATE_TRANSACTION": 4,
    "DATE_ANOMALY": 4,
    "STATUS_MISMATCH": 4
  }
}
```

### 5.3 Results Endpoint
Retrieves all computed reconciliation results. Supports dynamic filtering via query parameters.
- **HTTP Method**: `GET`
- **URL**: `/api/reconciliation/results`
- **Query Parameters (Optional)**: 
  - `status` (Enum: `MATCH`, `EXCEPTION`)
  - `exceptionType` (Enum: `NONE`, `AMOUNT_MISMATCH`, `MISSING_SETTLEMENT`, `DUPLICATE_TRANSACTION`, `DATE_ANOMALY`, `STATUS_MISMATCH`)
- **Successful Status**: `200 OK`
- **Error Status**: `400 Bad Request` (If an invalid enum is provided)
- **Response Structure**: Array of `ReconciliationResult` objects.

### 5.4 Individual Result Endpoint
Retrieves a specific reconciliation result by its unique Payment ID.
- **HTTP Method**: `GET`
- **URL**: `/api/reconciliation/results/{paymentId}`
- **Path Parameters**: `paymentId` (String)
- **Successful Status**: `200 OK`
- **Error Status**: `404 Not Found` (If the payment ID does not exist)
- **Response Example (Duplicate Transaction)**:
```json
{
  "paymentId": "PAY0004",
  "orderId": "ORD0004",
  "orderAmount": 5748.95,
  "orderStatus": "PAID",
  "paymentAmount": 5748.95,
  "paymentStatus": "CAPTURED",
  "paymentDate": "2026-01-07T17:15:00Z",
  "settlementPresent": true,
  "settlementGrossAmount": 5748.95,
  "settlementFee": 114.98,
  "settlementNetAmount": 5633.97,
  "settlementStatus": "SETTLED",
  "settlementDate": "2026-01-09T17:15:00Z",
  "bankTransactionCount": 2,
  "bankTransactionAmount": 11267.94,
  "bankTransactionStatus": "SUCCESS",
  "bankTransactionDate": "2026-01-10T02:15:00Z",
  "bankTransactions": [
    {
      "amount": 5633.97,
      "status": "SUCCESS",
      "date": "2026-01-10T02:15:00Z"
    },
    {
      "amount": 5633.97,
      "status": "SUCCESS",
      "date": "2026-01-10T02:20:00Z"
    }
  ],
  "overallStatus": "EXCEPTION",
  "exceptionType": "DUPLICATE_TRANSACTION",
  "explanation": "Multiple bank transactions (2) were found for payment PAY0004.",
  "confidenceScore": 1.0
}
```

### 5.5 Exceptions Endpoint
Retrieves only the reconciliation results classified as an exception.
- **HTTP Method**: `GET`
- **URL**: `/api/reconciliation/exceptions`
- **Parameters**: None
- **Successful Status**: `200 OK`
- **Response Structure**: Array of `ReconciliationResult` objects where `overallStatus` is `EXCEPTION`.

### 5.6 Exception-Type Endpoint
Retrieves exceptions explicitly matching the requested exception type.
- **HTTP Method**: `GET`
- **URL**: `/api/reconciliation/exceptions/{exceptionType}`
- **Path Parameters**: `exceptionType` (Valid enum string)
- **Successful Status**: `200 OK`
- **Error Status**: `400 Bad Request` (If the enum is invalid)
- **Response Structure**: Array of `ReconciliationResult` objects.

---

## 6. Response Models

### 6.1 ReconciliationResult
The core output of the deterministic engine.
* **`paymentId`**, **`orderId`**: Relational strings.
* **`orderAmount`**, **`paymentAmount`**: `BigDecimal` standard monetary values.
* **`orderStatus`**, **`paymentStatus`**: String domain flags.
* **`paymentDate`**: ISO-8601 Timestamp (e.g. `2026-01-07T17:15:00Z`).
* **`settlementPresent`**: Boolean flag. If `false`, subsequent settlement fields (`settlementGrossAmount`, etc.) will cleanly serialize as `null`.
* **`bankTransactionCount`**: Count of recorded payouts.
* **`bankTransactions`**: Array of underlying sub-transactions. Duplicates are strictly preserved as independent entries.
* **`overallStatus`**: `MATCH` or `EXCEPTION`.
* **`exceptionType`**: The explicit categorization enum.
* **`explanation`**: A human-readable rule trace generated by the engine.
* **`confidenceScore`**: Numeric threshold (`1.0` for rigid rule matching).

---

## 7. Exception Types
When a transaction fails the deterministic check, it receives one of the following categorizations:
- **`NONE`**: Perfectly reconciled payload (Status `MATCH`).
- **`AMOUNT_MISMATCH`**: The expected `settlementGrossAmount` minus internal fees diverges structurally from the physical `bankTransactionAmount`.
- **`MISSING_SETTLEMENT`**: The transaction was captured by the gateway but a corresponding banking settlement was never received (`settlementPresent = false`).
- **`DUPLICATE_TRANSACTION`**: Multiple identical or linked bank payout events were attached to the exact same `paymentId`.
- **`DATE_ANOMALY`**: Settlement timestamps fundamentally broke causal physics (e.g., settling temporally *before* a payment occurred).
- **`STATUS_MISMATCH`**: The payload represents conflicting state graphs (e.g., Gateway denotes `FAILED` but Bank denotes `SETTLED`).

---

## 8. Error Responses
The API implements a `GlobalExceptionHandler` shielding clients from internal stack traces, system paths, or credential leakage. All errors are rendered as a clean JSON payload.

**Example 404 (Missing Payment):**
```json
{
  "timestamp": "2026-08-27T06:32:45.050Z",
  "status": 404,
  "error": "Not Found",
  "message": "No reconciliation result found for payment PAY9999",
  "path": "/api/reconciliation/results/PAY9999"
}
```

**Example 400 (Invalid Enum):**
```json
{
  "timestamp": "2026-08-27T06:32:45.088Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid parameter value: INVALID_EXCEPTION",
  "path": "/api/reconciliation/exceptions/INVALID_EXCEPTION"
}
```

---

## 9. Docker Usage & CLI Examples
The application is preconfigured to load the synthetic benchmark dataset `/data` directly within its self-contained Docker runtime.

**Starting the environment:**
```bash
docker compose up -d backend
```

**Retrieving the benchmark report:**
```bash
curl -s http://localhost:8080/api/reconciliation/report
```

**Querying specific exceptions:**
```bash
curl -s http://localhost:8080/api/reconciliation/exceptions/AMOUNT_MISMATCH
```
