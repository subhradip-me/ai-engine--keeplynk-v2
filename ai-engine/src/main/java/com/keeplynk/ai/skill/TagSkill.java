package com.keeplynk.ai.skill;

import com.keeplynk.ai.agent.AgentContext;
import com.keeplynk.ai.llm.LlmClient;
import com.keeplynk.ai.memory.MemoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Order(3)
public class TagSkill implements Skill {

    private final LlmClient llmClient;
    private final MemoryService memoryService;

    public TagSkill(LlmClient llmClient, @Autowired(required = false) MemoryService memoryService) {
        this.llmClient = llmClient;
        this.memoryService = memoryService;
    }

    @Override
    public void apply(AgentContext context) {
        context.addReasoning("TagSkill started");

        // Build prompt with content if available
        String prompt;
        if (context.getContent() != null && !context.getContent().isEmpty()) {
            prompt = """
                Generate relevant tags based on the following content.

                URL: %s
                Persona: %s
                
                Content Preview:
                %s

                Rules:
                - Generate 3-5 relevant tags based on the actual content
                - Tags should be single words or short phrases (max 2 words)
                - Use lowercase
                - Separate tags with commas
                - Focus on key topics, technologies, or themes in the content
                - Output tags only in format: tag1, tag2, tag3
                """.formatted(context.getUrl(), context.getPersona(),
                    context.getContent().substring(0, Math.min(500, context.getContent().length())));
        } else {
            prompt = """
                Generate relevant tags for the following URL.

                URL: %s
                Persona: %s

                Rules:
                - Generate 3-5 relevant tags
                - Tags should be single words or short phrases (max 2 words)
                - Use lowercase
                - Separate tags with commas
                - Output tags only in format: tag1, tag2, tag3
                """.formatted(context.getUrl(), context.getPersona());
        }

        String response = llmClient.generate(prompt);

        List<String> candidateTags = Arrays.stream(response.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();

        List<String> finalTags = candidateTags.stream()
                .map(tag -> {
                    if (memoryService != null && context.getUserId() != null) {
                        return memoryService.reuseOrCreate(tag, context.getUserId());
                    }
                    return tag;
                })
                .distinct()
                .toList();

        context.getMemory().put("tags", finalTags);

        context.addReasoning("TagSkill inferred and reused tags" + 
            (context.getContent() != null ? " (content-based)" : " (URL-based)"));
    }
}
