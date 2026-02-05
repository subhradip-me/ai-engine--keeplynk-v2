package com.keeplynk.ai.skill;

import com.keeplynk.ai.agent.AgentContext;
import com.keeplynk.ai.llm.LlmClient;
import com.keeplynk.ai.memory.MemoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Category / Folder

@Component
@Order(4)
public class CategorySkill implements Skill {

    private final LlmClient llmClient;
    private final MemoryService memoryService;

    public CategorySkill(LlmClient llmClient, @Autowired(required = false) MemoryService memoryService) {
        this.llmClient = llmClient;
        this.memoryService = memoryService;
    }

    @Override
    public void apply(AgentContext context) {
        context.addReasoning("CategorySkill started");

        // Build prompt with content if available
        String prompt;
        if (context.getContent() != null && !context.getContent().isEmpty()) {
            prompt = """
                Categorize the following content into ONE category/folder name.

                URL: %s
                Persona: %s
                
                Content Preview:
                %s

                Rules:
                - Choose ONE category based on the actual content
                - Use simple, clear category names
                - Category should reflect the main topic/theme of the content
                - Output category name only
                """.formatted(context.getUrl(), context.getPersona(),
                    context.getContent().substring(0, Math.min(500, context.getContent().length())));
        } else {
            prompt = """
                Categorize the following URL into ONE category/folder name.

                URL: %s
                Persona: %s

                Rules:
                - Choose ONE category
                - Use simple, clear category names
                - Output category name only
                """.formatted(context.getUrl(), context.getPersona());
        }

        String rawCategory = llmClient.generate(prompt);

        String finalCategory = rawCategory;
        if (memoryService != null && context.getUserId() != null) {
            finalCategory = memoryService.reuseOrCreateCategory(rawCategory, context.getUserId());
        }

        context.getMemory().put("category", finalCategory);

        context.addReasoning(
            "CategorySkill reused category: " + finalCategory +
            (context.getContent() != null ? " (content-based)" : " (URL-based)")
        );
    }
}

