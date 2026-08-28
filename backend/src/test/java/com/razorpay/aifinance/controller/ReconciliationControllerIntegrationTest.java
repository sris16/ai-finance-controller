package com.razorpay.aifinance.controller;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.data.path=../data"})
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(ReconciliationControllerIntegrationTest.AiServiceMockConfig.class)
@SuppressWarnings("null")
class ReconciliationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.razorpay.aifinance.repository.ReconciliationRunRepository runRepository;

    @Autowired
    private com.razorpay.aifinance.service.ReconciliationService reconciliationService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        if (runRepository.count() == 0) {
            reconciliationService.executeReconciliationRun();
            while (!runRepository.existsByStatus(com.razorpay.aifinance.domain.enums.RunStatus.COMPLETED)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    void testGetReport() throws Exception {
        mockMvc.perform(get("/api/reconciliation/report")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords", is(100)))
                .andExpect(jsonPath("$.matchedRecords", is(80)))
                .andExpect(jsonPath("$.exceptionRecords", is(20)))
                .andExpect(jsonPath("$.matchRate", is(80.00)))
                .andExpect(jsonPath("$.exceptionRate", is(20.00)))
                .andExpect(jsonPath("$.exceptionBreakdown.AMOUNT_MISMATCH", is(4)))
                .andExpect(jsonPath("$.exceptionBreakdown.MISSING_SETTLEMENT", is(4)))
                .andExpect(jsonPath("$.exceptionBreakdown.DUPLICATE_TRANSACTION", is(4)))
                .andExpect(jsonPath("$.exceptionBreakdown.DATE_ANOMALY", is(4)))
                .andExpect(jsonPath("$.exceptionBreakdown.STATUS_MISMATCH", is(4)));
    }

    @Test
    void testGetResults() throws Exception {
        mockMvc.perform(get("/api/reconciliation/results")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.totalElements", is(100)));
    }

    @Test
    void testGetResultsPaginationBounds() throws Exception {
        mockMvc.perform(get("/api/reconciliation/results?page=-1&size=20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/reconciliation/results?page=0&size=0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/reconciliation/results?page=0&size=101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetResultByPaymentId() throws Exception {
        // Find a payment with DUPLICATE_TRANSACTION in the synthetic dataset.
        // I'll test PAY0004 as instructed in the prompt if it has it, or just assert structure.
        // Wait, I will just request PAY0004
        mockMvc.perform(get("/api/reconciliation/results/PAY0004")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is("PAY0004")))
                .andExpect(jsonPath("$.overallStatus", notNullValue()))
                .andExpect(jsonPath("$.paymentDate", notNullValue()))
                .andExpect(jsonPath("$.paymentAmount", notNullValue()));
    }

    @Test
    void testGetResultByPaymentIdNotFound() throws Exception {
        mockMvc.perform(get("/api/reconciliation/results/DOES_NOT_EXIST")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("No reconciliation result found for payment DOES_NOT_EXIST")));
    }

    @Test
    void testGetExceptions() throws Exception {
        mockMvc.perform(get("/api/reconciliation/exceptions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.totalElements", is(20)));
    }

    @Test
    void testGetExceptionsByType() throws Exception {
        mockMvc.perform(get("/api/reconciliation/exceptions/AMOUNT_MISMATCH")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[0].exceptionType", is(ExceptionType.AMOUNT_MISMATCH.name())))
                .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void testInvalidExceptionType() throws Exception {
        mockMvc.perform(get("/api/reconciliation/exceptions/INVALID_ENUM_VALUE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void testHealthEndpointStillWorks() throws Exception {
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class AiServiceMockConfig {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public com.razorpay.aifinance.ai.client.AiServiceClient mockAiServiceClient() {
            return new com.razorpay.aifinance.ai.client.AiServiceClient("http://localhost:8000", org.springframework.web.client.RestClient.builder()) {
                @Override
                public com.razorpay.aifinance.ai.dto.AiExplanationResponse getExplanation(com.razorpay.aifinance.ai.dto.AiExplanationRequest request) {
                    com.razorpay.aifinance.ai.dto.AiExplanationResponse mockResponse = new com.razorpay.aifinance.ai.dto.AiExplanationResponse();
                    mockResponse.setPaymentId(request.getPaymentId());
                    if ("PAY0004".equals(request.getPaymentId())) {
                        mockResponse.setSummary("This transaction appears to be a MATCH.");
                        mockResponse.setReasoning("Found 2 transactions");
                        mockResponse.setRecommendedAction("Reverse duplicates");
                    } else if ("PAY0001".equals(request.getPaymentId())) {
                        mockResponse.setSummary("Transaction fully reconciled successfully.");
                        mockResponse.setReasoning("Matches perfectly.");
                        mockResponse.setRecommendedAction("No action required.");
                    }
                    return mockResponse;
                }
            };
        }
    }

    @Test
    void testGetExplanationException() throws Exception {
        mockMvc.perform(get("/api/reconciliation/results/PAY0004/explanation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is("PAY0004")))
                .andExpect(jsonPath("$.overallStatus", is("EXCEPTION")))
                .andExpect(jsonPath("$.exceptionType", is("DUPLICATE_TRANSACTION")))
                .andExpect(jsonPath("$.explanation.summary", is("This transaction appears to be a MATCH.")));
    }

    @Test
    void testGetExplanationMatch() throws Exception {
        mockMvc.perform(get("/api/reconciliation/results/PAY0001/explanation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is("PAY0001")))
                .andExpect(jsonPath("$.overallStatus", is("MATCH")))
                .andExpect(jsonPath("$.exceptionType", is("NONE")))
                .andExpect(jsonPath("$.explanation.summary", is("Transaction fully reconciled successfully.")));
    }
}
