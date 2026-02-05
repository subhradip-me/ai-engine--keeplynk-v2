package com.keeplynk.ai.orchestrator;

import com.keeplynk.ai.agent.AgentContext;
import com.keeplynk.ai.agent.ResourceAgent;
import org.springframework.stereotype.Service;

@Service
public class AgentExecutor {

    private final ResourceAgent resourceAgent;

    public AgentExecutor(ResourceAgent resourceAgent) {
        this.resourceAgent = resourceAgent;
    }

    public void runResourceAgent(AgentContext context) {
        resourceAgent.execute(context);
    }
}
