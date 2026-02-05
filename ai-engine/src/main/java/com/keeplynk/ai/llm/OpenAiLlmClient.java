package com.keeplynk.ai.llm;

import org.springframework.beans.factory.annotation.Value;

// @Component - Disabled: Not configured yet
public class OpenAiLlmClient implements LlmClient {

    @Value("${llm.openai.api.key:}")
    private String apiKey;

    @Override
    public String generate(String prompt) {

        // PSEUDO CODE (we keep it simple for now)
        // Call provider
        // Return text only

        return "AI GENERATED TEXT (REAL LLM)";
    }
}
