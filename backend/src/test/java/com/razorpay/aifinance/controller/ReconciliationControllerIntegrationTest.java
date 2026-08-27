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
@org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer
@SuppressWarnings("null")
class ReconciliationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$", hasSize(100)));
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
                .andExpect(jsonPath("$", hasSize(20)));
    }

    @Test
    void testGetExceptionsByType() throws Exception {
        mockMvc.perform(get("/api/reconciliation/exceptions/AMOUNT_MISMATCH")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].exceptionType", is(ExceptionType.AMOUNT_MISMATCH.name())));
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

    @Autowired
    private org.springframework.test.web.client.MockRestServiceServer mockServer;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Test
    void testGetExplanationException() throws Exception {
        com.razorpay.aifinance.ai.dto.AiExplanationResponse mockResponse = new com.razorpay.aifinance.ai.dto.AiExplanationResponse();
        mockResponse.setPaymentId("PAY0004");
        mockResponse.setSummary("Duplicate Bank Transactions Detected");
        mockResponse.setReasoning("Found 2 transactions");
        mockResponse.setRecommendedAction("Reverse duplicates");

        mockServer.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://localhost:8000/api/explain"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/reconciliation/results/PAY0004/explanation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is("PAY0004")))
                .andExpect(jsonPath("$.overallStatus", is(ExceptionType.DUPLICATE_TRANSACTION.name().equals("DUPLICATE_TRANSACTION") ? "EXCEPTION" : "EXCEPTION")))
                .andExpect(jsonPath("$.exceptionType", is("DUPLICATE_TRANSACTION")))
                .andExpect(jsonPath("$.explanation.summary", is("Duplicate Bank Transactions Detected")));
        
        mockServer.reset();
    }

    @Test
    void testGetExplanationMatch() throws Exception {
        com.razorpay.aifinance.ai.dto.AiExplanationResponse mockResponse = new com.razorpay.aifinance.ai.dto.AiExplanationResponse();
        mockResponse.setPaymentId("PAY0001");
        mockResponse.setSummary("Transaction fully reconciled successfully.");
        mockResponse.setReasoning("Matches perfectly.");
        mockResponse.setRecommendedAction("No action required.");

        mockServer.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://localhost:8000/api/explain"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/reconciliation/results/PAY0001/explanation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is("PAY0001")))
                .andExpect(jsonPath("$.overallStatus", is("MATCH")))
                .andExpect(jsonPath("$.exceptionType", is("NONE")))
                .andExpect(jsonPath("$.explanation.summary", is("Transaction fully reconciled successfully.")));
        
        mockServer.reset();
    }
}
