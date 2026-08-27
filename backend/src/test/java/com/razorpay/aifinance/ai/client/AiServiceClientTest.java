package com.razorpay.aifinance.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.aifinance.ai.dto.AiExplanationRequest;
import com.razorpay.aifinance.ai.dto.AiExplanationResponse;
import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.exception.AiServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiServiceClientTest {

    private AiServiceClient aiServiceClient;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        restTemplate.setUriTemplateHandler(new org.springframework.web.util.DefaultUriBuilderFactory("http://localhost:8000"));
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestClient client = RestClient.create(restTemplate);
        aiServiceClient = new AiServiceClient(client);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetExplanation_Success200() throws Exception {
        AiExplanationRequest request = new AiExplanationRequest();
        request.setPaymentId("PAY0004");
        request.setOverallStatus(ReconciliationStatus.EXCEPTION);
        request.setExceptionType(ExceptionType.DUPLICATE_TRANSACTION);

        AiExplanationResponse mockResponse = new AiExplanationResponse();
        mockResponse.setPaymentId("PAY0004");
        mockResponse.setSummary("Duplicate found");
        mockResponse.setReasoning("Two identical settlements");
        mockResponse.setRecommendedAction("Reverse one");

        mockServer.expect(requestTo("http://localhost:8000/api/explain"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        AiExplanationResponse response = aiServiceClient.getExplanation(request);

        assertNotNull(response);
        assertEquals("PAY0004", response.getPaymentId());
        assertEquals("Duplicate found", response.getSummary());
        mockServer.verify();
    }

    @Test
    void testGetExplanation_400BadRequest() {
        AiExplanationRequest request = new AiExplanationRequest();
        request.setPaymentId("PAY0004");

        mockServer.expect(requestTo("http://localhost:8000/api/explain"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        AiServiceUnavailableException ex = assertThrows(AiServiceUnavailableException.class, () -> {
            aiServiceClient.getExplanation(request);
        });

        assertNotNull(ex);
        mockServer.verify();
    }

    @Test
    void testGetExplanation_500ServerError() {
        AiExplanationRequest request = new AiExplanationRequest();
        request.setPaymentId("PAY0004");

        mockServer.expect(requestTo("http://localhost:8000/api/explain"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        AiServiceUnavailableException ex = assertThrows(AiServiceUnavailableException.class, () -> {
            aiServiceClient.getExplanation(request);
        });

        assertNotNull(ex);
        mockServer.verify();
    }

    @Test
    void testGetExplanation_MalformedResponse() {
        AiExplanationRequest request = new AiExplanationRequest();
        request.setPaymentId("PAY0004");

        mockServer.expect(requestTo("http://localhost:8000/api/explain"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("Not a JSON object", MediaType.APPLICATION_JSON));

        AiServiceUnavailableException ex = assertThrows(AiServiceUnavailableException.class, () -> {
            aiServiceClient.getExplanation(request);
        });

        assertNotNull(ex);
        mockServer.verify();
    }
}
