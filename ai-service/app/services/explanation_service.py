import json
from decimal import Decimal
from app.models.explanation import ExplanationRequest, ExplanationResponse
from app.services.llm.groq_provider import GroqProvider

class ExplanationService:
    @staticmethod
    def _prepare_evidence(request: ExplanationRequest) -> dict:
        """Converts the structured Pydantic model into a dictionary suitable for LLM input, stringifying Decimals."""
        evidence = {}
        for key, value in request.model_dump().items():
            if value is not None:
                if isinstance(value, Decimal):
                    evidence[key] = str(value)
                elif hasattr(value, "value"): # Enums
                    evidence[key] = value.value
                else:
                    evidence[key] = value
        return evidence

    @staticmethod
    def generate_explanation(request: ExplanationRequest) -> ExplanationResponse:
        provider = GroqProvider()
        evidence = ExplanationService._prepare_evidence(request)
        return provider.generate_explanation(evidence)
