# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Backend (Spring Boot + Kotlin)
```bash
cd backend
./mvnw spring-boot:run              # Run development server (port 8080)
./mvnw clean package                # Build JAR
./mvnw clean package -DskipTests    # Build without tests
```

### Frontend (Next.js)
```bash
cd frontend
npm install                         # Install dependencies
npm run dev                         # Run development server (port 3000)
npm run build                       # Production build
npm run lint                        # Run ESLint
```

### Docker (Full Stack)
```bash
docker-compose up --build           # Build and run all services
docker-compose up -d --build        # Run in detached mode
docker-compose down                 # Stop services
```

## Architecture Overview

This is a full-stack authentication server with:
- **Backend**: Spring Boot 3.5.9 + Kotlin 2.3.0 + PostgreSQL
- **Frontend**: Next.js 14 + React 18 + TypeScript + Tailwind CSS

### Authentication Flow

1. User clicks OAuth button → Frontend redirects to `/oauth2/authorization/{provider}`
2. Backend handles OAuth2 flow with Google/GitHub/Microsoft
3. `CustomOAuth2UserService` or `CustomOidcUserService` processes provider response
4. Backend generates JWT access token (15 min) + refresh token (7 days)
5. Redirects to `/auth/callback?token=...&refreshToken=...`
6. Frontend stores tokens in HTTP-only cookies via `/api/auth/callback` route
7. Subsequent requests include JWT in `Authorization: Bearer` header
8. `JwtAuthenticationFilter` validates tokens and populates `SecurityContext`

### Key Backend Components

| Component | Location | Purpose |
|-----------|----------|---------|
| `SecurityConfig` | `config/` | Filter chain, CORS, public/protected endpoints |
| `JwtTokenProvider` | `security/` | Token generation and validation (JJWT) |
| `JwtAuthenticationFilter` | `security/` | Extracts JWT from requests, validates, sets auth context |
| `CustomOAuth2UserService` | `security/` | Handles OAuth2 user info from providers |
| `CustomOidcUserService` | `security/` | Handles OIDC (Microsoft) user info |
| `OAuth2AuthenticationSuccessHandler` | `security/` | Generates tokens after successful OAuth |
| `UserPrincipal` | `security/` | Implements `UserDetails` + `OidcUser` |

### Security Configuration

Defined in `SecurityConfig.kt`:
- **Public endpoints**: `/api/auth/**`, `/oauth2/**`, `/login/oauth2/**`, `/health`
- **Admin endpoints**: `/api/admin/**` requires `ADMIN` role
- **All other `/api/**`**: Requires authentication
- **Session**: Stateless (no server sessions)
- **CSRF**: Disabled (stateless API)

### Frontend Auth Pattern

- `lib/auth.ts`: Server-side auth helpers for Next.js server components
- `lib/api.ts`: API client with automatic token handling
- `middleware.ts`: Route protection (redirects unauthenticated users)
- Tokens stored in HTTP-only cookies (XSS protection)

## Database Schema

Three main entities with JPA/Hibernate (auto-generated schema):
- `User`: OAuth2 user with provider info (email, name, imageUrl, provider, providerId)
- `Role`: Simple role entity (USER, ADMIN)
- `RefreshToken`: Token storage with expiry tracking

User-Role is many-to-many via `user_roles` join table.

## Environment Variables

Critical variables (see `.env.example`):
- `JWT_SECRET`: Must be 256+ bits (32+ chars)
- `SPRING_DATASOURCE_URL`: PostgreSQL JDBC URL
- OAuth2 credentials: `GOOGLE_CLIENT_ID`, `GITHUB_CLIENT_ID`, `MICROSOFT_CLIENT_ID` + secrets
- `CORS_ALLOWED_ORIGINS`: Frontend URL(s)
- `OAUTH2_REDIRECT_URI`: Frontend callback URL
- `NEXT_PUBLIC_API_URL`: Backend URL for frontend

## Templates Directory

The `templates/` directory contains blueprint templates for creating new projects that integrate with this auth server. Use `templates/scripts/init-project.sh` to scaffold new projects that share authentication with this server via JWT validation.
