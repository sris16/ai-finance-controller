from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.health import router as health_router
from app.api.explain import router as explain_router
from app.utils.config import settings

app = FastAPI(
    title=settings.app_name,
    version=settings.version,
    description="FastAPI Service providing AI exception investigation and root-cause analysis for financial reconciliation."
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health_router, tags=["Health"])
app.include_router(explain_router, prefix="/api", tags=["Explain"])

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.host, port=settings.port, reload=True)
