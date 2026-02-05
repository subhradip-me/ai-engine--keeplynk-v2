package com.keeplynk.ai.llm;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Primary
public class GroqLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GroqLlmClient.class);

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
    @Lazy
    private GeminiLlmClient geminiLlmClient;
    
    @Autowired
    @Lazy
    private HuggingFaceLlmClient huggingFaceLlmClient;

    @Override
    public String generate(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(
                Map.of(
                    "role", "user",
                    "content", prompt
                )
            ),
            "temperature", 0.7,
            "max_tokens", 500
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = 
                restTemplate.postForEntity(GROQ_ENDPOINT, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            
            return message.get("content").toString();

        } catch (Exception e) {
            log.error("Groq API failed, falling back to Gemini", e);
            
            try {
                return geminiLlmClient.generate(prompt);
            } catch (Exception e2) {
                log.error("Gemini API failed, falling back to HuggingFace", e2);
                
                try {
                    return huggingFaceLlmClient.generate(prompt);
                } catch (Exception e3) {
                    log.error("All LLM providers failed", e3);
                    return "AI generation failed - all providers unavailable";
                }
            }
        }
    }
}
