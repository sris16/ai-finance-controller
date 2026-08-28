package com.razorpay.aifinance.ai.service;

import com.razorpay.aifinance.ai.client.AiServiceClient;
import com.razorpay.aifinance.ai.dto.AiExplanationRequest;
import com.razorpay.aifinance.ai.dto.AiExplanationResponse;
import com.razorpay.aifinance.ai.dto.ReconciliationExplanationResponse;
import com.razorpay.aifinance.domain.model.ReconciliationResult;
import org.springframework.stereotype.Service;

@Service
public class AiIntegrationService {

    private final AiServiceClient aiServiceClient;

    public AiIntegrationService(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    public ReconciliationExplanationResponse generateExplanation(ReconciliationResult result) {
        // Map deterministic result to AI request, sending only required minimum fields
        AiExplanationRequest request = new AiExplanationRequest();
        request.setPaymentId(result.getPaymentId());
        request.setOverallStatus(result.getOverallStatus());
        request.setExceptionType(result.getExceptionType());
        request.setPaymentAmount(result.getPaymentAmount());
        request.setSettlementGrossAmount(result.getSettlementGrossAmount());
        request.setSettlementPresent(result.getSettlementPresent());
        request.setBankTransactionCount(result.getBankTransactionCount());

        // Pass the deterministic explanation natively computed by the Java engine as baseline reasoning
        request.setExplanation(result.getExplanation());

        // Get AI Explanation (or throw AiServiceUnavailableException natively handled)
        AiExplanationResponse explanationResponse = aiServiceClient.getExplanation(request);

        // Combine deterministic classification with AI explanation
        return new ReconciliationExplanationResponse(
                result.getPaymentId(),
                result.getOverallStatus(),
                result.getExceptionType(),
                explanationResponse
        );
    }
}
