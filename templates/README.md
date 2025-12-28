# Project Templates

Blueprint templates for creating new projects that use the central authentication server (auth project).

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Central Auth Server                          │
│                    (Auth Project)                                │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────────┐ │
│  │   OAuth2    │  │    JWT      │  │    Token Introspection   │ │
│  │  Providers  │  │   Tokens    │  │    /api/auth/introspect  │ │
│  │ Google/GH/MS│  │             │  │                          │ │
│  └─────────────┘  └─────────────┘  └──────────────────────────┘ │
│                         ▲                                        │
│                         │ Token Introspection (no shared secret) │
└─────────────────────────┼───────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│  Map Animator │ │    Charts     │ │  New Project  │
│    Project    │ │    Project    │ │    (Future)   │
│               │ │               │ │               │
│ ┌───────────┐ │ │ ┌───────────┐ │ │ ┌───────────┐ │
│ │  Frontend │ │ │ │  Frontend │ │ │ │  Frontend │ │
│ │  Next.js  │ │ │ │  Next.js  │ │ │ │  Next.js  │ │
│ └───────────┘ │ │ └───────────┘ │ │ └───────────┘ │
│       ▲       │ │       ▲       │ │       ▲       │
│       │       │ │       │       │ │       │       │
│ ┌───────────┐ │ │ ┌───────────┐ │ │ ┌───────────┐ │
│ │  Backend  │ │ │ │  Backend  │ │ │ │  Backend  │ │
│ │Spring Boot│ │ │ │Spring Boot│ │ │ │Spring Boot│ │
│ └───────────┘ │ │ └───────────┘ │ │ └───────────┘ │
└───────────────┘ └───────────────┘ └───────────────┘
```

## Token Introspection

**No JWT_SECRET required!** Client projects validate tokens by calling the auth server's introspection endpoint:

```
POST /api/auth/introspect
{ "token": "access_token_here" }

Response (if valid):
{
  "active": true,
  "sub": "123",
  "email": "user@example.com",
  "name": "John Doe",
  "roles": "USER,ADMIN"
}
```

**Benefits:**
- No shared secrets between services
- Auth server has full control over token validity
- Token revocation works immediately
- Results are cached (5 min default) to minimize network calls

## Quick Start

### Create a new project

```bash
cd templates/scripts
./init-project.sh my-app ~/Projects/@vvise-co/my-app
```

This creates a Koyeb-ready project with:
- Root `Dockerfile` for unified deployment
- `nginx/` config for reverse proxy
- `.env.example` with all required variables
- Token introspection with Caffeine caching

## Directory Structure

```
templates/
├── Dockerfile               # Unified Dockerfile for Koyeb
├── .env.example             # Unified environment template
├── nginx/
│   └── nginx.conf.template  # Nginx reverse proxy config
│
├── backend-client/          # Spring Boot backend template
│   ├── src/main/kotlin/
│   ├── pom.xml
│   └── .env.example
│
├── frontend-client/         # Next.js frontend template
│   ├── src/
│   ├── package.json
│   └── .env.example
│
├── docker/                  # Development Docker files
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   └── docker-compose.yml
│
└── scripts/
    └── init-project.sh      # Project initializer
```

## Environment Variables

### Unified Deployment (Koyeb/Railway)

For production deployment where frontend + backend run in a single container:

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `PORT` | No | Server port (auto-set by Koyeb) | `8000` |
| `DATABASE_URL` | Yes | PostgreSQL JDBC URL | `jdbc:postgresql://host/db` |
| `DATABASE_USERNAME` | Yes | Database user | `postgres` |
| `DATABASE_PASSWORD` | Yes | Database password | `secret` |
| `AUTH_SERVER_URL` | Yes | Central auth server URL | `https://auth.koyeb.app` |
| `CORS_ALLOWED_ORIGINS` | Yes | Your app URL | `https://my-app.koyeb.app` |
| `NEXT_PUBLIC_APP_URL` | Yes | Your app URL | `https://my-app.koyeb.app` |
| `AUTH_CACHE_TTL` | No | Token cache TTL in seconds | `300` (default) |

**Notes:**
- No `JWT_SECRET` required! Token validation is done via introspection.
- No `NEXT_PUBLIC_API_URL` required! Nginx proxies `/api` to the backend.
- `AUTH_SERVER_URL` is used for both backend (introspection) and frontend (OAuth redirects).

**Unified routing:**
- `https://my-app.koyeb.app/` → Frontend
- `https://my-app.koyeb.app/api/**` → Backend
- `https://my-app.koyeb.app/health` → Backend health check

### Local Development (Separate Services)

