from app.models.explanation import ExplanationRequest, ExplanationResponse
from app.models.enums import ReconciliationStatus, ExceptionType

class ExplanationService:
    @staticmethod
    def generate_explanation(request: ExplanationRequest) -> ExplanationResponse:
        payment_id = request.paymentId
        
        if request.overallStatus == ReconciliationStatus.MATCH and request.exceptionType == ExceptionType.NONE:
            return ExplanationResponse(
                paymentId=payment_id,
                summary="Transaction fully reconciled successfully.",
                reasoning="The deterministic engine verified that all financial amounts, statuses, and timestamps align perfectly across the order, payment, settlement, and bank transaction boundaries.",
                recommendedAction="No action required."
            )
            
        summary = f"Exception detected: {request.exceptionType.value}"
        reasoning = request.explanation or "The deterministic engine detected a structural anomaly."
        recommended_action = "Investigate the anomaly."
        
        if request.exceptionType == ExceptionType.AMOUNT_MISMATCH:
            summary = "Financial Amount Mismatch"
            reasoning = f"The expected settlement amount (after fees) diverges from the actual bank transaction credit. Payment: {request.paymentAmount}, Settlement Gross: {request.settlementGrossAmount}."
            recommended_action = "Review gateway fee configurations and verify with the banking partner if partial settlements occurred."
            
        elif request.exceptionType == ExceptionType.MISSING_SETTLEMENT:
            summary = "Missing Banking Settlement"
            reasoning = "The transaction was captured by the payment gateway but the corresponding settlement file from the bank is missing."
            recommended_action = "Verify gateway settlement batch timing. If overdue, escalate to the banking partner with the gateway reference."
            
        elif request.exceptionType == ExceptionType.DUPLICATE_TRANSACTION:
            summary = "Duplicate Bank Transactions Detected"
            count = request.bankTransactionCount or 2
            reasoning = f"Multiple distinct bank payout events ({count}) were attached to this single payment identifier."
            recommended_action = "Reverse the duplicate bank credits immediately and audit the gateway webhook deduplication logic."
            
        elif request.exceptionType == ExceptionType.DATE_ANOMALY:
            summary = "Chronological Date Anomaly"
            reasoning = "The settlement timestamps fundamentally break causal physics, such as settling temporally before the payment was even captured."
            recommended_action = "Audit the timezone configurations and upstream chronological event streams."
            
        elif request.exceptionType == ExceptionType.STATUS_MISMATCH:
            summary = "Conflicting Status Graphs"
            reasoning = "The payload represents conflicting state graphs where the gateway status does not naturally align with the bank status."
            recommended_action = "Force a status sync with the gateway API to reconcile the orphaned terminal state."

        return ExplanationResponse(
            paymentId=payment_id,
            summary=summary,
            reasoning=reasoning,
            recommendedAction=recommended_action
        )
