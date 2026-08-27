# AI Finance Controller — Intelligent Multi-Source Reconciliation Agent

[![Track](https://img.shields.io/badge/Razorpay_Buildathon_2026-Track_04-blue)](https://razorpay.com)
[![Phase](https://img.shields.io/badge/Status-Phase_1_Foundation-green)](#development-status)

An intelligent financial reconciliation engine and autonomous AI agent designed for **Razorpay AI Buildathon 2026 — Track 04: AI Finance Controller**.

---

## 1. Project Objective & Track Requirement

Modern finance teams process high volumes of transactional records across disparate sources:
* **Orders**: E-commerce / storefront transactions
* **Payments**: Gateway authorized & captured transactions (e.g. Razorpay)
* **Refunds**: Merchant & gateway processed refunds
* **Settlements**: Bank payout settlements & fee breakdowns

In high-throughput environments, discrepancies (amount mismatches, fee miscalculations, delayed settlements, duplicate charges, ghost refunds) create severe operational drag and financial leakage.

This project delivers a **hybrid reconciliation architecture**:
1. **Deterministic Reconciliation Engine**: Executes high-speed mathematical & rules-based matching across 50+ synthetic multi-source records.
2. **Autonomous AI Finance Agent**: Investigates discrepancies, determines root causes, generates human-understandable explanations with recommendations, and maintains an immutable audit log.

> **Buildathon Requirement**: Build an agent that closes one finance-operations loop across a 50+ record batch of synthetic data, reporting throughput, measured accuracy, and an honest exception list.

---

## 2. Planned Solution Architecture

```text
               +----------------------------------+
               |     Multi-Source Data Batch      |
               | (Orders, Payments, Refunds, etc.) |
               +----------------------------------+
                                |
                                v
               +----------------------------------+
               |     Data Ingestion Pipeline      |
               +----------------------------------+
                                |
                                v
               +----------------------------------+
               | Deterministic Matching Engine    |
               |  (Amounts, Fees, IDs, Dates)     |
               +----------------------------------+
                        /                \
                       /                  \
            [Matched Records]      [Exceptions Detected]
                   |                        |
                   v                        v
          +------------------+    +------------------------+
          | Accuracy Metrics |    |  AI Financial Agent    |
          |   & Throughput   |    |  Investigation Loop    |
          +------------------+    +------------------------+
                   |                        |
                   |                        v
                   |              +------------------------+
                   |              | Root-Cause Analysis,   |
                   |              | Explanation & Advice   |
                   |              +------------------------+
                   |                        |
                   v                        v
         +--------------------------------------------------+
         |     Audit Trail & Executive Dashboard (UI)       |
         +--------------------------------------------------+
```

---

## 3. Technology Stack

* **Frontend**: React 18, TypeScript, Vite, Material UI (MUI), React Router, Axios, Recharts
* **Backend**: Java 21, Spring Boot 3.x, Spring Data JPA, PostgreSQL Driver, Spring Validation, REST APIs
* **AI Service**: Python 3.12, FastAPI, Uvicorn, Pandas, NumPy, Pydantic
* **Database**: PostgreSQL 16
* **Infrastructure & Containerization**: Docker, Docker Compose

---

## 4. Development Status

> **Current Phase: Phase 1 — Project Foundation & Architecture Setup**

In this phase, we have established:
- Scalable monorepo repository architecture
- Spring Boot Java 21 backend service scaffold with health endpoint (`/api/health`)
- FastAPI Python AI service scaffold with health endpoint (`/health`)
- React + TypeScript + Vite frontend application shell with Material UI
- PostgreSQL container configuration with persistent volume storage
- Environment template (`.env.example`) and Docker Compose configuration

---

## 5. Quick Start (Local Development)

### Prerequisites
- JDK 21+
- Node.js 20+ & npm
- Python 3.10+
- Docker & Docker Compose

### Environment Setup
1. Copy the example environment template:
   ```bash
   cp .env.example .env
   ```

### Option A: Run via Docker Compose
```bash
docker compose up -d
```
- **Frontend**: `http://localhost:3000`
- **Backend API**: `http://localhost:8080`
- **AI Service API**: `http://localhost:8000`
- **PostgreSQL**: `5433` (mapped to container `5432`)

**Useful `curl` Commands:**
```bash
# Check backend health
curl -s http://localhost:8080/api/health

# Get reconciliation operational report
curl -s http://localhost:8080/api/reconciliation/report

# Retrieve all DATE_ANOMALY exceptions
curl -s http://localhost:8080/api/reconciliation/exceptions/DATE_ANOMALY
```

### Option B: Run Services Locally

1. **Start PostgreSQL Database**:
   ```bash
   docker-compose up -d postgres
   ```

2. **Run Backend Service (Java / Spring Boot)**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. **Run AI Service (Python / FastAPI)**:
   ```bash
   cd ai-service
   python3 -m venv .venv
   source .venv/bin/activate  # On Windows: .venv\Scripts\activate
   pip install -r requirements.txt
   uvicorn app.main:app --reload --port 8000
   ```

4. **Run Frontend App (React / Vite)**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---

## 6. API Documentation

The REST API is thoroughly documented in [`docs/api.md`](docs/api.md).

It contains full specifications for:
- Operational Reporting
- Results Retrieval & Filtering
- Exception Categorization
- Error Contracts
- Core JSON Response Models

---

## 7. License

This project is licensed under the [MIT License](LICENSE).
