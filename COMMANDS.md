# Quick Command Reference

## 🚀 Getting Started

```bash
# Setup and start
docker-compose up -d

# Check health
curl http://localhost:8081/actuator/health
```

## 📊 Monitoring

```bash
# View all logs
docker-compose logs -f

# View AI Engine logs only
docker-compose logs -f ai-engine

# Check container status
docker-compose ps

# Resource usage
docker stats
```

## 🔄 Container Management

```bash
# Restart services
docker-compose restart

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Rebuild images
docker-compose build --no-cache

# Rebuild and restart
docker-compose up --build -d
```

## 🐛 Debugging

```bash
# Enter container shell
docker-compose exec ai-engine sh

# View container details
docker inspect keeplynk-ai-engine

# Check environment variables
docker-compose exec ai-engine env

# Test MongoDB connection
docker-compose exec mongodb mongosh
```

## 🧹 Cleanup

```bash
# Stop and remove everything
docker-compose down -v --rmi all

# Remove unused Docker resources
docker system prune -a

# Remove specific image
docker rmi keeplynk/ai-engine
```

## ⚙️ Configuration

```bash
# Edit environment variables
nano .env

# Restart after config change
docker-compose restart

# Verify environment loaded
docker-compose config
```

## 📈 Production Commands

```bash
# Deploy to Docker Swarm
docker stack deploy -c docker-compose.yml keeplynk

# Scale service
docker service scale keeplynk_ai-engine=3

# Update service
docker service update --image keeplynk/ai-engine:latest keeplynk_ai-engine
```

---

See [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) for complete guide.
