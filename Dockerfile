# Unified Dockerfile for Koyeb/Railway deployment
# React SPA frontend + Spring Boot backend in single container
# No nginx required - Spring Boot serves static files directly

# ============================================
# FRONTEND BUILD STAGE
# ============================================
FROM node:20-alpine AS frontend-build
WORKDIR /app

# Install dependencies
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci

# Build React app
COPY frontend/ .
RUN npm run build

# ============================================
# BACKEND BUILD STAGE
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app

# Copy Maven wrapper and download dependencies
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY backend/src src

# Copy frontend build to static resources
COPY --from=frontend-build /app/dist src/main/resources/static

# Build the application
RUN ./mvnw package -DskipTests

# ============================================
# FINAL STAGE
# ============================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR
COPY --from=backend-build /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:${PORT:-8000}/health || exit 1

# Default port (Koyeb sets PORT env var automatically)
ENV PORT=8000
EXPOSE 8000

# Run the application
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8000}"]
