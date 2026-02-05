# AI Engine Documentation

## Table of Contents
1. [Overview](#overview)
2. [What It Does](#what-it-does)
3. [Why This Architecture](#why-this-architecture)
4. [How It Works](#how-it-works)
5. [Architecture](#architecture)
6. [Code Standards](#code-standards)
7. [Execution Layer](#execution-layer)
8. [API Reference](#api-reference)

---

## Overview

The **AI Engine** is a microservice designed for the KeepLynk platform that automatically enriches web resources (links/URLs) with AI-generated metadata. It uses Large Language Models (LLMs) to generate titles, descriptions, and tags for saved links, making them more discoverable and organized.

**Technology Stack:**
- Java 17+
- Spring Boot 3.x
- Maven
- Google Gemini API (LLM Provider)
- RESTful API

---

## What It Does

### Core Functionality
The AI Engine processes resource enrichment requests and generates:

1. **Title Generation**: Creates concise, descriptive titles (max 10 words) for URLs
2. **Description Generation**: Produces informative summaries (max 30 words) explaining what the resource is about
3. **Tag Extraction**: Identifies 3-5 relevant tags/keywords for categorization

### Use Cases
- **Auto-enrichment**: When a user saves a link without metadata, the engine automatically generates it
- **Link Organization**: Helps categorize and organize saved resources
- **Search Optimization**: Generated metadata improves searchability within KeepLynk
- **Personalization**: Takes user persona into account for context-aware generation

---

## Why This Architecture

### Design Principles

#### 1. **Agent-Based Pattern**
The system follows an agent-based architecture where specialized agents handle specific tasks. This provides:
- **Modularity**: Each component has a single, well-defined responsibility
- **Extensibility**: New agents can be added without modifying existing code
- **Testability**: Each agent can be tested in isolation
- **Maintainability**: Changes to one agent don't affect others

#### 2. **Skill-Based Execution**
Skills are atomic units of work that can be composed and reused:
- **Composability**: Skills can be chained in any order
- **Reusability**: Same skill can be used across different agents
- **Ordered Execution**: `@Order` annotation controls execution sequence
- **Independence**: Each skill is self-contained and stateless

#### 3. **Decision Engine Pattern**
Separates decision-making from execution:
- **Separation of Concerns**: Logic for "what to do" is separate from "how to do it"
- **Rule-Based**: Easy to understand and modify business rules
- **Confidence Scoring**: Provides transparency into decision quality
- **Extensible**: New rules can be added without changing execution flow

#### 4. **Context Pattern**
Using `AgentContext` as a shared memory space:
- **Communication**: Skills communicate through shared context
- **Traceability**: Reasoning trail shows execution flow
- **Transparency**: Complete audit trail of what happened and why
- **Flexibility**: Context can carry any data between skills

---

## How It Works

### Request Flow

```
User/System → AgentController → DecisionEngine → AgentExecutor → ResourceAgent → Skills → LLM → Response
```

### Step-by-Step Execution

1. **Request Reception**
   - API receives POST request at `/agent/resource/enrich`
   - Request contains: `resourceId`, `url`, `persona`, `event`

2. **Decision Phase**
   - `DecisionEngine` analyzes input
   - Applies business rules to determine action
   - Returns decision with confidence score

3. **Context Creation**
   - If decision is "ENRICH", create `AgentContext`
   - Context initialized with input data
   - Reasoning trail started

4. **Agent Execution**
   - `AgentExecutor` invokes `ResourceAgent`
   - Agent iterates through all skills in order

5. **Skill Application**
   - **TitleSkill** (Order 1): Generates title using LLM
   - **DescriptionSkill** (Order 2): Generates description using LLM
   - **TagSkill** (Order 3): Generates tags using LLM
   - Each skill updates context memory with results

6. **Response**
   - Controller returns enriched `AgentContext`
   - Contains: generated metadata, confidence score, reasoning trail

### Example Request/Response

**Request:**
```json
POST /agent/resource/enrich
{
  "resourceId": "res_123",
  "url": "https://example.com/article",
  "persona": "software developer",
  "event": "LINK_SAVED"
}
```

**Response:**
```json
{
  "resourceId": "res_123",
  "url": "https://example.com/article",
  "persona": "software developer",
  "memory": {
    "suggestedTitle": "Advanced Java Design Patterns Tutorial",
    "description": "Comprehensive guide covering enterprise design patterns in Java with practical examples and best practices",
    "tags": "java, design patterns, tutorial, programming, software",
    "confidence": 0.65
  },
  "reasoning": [
    "DecisionEngine selected action: ENRICH",
    "Reason: New link detected, enriching with metadata",
    "TitleSkill started",
    "TitleSkill generated suggestedTitle",
    "DescriptionSkill started",
    "DescriptionSkill generated description",
    "TagSkill started",
    "TagSkill inferred tags"
  ]
}
```

---

## Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────┐
│             AgentController                      │
│  (REST API - Entry Point)                       │
└───────────────┬─────────────────────────────────┘
                │
                ├──→ DecisionEngine
                │    (Business Rules & Logic)
                │
                └──→ AgentExecutor
                     (Orchestration Layer)
                     │
                     └──→ ResourceAgent
                          (Agent Implementation)
                          │
                          ├──→ TitleSkill (Order 1)
                          ├──→ DescriptionSkill (Order 2)
                          └──→ TagSkill (Order 3)
                               │
                               └──→ LlmClient Interface
                                    │
                                    ├──→ GeminiLlmClient (Active)
                                    ├──→ OpenAiLlmClient (Available)
                                    └──→ DummyLlmClient (Testing)
```

### Component Breakdown

#### 1. **Controller Layer**
- **AgentController**: REST endpoint for resource enrichment
- **HealthController**: Health check endpoint
- **Responsibility**: HTTP handling, request validation, response formatting

#### 2. **Decision Layer**
- **DecisionEngine**: Evaluates input and decides action
- **AgentDecision**: Value object containing decision details
- **Responsibility**: Business logic, rule evaluation, confidence scoring

#### 3. **Orchestration Layer**
- **AgentExecutor**: Coordinates agent execution
- **Responsibility**: Agent lifecycle management, execution flow control

#### 4. **Agent Layer**
- **Agent** (Interface): Contract for all agents
- **ResourceAgent**: Implementation for resource enrichment
- **AgentContext**: Shared state and memory
- **AgentInput**: Input data transfer object
- **Responsibility**: Task execution, skill coordination

#### 5. **Skill Layer**
- **Skill** (Interface): Contract for all skills
- **TitleSkill**: Generates titles
- **DescriptionSkill**: Generates descriptions
- **TagSkill**: Generates tags
- **Responsibility**: Atomic operations, LLM interaction

#### 6. **LLM Layer**
- **LlmClient** (Interface): Abstraction for LLM providers
- **GeminiLlmClient**: Google Gemini integration (primary)
- **OpenAiLlmClient**: OpenAI integration (alternative)
- **DummyLlmClient**: Mock implementation for testing
- **Responsibility**: External API integration, prompt execution

---

## Code Standards

### Package Structure
```
com.keeplynk.ai
├── agent/           # Agent implementations and context
├── controller/      # REST controllers
├── decision/        # Decision engine logic
├── llm/            # LLM client implementations
├── orchestrator/   # Execution coordination
└── skill/          # Skill implementations
```

### Naming Conventions

#### Classes
- **Agents**: `*Agent` suffix (e.g., `ResourceAgent`)
- **Skills**: `*Skill` suffix (e.g., `TitleSkill`)
- **Controllers**: `*Controller` suffix (e.g., `AgentController`)
- **Services**: `*Service` or `*Engine` suffix (e.g., `DecisionEngine`)
- **Clients**: `*Client` suffix (e.g., `LlmClient`)

#### Methods
- Use descriptive, verb-based names
- `apply()` for skill execution
- `execute()` for agent execution
- `decide()` for decision logic
- `generate()` for LLM calls

#### Variables
- CamelCase for local variables
- Meaningful names (avoid single letters except loop counters)
- `context` for AgentContext
- `input` for AgentInput

### Design Patterns Used

#### 1. **Strategy Pattern**
- **Where**: LlmClient implementations
- **Why**: Allow switching between different LLM providers without changing code
- **Example**: `GeminiLlmClient`, `OpenAiLlmClient`, `DummyLlmClient`

#### 2. **Chain of Responsibility**
- **Where**: Skill execution in ResourceAgent
- **Why**: Sequential processing where each skill adds to the context
- **Example**: TitleSkill → DescriptionSkill → TagSkill

#### 3. **Dependency Injection**
- **Where**: Throughout the application (Spring-managed)
- **Why**: Loose coupling, testability, configuration flexibility
- **Example**: All `@Component` and `@Service` classes

#### 4. **Template Method**
- **Where**: Skill interface
- **Why**: Define skeleton of skill execution
- **Example**: All skills implement `apply(AgentContext)`

#### 5. **Factory Pattern**
- **Where**: AgentContext creation
- **Why**: Centralized object creation with different configurations
- **Example**: `AgentContext.from()`, `AgentContext.empty()`

### Spring Annotations

- **`@SpringBootApplication`**: Main application class
- **`@RestController`**: REST API controllers
- **`@Service`**: Business logic services
- **`@Component`**: General Spring-managed beans
- **`@Order(n)`**: Skill execution order (lower numbers execute first)
- **`@RequestMapping`**: API path mapping
- **`@PostMapping`**: HTTP POST endpoints
- **`@RequestBody`**: JSON request binding

### Code Quality Rules

1. **Single Responsibility**: Each class has one clear purpose
2. **Dependency Injection**: Constructor-based DI only
3. **Immutability**: Prefer final fields and immutable objects where possible
4. **Interface Segregation**: Small, focused interfaces
5. **Documentation**: Javadoc for public APIs
6. **Error Handling**: Proper exception handling (to be implemented)
7. **Logging**: Use SLF4J for logging (to be implemented)

---

## Execution Layer

### Skill Execution Order

Skills are executed sequentially using Spring's `@Order` annotation:

```java
@Order(1) → TitleSkill
@Order(2) → DescriptionSkill
@Order(3) → TagSkill
```

### Execution Flow Detail

#### Phase 1: Initialization
```java
AgentContext context = AgentContext.from(input);
context.addReasoning("DecisionEngine selected action: ENRICH");
```

#### Phase 2: Skill Pipeline
```java
for (Skill skill : skills) {  // Ordered by @Order annotation
    skill.apply(context);     // Each skill modifies context
}
```

#### Phase 3: Skill Execution (TitleSkill Example)

1. **Start**: Log reasoning
   ```java
   context.addReasoning("TitleSkill started");
   ```

2. **Prepare Prompt**: Format prompt with URL and persona
   ```java
   String prompt = """
       Generate a concise, clear title for the following URL.
       URL: %s
       Persona: %s
       Rules:
       - Max 10 words
       - No emojis
       - No quotes
       - Output title only
   """.formatted(context.getUrl(), context.getPersona());
   ```

3. **Call LLM**: Generate content
   ```java
   String title = llmClient.generate(prompt);
   ```

4. **Store Result**: Update context memory
   ```java
   context.getMemory().put("suggestedTitle", title);
   ```

5. **Complete**: Log completion
   ```java
   context.addReasoning("TitleSkill generated suggestedTitle");
   ```

### LLM Client Execution

#### Gemini API Call Flow

1. **Prepare Request**
   ```java
   JSONObject requestBody = new JSONObject();
   requestBody.put("contents", new JSONArray()
       .put(new JSONObject()
           .put("parts", new JSONArray()
               .put(new JSONObject().put("text", prompt)))));
   ```

2. **HTTP POST**
   ```java
   HttpRequest request = HttpRequest.newBuilder()
       .uri(URI.create(endpoint + "?key=" + apiKey))
       .header("Content-Type", "application/json")
       .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
       .build();
   ```

3. **Parse Response**
   ```java
   JSONObject jsonResponse = new JSONObject(body);
   return jsonResponse
       .getJSONArray("candidates")
       .getJSONObject(0)
       .getJSONObject("content")
       .getJSONArray("parts")
       .getJSONObject(0)
       .getString("text")
       .trim();
   ```

### Context Memory Structure

The context maintains a map of generated results:

```java
Map<String, Object> memory = {
    "suggestedTitle": "Generated Title",
    "description": "Generated Description",
    "tags": "tag1, tag2, tag3",
    "confidence": 0.65
}
```

### Reasoning Trail

Provides complete audit trail:

```java
List<String> reasoning = [
    "DecisionEngine selected action: ENRICH",
    "Reason: New link detected, enriching with metadata",
    "TitleSkill started",
    "TitleSkill generated suggestedTitle",
    "DescriptionSkill started",
    "DescriptionSkill generated description",
    "TagSkill started",
    "TagSkill inferred tags"
]
```

### Decision Rules

The DecisionEngine evaluates input using these rules (in order):

1. **Rule 1**: If `resourceId` AND `url` present → ENRICH (confidence: 0.75)
2. **Rule 2**: If `event` is null → NONE (confidence: 0.1)
3. **Rule 3**: If event is "RESOURCE_ENRICH" → ENRICH (confidence: 0.75)
4. **Rule 4**: If event is "LINK_SAVED" → ENRICH (confidence: 0.65)
5. **Default**: Unhandled event → NONE (confidence: 0.05)

### Thread Safety

- **Stateless Services**: All services are stateless (thread-safe)
- **Immutable Inputs**: AgentInput is effectively immutable during processing
- **Context Isolation**: Each request gets its own AgentContext instance
- **Concurrent Requests**: Multiple requests can be processed simultaneously

---

## API Reference

### Endpoints

#### 1. Resource Enrichment
```
POST /agent/resource/enrich
```

**Request Body:**
```json
{
  "resourceId": "string",      // Required: Resource identifier
  "url": "string",             // Required: URL to enrich
  "persona": "string",         // Optional: User persona for context
  "event": "string",           // Optional: Event type (LINK_SAVED, RESOURCE_ENRICH)
  "userId": "string",          // Optional: User identifier
  "contentType": "string"      // Optional: Content type hint
}
```

**Response:**
```json
{
  "resourceId": "string",
  "url": "string",
  "persona": "string",
  "memory": {
    "suggestedTitle": "string",
    "description": "string",
    "tags": "string",
    "confidence": "number"
  },
  "reasoning": ["string"]
}
```

**Status Codes:**
- `200 OK`: Successful enrichment
- `400 Bad Request`: Invalid input
- `500 Internal Server Error`: Processing error

#### 2. Health Check
```
GET /health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2026-01-04T..."
}
```

### Configuration

**application.properties:**
```properties
# Server Configuration
spring.application.name=ai-engine
server.port=8081

# Gemini API Configuration
llm.gemini.api.key=YOUR_API_KEY
llm.gemini.endpoint=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
```

### Environment Variables

- `GEMINI_API_KEY`: Google Gemini API key (overrides properties file)
- `SERVER_PORT`: Application port (default: 8081)

---

## Future Enhancements

### Planned Features
1. **Caching**: Cache LLM responses to reduce API calls
2. **Rate Limiting**: Protect against API quota exhaustion
3. **Async Processing**: Queue-based processing for high volume
4. **Multiple Personas**: Support for different user personas
5. **Content Analysis**: Fetch and analyze page content
6. **Custom Prompts**: User-configurable prompt templates
7. **Metrics**: Prometheus metrics for monitoring
8. **Error Recovery**: Retry logic and circuit breakers

### Extensibility Points
- Add new Skills: Implement `Skill` interface
- Add LLM Providers: Implement `LlmClient` interface
- Add Agents: Implement `Agent` interface
- Add Decision Rules: Extend `DecisionEngine`

---

## Development

### Running Locally
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Access
http://localhost:8081
```

### Testing
```bash
# Run all tests
mvn test

# Test endpoint
curl -X POST http://localhost:8081/agent/resource/enrich \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": "test_1",
    "url": "https://example.com",
    "persona": "developer",
    "event": "LINK_SAVED"
  }'
```

---

**Last Updated**: January 4, 2026  
**Version**: 1.0.0  
**Maintained By**: KeepLynk Team