@echo off
REM Quick Docker Setup Script for AI Engine

echo.
echo 🐳 KeepLynk AI Engine - Docker Setup
echo ====================================
echo.

REM Check Docker
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker is not installed. Please install Docker first.
    echo    Visit: https://docs.docker.com/get-docker/
    exit /b 1
)

where docker-compose >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker Compose is not installed.
    exit /b 1
)

echo ✅ Docker is ready
echo.

REM Check for .env file
if not exist .env (
    echo 📝 Creating .env file from template...
    copy .env.example .env
    echo ⚠️  Please edit .env file with your API keys before continuing!
    echo.
    set /p continue="Have you updated .env with your API keys? (y/n): "
    if /i not "%continue%"=="y" (
        echo Please update .env file and run this script again.
        exit /b 1
    )
)

echo 🔨 Building Docker images...
docker-compose build

echo.
echo 🚀 Starting services...
docker-compose up -d

echo.
echo ⏳ Waiting for services to be ready...
timeout /t 10 /nobreak > nul

echo.
echo 🏥 Checking health...
for /l %%i in (1,1,10) do (
    curl -f http://localhost:8081/actuator/health >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo ✅ AI Engine is healthy!
        goto :healthy
    )
    echo    Attempt %%i/10 - waiting...
    timeout /t 3 /nobreak > nul
)

:healthy
echo.
echo ====================================
echo ✅ Setup complete!
echo.
echo 📊 View logs:    docker-compose logs -f
echo 🔍 Status:       docker-compose ps
echo 🛑 Stop:         docker-compose down
echo 🔄 Restart:      docker-compose restart
echo.
echo 🌐 API endpoint: http://localhost:8081
echo 🏥 Health check: http://localhost:8081/actuator/health
echo.
echo 📚 Full guide: .\DOCKER_DEPLOYMENT.md
echo ====================================
echo.
pause
