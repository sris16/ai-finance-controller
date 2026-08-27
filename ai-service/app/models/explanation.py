from pydantic import BaseModel, Field
from typing import Optional
from decimal import Decimal
from app.models.enums import ReconciliationStatus, ExceptionType

class ExplanationRequest(BaseModel):
    paymentId: str = Field(..., description="Unique identifier for the payment")
    overallStatus: ReconciliationStatus = Field(..., description="Reconciliation status (MATCH or EXCEPTION)")
    exceptionType: ExceptionType = Field(..., description="Exception categorization from deterministic engine")
    paymentAmount: Optional[Decimal] = None
    settlementGrossAmount: Optional[Decimal] = None
    settlementPresent: Optional[bool] = None
    bankTransactionCount: Optional[int] = None
    explanation: Optional[str] = None

class ExplanationResponse(BaseModel):
    paymentId: str
    summary: str
    reasoning: str
    recommendedAction: str
