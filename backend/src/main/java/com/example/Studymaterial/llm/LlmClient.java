package com.example.Studymaterial.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Thin wrapper around any OpenAI-compatible "chat completions" API. */
@Component
public class LlmClient {

    private final RestClient http;
    private final String model;
    private final boolean configured;

    public LlmClient(@Value("${llm.base-url}") String baseUrl,
                     @Value("${llm.api-key:}") String apiKey,
                     @Value("${llm.model}") String model,
                     @Value("${llm.timeout-seconds:120}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(15));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.model = model;
        this.configured = apiKey != null && !apiKey.isBlank();
        this.http = RestClient.builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl)
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /** Sends one request and returns the model's text (expected to be JSON). */
    public String chatJson(String system, String user) {
        if (!configured) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "LLM is not configured. Set the LLM_API_KEY environment variable and restart the backend.");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.4,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)));

        try {
            ChatResponse res = http.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(ChatResponse.class);

            if (res == null || res.choices() == null || res.choices().isEmpty()
                    || res.choices().get(0).message() == null
                    || res.choices().get(0).message().content() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The LLM returned an empty answer");
            }
            return res.choices().get(0).message().content();

        } catch (RestClientResponseException e) {
            String detail = e.getResponseBodyAsString();
            if (detail.length() > 300) detail = detail.substring(0, 300);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "LLM request failed (" + e.getStatusCode().value() + "): " + detail);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Cannot reach the LLM service (check internet / base-url / timeout)");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatResponse(List<Choice> choices) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String content) {}
}
