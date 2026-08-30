# System Architecture Specification

## 1. Architecture Overview

The **AI Finance Controller** system follows a highly decoupled, service-oriented hybrid architecture. It integrates a deterministic financial engine with intelligent AI exception investigation.

**React Frontend → Spring Boot Backend → PostgreSQL**

with a secondary AI reasoning loop:

**Spring Boot Backend → FastAPI AI Service → External LLM**

> **Core Philosophy:** Deterministic reconciliation establishes financial truth. AI is used strictly for explanation and investigation of exceptions.

---

## 2. High-Level Architecture Diagram

```text
               +-----------------------------------------------------+
               |                   REACT FRONTEND                    |
               |     (Dashboard, Upload Modal, Investigation UI)     |
               +-----------------------------------------------------+
                    |                                        ^
       JSON / HTTP  | (Upload, Start Run)                    | (Metrics, Results, AI Explanations)
                    v                                        |
               +-----------------------------------------------------+
               |                 SPRING BOOT BACKEND                 |
               |                                                     |
               |  [Dataset Management] <--- CSV Ingestion            |
               |  [Async Orchestrator] ---> Orphaned Run Recovery    |
               |  [Deterministic Engine] -> Financial Truth          |
               +-----------------------------------------------------+
                    |               |                        ^
         JPA / SQL  |               | Internal HTTP          | JSON
     (Read/Write)   |               v                        | (summary, reasoning, action)
                    v         +--------------------------------------+
+-------------------------+   |          FASTAPI AI SERVICE          |
|   POSTGRESQL DATABASE   |   |   (Stateless Context Interpreter)    |
| (Datasets, Runs,        |   +--------------------------------------+
|  Reconciliation Results)|                     |
+-------------------------+                     | External API
                                                v
                                      +--------------------+
                                      |    EXTERNAL LLM    |
                                      +--------------------+
```

---

## 3. Component Responsibilities

### 3.1 React Frontend
The React (Vite/TypeScript) single-page application is responsible for:
- **Dashboard**: Visualizing operational metrics (Match Count vs. Exception Count).
- **Dataset Upload UI**: A modal allowing users to safely upload custom CSV datasets.
- **Reconciliation Results**: A scalable, paginated, filterable grid displaying all processed transactions.
- **Investigation UI**: An interactive drawer that displays deterministic facts side-by-side with the AI's explanation.

### 3.2 Spring Boot Backend
The Java 21 Spring Boot application acts as the core orchestrator:
- **REST API Layer**: Exposes secure endpoints for datasets, runs, reporting, and results.
- **Dataset Lifecycle Management**: Handles multipart uploads, dynamic paths, UUID assignment, and atomic directory operations.
- **Asynchronous Processing**: Uses Spring's `@Async` to offload heavy reconciliation workloads without blocking the HTTP request thread.
- **Persistence & Aggregation**: Uses Spring Data JPA to write millions of potential records and execute fast database-backed aggregation queries.
- **Orphaned Run Recovery**: Automatically scans and recovers `IN_PROGRESS` runs that were abruptly halted by system restarts.

### 3.3 Deterministic Reconciliation Engine
This engine is the **absolute financial source of truth**. It parses ingested CSV records and performs rules-based matching across amounts, settlement dates, and gateway statuses.
It categorizes discrepancies precisely into the following supported types:
- `AMOUNT_MISMATCH`
- `MISSING_SETTLEMENT`
- `DUPLICATE_TRANSACTION`
- `DATE_ANOMALY`
- `STATUS_MISMATCH`

### 3.4 PostgreSQL
The relational store provides ACID guarantees and persists:
- `reconciliation_datasets`: Metadata for uploaded immutable datasets.
- `reconciliation_runs`: Tracked execution batches and their status (`IN_PROGRESS`, `COMPLETED`, `FAILED`).
- `reconciliation_results`: Granular record-by-record output linked to a specific run, heavily indexed for paginated reads.

### 3.5 FastAPI AI Service
The Python 3.12 AI service operates as a stateless reasoning boundary:
- **Receives Context**: Accepts a structured exception payload from the Java backend.
- **Prompt Engineering**: Constructs strict prompts preventing hallucinated math.
- **Generates Explanations**: Produces a structured JSON payload containing a `summary`, detailed `reasoning`, and a `recommendedAction`.
- **Note**: The AI *never* decides if a record is a match or exception.

---

## 4. Dataset Lifecycle

Custom financial datasets follow a strict immutability flow:

```text
CSV Upload (multipart/form-data)
   ↓
Validation (Ensures required files like orders.csv exist)
   ↓
Dataset UUID Generated
   ↓
Files Atomically Moved to Persistent Storage
   ↓
Persistent Dataset Record Created in DB
   ↓
Reconciliation Run Starts (Linked to UUID)
   ↓
Async Processing
```

