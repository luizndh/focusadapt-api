package com.focus.adapt.api.infrastructure.integration.gemini;

import com.focus.adapt.api.infrastructure.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient {

    private final RestClient geminiRestClient;
    private final GeminiConfig geminiConfig;
    private RateLimiter rateLimiter;

    @PostConstruct
    public void init() {
        // Instantiate the token bucket rate limiter with the configured RPM
        this.rateLimiter = new RateLimiter(geminiConfig.getRateLimitRpm());
    }

    /**
     * Sends a generation prompt request to the Gemini API with built-in retries, timeouts, and rate limiting.
     */
    public String generateContent(String prompt) {
        String url = String.format("/models/%s:generateContent?key=%s",
                geminiConfig.getModel(), geminiConfig.getApiKey());

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        int attempts = 0;
        int maxAttempts = geminiConfig.getMaxRetries() + 1;
        long backoffMs = 1000; // Starting backoff: 1 second

        while (attempts < maxAttempts) {
            attempts++;
            try {
                // Apply rate limiting before making the HTTP call
                rateLimiter.acquire();

                log.info("GeminiClient: Sending prompt to Gemini API (Attempt {}/{})", attempts, maxAttempts);
                
                GeminiResponse response = geminiRestClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(GeminiResponse.class);

                if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                    throw new BusinessException("Gemini returned an empty response");
                }

                String responseText = response.candidates().get(0)
                        .content().parts().get(0).text();

                log.info("GeminiClient: Response received successfully");
                return responseText;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("GeminiClient request thread interrupted", e);
                throw new BusinessException("AI request execution was interrupted");
            } catch (RestClientResponseException e) {
                log.warn("GeminiClient error response from API (Attempt {}/{}): HTTP {} - {}",
                        attempts, maxAttempts, e.getStatusCode(), e.getResponseBodyAsString());

                if (attempts >= maxAttempts || !isTransientError(e.getStatusCode().value())) {
                    throw new BusinessException("Gemini API call failed with status: " + e.getStatusCode());
                }
            } catch (ResourceAccessException e) {
                // Occurs on read timeouts or network disconnects
                log.warn("GeminiClient connection timeout/network failure (Attempt {}/{}): {}",
                        attempts, maxAttempts, e.getMessage());

                if (attempts >= maxAttempts) {
                    throw new BusinessException("Gemini API request timed out or network was unreachable");
                }
            }

            // Execute backoff delay before the next attempt
            try {
                log.info("GeminiClient: Retrying in {} ms...", backoffMs);
                Thread.sleep(backoffMs);
                backoffMs *= 2; // Exponential backoff
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("Retry sleep interrupted");
            }
        }

        throw new BusinessException("AI request failed after maximum retry attempts");
    }

    private boolean isTransientError(int statusCode) {
        // Retry on 429 (Too many requests) or 5xx (Server issues)
        return statusCode == 429 || (statusCode >= 500 && statusCode < 600);
    }

    // Structured JSON Response records
    private record GeminiResponse(List<Candidate> candidates) {}
    private record Candidate(Content content) {}
    private record Content(List<Part> parts) {}
    private record Part(String text) {}
}
