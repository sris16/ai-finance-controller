# System Architecture Specification

## 1. High-Level Architecture Overview

The **AI Finance Controller** system follows a decoupled, service-oriented architecture designed to handle high-throughput deterministic reconciliation alongside intelligent AI exception investigation.

```text
+-------------------------------------------------------------------------+
|                              REACT FRONTEND                             |
|       (Vite, TypeScript, Material UI, Recharts, React Router)           |
+-------------------------------------------------------------------------+
                                    |
                                    | REST APIs (JSON / HTTP)
                                    v
+-------------------------------------------------------------------------+
|                          SPRING BOOT BACKEND                            |
|             (Java 21, Spring Data JPA, REST Controllers)                |
+-------------------------------------------------------------------------+
                     /                                 \
                    /                                   \
   Database SQL    /                                     \  Internal HTTP / REST
  (Read/Write)    /                                       \  Requests
                 v                                         v
+-----------------------------+          +--------------------------------+
|     POSTGRESQL DATABASE     |          |       FASTAPI AI SERVICE       |
| (Transactions, Records,     |          |    (Python 3.12, Pandas,       |
| Audit Logs, Discrepancies)  |          |      NumPy, AI Agent Logic)     |
+-----------------------------+          +--------------------------------+
                                                           |
                                                           | External LLM API
                                                           | (Gemini / OpenAI)
                                                           v
                                         +--------------------------------+
                                         |         EXTERNAL LLM           |
                                         |   (Root Cause Reasoning,       |
                                         |     Structured Explanations)   |
                                         +--------------------------------+
```

---

## 2. Component Responsibilities

### 2.1 React Frontend
- Displays executive summary cards (Total Records, Processed, Accuracy %, Throughput req/sec).
- Visualizes discrepancy breakdown via interactive charts.
- Provides interactive exception investigation workbench with AI explanation viewer.
- Displays full audit trail log and export options.

### 2.2 Spring Boot Backend
- Orchestrates financial data ingestion (CSV / JSON batches).
- Runs high-speed deterministic matching algorithms (Amount matching, Fee verification, Gateway vs Storefront matching).
- Identifies discrepancy exceptions and persists state to PostgreSQL database.
- Dispatches exception payloads to the Python AI Service for investigation.
- Exposes secure REST endpoints for frontend consumption.

### 2.3 PostgreSQL Database
- Relational schema storing Orders, Payments, Refunds, Settlements, Reconciliations, and Audit Trail logs.
- Guarantees ACID transactional compliance for financial auditability.

### 2.4 FastAPI AI Service
- Receives structured financial discrepancy payloads from backend.
- Constructs contextual prompt templates with deterministic transaction context.
- Invokes external LLMs to analyze root causes, generate human explanations, and recommend corrective actions.
- Returns structured JSON responses back to the Java Backend for audit recording.

---

## 3. Key Design Principles

1. **Separation of Concerns**: Deterministic mathematical reconciliation is strictly separated from AI natural-language reasoning.
2. **Stateless AI Processing**: The Python AI Service acts as a stateless reasoning agent, receiving contextual data and returning structured analysis.
3. **Immutability & Auditability**: Every discrepancy investigation produces an immutable audit record stored in PostgreSQL.
