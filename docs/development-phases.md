# Development Phases & Roadmap

This project follows an iterative 11-phase development roadmap for the **Razorpay AI Buildathon 2026**.

---

## Roadmap Overview

```text
Phase 0 ──► Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5
Def.        Found.      Dataset     Data/DB     Engine      AI Agent
                                                               │
Phase 10 ◄── Phase 9 ◄── Phase 8 ◄── Phase 7 ◄── Phase 6 ◄─────┘
Submission  Polish      Metrics     Dashboard   APIs
```

---

## Detailed Phase Breakdown

### Phase 0 — Project Definition & Scope Setup
- Define core requirements for Razorpay Track 04.
- Outline architecture, data flows, and project scope boundaries.

### Phase 1 — Foundation & Architecture Setup (CURRENT PHASE)
- Establish monorepo structure (`frontend`, `backend`, `ai-service`, `data`, `docs`, `docker`).
- Configure Spring Boot (Java 21), FastAPI (Python 3.12), React + Vite (TypeScript), and PostgreSQL.
- Setup environment variables, Docker Compose configuration, `.gitignore`, and documentation.
- Implement basic health endpoints across services.

### Phase 2 — Synthetic Dataset Generation
- Build a synthetic financial dataset generator script in Python.
- Generate realistic batches of 50+ records spanning Orders, Payments, Refunds, and Settlements.
- Inject controlled anomaly scenarios (amount mismatch, fee deviation, missing refund, delayed settlement).

### Phase 3 — Database Schema & Data Ingestion Pipeline
- Design PostgreSQL relational schema (Entities: `Order`, `Payment`, `Refund`, `Settlement`, `ReconciliationBatch`, `Discrepancy`, `AuditLog`).
- Implement Spring Data JPA repositories and batch CSV/JSON data ingestion services.

### Phase 4 — Deterministic Reconciliation Engine
- Implement high-performance matching algorithm in Java Spring Boot.
- Compare transaction amounts, gateway fees, taxes, and status flags across sources.
- Flag exact matches vs. discrepancy exceptions.
- Calculate batch metrics: processing time, throughput, accuracy rate.

### Phase 5 — AI Financial Agent (FastAPI + LLM Integration)
- Build FastAPI AI agent endpoints receiving discrepancy payloads.
- Design structured prompt templates for financial root-cause analysis.
- Connect to LLM API (e.g. Gemini API) to generate explanations, evidence summaries, and recommendations.

### Phase 6 — Backend REST APIs & DTO Layer
- Implement clean REST controllers exposing batch reconciliation triggers, metric stats, discrepancy detail views, and audit trail query APIs.
- Add input validation and unified error handling.

### Phase 7 — Frontend Dashboard & Executive UI
- Build modern React UI using Material UI components and Recharts data visualizations.
- Create KPI summary tiles, Batch Ingestion control panel, Interactive Discrepancy Table, and AI Investigation Drawer.

### Phase 8 — Evaluation, Performance & Accuracy Measurement
- Benchmark reconciliation performance on 50+ record synthetic batches.
- Measure throughput (records/second), execution duration, and exception detection precision.
- Verify honest reporting of matched vs. unresolved exceptions.

### Phase 9 — Security, Containerization & Polish
- Secure API endpoints and sanitize environment configs.
- Test full multi-container deployment via Docker Compose.
- Refine UI animations, dark mode aesthetics, and micro-interactions.

### Phase 10 — Buildathon Submission & Demonstration
- Finalize documentation, architecture diagrams, and user walkthrough.
- Prepare demonstration dataset runs and submission artifact bundle.
