from fastapi import APIRouter
from app.models.explanation import ExplanationRequest, ExplanationResponse
from app.services.explanation_service import ExplanationService

router = APIRouter()

@router.post("/explain", response_model=ExplanationResponse)
def explain_reconciliation(request: ExplanationRequest):
    """
    Provides an AI-assisted explanation for a given deterministic reconciliation result.
    Does NOT override or change the financial classification.
    """
    return ExplanationService.generate_explanation(request)
