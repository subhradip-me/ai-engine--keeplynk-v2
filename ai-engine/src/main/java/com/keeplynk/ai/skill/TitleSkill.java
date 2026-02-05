package com.keeplynk.ai.skill;

import com.keeplynk.ai.agent.AgentContext;
import com.keeplynk.ai.llm.LlmClient;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class TitleSkill implements Skill {

    private final LlmClient llmClient;

    public TitleSkill(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public void apply(AgentContext context) {
        context.addReasoning("TitleSkill started");
        
        // Build prompt with content if available
        String prompt;
        if (context.getContent() != null && !context.getContent().isEmpty()) {
            prompt = """
                Generate a concise, clear title based on the following content.

                URL: %s
                Persona: %s
                
                Content Preview:
                %s

                Rules:
                - Max 10 words
                - Title should reflect the main topic of the content
                - No emojis
                - No quotes
                - Output title only
                """.formatted(context.getUrl(), context.getPersona(),
                    context.getContent().substring(0, Math.min(500, context.getContent().length())));
        } else {
            prompt = """
                Generate a concise, clear title for the following URL.

                URL: %s
                Persona: %s

                Rules:
                - Max 10 words
                - No emojis
                - No quotes
                - Output title only
                """.formatted(context.getUrl(), context.getPersona());
        }

        String title = llmClient.generate(prompt);
        context.getMemory().put("suggestedTitle", title);
        
        context.addReasoning("TitleSkill generated suggestedTitle" +
            (context.getContent() != null ? " (content-based)" : " (URL-based)"));
    }
}

