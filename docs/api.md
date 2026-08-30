# API Documentation

## 1. API Overview
The AI Finance Controller exposes a comprehensive RESTful API layer managing the end-to-end reconciliation lifecycle. The architecture utilizes a **Spring Boot Backend** for dataset management, deterministic reconciliation orchestration, and results pagination, and a **FastAPI Python AI Service** for LLM exception investigation.

All API responses are strictly JSON formatted (except for file uploads, which use `multipart/form-data`).

---

## 2. Base URLs
When running via the provided Docker Compose environment:

* **Backend API**: `http://localhost:8080`
* **AI Service API**: `http://localhost:8000`
* **Frontend**: `http://localhost:3000`

---

## 3. Authentication & Scope
> The current Buildathon implementation does not require production authentication or SSO. APIs are intended for the local/containerized demonstration environment. There is no real money movement or live payment gateway integration.

---

## 4. API Conventions
* Standard HTTP methods (`GET`, `POST`) are used.
* All successful responses return `200 OK` or `201 Created`.
* Date and time fields are represented as ISO 8601 Strings (`Instant` representations in UTC).
* Most GET requests returning lists of transactions are paginated using standard Spring `Page` objects.

---

## 5. Health API

### Get Backend Health
```http
GET /api/health
```
**Response (200 OK)**
```json
{
  "status": "UP",
  "service": "ai-finance-controller-backend",
  "version": "1.0.0"
}
```

### Get AI Service Health
```http
GET /health
```
*(Available directly on the AI service at `http://localhost:8000`)*

---

## 6. Dataset Management API

Datasets act as the immutable source data for reconciliation runs. A dataset is assigned a unique UUID to trace exactly which file batch was used for any historical run.

### Get All Datasets
```http
GET /api/datasets
```
**Response (200 OK)**
Returns a list of all uploaded datasets.
```json
[
  {
    "id": "a812241a-08b1-4b4d-b8b2-24bb9135c61b",
    "name": "September Synthetic Batch",
    "uploadedAt": "2026-09-01T10:00:00Z"
  }
]
```

### Upload Dataset
```http
POST /api/datasets
Content-Type: multipart/form-data
```
**Form Data Fields:**
* `name` (String, required): Human-readable dataset name.
* `orders` (File, required): `orders.csv`
* `payments` (File, required): `payments.csv`
* `settlements` (File, optional): `settlements.csv`
* `bankTransactions` (File, optional): `bank_transactions.csv`

**Response (201 Created)**
```json
{
  "id": "a812241a-08b1-4b4d-b8b2-24bb9135c61b",
  "name": "September Synthetic Batch",
  "uploadedAt": "2026-09-01T10:00:00Z"
}
```

---

## 7. Reconciliation Run API

A reconciliation run is an asynchronous execution of the deterministic matching engine against a specific dataset.

### Trigger Reconciliation Run
```http
POST /api/reconciliation/runs?datasetId={datasetId}
```
**Query Parameters:**
* `datasetId` (String, optional): The UUID of the uploaded dataset. If omitted, it falls back to the legacy configured local path.

**Response (201 Created)**
```json
{
  "id": "73062ca7-0a33-4f54-9bd5-b4aa53d1615a",
  "executionTime": "2026-09-01T10:05:00Z",
  "status": "IN_PROGRESS",
  "totalRecords": null,
  "datasetId": "a812241a-08b1-4b4d-b8b2-24bb9135c61b"
}
```

### Get All Runs
```http
GET /api/reconciliation/runs
```
**Response (200 OK)**
```json
[
  {
    "id": "73062ca7-0a33-4f54-9bd5-b4aa53d1615a",
    "executionTime": "2026-09-01T10:05:05Z",
    "status": "COMPLETED",
    "totalRecords": 100,
    "datasetId": "a812241a-08b1-4b4d-b8b2-24bb9135c61b"
  }
]
```
**Run Status Enum (`status`):**
* `IN_PROGRESS`: Execution currently running asynchronously.
* `COMPLETED`: Run successfully finished and results persisted.
* `FAILED`: An unrecoverable exception occurred during parsing or matching.

---

## 8. Reporting API

### Get Operational Report
```http
GET /api/reconciliation/report?runId={runId}
```
**Query Parameters:**
* `runId` (String, optional): UUID of the specific run. If omitted, targets a default operational view.

**Response (200 OK)**
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

---

## 9. Results API

