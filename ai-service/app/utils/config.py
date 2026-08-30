import os
from pydantic import BaseModel

class Settings(BaseModel):
    app_name: str = "AI Finance Controller - AI Service"
    version: str = "1.0.0"
    port: int = int(os.getenv("AI_SERVICE_PORT", "8000"))
    host: str = os.getenv("AI_SERVICE_HOST", "0.0.0.0")
    groq_api_key: str = os.getenv("GROQ_API_KEY", "")
    ai_model: str = os.getenv("AI_MODEL", "openai/gpt-oss-120b")

settings = Settings()
