from abc import ABC, abstractmethod
from app.models.explanation import ExplanationResponse

class LLMProvider(ABC):
    @abstractmethod
    def generate_explanation(self, evidence: dict) -> ExplanationResponse:
        """
        Takes a structured dictionary of evidence and returns a structured ExplanationResponse.
        """
        pass
