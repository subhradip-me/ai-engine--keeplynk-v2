package com.keeplynk.ai.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentContext {

    private String resourceId;
    private String url;
    private String persona;
    private String userId;
    private String content;  // Actual page/document content
    private List<String> existingFolders;  // User's existing folder names
    private Map<String, Object> memory = new HashMap<>();
    private List<String> reasoning = new ArrayList<>();
    private Map<String, Boolean> needs; // what AI should do

    public void addReasoning(String step) {
        this.reasoning.add(step);
    }

    public static AgentContext from(AgentInput input) {
        AgentContext context = new AgentContext();
        context.setResourceId(input.getResourceId());
        context.setUrl(input.getUrl());
        context.setPersona(input.getPersona());
        context.setUserId(input.getUserId());
        context.setContent(input.getContent());
        context.setExistingFolders(input.getExistingFolders());
        context.setNeeds(input.getNeeds());
        return context;
    }

    public static AgentContext empty(AgentInput input) {
        AgentContext context = new AgentContext();
        context.setResourceId(input.getResourceId());
        context.setUrl(input.getUrl());
        context.setPersona(input.getPersona());
        context.setUserId(input.getUserId());
        context.setContent(input.getContent());
        context.setExistingFolders(input.getExistingFolders());
        context.setNeeds(input.getNeeds());
        return context;
    }

	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPersona() {
		return persona;
	}
	public void setPersona(String persona) {
		this.persona = persona;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Map<String, Object> getMemory() {
		return memory;
	}
	public void setMemory(Map<String, Object> memory) {
		this.memory = memory;
	}
	public List<String> getReasoning() {
		return reasoning;
	}
	public void setReasoning(List<String> reasoning) {
		this.reasoning = reasoning;
	}
	public Map<String, Boolean> getNeeds() {
		return needs;
	}
	public void setNeeds(Map<String, Boolean> needs) {
		this.needs = needs;
	}

	public List<String> getExistingFolders() {
		return existingFolders;
	}

	public void setExistingFolders(List<String> existingFolders) {
		this.existingFolders = existingFolders;
	}

    
}
