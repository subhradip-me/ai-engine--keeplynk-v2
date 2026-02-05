package com.keeplynk.ai.llm;

import org.springframework.stereotype.Component;

@Component
public class DummyLlmClient implements LlmClient {

    @Override
    public String generate(String prompt) {
        // Smart mock responses based on prompt content
        String lowerPrompt = prompt.toLowerCase();
        
        // Description generation
        if (lowerPrompt.contains("generate a brief, informative description")) {
            if (lowerPrompt.contains("dribbble") || lowerPrompt.contains("design")) {
                return "A platform for designers to showcase creative work and find design inspiration.";
            } else if (lowerPrompt.contains("github")) {
                return "Code hosting platform for version control and collaboration on software projects.";
            } else if (lowerPrompt.contains("youtube")) {
                return "Video sharing platform for educational content, tutorials, and entertainment.";
            } else if (lowerPrompt.contains("medium")) {
                return "Online publishing platform for articles, blog posts, and thought leadership.";
            } else {
                return "A valuable online resource with relevant content and information.";
            }
        }
        
        // Tag generation
        if (lowerPrompt.contains("generate relevant tags")) {
            if (lowerPrompt.contains("dribbble") || lowerPrompt.contains("design")) {
                return "design, inspiration, portfolio, ui/ux, creative";
            } else if (lowerPrompt.contains("github")) {
                return "coding, development, programming, open-source, collaboration";
            } else if (lowerPrompt.contains("youtube")) {
                return "video, tutorial, learning, education, entertainment";
            } else if (lowerPrompt.contains("medium")) {
                return "article, blog, reading, writing, publishing";
            } else {
                return "resource, web, reference, online, content";
            }
        }
        
        // Category generation
        if (lowerPrompt.contains("categorize") || lowerPrompt.contains("category")) {
            if (lowerPrompt.contains("dribbble") || lowerPrompt.contains("design")) {
                return "Design";
            } else if (lowerPrompt.contains("github")) {
                return "Development";
            } else if (lowerPrompt.contains("youtube")) {
                return "Learning";
            } else if (lowerPrompt.contains("medium")) {
                return "Reading";
            } else {
                return "General";
            }
        }
        
        // Default fallback
        return "AI-generated content";
    }
}
