package com.keeplynk.ai.controller;

import com.keeplynk.ai.agent.AgentContext;
import com.keeplynk.ai.agent.AgentInput;
import com.keeplynk.ai.decision.AgentDecision;
import com.keeplynk.ai.decision.DecisionEngine;
import com.keeplynk.ai.orchestrator.AgentExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final DecisionEngine decisionEngine;
    private final AgentExecutor agentExecutor;

    public AgentController(
        DecisionEngine decisionEngine,
        AgentExecutor agentExecutor
    ) {
        this.decisionEngine = decisionEngine;
        this.agentExecutor = agentExecutor;
    }

    @PostMapping("/resource/enrich")
    public ResponseEntity<?> enrichResource(@RequestBody AgentInput input) {
        try {
            log.info("Received enrichment request for URL: {}", input.getUrl());

            AgentDecision decision = decisionEngine.decide(input);

            if ("NONE".equals(decision.getAction())) {
                return ResponseEntity.ok(AgentContext.empty(input));
            }

            AgentContext context = AgentContext.from(input);
            context.addReasoning("DecisionEngine selected action: " + decision.getAction());
            context.addReasoning("Reason: " + decision.getReason());
            
            agentExecutor.runResourceAgent(context);

            context.getMemory().put("confidence", decision.getConfidence());

            log.info("Successfully enriched resource for URL: {}", input.getUrl());
            return ResponseEntity.ok(context);
            
        } catch (Exception e) {
            log.error("Error enriching resource", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to enrich resource",
                    "message", e.getMessage(),
                    "type", e.getClass().getSimpleName()
                ));
        }
    }
}
