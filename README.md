# AI Finance Controller
Intelligent Multi-Source Reconciliation Agent

[![Track](https://img.shields.io/badge/Razorpay_Buildathon_2026-Track_04-blue)](https://razorpay.com)
[![Phase](https://img.shields.io/badge/Status-Buildathon_Submission_/_Phase_10-green)](#buildathon-submission-status)

An intelligent financial reconciliation engine and autonomous AI agent designed for **Razorpay AI Buildathon 2026 — Track 04: AI Finance Controller**.

---

## 1. Project Overview

Modern finance teams process high volumes of transactional records across disparate sources:
* **Orders**: E-commerce / storefront transactions
* **Payments**: Gateway authorized & captured transactions
* **Refunds**: Merchant & gateway processed refunds
* **Settlements**: Bank payout settlements & fee breakdowns

In high-throughput environments, discrepancies (amount mismatches, fee miscalculations, delayed settlements, duplicate charges, ghost refunds) create severe operational drag and financial leakage.

This project delivers a **hybrid reconciliation architecture**:
1. **Deterministic Reconciliation Engine**: Executes high-speed mathematical & rules-based matching across 100+ multi-source records, strictly determining financial truth.
2. **Autonomous AI Finance Agent**: Investigates exceptions, determines root causes, and generates human-understandable explanations with recommendations.
3. **Executive Dashboard**: A comprehensive React UI for tracking metrics, uploading datasets, and managing reconciliation runs.

> **Key Architectural Philosophy:** Deterministic logic establishes financial truth; AI explains exceptions and assists investigation.

---

## 2. Razorpay Track 04 Alignment

This project fully implements the Track 04 Buildathon requirements:
* **Multi-Source Synthetic Data**: Analyzes a generated dataset of 100 realistic records spanning Orders, Payments, and Settlements.
* **Deterministic Reconciliation**: Programmatically reconciles records using deterministic rules, achieving 100% classification accuracy on the verified 100-record benchmark.
* **Exception Detection & Classification**: Identifies specific discrepancies like amount mismatches, duplicate transactions, and date anomalies.
* **AI-Assisted Investigation**: Utilizes a FastAPI/Python backend connected to an external LLM to analyze the exceptions and offer actionable business advice without hallucinating financial math.
* **Measurable Accuracy**: Honest evaluation metrics proving 100% correct classification of both matches and exceptions.
* **Dashboard Experience**: A fully containerized React frontend delivering operational throughput reports and interactive investigation tools.

---

## 3. Key Features

### Dataset Management
* **CSV Dataset Upload**: Dynamically upload new reconciliation batches (`orders.csv`, `payments.csv`, etc.).
* **Dataset UUID Identification**: Datasets are immutably tracked with unique UUIDs.
* **Dataset Lifecycle**: Atomic uploads with strict validation preventing orphaned files or partial states.

### Reconciliation
* **Asynchronous Reconciliation**: Non-blocking API for executing heavy matching logic in the background.
* **Reconciliation Run UUIDs**: Every execution is tracked independently for historical accuracy.
* **Deterministic Matching**: Rules-based engine determining status.
* **Result Persistence**: Results are securely stored in PostgreSQL.
* **Run Lifecycle Management**: Tracks status (`IN_PROGRESS`, `COMPLETED`, `FAILED`).
* **Orphaned Run Recovery**: Automatically detects and recovers `IN_PROGRESS` runs orphaned by system restarts.

### Exception Types
The deterministic engine actively detects and categorizes the following discrepancies:
* `AMOUNT_MISMATCH`
* `MISSING_SETTLEMENT`
* `DUPLICATE_TRANSACTION`
* `DATE_ANOMALY`
* `STATUS_MISMATCH`

### Reporting & Pagination
* **Operational Metrics**: Aggregates total records, matched records, exception records, and computes match/exception rates.
* **Scalable Pagination**: Backend-driven pagination and filtering for exploring results efficiently.

### AI Investigation
* **LLM Integration**: AI seamlessly provides a `summary`, `reasoning`, and `recommendedAction` for any exception directly in the dashboard context.
* **Safe Boundary**: The AI operates strictly on DTOs, preventing it from inventing financial data or corrupting the deterministic truth.

### Frontend
* **Executive Dashboard**: Visualizes run metrics and exceptions.
* **Dataset Upload UI**: Simple file selection modal to ingest new batches.
* **Investigation Drawer**: Detailed view per transaction displaying source facts alongside the AI's explanation.

---

## 4. Architecture

```text
               [ React Frontend (Vite, TypeScript, MUI) ]
                                |
                                v
               [ Spring Boot Backend (Java 21) ] <------> [ PostgreSQL 16 ]
                                |
                                v
               [ FastAPI AI Service (Python 3.12) ]
                                |
                                v
                       [ External LLM ]
```

**End-to-End Data Workflow:**
```text
Dataset Upload
      ↓
Dataset UUID Generated
      ↓
Reconciliation Run Created
      ↓
CSV Ingestion & Validation
      ↓
Deterministic Engine Evaluates
      ↓
Results Persisted to DB
      ↓
Dashboard & AI Investigation
```

---

## 5. Technology Stack

* **Backend**: Java 21, Spring Boot, Spring Data JPA
* **Database**: PostgreSQL 16
* **AI Service**: Python 3.12, FastAPI
* **Frontend**: React, TypeScript, Vite, Material UI
* **Infrastructure**: Docker, Docker Compose

---

## 6. Reconciliation Accuracy & Benchmark

The deterministic engine strictly evaluates records independent of ground-truth data. Ground truth is used purely for this evaluation benchmark.

| Metric                          | Result |
| ------------------------------- | -----: |
| Records evaluated               |    100 |
| Matches                         |     80 |
| Exceptions                      |     20 |
| Overall classification accuracy |   100% |
| Correct classifications         |    100 |
| Incorrect classifications       |      0 |

**Exception Distribution Detected:**

| Exception Type        | Count |
| --------------------- | ----: |
| AMOUNT_MISMATCH       |     4 |
| MISSING_SETTLEMENT    |     4 |
| DUPLICATE_TRANSACTION |     4 |
| DATE_ANOMALY          |     4 |
| STATUS_MISMATCH       |     4 |

---

## 7. End-to-End Workflow

1. User uploads a financial dataset (`orders.csv`, `payments.csv`, etc.).
2. Dataset receives a unique UUID and files are validated.
3. User triggers a reconciliation batch.
4. Backend creates a reconciliation run and processes data asynchronously.
5. Deterministic engine evaluates all records.
6. Results and exceptions are persisted to PostgreSQL.
7. Dashboard retrieves the run metrics.
8. Finance user investigates exceptions in the UI.
9. AI provides automated explanations and recommendations for discrepancies.

---

## 8. API Overview

Key REST endpoints driving the platform:

| Area                  | Endpoint                         |
| --------------------- | -------------------------------- |
| Health                | `/api/health`                    |
| Dataset Management    | `/api/datasets`                  |
| Run Management        | `/api/reconciliation/runs`       |
| Reconciliation Report | `/api/reconciliation/report`     |
| Results               | `/api/reconciliation/results`    |
| Exceptions            | `/api/reconciliation/exceptions` |

*For complete details, see [docs/api.md](docs/api.md).*

---

## 9. Local Development

### Prerequisites
* Docker & Docker Compose

### Environment
Copy the example environment template:
```bash
cp .env.example .env
```

### Run via Docker Compose
Build and start the full platform:
```bash
docker compose build
docker compose up -d
```
Check status:
```bash
docker compose ps
```

**Services:**
* Frontend: `http://localhost:3000`
* Backend API: `http://localhost:8080`
* AI Service: `http://localhost:8000`
* PostgreSQL: `localhost:5433`

---

## 10. Verification

The project is thoroughly tested. You can verify its integrity using the following:

**Backend Tests:**
```bash
cd backend
./mvnw test
```
*(Expects `BUILD SUCCESS` with 47 tests passed.)*

**Frontend Build:**
```bash
cd frontend
npm run build
```
*(Expects a successful build. Note: A chunk size warning may appear but it does not fail the build.)*

---

## 11. Project Scope & Limitations

* **Synthetic Data**: Operates on generated synthetic datasets designed to simulate edge cases.
* **No Real Money Movement**: This is an analytical and investigatory tool. It does not interface with live payment gateways or execute real banking transactions.
* **Authentication**: Production SSO/Auth is not currently implemented.

---

## 12. Buildathon Submission Status

* **Phase 0–9**: Completed (Infrastructure, Dataset, Engine, AI Agent, UI, Persistence, Containerization)
* **Phase 10**: Final Submission Preparation

---

## 13. Documentation

* [API Documentation](docs/api.md)
* [Architecture](docs/architecture.md)
* [Development Phases](docs/development-phases.md)
* [Project Scope](docs/project-scope.md)

---

## 14. License

This project is licensed under the [MIT License](LICENSE).
