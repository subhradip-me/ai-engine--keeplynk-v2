# Docker Deployment Guide - AI Engine

Complete guide for deploying the KeepLynk AI Engine using Docker and Docker Compose.

## 📋 Prerequisites

- Docker Engine 20.10+ ([Install Docker](https://docs.docker.com/get-docker/))
- Docker Compose 2.0+ (included with Docker Desktop)
- 2GB+ RAM available
- API Keys:
  - [Google Gemini API](https://makersuite.google.com/app/apikey)
  - [GROQ API](https://console.groq.com/)
  - [Hugging Face](https://huggingface.co/settings/tokens)

## 🚀 Quick Start

### 1. Configure Environment Variables

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your API keys
nano .env  # or use your preferred editor
```

Required variables:
```env
GEMINI_API_KEY=your_actual_gemini_key
GROQ_API_KEY=your_actual_groq_key
HF_API_KEY=your_actual_huggingface_key
```

### 2. Start the Services

```bash
# Build and start all services (AI Engine + MongoDB)
docker-compose up -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

### 3. Verify Deployment

```bash
# Health check
curl http://localhost:8081/actuator/health

# Should return: {"status":"UP"}
```

## 🐳 Docker Commands Reference

### Basic Operations

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Restart services
docker-compose restart

# View logs
docker-compose logs -f ai-engine

# Check status
docker-compose ps
```

### Development Mode

```bash
# Start with build (rebuilds images)
docker-compose up --build

# Start without detached mode (see logs in real-time)
docker-compose up

# Follow logs for specific service
docker-compose logs -f ai-engine
```

### Maintenance

```bash
# Stop and remove containers, networks, volumes
docker-compose down -v

# Remove all unused Docker resources
docker system prune -a

# View container resource usage
docker stats

# Execute command in running container
docker-compose exec ai-engine sh
```

## 🔧 Configuration

### Port Configuration

Change the exposed port in `.env`:
```env
PORT=8082
```

Or override in docker-compose:
```bash
docker-compose up -d --build
```

### Memory Configuration

Adjust JVM memory in `.env`:
```env
JAVA_OPTS=-Xmx1024m -Xms512m -XX:+UseG1GC
```

### MongoDB Configuration

Custom MongoDB credentials in `.env`:
```env
MONGO_ROOT_USER=your_username
MONGO_ROOT_PASSWORD=your_secure_password
MONGO_DATABASE=keeplynk_ai
```

### CORS Configuration

Allow specific origins in `.env`:
```env
ALLOWED_ORIGINS=https://app.keeplynk.com,https://www.keeplynk.com
```

## 🏗️ Production Deployment

### Docker Swarm

Initialize swarm and deploy:
```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.yml keeplynk

# List services
docker service ls

# View service logs
docker service logs keeplynk_ai-engine
```

### Kubernetes

Create Kubernetes manifests:

**deployment.yml**:
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
        - name: GEMINI_API_KEY
          valueFrom:
            secretKeyRef:
              name: ai-engine-secrets
              key: gemini-api-key
        - name: GROQ_API_KEY
          valueFrom:
            secretKeyRef:
              name: ai-engine-secrets
              key: groq-api-key
        - name: HF_API_KEY
          valueFrom:
            secretKeyRef:
              name: ai-engine-secrets
              key: hf-api-key
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: ai-engine-secrets
              key: mongodb-uri
```

**service.yml**:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: ai-engine
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8081
  selector:
    app: ai-engine
```

Deploy:
```bash
kubectl apply -f deployment.yml
kubectl apply -f service.yml
```

### Docker on Railway

Railway can detect and use your Dockerfile automatically:

1. Push code to GitHub
2. Connect repository to Railway
3. Set root directory: `services/ai-engine/ai-engine`
4. Add environment variables in Railway dashboard
5. Railway will use your Dockerfile automatically

### AWS ECS/Fargate

1. Build and push image to ECR:
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Build image
docker build -t keeplynk/ai-engine ./ai-engine

# Tag image
docker tag keeplynk/ai-engine:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/keeplynk-ai-engine:latest

# Push image
docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/keeplynk-ai-engine:latest
```

2. Create ECS task definition with your image
3. Configure environment variables in task definition
4. Create ECS service with load balancer

## 📊 Monitoring & Logs

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f ai-engine

# Last 100 lines
docker-compose logs --tail=100 ai-engine

# Since specific time
docker-compose logs --since 2024-01-09T10:00:00 ai-engine
```

### Health Checks

```bash
# Application health
curl http://localhost:8081/actuator/health

# Detailed health (requires authentication)
curl http://localhost:8081/actuator/health/details

# MongoDB connection
docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')"
```

### Resource Monitoring

```bash
# Real-time stats
docker stats

# Specific container
docker stats keeplynk-ai-engine

# Container inspection
docker inspect keeplynk-ai-engine
```

## 🐛 Troubleshooting

### Container Won't Start

**Check logs:**
```bash
docker-compose logs ai-engine
```

**Common issues:**
- Missing environment variables
- Port already in use
- Insufficient memory

**Solutions:**
```bash
# Check if port is in use
netstat -an | findstr :8081  # Windows
lsof -i :8081               # Mac/Linux

# Increase memory limit
docker-compose down
# Edit JAVA_OPTS in .env
docker-compose up -d
```

### MongoDB Connection Failed

**Check MongoDB is running:**
```bash
docker-compose ps
docker-compose logs mongodb
```

**Test connection:**
```bash
docker-compose exec mongodb mongosh -u admin -p changeme
```

**Restart MongoDB:**
```bash
docker-compose restart mongodb
```

### Build Failures

**Clean build:**
```bash
# Remove old images and rebuild
docker-compose down --rmi all
docker-compose build --no-cache
docker-compose up -d
```

**Maven dependency issues:**
```bash
# Build with fresh dependency download
docker-compose build --build-arg MAVEN_OPTS="-U" --no-cache
```

### High Memory Usage

**Adjust JVM settings in `.env`:**
```env
JAVA_OPTS=-Xmx256m -Xms128m -XX:+UseSerialGC
```

**Restart:**
```bash
docker-compose restart ai-engine
```

## 🔐 Security Best Practices

### 1. Don't Commit Secrets

```bash
# Ensure .env is in .gitignore
echo ".env" >> .gitignore
```

### 2. Use Docker Secrets (Swarm)

```bash
# Create secrets
echo "your_api_key" | docker secret create gemini_api_key -

# Use in docker-compose.yml
services:
  ai-engine:
    secrets:
      - gemini_api_key
```

### 3. Non-Root User

The Dockerfile already uses a non-root user (`spring:1001`)

### 4. Network Isolation

```yaml
# In docker-compose.yml
networks:
  keeplynk-network:
    driver: bridge
    internal: true  # No external access
```

### 5. Read-Only Filesystem

```yaml
# In docker-compose.yml
services:
  ai-engine:
    read_only: true
    tmpfs:
      - /tmp
```

## 📈 Performance Tuning

### JVM Options

**For production (2GB RAM available):**
```env
JAVA_OPTS=-Xmx1536m -Xms1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

**For development (1GB RAM available):**
```env
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseSerialGC
```

**For high-traffic production (4GB+ RAM):**
```env
JAVA_OPTS=-Xmx3072m -Xms2048m -XX:+UseG1GC -XX:+UseStringDeduplication
```

### Docker Resource Limits

In `docker-compose.yml`:
```yaml
services:
  ai-engine:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### Connection Pooling

In `application-production.properties`:
```properties
spring.data.mongodb.max-pool-size=20
spring.data.mongodb.min-pool-size=5
```

## 🔄 CI/CD Integration

### GitHub Actions

**.github/workflows/docker-deploy.yml**:
```yaml
name: Build and Deploy Docker

on:
  push:
    branches: [ main ]
    paths:
      - 'services/ai-engine/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        run: |
          cd services/ai-engine/ai-engine
          docker build -t keeplynk/ai-engine:latest .
      
      - name: Run tests
        run: |
          docker-compose -f services/ai-engine/docker-compose.yml up -d
          sleep 30
          curl -f http://localhost:8081/actuator/health
      
      - name: Push to Docker Hub
        run: |
          echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
          docker push keeplynk/ai-engine:latest
```

### GitLab CI

**.gitlab-ci.yml**:
```yaml
build:
  stage: build
  image: docker:latest
  services:
    - docker:dind
  script:
    - cd services/ai-engine/ai-engine
    - docker build -t keeplynk/ai-engine:latest .
    - docker push keeplynk/ai-engine:latest
```

## 💰 Cost Estimation

### Local Development
- **Free** - Runs on your machine

### Cloud Hosting (estimated monthly)

**Digital Ocean:**
- Basic Droplet (2GB RAM): $12/month
- + Managed MongoDB: $15/month
- **Total**: ~$27/month

**AWS ECS Fargate:**
- 0.5 vCPU, 1GB RAM: ~$15/month
- + RDS or DocumentDB: ~$30/month
- **Total**: ~$45/month

**Google Cloud Run:**
- Auto-scaling instances: ~$10-30/month (based on usage)
- + Cloud MongoDB: ~$20/month
- **Total**: ~$30-50/month

**Railway (recommended):**
- Auto-scaling: ~$20-40/month (usage-based)
- Includes MongoDB
- **Total**: ~$20-40/month

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Best Practices for Writing Dockerfiles](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)

## ✅ Deployment Checklist

- [ ] Docker and Docker Compose installed
- [ ] `.env` file created with all API keys
- [ ] MongoDB credentials configured
- [ ] Ports 8081 and 27017 available
- [ ] At least 2GB RAM available
- [ ] Docker images built successfully
- [ ] Services started: `docker-compose up -d`
- [ ] Health check passes: `/actuator/health`
- [ ] API endpoints working
- [ ] Logs monitored: `docker-compose logs -f`

## 🆘 Getting Help

- **Docker Issues**: [Docker Community Forum](https://forums.docker.com/)
- **Spring Boot**: [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- **Project Issues**: Create issue in GitHub repository

---

**Last Updated**: January 9, 2026  
**Docker Version**: 24.0+  
**Docker Compose Version**: 2.0+
