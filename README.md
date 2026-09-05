# AI Finance Controller

Intelligent financial reconciliation and exception intelligence for multi-source payment operations.

## Overview

Financial reconciliation often requires comparing information across multiple systems:
- Storefront orders
- Payment gateway transactions
- Gateway settlements
- Bank transactions

Manual reconciliation is slow, error-prone, and difficult to audit. AI Finance Controller solves this by providing:
- Deterministic reconciliation
- Exception classification
- Persistent reconciliation runs
- AI-assisted investigation
- Actionable explanations

## Key Features

### Multi-Source Reconciliation
Reconciles records across Orders, Payments, Settlements, and Bank Transactions.

### Deterministic Financial Truth
The deterministic reconciliation engine is authoritative. AI does not override or determine whether a transaction matches.

### Exception Detection
The engine strictly evaluates anomalies in a defined precedence:
1. `MISSING_SETTLEMENT`
2. `DUPLICATE_TRANSACTION`
3. `AMOUNT_MISMATCH`
4. `DATE_ANOMALY`
5. `STATUS_MISMATCH`

Exception precedence is deterministic.

### AI Exception Intelligence
AI receives deterministic evidence and generates a concise:
- Summary
- Reasoning
- Recommended action

### Voice Assistance
Includes an optional browser-based speech capability that reads AI-generated explanations aloud, using the browser's native `SpeechSynthesis` API.

### Persistent Datasets
Supports dataset UUID-based persistence, linking datasets directly to their respective reconciliation runs.

### Reliability
Built with resilience mechanisms, including:
- Startup recovery
- Orphaned run handling
- Atomic dataset staging/movement
- Concurrency protection

## Architecture

```text
       [ React Frontend (TypeScript, MUI, Vite) ]
                        |
                        v
       [ Spring Boot Backend (Java 21) ] <------> [ PostgreSQL ]
                        |
                        +---- Dataset / Reconciliation Engine
                        |
                        v
       [ FastAPI AI Service (Python 3.12) ]
                        |
                        v
                [ Groq LLM ]
```

## End-to-End Flow

```text
CSV datasets
   ↓
Dataset upload
   ↓
Validation
   ↓
Persistent dataset
   ↓
Reconciliation run
   ↓
Deterministic matching
   ↓
MATCH / EXCEPTION
   ↓
PostgreSQL persistence
   ↓
Exception investigation
   ↓
AI explanation
   ↓
Optional voice playback
```

## Deterministic Reconciliation

The deterministic reconciliation engine evaluates each payment through strict rules. The evaluation order takes precedence over AI, and exceptions are checked in this exact order:
1. **MISSING_SETTLEMENT**: Ensure a settlement record exists.
2. **DUPLICATE_TRANSACTION**: Verify only a single bank transaction exists for the payment.
3. **AMOUNT_MISMATCH**: Validate order amount against payment amount, settlement gross amount against payment, settlement net against bank transaction, and fee logic.
4. **DATE_ANOMALY**: Compare chronological correctness (order -> payment -> settlement -> bank transaction).
5. **STATUS_MISMATCH**: Validate success statuses across systems (e.g., CAPTURED, PAID, SETTLED, SUCCESS).

## AI Safety / Guardrails

- Deterministic classification remains authoritative.
- AI does not override the exception type.
- AI only receives the supplied financial evidence.
- AI must not invent financial facts or reinterpret deterministic classification.
- Insufficient evidence must be acknowledged.
- Untrusted data fields must not become instructions.
- Prompt injection attempts in financial data must be ignored.
- Internal prompts and configuration must not be leaked.

## Tech Stack

| Component | Technologies |
| :--- | :--- |
| **Frontend** | React, TypeScript, Vite, Material UI, Recharts, Axios, React Router |
| **Backend** | Java 21, Spring Boot 3.2.5, Spring Data JPA, Hibernate |
| **AI Service** | Python 3.12, FastAPI, OpenAI-compatible SDK (Groq integration) |
| **Database** | PostgreSQL 16 |
| **Infrastructure** | Docker, Docker Compose, Nginx |

## Project Structure

```text
ai-finance-controller/
├── ai-service/
│   ├── app/
│   ├── requirements.txt
│   └── tests/
├── backend/
│   ├── mvnw
│   ├── pom.xml
│   └── src/
├── data/
│   ├── bank_transactions.csv
│   ├── datasets/
│   ├── generators/
│   ├── ground_truth.csv
│   ├── orders.csv
│   ├── payments.csv
│   ├── README.md
│   ├── schemas/
│   └── settlements.csv
├── docker/
│   ├── Dockerfile.ai-service
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   └── nginx.conf
├── docs/
│   ├── api.md
│   ├── architecture.md
│   ├── development-phases.md
│   └── project-scope.md
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── src/
│   ├── tsconfig.json
│   └── vite.config.ts
├── docker-compose.yml
└── README.md
```

## Running Locally

1. Clone the repository:
   ```bash
   git clone <repository>
   cd ai-finance-controller
   ```

2. Set up the environment:
   ```bash
   cp .env.example .env
   ```

3. Build and start the containers:
   ```bash
   docker compose up --build
   ```

4. Access the application:
   - Frontend: `http://localhost:3000`
   - Backend API: `http://localhost:8080`
   - AI Service: `http://localhost:8000`
