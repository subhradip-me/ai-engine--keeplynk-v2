package com.keeplynk.ai.decision;

import com.keeplynk.ai.agent.AgentInput;
import org.springframework.stereotype.Service;

@Service
public class DecisionEngine {

    public AgentDecision decide(AgentInput input) {

        // Rule 1: If resourceId and URL present, enrich by default
        if (input.getResourceId() != null && input.getUrl() != null) {
            return new AgentDecision(
                "ENRICH",
                0.75,
                "Resource needs enrichment with AI-generated metadata"
            );
        }

        // Rule 2: If event is missing → do nothing
        if (input.getEvent() == null) {
            return new AgentDecision(
                "NONE",
                0.1,
                "Missing event type and resource information"
            );
        }

        // Rule 3: Resource enrichment event
        if ("RESOURCE_ENRICH".equals(input.getEvent())) {
            return new AgentDecision(
                "ENRICH",
                0.75,
                "Resource needs enrichment with AI-generated metadata"
            );
        }

        // Rule 4: Link saved event
        if ("LINK_SAVED".equals(input.getEvent())) {
            return new AgentDecision(
                "ENRICH",
                0.65,
                "New link detected, enriching with metadata"
            );
        }

        // Default fallback
        return new AgentDecision(
            "NONE",
            0.05,
            "Unhandled event"
        );
    }
}
