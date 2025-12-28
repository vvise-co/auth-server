# Unified Dockerfile for Koyeb/Railway deployment
# Frontend + Backend with Nginx reverse proxy
# Frontend accessible at root (/), Backend at /api/**
# Port is configurable via PORT env var (default: 8000)
#
# Build: docker build -t your-project .
# Run:   docker run -p 8000:8000 --env-file .env your-project

# ============================================
# BACKEND BUILD STAGE
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B
COPY backend/src src
RUN ./mvnw package -DskipTests

# ============================================
# FRONTEND DEPS STAGE
# ============================================
FROM node:20-alpine AS frontend-deps
RUN apk add --no-cache libc6-compat
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci || npm install

# ============================================
# FRONTEND BUILD STAGE
# ============================================
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY --from=frontend-deps /app/node_modules ./node_modules
COPY frontend/ .
RUN mkdir -p public
# NEXT_PUBLIC_API_URL: Not needed - nginx proxies /api to backend (same origin)
# NEXT_PUBLIC_AUTH_SERVER_URL: Set from AUTH_SERVER_URL at build time
# NEXT_PUBLIC_APP_URL: Your deployed app URL
ARG NEXT_PUBLIC_API_URL=""
ARG AUTH_SERVER_URL=""
ARG NEXT_PUBLIC_APP_URL=""
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_AUTH_SERVER_URL=$AUTH_SERVER_URL
ENV NEXT_PUBLIC_APP_URL=$NEXT_PUBLIC_APP_URL
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# ============================================
# FINAL UNIFIED STAGE
# ============================================
FROM alpine:3.19

# Install required packages
RUN apk add --no-cache \
    nginx \
    supervisor \
    openjdk21-jre \
    nodejs \
    curl

# Create necessary directories
RUN mkdir -p /var/log/supervisor /run/nginx /app/backend /app/frontend

# Copy nginx template
COPY nginx/nginx.conf.template /etc/nginx/nginx.conf.template

# Copy backend JAR
COPY --from=backend-build /app/target/*.jar /app/backend/app.jar

# Copy frontend build
COPY --from=frontend-build /app/public /app/frontend/public
COPY --from=frontend-build /app/.next/standalone /app/frontend/
COPY --from=frontend-build /app/.next/static /app/frontend/.next/static

# Create startup script that generates nginx config from template
RUN cat > /app/start.sh << 'EOF'
#!/bin/sh
set -e

# Default port for cloud platforms
export PORT=${PORT:-8000}

# Generate nginx config from template (only substitute PORT, preserve nginx variables)
sed "s/\${PORT:-80}/$PORT/g" /etc/nginx/nginx.conf.template > /etc/nginx/http.d/default.conf

echo "Starting services on port $PORT..."
exec /usr/bin/supervisord -c /etc/supervisord.conf
EOF
RUN chmod +x /app/start.sh

# Create supervisor configuration
RUN cat > /etc/supervisord.conf << 'EOF'
[supervisord]
nodaemon=true
logfile=/var/log/supervisor/supervisord.log
pidfile=/var/run/supervisord.pid
user=root

[program:nginx]
command=nginx -g "daemon off;"
autostart=true
autorestart=true
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0

[program:backend]
command=java -jar /app/backend/app.jar
directory=/app/backend
autostart=true
autorestart=true
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0
environment=SERVER_PORT="8080"

[program:frontend]
command=node /app/frontend/server.js
directory=/app/frontend
autostart=true
autorestart=true
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0
environment=NODE_ENV="production",PORT="3000",HOSTNAME="0.0.0.0"
EOF

# Health check using the configured port
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8000}/health || exit 1

# Cloud platforms use PORT env var, default to 8000
ENV PORT=8000
EXPOSE 8000

CMD ["/app/start.sh"]
