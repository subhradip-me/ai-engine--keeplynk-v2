# AI Engine

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-Build-orange)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)

**AI Engine** is an intelligent agent orchestration system for KeepLynk. It provides decision-making capabilities, content analysis, and automated agent workflows to enhance user experience through smart content management and personalization.

The engine uses a modular, skill-based architecture with multiple LLM providers to automatically generate metadata (titles, descriptions, tags) for saved web resources, making them more discoverable and organized.

## 🚀 Quick Start with Docker

```bash
# 1. Configure environment
cp .env.example .env
# Edit .env with your API keys

# 2. Start services
docker-compose up -d

# 3. Verify health
curl http://localhost:8081/actuator/health
```

📚 **[Docker Deployment Guide](DOCKER_DEPLOYMENT.md)** | **[Railway Deployment](RAILWAY_DEPLOYMENT_GUIDE.md)**

## 📋 Table of Contents

- [Deploy to Railway](#-deploy-to-railway)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Code Standards](#code-standards)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## ✨ Features

### Core Functionality
- **Intelligent Resource Enrichment**: Automatically generate metadata for saved web resources
  - **Title Generation**: Concise, descriptive titles (max 10 words)
  - **Description Generation**: Informative summaries (max 30 words)
  - **Tag Extraction**: 3-5 relevant tags for categorization
  - **Category Classification**: Automatic content categorization

### Architecture Features
- **Event-Driven Decision Engine**: Process user events and determine appropriate actions with confidence scoring
- **Agent Orchestration**: Coordinate multiple AI agents for complex workflows
- **Skill-Based Architecture**: Modular, ordered skill system for content analysis
- **Multi-LLM Support**: Intelligent fallback system across multiple providers
  - Primary: **Groq API** (llama-3.3-70b-versatile)
  - Fallback 1: **Google Gemini** (gemini-2.5-flash)
  - Fallback 2: **Hugging Face** (Mistral-7B-Instruct-v0.3)
  - Testing: **DummyLlmClient** for offline development
- **Rule-Based Decision Making**: Transparent AI decisions with reasoning traces
- **RESTful API**: Clean REST endpoints for easy integration
- **Health Monitoring**: Built-in health checks for service monitoring
- **Persona-Aware Processing**: Context-aware generation based on user profiles

## 🏗️ Architecture

The AI Engine follows a modular, layered architecture:

```
┌─────────────────────────────────────┐
│         REST Controllers            │  ← API Layer
├─────────────────────────────────────┤
│      Decision Engine                │  ← Core Logic
├─────────────────────────────────────┤
│   Agent Orchestrator & Executor     │  ← Orchestration
├─────────────────────────────────────┤
│    Skills (Title, Tags, Desc)       │  ← Capabilities
├─────────────────────────────────────┤
│       LLM Client Interface          │  ← AI Integration
└─────────────────────────────────────┘
```

### Core Components

1. **Agent Layer** (`agent/`)
   - `Agent`: Base agent interface for task execution
   - `ResourceAgent`: Manages resource-based enrichment operations
   - `AgentInput`: Input data models for agent requests (event, userId, contentType)
   - `AgentContext`: Context management with shared memory and input tracking

2. **Decision Engine** (`decision/`)
   - `DecisionEngine`: Core decision-making logic with rule-based system
   - `AgentDecision`: Decision output with action, confidence score, and reasoning chain

3. **Skills** (`skill/`)
   - `TitleSkill` (Order: 1): Generates concise titles for URLs
   - `DescriptionSkill` (Order: 2): Creates informative content descriptions
   - `TagSkill` (Order: 3): Extracts and suggests relevant tags
   - `CategorySkill` (Order: 4): Classifies content into categories
   - Each skill is independently testable and follows single responsibility principle

4. **LLM Integration** (`llm/`)
   - `LlmClient`: Interface for LLM providers (Strategy Pattern)
   - `GroqLlmClient`: Primary provider with llama-3.3-70b-versatile (marked `@Primary`)
   - `GeminiLlmClient`: Google Gemini integration (gemini-2.5-flash)
   - `HuggingFaceLlmClient`: Hugging Face inference API (Mistral-7B-Instruct-v0.3)
   - `OpenAiLlmClient`: OpenAI integration (alternative provider)
   - `DummyLlmClient`: Mock implementation for testing and offline development
   - **Intelligent Fallback**: Automatic failover from Groq → Gemini → HuggingFace

5. **Orchestration** (`orchestrator/`)
   - `AgentExecutor`: Coordinates multi-agent workflows and manages execution lifecycle

6. **Controllers** (`controller/`)
   - `AgentController`: Resource enrichment endpoint (`/agent/resource/enrich`)
   - `HealthController`: Service health monitoring endpoint (`/health`)

## 📦 Prerequisites

- **Java**: JDK 21 or higher
- **Maven**: 3.8+ (included via Maven Wrapper)
- **Memory**: Minimum 512MB RAM
- **OS**: Windows, Linux, or macOS

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ai-engine
```

### 2. Build the Project

Using Maven Wrapper (recommended):

```bash
# Windows
.\mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

Or with system Maven:

```bash
mvn clean install
```

### 3. Run the Application

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

The service will start on `http://localhost:8081`

## ⚙️ Configuration

Configuration is managed through `src/main/resources/application.properties`:

```properties
# Application Settings
spring.application.name=ai-engine
server.port=8081

# Google Gemini API Configuration
llm.gemini.api.key=YOUR_GEMINI_API_KEY
llm.gemini.endpoint=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent

# Groq API Configuration (Primary LLM)
GROQ_API_KEY=YOUR_GROQ_API_KEY
groq.api.key=${GROQ_API_KEY}

# Hugging Face API Configuration (Fallback)
HF_API_KEY=YOUR_HUGGINGFACE_API_KEY
hf.api.key=${HF_API_KEY}
```

### Required API Keys

1. **Groq API** (Primary - Recommended)
   - Sign up at: https://console.groq.com
   - Free tier available with generous limits
   - Used for: Fast inference with llama-3.3-70b-versatile

2. **Google Gemini API** (Fallback 1)
   - Sign up at: https://ai.google.dev
   - Free tier available
   - Used for: Backup when Groq is unavailable

3. **Hugging Face API** (Fallback 2)
   - Sign up at: https://huggingface.co
   - Free inference API available
   - Used for: Final fallback with Mistral-7B-Instruct

### Environment Variables

You can override properties using environment variables:

```bash
export SERVER_PORT=8082
export GROQ_API_KEY=your-groq-api-key
export GEMINI_API_KEY=your-gemini-api-key
export HF_API_KEY=your-hf-api-key
```

### LLM Fallback Chain

The system automatically falls back through providers if one fails:
1. **Groq** (Primary) - Fast, reliable, high-quality
2. **Gemini** (Fallback 1) - Google's LLM, good quality
3. **Hugging Face** (Fallback 2) - Open-source models
4. **Error** - All providers failed

Each failure is logged with details for debugging.

## 📚 API Documentation

### Health Check

Check service status:

```http
GET /health
```

**Response:**
```json
{
  "status": "OK",
  "service": "ai-engine"
}
```

### Resource Enrichment

Enrich a resource with AI-powered metadata:

```http
POST /agent/resource/enrich
Content-Type: application/json

{
  "event": "LINK_SAVED",
  "userId": "user123",
  "contentType": "article"
}
```

**Success Response (200 OK):**
```json
{
  "input": {
    "event": "LINK_SAVED",
    "userId": "user123",
    "contentType": "article"
  },
  "memory": {
    "confidence": 0.2
  }
}
```

**No Enrichment Needed (200 OK):**
```json
{
  "timestamp": "2026-01-03T10:30:00.000+00:00",
  "status": 200,
  "error": "OK",
  "message": "No enrichment needed",
  "path": "/agent/resource/enrich"
}
```

### Event Types

Currently supported events:
- `LINK_SAVED`: User saves a link/bookmark
- `LINK_UPDATED`: User updates link metadata
- `RESOURCE_SHARED`: Resource is shared with others
- More events coming soon...

### Decision Actions

Possible agent actions based on event analysis:
- `NONE`: No action required (resource doesn't need enrichment)
- `ENRICH`: Enrich content with AI-generated metadata (executes resource agent)
- `CATEGORIZE`: Categorize the content
- `RECOMMEND`: Generate recommendations
- `UPDATE`: Update existing metadata

### LLM Providers

#### Groq (Primary)
```java
@Component
@Primary
public class GroqLlmClient implements LlmClient {
    // Uses llama-3.3-70b-versatile model
    // Endpoint: https://api.groq.com/openai/v1/chat/completions
}
```

#### Gemini (Fallback 1)
```java
@Component
public class GeminiLlmClient implements LlmClient {
    // Uses gemini-2.5-flash model
    // Endpoint configured in application.properties
}
```

#### Hugging Face (Fallback 2)
```java
@Component
public class HuggingFaceLlmClient implements LlmClient {
    // Uses mistralai/Mistral-7B-Instruct-v0.3
    // Endpoint: https://api-inference.huggingface.co/models/...
}
```

## 🎨 Code Standards

### Package Structure
```
com.keeplynk.ai/
├── AiEngineApplication.java    # Main Spring Boot application
├── agent/                       # Agent implementations and context
│   ├── Agent.java              # Base agent interface
│   ├── AgentContext.java       # Shared execution context
│   ├── AgentInput.java         # Input data transfer object
│   └── ResourceAgent.java      # Resource enrichment agent
├── controller/                  # REST API endpoints
│   ├── AgentController.java    # /agent/resource/enrich
│   └── HealthController.java   # /health
├── decision/                    # Decision making logic
│   ├── AgentDecision.java      # Decision output model
│   └── DecisionEngine.java     # Rule-based decision engine
├── llm/                        # LLM provider integrations
│   ├── LlmClient.java          # Provider interface
│   ├── GroqLlmClient.java      # Groq implementation (Primary)
│   ├── GeminiLlmClient.java    # Google Gemini implementation
│   ├── HuggingFaceLlmClient.java # Hugging Face implementation
│   ├── OpenAiLlmClient.java    # OpenAI implementation
│   └── DummyLlmClient.java     # Mock for testing
├── orchestrator/               # Execution coordination
│   └── AgentExecutor.java      # Agent lifecycle management
└── skill/                      # Modular skill implementations
    ├── Skill.java              # Base skill interface
    ├── TitleSkill.java         # @Order(1) - Title generation
    ├── DescriptionSkill.java   # @Order(2) - Description generation
    ├── TagSkill.java           # @Order(3) - Tag extraction
    └── CategorySkill.java      # @Order(4) - Categorization
```

### Naming Conventions

#### Classes
- **Agents**: `*Agent` suffix (e.g., `ResourceAgent`)
- **Skills**: `*Skill` suffix (e.g., `TitleSkill`)
- **Controllers**: `*Controller` suffix (e.g., `AgentController`)
- **Services**: `*Service` or `*Engine` suffix (e.g., `DecisionEngine`)
- **Clients**: `*Client` suffix (e.g., `LlmClient`, `GroqLlmClient`)
- **DTOs**: Descriptive noun (e.g., `AgentInput`, `AgentDecision`)

#### Methods
- Use descriptive, verb-based names
- `apply(AgentContext)` for skill execution
- `execute()` for agent execution
- `decide()` for decision logic
- `generate(String prompt)` for LLM calls
- `from()` for factory methods

#### Variables
- camelCase for local variables
- Meaningful names (avoid single letters except loop counters like `i`, `j`)
- `context` for AgentContext instances
- `input` for AgentInput instances
- `decision` for AgentDecision instances

### Design Patterns

#### 1. **Strategy Pattern**
- **Where**: LlmClient implementations
- **Why**: Switch between LLM providers without code changes
- **Example**:
```java
public interface LlmClient {
    String generate(String prompt);
}

@Component
@Primary
public class GroqLlmClient implements LlmClient { ... }

@Component
public class GeminiLlmClient implements LlmClient { ... }
```

#### 2. **Chain of Responsibility**
- **Where**: Skill execution in ResourceAgent
- **Why**: Sequential processing where each skill adds to context
- **Example**: TitleSkill → DescriptionSkill → TagSkill → CategorySkill

#### 3. **Dependency Injection**
- **Where**: Throughout the application (Spring-managed)
- **Why**: Loose coupling, testability, configuration flexibility
- **Example**: All `@Component`, `@Service`, `@RestController` classes

#### 4. **Template Method**
- **Where**: Skill interface
- **Why**: Define skeleton of skill execution
- **Example**:
```java
public interface Skill {
    void apply(AgentContext context);
}
```

#### 5. **Factory Pattern**
- **Where**: AgentContext creation
- **Why**: Centralized object creation
- **Example**: `AgentContext.from(AgentInput)`

### Spring Annotations

#### Component Scanning
- `@SpringBootApplication` - Main application class
- `@Component` - General Spring-managed beans
- `@Service` - Business logic services
- `@RestController` - REST API controllers
- `@Primary` - Marks default implementation when multiple beans exist

#### Execution Control
- `@Order(n)` - Skill execution order (lower numbers first)
  - `@Order(1)` → TitleSkill
  - `@Order(2)` → DescriptionSkill
  - `@Order(3)` → TagSkill
  - `@Order(4)` → CategorySkill

#### Dependency Injection
- `@Autowired` - Constructor injection (preferred)
- `@Lazy` - Lazy initialization to prevent circular dependencies
- `@Value` - Property injection from application.properties

#### API Mapping
- `@RequestMapping` - Base path mapping
- `@PostMapping` - HTTP POST endpoints
- `@GetMapping` - HTTP GET endpoints
- `@RequestBody` - JSON request binding

### Code Quality Rules

1. **Single Responsibility Principle**
   - Each class has one clear, well-defined purpose
   - Skills are atomic and independent

2. **Dependency Injection**
   - Constructor-based DI only (no field injection)
   - Use `@Lazy` to resolve circular dependencies

3. **Immutability**
   - Prefer `final` fields
   - Immutable objects where possible

4. **Interface Segregation**
   - Small, focused interfaces
   - One method per interface when appropriate

5. **Error Handling**
   - Proper exception handling with fallbacks
   - Log errors with context
   - Return graceful error messages

6. **Documentation**
   - Javadoc for public APIs
   - Inline comments for complex logic
   - README for setup and usage

7. **Testing**
   - Unit tests for business logic
   - Integration tests for API endpoints
   - Mock external dependencies

### Skill Development Guidelines

When creating new skills:

```java
package com.keeplynk.ai.skill;

import com.keeplynk.ai.agent.AgentContext;
import com.keeplynk.ai.llm.LlmClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)  // Set execution order
public class NewSkill implements Skill {
    
    private final LlmClient llmClient;
    
    public NewSkill(LlmClient llmClient) {
        this.llmClient = llmClient;
    }
    
    @Override
    public void apply(AgentContext context) {
        // 1. Log start
        context.addReasoning("NewSkill started");
        
        // 2. Prepare prompt
        String prompt = """
            Your prompt here with context:
            URL: %s
            Persona: %s
            """.formatted(context.getUrl(), context.getPersona());
        
        // 3. Call LLM
        String result = llmClient.generate(prompt);
        
        // 4. Store in context
        context.getMemory().put("skillResult", result);
        
        // 5. Log completion
        context.addReasoning("NewSkill completed");
    }
}
```

### LLM Client Development Guidelines

When adding new LLM providers:

```java
package com.keeplynk.ai.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component  // Don't use @Primary unless this is the default
public class NewLlmClient implements LlmClient {
    
    @Value("${new.api.key}")
    private String apiKey;
    
    private static final String ENDPOINT = "https://api.example.com/v1/...";
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public String generate(String prompt) {
        try {
            // 1. Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 2. Prepare request body
            Map<String, Object> body = Map.of(
                "prompt", prompt,
                "max_tokens", 500
            );
            
            // 3. Make HTTP request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(ENDPOINT, request, Map.class);
            
            // 4. Parse response
            Map<String, Object> responseBody = response.getBody();
            return responseBody.get("text").toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "AI generation failed (NewLLM)";
        }
    }
}
```

## 📂 Project Structure

```
ai-engine/
├── src/
│   ├── main/
│   │   ├── java/com/keeplynk/ai/
│   │   │   ├── AiEngineApplication.java           # Main Spring Boot application
│   │   │   ├── agent/                             # Agent domain models
│   │   │   │   ├── Agent.java                     # Base agent interface
│   │   │   │   ├── AgentContext.java              # Execution context with memory
│   │   │   │   ├── AgentInput.java                # Input DTO (event, userId, etc.)
│   │   │   │   └── ResourceAgent.java             # Resource enrichment agent
│   │   │   ├── controller/                        # REST API endpoints
│   │   │   │   ├── AgentController.java           # /agent/resource/enrich
│   │   │   │   └── HealthController.java          # /health
│   │   │   ├── decision/                          # Decision engine
│   │   │   │   ├── AgentDecision.java             # Decision output model
│   │   │   │   └── DecisionEngine.java            # Rule-based decision logic
│   │   │   ├── llm/                               # LLM integrations
│   │   │   │   ├── LlmClient.java                 # Provider interface
│   │   │   │   ├── GroqLlmClient.java             # Groq (@Primary)
│   │   │   │   ├── GeminiLlmClient.java           # Google Gemini
│   │   │   │   ├── HuggingFaceLlmClient.java      # Hugging Face
│   │   │   │   ├── OpenAiLlmClient.java           # OpenAI
│   │   │   │   └── DummyLlmClient.java            # Mock for testing
│   │   │   ├── orchestrator/                      # Orchestration layer
│   │   │   │   └── AgentExecutor.java             # Agent lifecycle manager
│   │   │   └── skill/                             # Modular skills
│   │   │       ├── Skill.java                     # Base skill interface
│   │   │       ├── TitleSkill.java                # @Order(1)
│   │   │       ├── DescriptionSkill.java          # @Order(2)
│   │   │       ├── TagSkill.java                  # @Order(3)
│   │   │       └── CategorySkill.java             # @Order(4)
│   │   └── resources/
│   │       └── application.properties             # Configuration (API keys, etc.)
│   └── test/
│       └── java/com/keeplynk/ai/
│           └── AiEngineApplicationTests.java      # Integration tests
├── target/                                         # Build output
│   └── ai-engine-0.0.1-SNAPSHOT.jar              # Executable JAR
├── pom.xml                                         # Maven dependencies
├── mvnw                                            # Maven wrapper (Unix)
├── mvnw.cmd                                        # Maven wrapper (Windows)
├── README.md                                       # This file
├── DOC.md                                          # Detailed architecture docs
└── HELP.md                                         # Spring Boot help reference
```

## 💻 Development

### Running in Development Mode

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Using DummyLlmClient for Offline Development

To develop without API keys, temporarily change `@Primary` annotation:

```java
// In DummyLlmClient.java
@Component
@Primary  // Add this annotation
public class DummyLlmClient implements LlmClient {
    // ...
}

// In GroqLlmClient.java
@Component
// @Primary  // Comment out this annotation
public class GroqLlmClient implements LlmClient {
    // ...
}
```

Or use Spring profiles to switch implementations.

### Code Style & Conventions

This project follows:
- **Spring Boot conventions** for structure and configuration
- **Strategy Pattern** for LLM providers
- **Chain of Responsibility** for skill execution
- **Dependency Injection** throughout (constructor-based)
- **Single Responsibility Principle** for all classes
- **Ordered execution** using `@Order` annotation

### Adding New Skills

1. Create a new class in `src/main/java/com/keeplynk/ai/skill/`
2. Implement the `Skill` interface
3. Add `@Component` and `@Order(n)` annotations
4. Inject `LlmClient` via constructor
5. Implement `apply(AgentContext context)` method

Example:
```java
package com.keeplynk.ai.skill;

import com.keeplynk.ai.agent.AgentContext;
import com.keeplynk.ai.llm.LlmClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class SummarySkill implements Skill {
    
    private final LlmClient llmClient;
    
    public SummarySkill(LlmClient llmClient) {
        this.llmClient = llmClient;
    }
    
    @Override
    public void apply(AgentContext context) {
        context.addReasoning("SummarySkill started");
        
        String prompt = """
            Generate a summary for: %s
            Persona: %s
            """.formatted(context.getUrl(), context.getPersona());
        
        String summary = llmClient.generate(prompt);
        context.getMemory().put("summary", summary);
        
        context.addReasoning("SummarySkill completed");
    }
}
```

The skill will automatically be picked up by Spring and executed in order.

### Adding New LLM Providers

1. Create a new class in `src/main/java/com/keeplynk/ai/llm/`
2. Implement the `LlmClient` interface
3. Add `@Component` annotation
4. Use `@Primary` if this should be the default provider
5. Configure API key in `application.properties`

Example:
```java
package com.keeplynk.ai.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AnthropicLlmClient implements LlmClient {
    
    @Value("${anthropic.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public String generate(String prompt) {
        // Implementation
        return "Generated text";
    }
}
```

### Adding New Event Types

1. Open `src/main/java/com/keeplynk/ai/decision/DecisionEngine.java`
2. Add new event type to decision logic:

```java
public AgentDecision decide(AgentInput input) {
    String event = input.getEvent();
    
    if ("NEW_EVENT_TYPE".equals(event)) {
        return new AgentDecision(
            "ENRICH",
            0.9,
            "New event type detected, enrichment recommended"
        );
    }
    // ... existing logic
}
```

3. Update API documentation in this README

### Hot Reload During Development

Spring Boot DevTools enables automatic restart on code changes:

```xml
<!-- Already included in pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

Simply save files and the application will restart automatically.

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test

```bash
./mvnw test -Dtest=AiEngineApplicationTests
```

### Integration Testing

Test the health endpoint:
```bash
curl http://localhost:8081/health
```

Test resource enrichment:
```bash
curl -X POST http://localhost:8081/agent/resource/enrich \
  -H "Content-Type: application/json" \
  -d '{"event":"LINK_SAVED","userId":"test","contentType":"article"}'
```

## 🚢 Deployment

### Building for Production

```bash
./mvnw clean package -DskipTests
```

The executable JAR will be created in `target/ai-engine-0.0.1-SNAPSHOT.jar`

### Running the Production Build

```bash
java -jar target/ai-engine-0.0.1-SNAPSHOT.jar
```

### Production Configuration

Set environment variables for production:

```bash
export SERVER_PORT=8081
export GROQ_API_KEY=your-production-groq-key
export GEMINI_API_KEY=your-production-gemini-key
export HF_API_KEY=your-production-hf-key
export SPRING_PROFILES_ACTIVE=prod
```

### Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ai-engine-0.0.1-SNAPSHOT.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/health || exit 1

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
# Build the image
docker build -t keeplynk/ai-engine:latest .

# Run the container
docker run -d \
  -p 8081:8081 \
  -e GROQ_API_KEY=${GROQ_API_KEY} \
  -e GEMINI_API_KEY=${GEMINI_API_KEY} \
  -e HF_API_KEY=${HF_API_KEY} \
  --name ai-engine \
  keeplynk/ai-engine:latest
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  ai-engine:
    build: .
    ports:
      - "8081:8081"
    environment:
      - GROQ_API_KEY=${GROQ_API_KEY}
      - GEMINI_API_KEY=${GEMINI_API_KEY}
      - HF_API_KEY=${HF_API_KEY}
      - SPRING_PROFILES_ACTIVE=prod
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8081/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 30s
    restart: unless-stopped
```

Run with:

```bash
docker-compose up -d
```

### Kubernetes Deployment

Create `k8s-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ai-engine
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ai-engine
  template:
    metadata:
      labels:
        app: ai-engine
    spec:
      containers:
      - name: ai-engine
        image: keeplynk/ai-engine:latest
        ports:
        - containerPort: 8081
        env:
        - name: GROQ_API_KEY
          valueFrom:
            secretKeyRef:
              name: llm-secrets
              key: groq-api-key
        - name: GEMINI_API_KEY
          valueFrom:
            secretKeyRef:
              name: llm-secrets
              key: gemini-api-key
        - name: HF_API_KEY
          valueFrom:
            secretKeyRef:
              name: llm-secrets
              key: hf-api-key
        livenessProbe:
          httpGet:
            path: /health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 8081
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ai-engine
spec:
  selector:
    app: ai-engine
  ports:
  - port: 8081
    targetPort: 8081
  type: ClusterIP
```

Deploy:

```bash
kubectl apply -f k8s-deployment.yaml
```

## 🔧 Troubleshooting

### Common Issues

#### 1. API Key Not Working

**Symptoms**: `AI generation failed` errors in logs

**Solutions**:
- Verify API keys are correctly set in `application.properties`
- Check environment variables are exported
- Ensure API keys have sufficient quota
- Test each provider individually

```bash
# Test Groq
curl https://api.groq.com/openai/v1/models \
  -H "Authorization: Bearer ${GROQ_API_KEY}"

# Test Gemini
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}" \
  -H 'Content-Type: application/json' \
  -d '{"contents":[{"parts":[{"text":"Hello"}]}]}'
```

#### 2. Port Already in Use

**Symptoms**: `Address already in use: bind`

**Solutions**:
```bash
# Change port in application.properties
server.port=8082

# Or use environment variable
export SERVER_PORT=8082
./mvnw spring-boot:run
```

#### 3. All LLM Providers Failing

**Symptoms**: `AI generation failed - all providers unavailable`

**Solutions**:
1. Check internet connectivity
2. Verify all API keys
3. Check API provider status pages
4. Review application logs for detailed errors
5. Use `DummyLlmClient` for testing

#### 4. Skill Not Executing

**Symptoms**: Expected metadata not generated

**Solutions**:
- Verify skill has `@Component` annotation
- Check `@Order` number is correct
- Ensure skill implements `Skill` interface
- Review logs for reasoning trace

#### 5. Build Failures

**Symptoms**: Maven build errors

**Solutions**:
```bash
# Clean build
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests

# Update dependencies
./mvnw dependency:purge-local-repository
```

### Debugging

Enable debug logging in `application.properties`:

```properties
# Debug logging
logging.level.com.keeplynk.ai=DEBUG
logging.level.org.springframework.web=DEBUG

# Trace all HTTP requests
logging.level.org.springframework.web.client.RestTemplate=TRACE
```

View detailed logs:

```bash
# Run with debug output
./mvnw spring-boot:run -Dspring-boot.run.arguments=--debug

# Check specific package logs
./mvnw spring-boot:run -Dlogging.level.com.keeplynk.ai=DEBUG
```

### Health Check Endpoints

Monitor service health:

```bash
# Basic health check
curl http://localhost:8081/health

# Detailed status (if Spring Actuator enabled)
curl http://localhost:8081/actuator/health
```

### Performance Tuning

Adjust timeouts for LLM calls:

```java
@Bean
public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);  // 5 seconds
    factory.setReadTimeout(30000);    // 30 seconds
    return new RestTemplate(factory);
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 Roadmap

### Phase 1: Core Functionality (✅ Completed)
- [x] Event-driven decision engine
- [x] Agent orchestration system
- [x] Skill-based architecture
- [x] Multi-LLM provider support with fallback
- [x] Basic REST API endpoints
- [x] Title, description, and tag generation
- [x] Category classification

### Phase 2: Enhancement (🚧 In Progress)
- [x] User preference memory layer
- [ ] Advanced caching for LLM responses
- [ ] Async processing for heavy workloads
- [ ] Rate limiting and quota management
- [ ] Comprehensive logging with structured output
- [ ] Metrics and monitoring integration
- [ ] Spring Actuator for observability

### Phase 3: Intelligence (🔮 Planned)
- [ ] Machine learning models for confidence scoring
- [ ] Personalized recommendations based on history
- [ ] Sentiment analysis skill
- [ ] Language detection and translation
- [ ] Image analysis integration
- [ ] Content quality scoring
- [ ] Duplicate detection

### Phase 4: Scale & Performance (🔮 Planned)
- [ ] Redis caching layer
- [ ] Message queue integration (RabbitMQ/Kafka)
- [ ] Horizontal scaling support
- [ ] Load balancing configuration
- [ ] Database integration for persistent memory
- [ ] Batch processing support
- [ ] GraphQL API support

### Phase 5: Advanced Features (🔮 Planned)
- [ ] Admin dashboard for agent management
- [ ] A/B testing framework for prompts
- [ ] Custom skill marketplace
- [ ] Multi-language support
- [ ] Fine-tuned models for specific domains
- [ ] Webhook support for events
- [ ] Real-time notifications

## 📄 License

Copyright © 2026 KeepLynk. All rights reserved.

## 🆘 Support

### Getting Help

For issues and questions:

1. **Documentation**
   - Check [DOC.md](DOC.md) for detailed architecture documentation
   - Review [HELP.md](HELP.md) for Spring Boot reference
   - Read this README thoroughly

2. **GitHub Issues**
   - Search existing issues before creating new ones
   - Provide detailed reproduction steps
   - Include logs and error messages
   - Tag appropriately (bug, enhancement, question)

3. **Development Team**
   - Contact through project communication channels
   - Join development discussions
   - Participate in code reviews

### Reporting Bugs

When reporting bugs, please include:

```
**Environment:**
- OS: [e.g., Windows 11, Ubuntu 22.04]
- Java Version: [e.g., Java 21]
- Spring Boot Version: [e.g., 4.0.1]

**Steps to Reproduce:**
1. Step one
2. Step two
3. Step three

**Expected Behavior:**
What should happen

**Actual Behavior:**
What actually happens

**Logs:**
```
Paste relevant logs here
```

**Additional Context:**
Any other relevant information
```

### Feature Requests

For new features:
- Explain the use case
- Describe expected behavior
- Suggest implementation approach (optional)
- Consider impact on existing features

---

## 🙏 Acknowledgments

Built with these amazing technologies:
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Groq](https://groq.com/) - Primary LLM provider
- [Google Gemini](https://ai.google.dev/) - Fallback LLM provider
- [Hugging Face](https://huggingface.co/) - Open-source LLM provider
- [Maven](https://maven.apache.org/) - Build automation

---

Built with ❤️ by the KeepLynk team | Copyright © 2026 KeepLynk. All rights reserved.