When running frontend (3001), backend (8080), and auth server (8081) separately:

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend URL (only for local dev) | `http://localhost:8080` |
| `NEXT_PUBLIC_AUTH_SERVER_URL` | Auth server URL (browser) | `http://localhost:8081` |
| `AUTH_SERVER_URL` | Auth server URL (server-side) | `http://localhost:8081` |
| `NEXT_PUBLIC_APP_URL` | Frontend URL | `http://localhost:3001` |

## Deployment

### Option 1: Koyeb/Railway (Recommended)

```bash
# Build the unified image
docker build -t my-app .

# Run locally with default port (8000)
docker run -p 8000:8000 --env-file .env my-app

# Run with custom port
docker run -e PORT=8080 -p 8080:8080 --env-file .env my-app
```

**Koyeb deployment:**
1. Push your project to GitHub
2. Connect Koyeb to your repository
3. Koyeb auto-detects the root `Dockerfile`
4. Set environment variables in Koyeb dashboard
5. Deploy

### Option 2: Local Development (Separate Services)

1. **Start the auth server** (port 8081):
   ```bash
   cd /path/to/auth
   docker-compose up
   ```

2. **Start your project's backend** (port 8080):
   ```bash
   cd your-project/backend
   ./mvnw spring-boot:run
   ```

3. **Start your project's frontend** (port 3001):
   ```bash
   cd your-project/frontend
   npm install && npm run dev
   ```

## Authentication Flow

1. **User clicks "Sign in with Google/GitHub/Microsoft"**
   - Frontend redirects to `AUTH_SERVER/oauth2/authorization/{provider}`

2. **Auth server handles OAuth2 flow**
   - User authenticates with the provider
   - Auth server creates/updates user record
   - Generates JWT access token and refresh token

3. **Auth server redirects back to your app**
   - Redirects to `YOUR_APP/auth/callback?token=...&refreshToken=...`

4. **Your frontend stores tokens**
   - Tokens are stored in HTTP-only cookies via API route
   - User is redirected to the dashboard

5. **Protected API requests**
   - Frontend includes access token in requests
   - Your backend validates token via introspection (cached)
   - Auth server confirms token validity

## Key Components

### Backend

- **TokenIntrospectionService**: Validates tokens via auth server (with caching)
- **JwtAuthenticationFilter**: Extracts tokens and calls introspection service
- **AuthenticatedUser**: Represents the logged-in user (from introspection response)
- **@CurrentUser**: Annotation to inject the current user into controllers
- **CacheConfig**: Caffeine cache for token introspection results

### Frontend

- **lib/auth.ts**: Server-side authentication helpers
- **lib/api.ts**: API client for backend and auth server requests
- **middleware.ts**: Route protection middleware
- **OAuthButtons**: OAuth provider login buttons
- **UserMenu**: User dropdown with logout

## Adding New Features

### Adding a new protected endpoint

```kotlin
@RestController
@RequestMapping("/api/projects")
class ProjectController {

    @GetMapping
    fun getProjects(@CurrentUser user: AuthenticatedUser): List<Project> {
        // user.id, user.email, user.roles, user.imageUrl are available
        return projectService.getProjectsForUser(user.id)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createProject(@CurrentUser user: AuthenticatedUser, @RequestBody project: CreateProjectDto): Project {
        return projectService.create(project, user.id)
    }
}
```

### Adding role-based UI

```tsx
import { getCurrentUser, isAdmin } from '@/lib/auth';

export default async function Page() {
  const user = await getCurrentUser();

  return (
    <div>
      <h1>Welcome {user?.name}</h1>
      {isAdmin(user) && (
        <AdminPanel />
      )}
    </div>
  );
}
```

## Security Considerations

1. **Token Introspection**: Client apps don't need the JWT secret - they call the auth server to validate tokens

2. **Caching**: Token introspection results are cached for 5 minutes to reduce auth server load

3. **CORS**: Configure `CORS_ALLOWED_ORIGINS` to only allow your frontend domains

4. **HTTP-Only Cookies**: Tokens are stored in HTTP-only cookies to prevent XSS attacks

5. **Token Expiration**: Access tokens expire in 15 minutes; refresh tokens in 7 days

6. **HTTPS**: Always use HTTPS in production

## Troubleshooting

### "Token validation failed"
- Ensure `AUTH_SERVER_URL` is correct and reachable
- Check that the auth server is running

### "CORS error"
- Add your frontend URL to `CORS_ALLOWED_ORIGINS` in your backend

### "Redirect loop on login"
- Check that the auth server's OAuth callback URL includes your app's callback URL
- Verify `AUTH_SERVER_URL` is correct

### "Token expired"
- The frontend should automatically refresh tokens
- Check that the refresh token endpoint is accessible

### Cache issues
- Token introspection results are cached for 5 minutes
- Set `AUTH_CACHE_TTL=60` for shorter cache during development
