from fastapi import APIRouter
from datetime import datetime, timezone
from app.utils.config import settings

router = APIRouter()

@router.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": settings.app_name,
        "version": settings.version,
        "timestamp": datetime.now(timezone.utc).isoformat()
    }
