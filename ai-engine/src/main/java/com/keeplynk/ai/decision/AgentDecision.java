package com.keeplynk.ai.decision;

public class AgentDecision {

    private String action;
    private double confidence;
    private String reason;

    public AgentDecision(String action, double confidence, String reason) {
        this.action = action;
        this.confidence = confidence;
        this.reason = reason;
    }

    public String getAction() {
        return action;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getReason() {
        return reason;
    }
}
