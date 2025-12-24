# Multi-service Dockerfile for Koyeb deployment
# Use BUILD_TARGET arg to select: "backend" or "frontend"
# Default is backend

ARG BUILD_TARGET=backend

# ============================================
# BACKEND BUILD STAGE
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS backend-build
WORKDIR /app
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B
COPY backend/src src
RUN ./mvnw package -DskipTests

# ============================================
# BACKEND RUNTIME STAGE
# ============================================
FROM eclipse-temurin:17-jre-alpine AS backend-runtime
WORKDIR /app
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
COPY --from=backend-build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

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
ARG NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# ============================================
# FRONTEND RUNTIME STAGE
# ============================================
FROM node:20-alpine AS frontend-runtime
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
RUN addgroup --system --gid 1001 nodejs && adduser --system --uid 1001 nextjs
COPY --from=frontend-build /app/public ./public
RUN mkdir .next && chown nextjs:nodejs .next
COPY --from=frontend-build --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=frontend-build --chown=nextjs:nodejs /app/.next/static ./.next/static
USER nextjs
EXPOSE 3000
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"
CMD ["node", "server.js"]

# ============================================
# FINAL STAGE SELECTOR
# ============================================
FROM ${BUILD_TARGET}-runtime AS final
