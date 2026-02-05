# Railway Deployment Guide for AI Engine Service

Quick guide to deploy the KeepLynk AI Engine on Railway. Railway will automatically detect and use the Dockerfile.

## 📋 Prerequisites

- [Railway account](https://railway.app/) (sign up with GitHub)
- API keys: [Gemini](https://makersuite.google.com/app/apikey), [GROQ](https://console.groq.com/), [Hugging Face](https://huggingface.co/settings/tokens)
- Git repository with code pushed to GitHub

## 🚀 Quick Deploy

### Step 1: Connect to Railway

1. Go to [Railway.app](https://railway.app/)
2. Click **"New Project"**
3. Select **"Deploy from GitHub repo"**
4. Choose your repository: `subhradip-me/ai-engine--keeplynk`
5. Railway will detect the Dockerfile automatically

## 🚀 Deployment Steps

### Step 1: Create a New Railway Project

1. Log in to [Railway](https://railway.app/)
2. Click **"New Project"**
3. Select **"Deploy from GitHub repo"**
4. Authorize Railway to access your GitHub repositories
5. Select your `KeepLynk` repository
6. Railway will automatically detect your project structure

### Step 2: Configure the Service

**Important:** Railway scans the repository root. The project is already configured with:
- ✅ `Dockerfile` at root level (references `ai-engine/` subdirectory)
- ✅ `railway.json` - Railway configuration
- ✅ `railway.toml` - Alternative configuration

Railway will automatically:
1. Detect the Dockerfile
2. Build the Java application from the `ai-engine/` subdirectory
3. Create a container with the application

No additional root directory configuration needed!

### Step 3: Add MongoDB Database

#### Option A: Using Railway's MongoDB

1. In your project dashboard, click **"+ New"**
2. Select **"Database"** → **"Add MongoDB"**
3. Railway will provision a MongoDB instance
4. Once created, click on the MongoDB service
5. Go to **"Connect"** tab and copy the connection string
6. The connection string format: `mongodb://mongo:password@containers-us-west-xxx.railway.app:port`

#### Option B: Using MongoDB Atlas (External)

1. Create a free cluster on [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Get your connection string
3. Ensure IP whitelist allows Railway's IPs (or use `0.0.0.0/0` for development)

### Step 4: Set Environment Variables

1. Click on your AI Engine service
2. Go to **"Variables"** tab
3. Add the following environment variables:

```bash
# Application Profile
SPRING_PROFILES_ACTIVE=production

# Port (Railway provides this automatically, but you can override)
PORT=8081

# LLM API Keys
GEMINI_API_KEY=your_gemini_api_key_here
GROQ_API_KEY=your_groq_api_key_here
HF_API_KEY=your_huggingface_api_key_here

# MongoDB Configuration
MONGODB_URI=mongodb://username:password@host:port/database
MONGODB_DATABASE=keeplynk_ai

# Optional: If MongoDB is not needed, exclude auto-configuration
MONGO_EXCLUDE=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration

# CORS Configuration (comma-separated list of allowed origins)
ALLOWED_ORIGINS=https://your-frontend-domain.com,https://www.your-domain.com

# Java Options (optional, for performance tuning)
JAVA_OPTS=-Xmx512m -Xms256m
```

### Step 5: Configure MongoDB Connection (If using Railway MongoDB)

If you added Railway's MongoDB:

1. Click on your AI Engine service
2. Go to **"Variables"** tab
3. Click **"+ New Variable"** → **"Add Reference"**
4. Select your MongoDB service
5. Choose `MONGO_URL` and name it `MONGODB_URI`
6. This automatically connects your service to the database

### Step 6: Deploy

1. Railway automatically deploys when you push to your repository
2. For manual deployment:
   - Go to **"Deployments"** tab
   - Click **"Deploy"** button
3. Watch the build logs in real-time:
   - Maven will download dependencies
   - Build the application
   - Create the JAR file
   - Start the Spring Boot application

### Step 7: Verify Deployment

1. Once deployed, Railway provides a public URL
2. To get/set a custom domain:
   - Go to **"Settings"** tab
   - Under **"Domains"**, Railway provides a `*.up.railway.app` domain
   - Click **"Generate Domain"** to get a public URL
   - Or add your custom domain

3. Test the deployment:
```bash
# Check health endpoint (if you added Spring Actuator)
curl https://your-app.up.railway.app/actuator/health

# Test your API endpoints
curl https://your-app.up.railway.app/api/your-endpoint
```

## 📊 Monitoring and Logs

### View Logs
1. Click on your service
2. Go to **"Deployments"** tab
3. Click on the active deployment
4. View real-time logs in the **"Logs"** section

### Metrics
1. Railway provides built-in metrics:
   - CPU usage
   - Memory usage
   - Network traffic
2. Access metrics from the **"Metrics"** tab

## 🔧 Advanced Configuration

### Enable Health Check Endpoint

Add Spring Boot Actuator dependency to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Then configure in Railway:
1. Go to **"Settings"** → **"Health Check"**
2. Set path to: `/actuator/health`
3. Enable health checks

### Custom Domain Setup

1. Go to **"Settings"** → **"Domains"**
2. Click **"Custom Domain"**
3. Add your domain (e.g., `api.keeplynk.com`)
4. Follow Railway's instructions to update your DNS records:
   - Add a CNAME record pointing to Railway's provided domain
5. SSL certificates are automatically provisioned by Railway

### Scaling

1. Go to **"Settings"** → **"Resources"**
2. Adjust:
   - **CPU**: Up to 8 vCPUs
   - **Memory**: Up to 32 GB RAM
3. Railway charges based on usage

### Automatic Deployments

Railway automatically deploys when you push to your main branch. To configure:

1. Go to **"Settings"** → **"Service"**
2. Under **"Source Repo"**, configure:
   - **Branch**: main (or your preferred branch)
   - **Auto Deploy**: Enable
3. Every push triggers a new deployment

## 🐛 Troubleshooting

### Build Fails

**Problem**: Maven build fails with dependency errors

**Solution**:
```bash
# In railway.json or build settings, ensure:
"buildCommand": "./mvnw clean package -DskipTests -U"
# The -U flag forces dependency updates
```

### Application Won't Start

**Problem**: Application starts but immediately crashes

**Solution**:
1. Check logs for specific error messages
2. Verify all environment variables are set correctly
3. Ensure MongoDB connection string is valid
4. Check Java version compatibility (should be Java 21)

### MongoDB Connection Issues

**Problem**: Can't connect to MongoDB

**Solution**:
1. Verify `MONGODB_URI` environment variable
2. Check MongoDB service is running
3. Ensure connection string format is correct:
   ```
   mongodb://username:password@host:port/database
   ```
4. If using Railway MongoDB, use the reference variable instead of hardcoding

### Port Binding Errors

**Problem**: Application fails to bind to port

**Solution**:
Ensure `application-production.properties` uses:
```properties
server.port=${PORT:8081}
```
Railway sets the `PORT` environment variable automatically.

### Out of Memory Errors

**Problem**: Application crashes with OOM errors

**Solution**:
1. Increase memory allocation in Railway settings
2. Or optimize JVM settings via `JAVA_OPTS`:
```bash
JAVA_OPTS=-Xmx1024m -Xms512m -XX:+UseG1GC
```

## 📝 Environment Variables Reference

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | No | Active Spring profile | `production` |
| `PORT` | No* | Server port (*Railway sets this) | `8081` |
| `GEMINI_API_KEY` | Yes | Google Gemini API key | `AIzaSy...` |
| `GROQ_API_KEY` | Yes | GROQ API key | `gsk_...` |
| `HF_API_KEY` | Yes | Hugging Face API key | `hf_...` |
| `MONGODB_URI` | Yes** | MongoDB connection string | `mongodb://...` |
| `MONGODB_DATABASE` | No | Database name | `keeplynk_ai` |
| `MONGO_EXCLUDE` | No | Exclude MongoDB if not used | See above |
| `ALLOWED_ORIGINS` | No | CORS allowed origins | `https://app.com` |
| `JAVA_OPTS` | No | JVM options | `-Xmx512m` |

** Required if using MongoDB

## 🔐 Security Best Practices

1. **Never commit API keys** to your repository
2. **Use Railway's environment variables** for all sensitive data
3. **Enable CORS** only for trusted domains
4. **Use HTTPS** for all production endpoints (Railway provides this automatically)
5. **Rotate API keys** regularly
6. **Use Railway's secrets** for database credentials
7. **Enable authentication** on your API endpoints
8. **Monitor logs** for suspicious activity

## 💰 Cost Estimation

Railway pricing (as of 2026):
- **Developer Plan**: $5/month base + usage
- **Hobby Plan**: $5 for starter resources
- **Usage-based**: ~$0.000463 per GB-hour of RAM

Example monthly cost for AI Engine:
- **Small app** (512MB RAM, low traffic): ~$5-10/month
- **Medium app** (1GB RAM, moderate traffic): ~$15-25/month
- **Large app** (2GB+ RAM, high traffic): ~$30-50/month

## 🔄 CI/CD Pipeline

Railway automatically sets up CI/CD. For advanced workflows:

1. Create `.github/workflows/railway-deploy.yml`:
```yaml
name: Deploy to Railway

on:
  push:
    branches: [ main ]
    paths:
      - 'services/ai-engine/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Install Railway CLI
        run: npm install -g @railway/cli
      
      - name: Deploy to Railway
        run: railway up
        env:
          RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
```

2. Add `RAILWAY_TOKEN` to GitHub repository secrets

## 📚 Additional Resources

- [Railway Documentation](https://docs.railway.app/)
- [Spring Boot on Railway](https://docs.railway.app/guides/spring-boot)
- [Railway CLI](https://docs.railway.app/develop/cli)
- [MongoDB on Railway](https://docs.railway.app/databases/mongodb)

## 🎯 Quick Start Checklist

- [ ] Create Railway account
- [ ] Connect GitHub repository
- [ ] Set root directory to `services/ai-engine/ai-engine`
- [ ] Add MongoDB service (if needed)
- [ ] Configure all environment variables
- [ ] Generate public domain
- [ ] Test deployment
- [ ] Set up custom domain (optional)
- [ ] Enable health checks
- [ ] Monitor logs and metrics

## 📞 Support

If you encounter issues:
1. Check Railway status: https://status.railway.app/
2. Railway Discord: https://discord.gg/railway
3. Railway Documentation: https://docs.railway.app/
4. GitHub Issues: Create an issue in your repository

---

**Deployment Status**: Ready for Railway ✅

Last Updated: January 9, 2026
