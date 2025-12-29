# Unified Dockerfile for Koyeb/Railway deployment
# React SPA frontend served by Spring Boot backend
# Port is configurable via PORT env var (default: 8000)
#
# Build: docker build -t your-project .
# Run:   docker run -p 8000:8000 --env-file .env your-project

# ============================================
# FRONTEND BUILD STAGE (React SPA with Vite)
# ============================================
FROM node:20-alpine AS frontend-build
WORKDIR /app

# Copy package files
COPY frontend/package.json frontend/package-lock.json* ./

# Install dependencies
RUN npm ci || npm install

# Copy source files
COPY frontend/ .

# Build args for environment variables at build time
ARG VITE_AUTH_SERVER_URL=""
ENV VITE_AUTH_SERVER_URL=$VITE_AUTH_SERVER_URL

# Build the React app
RUN npm run build

# ============================================
# BACKEND BUILD STAGE
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app

# Copy Maven wrapper and config
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .

# Download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy backend source
COPY backend/src src

# Copy frontend build to static resources
COPY --from=frontend-build /app/dist src/main/resources/static

# Build the application
RUN ./mvnw package -DskipTests

# ============================================
# FINAL RUNTIME STAGE
# ============================================
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy the built JAR
COPY --from=backend-build /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8000}/health || exit 1

# Cloud platforms use PORT env var, default to 8000
ENV PORT=8000
EXPOSE 8000

# Start the application
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8000}"]
