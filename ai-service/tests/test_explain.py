from fastapi.testclient import TestClient
from app.main import app
import pytest

client = TestClient(app)

def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"

def test_explain_match():
    payload = {
        "paymentId": "PAY0001",
        "overallStatus": "MATCH",
        "exceptionType": "NONE"
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["paymentId"] == "PAY0001"
    assert "reconciled successfully" in data["summary"].lower()

def test_explain_amount_mismatch():
    payload = {
        "paymentId": "PAY0002",
        "overallStatus": "EXCEPTION",
        "exceptionType": "AMOUNT_MISMATCH",
        "paymentAmount": 500.0,
        "settlementGrossAmount": 490.0
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["summary"] == "Financial Amount Mismatch"
    assert "diverges" in data["reasoning"].lower()

def test_explain_missing_settlement():
    payload = {
        "paymentId": "PAY0003",
        "overallStatus": "EXCEPTION",
        "exceptionType": "MISSING_SETTLEMENT"
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 200
    assert response.json()["summary"] == "Missing Banking Settlement"

def test_explain_duplicate_transaction():
    payload = {
        "paymentId": "PAY0004",
        "overallStatus": "EXCEPTION",
        "exceptionType": "DUPLICATE_TRANSACTION",
        "bankTransactionCount": 3
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 200
    assert response.json()["summary"] == "Duplicate Bank Transactions Detected"
    assert "(3)" in response.json()["reasoning"]

def test_explain_date_anomaly():
    payload = {
        "paymentId": "PAY0005",
        "overallStatus": "EXCEPTION",
        "exceptionType": "DATE_ANOMALY"
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 200
    assert response.json()["summary"] == "Chronological Date Anomaly"

def test_explain_status_mismatch():
    payload = {
        "paymentId": "PAY0006",
        "overallStatus": "EXCEPTION",
        "exceptionType": "STATUS_MISMATCH"
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 200
    assert response.json()["summary"] == "Conflicting Status Graphs"

def test_missing_required_fields():
    # missing paymentId
    payload = {
        "overallStatus": "EXCEPTION",
        "exceptionType": "STATUS_MISMATCH"
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 422

def test_invalid_overall_status():
    payload = {
        "paymentId": "PAY0007",
        "overallStatus": "INVALID_STATUS",
        "exceptionType": "NONE"
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 422

def test_invalid_exception_type():
    payload = {
        "paymentId": "PAY0008",
        "overallStatus": "MATCH",
        "exceptionType": "FAKE_ERROR"
    }
    response = client.post("/api/explain", json=payload)
    assert response.status_code == 422
