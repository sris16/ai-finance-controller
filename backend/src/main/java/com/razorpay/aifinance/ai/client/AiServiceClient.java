package com.razorpay.aifinance.ai.client;

import com.razorpay.aifinance.ai.dto.AiExplanationRequest;
import com.razorpay.aifinance.ai.dto.AiExplanationResponse;
import com.razorpay.aifinance.exception.AiServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@SuppressWarnings("null")
public class AiServiceClient {

    private final RestClient restClient;

    @org.springframework.beans.factory.annotation.Autowired
    public AiServiceClient(@Value("${ai-service.url}") String aiServiceUrl, RestClient.Builder restClientBuilder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds connection timeout
        factory.setReadTimeout(10000); // 10 seconds read timeout

        this.restClient = restClientBuilder
                .baseUrl(aiServiceUrl)
                .requestFactory(factory)
                .build();
    }

    public AiServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }



    public AiExplanationResponse getExplanation(AiExplanationRequest request) {
        try {
            return restClient.post()
                    .uri("/api/explain")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                        throw new AiServiceUnavailableException("AI Service responded with error: " + res.getStatusCode());
                    })
                    .body(AiExplanationResponse.class);
        } catch (RestClientException ex) {
            throw new AiServiceUnavailableException("Failed to communicate with AI Service", ex);
        }
    }
}
