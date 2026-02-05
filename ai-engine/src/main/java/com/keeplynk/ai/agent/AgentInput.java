package com.keeplynk.ai.agent;

import java.util.Map;

public class AgentInput {

    private String resourceId;
    private String url;
    private String persona;
    private String event;
    private String userId;
    private String contentType;
    private String content;  // Actual page/document content for AI analysis
    
    // New fields for Auto Organise feature
    private String existingTitle;
    private String existingDescription;
    private Map<String, Boolean> needs; // what AI should do: {title: true, description: false, tags: true}

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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExistingTitle() {
        return existingTitle;
    }

    public void setExistingTitle(String existingTitle) {
        this.existingTitle = existingTitle;
    }

    public String getExistingDescription() {
        return existingDescription;
    }

    public void setExistingDescription(String existingDescription) {
        this.existingDescription = existingDescription;
    }

    public Map<String, Boolean> getNeeds() {
        return needs;
    }

    public void setNeeds(Map<String, Boolean> needs) {
        this.needs = needs;
    }
}
