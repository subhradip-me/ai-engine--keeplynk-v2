package com.keeplynk.ai.llm;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiLlmClient implements LlmClient {

    @Value("${llm.gemini.api.key}")
    private String apiKey;

    @Value("${llm.gemini.endpoint}")
    private String endpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String generate(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-goog-api-key", apiKey);   // ✅ CORRECT
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "contents", List.of(
                Map.of(
                    "role", "user",
                    "parts", List.of(
                        Map.of("text", prompt)
                    )
                )
            )
        );

        HttpEntity<Map<String, Object>> request =
            new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response =
                restTemplate.postForEntity(endpoint, request, Map.class);

            Map responseBody = response.getBody();

            List candidates = (List) responseBody.get("candidates");
            Map first = (Map) candidates.get(0);
            Map content = (Map) first.get("content");
            List parts = (List) content.get("parts");
            Map textPart = (Map) parts.get(0);

            return textPart.get("text").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "AI generation failed (Gemini)";
        }
    }
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(8000);
        return new RestTemplate(f);
    }

}