---

## 5. Reconciliation Run Lifecycle

Reconciliation is tracked via a database-backed state machine.

**Normal Execution:**
```text
IN_PROGRESS (Run created, thread dispatched)
     ↓
COMPLETED (All records evaluated and persisted)
```

**Failure Handling:**
```text
IN_PROGRESS
     ↓
FAILED (Exception caught during ingestion or matching)
```

**Startup Recovery:**
If the JVM terminates unexpectedly, the `JobRecoveryService` detects orphaned `IN_PROGRESS` runs on startup and safely transitions them to `FAILED` to unblock the system.

---

## 6. Deterministic Reconciliation Flow

```text
Selected Dataset (UUID)
 ↓
CSV Ingestion
 ↓
Domain Records Formed (Order, Payment, Settlement)
 ↓
Deterministic Matching Rules Executed
 ↓
MATCH / EXCEPTION Determined
 ↓
Exception Classification (e.g. AMOUNT_MISMATCH)
 ↓
Database Persistence (reconciliation_results)
 ↓
Metrics Aggregation (Available via REST API)
```
*Note: A ground-truth CSV is used purely for benchmark validation testing. It is never used by the operational engine to make matching decisions.*

---

## 7. AI Investigation Flow

```text
Persisted Exception in DB
        ↓
User Clicks "Investigate" in UI
        ↓
Structured Context Sent to FastAPI AI Service
        ↓
External LLM Reasoning
        ↓
[ Structured Output ]
  - Summary
  - Reasoning
  - Recommended Action
        ↓
Frontend Investigation UI Renders Explanation
```

**Safety Boundary:**
**Deterministic engine = Financial truth**
**AI = Explanation/Investigation assistance**

---

## 8. Data Persistence Model

The core schema consists of three highly relational tables:

- **`reconciliation_datasets`**: Stores `id` (UUID), `name`, and `uploaded_at`.
- **`reconciliation_runs`**: Stores `id` (UUID), `status`, `execution_time`, `total_records`, and a foreign key to `dataset_id`.
- **`reconciliation_results`**: Stores the granular output of every transaction, foreign-keyed to `run_id`. Includes fields like `overall_status`, `exception_type`, and the actual transaction amounts/dates. Indexed heavily by `overall_status` and `run_id` for UI pagination.

---

## 9. API / Service Communication

- **Frontend → Backend**: REST/HTTP + JSON
- **Backend → AI Service**: Internal HTTP/REST using Spring `RestTemplate`.
- **Backend → PostgreSQL**: Spring Data JPA / JDBC Driver.
- **AI Service → External LLM**: OpenAI SDK / HTTP requests over TLS.

---

## 10. Reliability & Recovery

The architecture guarantees high reliability without a message broker:
- **Asynchronous Threads**: Heavy reconciliation operations are detached from the HTTP thread pool.
- **Job Status Tracking**: Every run is persisted before execution starts.
- **Failure Handling**: Global exception handlers and async failure callbacks ensure runs don't hang indefinitely.
- **Orphaned Run Recovery**: Startup hooks sanitize database state automatically.
- **Pagination**: Massive result sets are never fully loaded into memory.

---

## 11. Security & Scope Boundaries

- **Configuration**: All secrets, DB credentials, and AI keys are securely managed via `.env`.
- **API Protection**: Clean, structured error responses prevent leaking stack traces.
- **Scope Limitations**: The system uses synthetic data, has no real payment gateway integration, and cannot execute real money movement. There is currently no production SSO/OAuth implementation.

---

## 12. Deployment Architecture

The entire platform is orchestrated using Docker Compose.

```text
Docker Compose Network
 ├── frontend   (Port 3000)
 ├── backend    (Port 8080)
 ├── ai-service (Port 8000)
 └── postgres   (Port 5433 → internal 5432)
```

---

## 13. Design Principles

1. **Deterministic truth before AI interpretation.**
2. **Separation of financial computation and natural-language reasoning.**
3. **Dataset/run UUID traceability.**
4. **Persistent reconciliation history.**
5. **Explicit exception classification.**
6. **Failure recovery.**
7. **Honest benchmark evaluation.**
8. **Synthetic-data-first scope for the Buildathon.**

---

## 14. Architecture Summary

The AI Finance Controller completely satisfies the Razorpay Track 04 requirement by closing the finance operations loop. It provides an enterprise-grade infrastructure to:

**Upload → Reconcile → Detect → Explain → Investigate**

The decoupled approach guarantees that AI hallucination cannot corrupt financial accounting, while simultaneously utilizing AI to massively reduce human investigation time.
