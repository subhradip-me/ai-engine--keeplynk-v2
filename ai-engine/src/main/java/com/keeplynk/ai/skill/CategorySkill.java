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

        // Get existing folders for intelligent reuse
        String existingFoldersText = "";
        if (context.getExistingFolders() != null && !context.getExistingFolders().isEmpty()) {
            existingFoldersText = "\n\nUser's Existing Folders:\n" + String.join(", ", context.getExistingFolders());
        }

        // Build prompt with content if available
        String prompt;
        if (context.getContent() != null && !context.getContent().isEmpty()) {
            prompt = """
                Categorize the following content into ONE category/folder name.

                URL: %s
                Persona: %s
                
                Content Preview:
                %s
                %s

                Rules:
                - Choose ONE category based on the actual content
                - PREFER reusing one of the user's existing folders if it matches the content theme
                - Only create a new folder name if none of the existing folders are suitable
                - Use simple, clear category names
                - Category should reflect the main topic/theme of the content
                - Output category name only
                """.formatted(context.getUrl(), context.getPersona(),
                    context.getContent().substring(0, Math.min(500, context.getContent().length())),
                    existingFoldersText);
        } else {
            prompt = """
                Categorize the following URL into ONE category/folder name.

                URL: %s
                Persona: %s
                %s

                Rules:
                - Choose ONE category
                - PREFER reusing one of the user's existing folders if it matches
                - Only create a new folder name if none of the existing folders are suitable
                - Use simple, clear category names
                - Output category name only
                """.formatted(context.getUrl(), context.getPersona(), existingFoldersText);
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

