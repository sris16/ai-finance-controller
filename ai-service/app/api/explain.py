from fastapi import APIRouter, HTTPException
from app.models.explanation import ExplanationRequest, ExplanationResponse
from app.services.explanation_service import ExplanationService
from app.services.llm.exceptions import (
    AIServiceUnavailableError,
    AIProviderAuthenticationError,
    AIProviderRateLimitError,
    AIProviderTimeoutError,
    AIProviderResponseError
)

router = APIRouter()

@router.post("/explain", response_model=ExplanationResponse)
def explain_reconciliation(request: ExplanationRequest):
    """
    Provides an AI-assisted explanation for a given deterministic reconciliation result.
    Does NOT override or change the financial classification.
    """
    try:
        return ExplanationService.generate_explanation(request)
    except AIServiceUnavailableError as e:
        raise HTTPException(status_code=503, detail=str(e))
    except (AIProviderAuthenticationError, AIProviderResponseError) as e:
        raise HTTPException(status_code=500, detail=str(e))
    except AIProviderRateLimitError as e:
        raise HTTPException(status_code=429, detail=str(e))
    except AIProviderTimeoutError as e:
        raise HTTPException(status_code=504, detail=str(e))
