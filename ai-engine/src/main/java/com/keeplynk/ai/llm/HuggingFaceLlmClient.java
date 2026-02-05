package com.keeplynk.ai.llm;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HuggingFaceLlmClient implements LlmClient {

    @Value("${hf.api.key}")
    private String apiKey;

    private static final String HF_ENDPOINT = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.3";
    
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String generate(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "inputs", prompt,
            "parameters", Map.of(
                "max_new_tokens", 500,
                "temperature", 0.7,
                "return_full_text", false
            )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<List> response = 
                restTemplate.postForEntity(HF_ENDPOINT, request, List.class);

            List<Map<String, Object>> responseBody = response.getBody();
            
            if (responseBody != null && !responseBody.isEmpty()) {
                Map<String, Object> firstResult = responseBody.get(0);
                return firstResult.get("generated_text").toString();
            }
            
            return "AI generation failed (HuggingFace - Empty response)";

        } catch (Exception e) {
            e.printStackTrace();
            return "AI generation failed (HuggingFace)";
        }
    }
}
