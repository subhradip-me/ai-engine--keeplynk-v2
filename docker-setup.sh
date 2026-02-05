#!/bin/bash

# Quick Docker Setup Script for AI Engine
echo "🐳 KeepLynk AI Engine - Docker Setup"
echo "===================================="

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    echo "   Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed."
    exit 1
fi

echo "✅ Docker is ready"
echo ""

# Check for .env file
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp .env.example .env
    echo "⚠️  Please edit .env file with your API keys before continuing!"
    echo ""
    read -p "Have you updated .env with your API keys? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Please update .env file and run this script again."
        exit 1
    fi
fi

echo "🔨 Building Docker images..."
docker-compose build

echo ""
echo "🚀 Starting services..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

echo ""
echo "🏥 Checking health..."
for i in {1..10}; do
    if curl -f http://localhost:8081/actuator/health &> /dev/null; then
        echo "✅ AI Engine is healthy!"
        break
    fi
    echo "   Attempt $i/10 - waiting..."
    sleep 3
done

echo ""
echo "===================================="
echo "✅ Setup complete!"
echo ""
echo "📊 View logs:    docker-compose logs -f"
echo "🔍 Status:       docker-compose ps"
echo "🛑 Stop:         docker-compose down"
echo "🔄 Restart:      docker-compose restart"
echo ""
echo "🌐 API endpoint: http://localhost:8081"
echo "🏥 Health check: http://localhost:8081/actuator/health"
echo ""
echo "📚 Full guide: ./DOCKER_DEPLOYMENT.md"
echo "===================================="