### Get Paginated Results
```http
GET /api/reconciliation/results?runId={runId}&status={status}&exceptionType={exceptionType}&page=0&size=20
```
**Query Parameters:**
* `runId` (String, optional): UUID of the reconciliation run to scope results.
* `status` (Enum, optional): `MATCH` or `EXCEPTION`.
* `exceptionType` (Enum, optional): Filter by a specific exception type.
* `page` (Integer, default 0): Page index.
* `size` (Integer, default 20, max 100): Page size.

**Response (200 OK)**
Returns a Spring Data `Page<ReconciliationResult>`.

### Get Specific Transaction Result
```http
GET /api/reconciliation/results/{paymentId}?runId={runId}
```

---

## 10. Exception API

### Get All Exceptions
```http
GET /api/reconciliation/exceptions?runId={runId}&page=0&size=20
```

### Get Exceptions by Type
```http
GET /api/reconciliation/exceptions/{exceptionType}?runId={runId}&page=0&size=20
```

**ExceptionType Enum values defined by the Deterministic Engine:**
* `NONE`: Represents a clean match.
* `AMOUNT_MISMATCH`: Payment amount diverges from order amount or calculated fees are incorrect.
* `MISSING_SETTLEMENT`: A captured payment has no corresponding bank settlement.
* `DUPLICATE_TRANSACTION`: Multiples of the same transaction ID exist.
* `DATE_ANOMALY`: Dates across the lifecycle violate chronological rules (e.g., settlement before payment).
* `STATUS_MISMATCH`: The gateway status contradicts the storefront status.

---

## 11. AI Investigation API

> **Safety Boundary:** AI analysis does not determine the underlying reconciliation result. The deterministic reconciliation engine establishes financial truth; AI provides explanation and investigation assistance.

### Generate AI Explanation
```http
GET /api/reconciliation/results/{paymentId}/explanation?runId={runId}
```
*Note: This route hits the Spring Boot backend, which seamlessly acts as a proxy/orchestrator to call the internal FastAPI AI service.*

**Response (200 OK)**
```json
{
  "paymentId": "pay_XYZ123",
  "overallStatus": "EXCEPTION",
  "exceptionType": "AMOUNT_MISMATCH",
  "explanation": {
    "paymentId": "pay_XYZ123",
    "summary": "Gateway fee miscalculation caused an amount mismatch.",
    "reasoning": "The order amount was $100.00, but the captured payment amount was $98.00 due to an unauthorized 2% gateway deduction applied before capture. This violates the gross-amount capturing rule.",
    "recommendedAction": "Raise a ticket with the payment gateway regarding incorrect pre-capture deductions for this merchant account."
  }
}
```

---

## 12. Error Contract

When an error occurs, the API returns a structured JSON payload driven by the `GlobalExceptionHandler`.

**Example: 400 Bad Request (Dataset Upload Failed)**
```json
{
  "timestamp": "2026-09-01T10:12:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Required file 'orders.csv' is missing",
  "path": "/api/datasets"
}
```

**Common HTTP Status Codes:**
* `400 Bad Request`: Validation failure, missing parameters, or invalid enum value.
* `404 Not Found`: Resource (e.g., payment ID, run ID) could not be located.
* `409 Conflict`: Concurrent execution exception (e.g., trying to start a run while another is `IN_PROGRESS`).
* `503 Service Unavailable`: The FastAPI AI Service could not be reached or timed out.
* `500 Internal Server Error`: Unexpected system failure.

---

## 13. End-to-End API Workflow

1. **Upload Dataset:** `POST /api/datasets` -> Retrieves a Dataset UUID.
2. **Start Run:** `POST /api/reconciliation/runs?datasetId={UUID}` -> Initiates async processing and retrieves a Run UUID.
3. **Poll Run Status:** `GET /api/reconciliation/runs` -> Poll until status is `COMPLETED`.
4. **Retrieve Metrics:** `GET /api/reconciliation/report?runId={UUID}` -> Fetch the operational summary.
5. **View Exceptions:** `GET /api/reconciliation/exceptions?runId={UUID}` -> Paginate through mismatches.
6. **Investigate Exception:** `GET /api/reconciliation/results/{paymentId}/explanation?runId={UUID}` -> Request an AI explanation.

---

## 14. Ground-Truth Separation

The deterministic engine algorithmically classifies matches and exceptions using pure business logic. A benchmark file (`ground_truth.csv`) may be present in the repository, but it is strictly utilized for **evaluation/testing** by the integration test suite. The operational API endpoints documented above do NOT consult the ground truth for decision-making.
