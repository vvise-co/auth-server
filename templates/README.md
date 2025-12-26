# Project Templates

Blueprint templates for creating new projects that use the central authentication server (auth project).

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Central Auth Server                          │
│                    (Auth Project)                                │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────────┐ │
│  │   OAuth2    │  │    JWT      │  │    User Management       │ │
│  │  Providers  │  │   Tokens    │  │    /api/users, /api/auth │ │
│  │ Google/GH/MS│  │             │  │                          │ │
│  └─────────────┘  └─────────────┘  └──────────────────────────┘ │
│                         ▲                                        │
│                         │ Shared JWT_SECRET                      │
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

## Quick Start

### Option 1: Use the init script

```bash
cd templates/scripts
./init-project.sh map-animator ~/Projects/@vvise-co/map-animator
```

### Option 2: Manual setup

1. Copy the template directories to your new project
2. Update package names and project identifiers
3. Configure environment variables
4. Start the auth server and your new project

## Directory Structure

```
templates/
├── backend-client/          # Spring Boot backend template
│   ├── src/main/kotlin/     # Kotlin source files
│   │   └── com/vvise/template/
│   │       ├── config/      # Security & CORS config
│   │       ├── controller/  # REST endpoints
│   │       ├── dto/         # Data transfer objects
│   │       ├── security/    # JWT validation, auth filter
│   │       └── service/     # Auth server client
│   ├── src/main/resources/  # Application configuration
│   ├── pom.xml              # Maven dependencies
│   └── .env.example         # Environment template
│
├── frontend-client/         # Next.js frontend template
│   ├── src/
│   │   ├── app/             # Next.js App Router pages
│   │   ├── components/      # React components
│   │   ├── lib/             # Auth helpers, API client
│   │   └── middleware.ts    # Route protection
│   ├── package.json         # NPM dependencies
│   └── .env.example         # Environment template
│
├── docker/                  # Docker configuration
│   ├── Dockerfile.backend   # Spring Boot Dockerfile
│   ├── Dockerfile.frontend  # Next.js Dockerfile
│   └── docker-compose.yml   # Local development compose
│
├── scripts/                 # Utility scripts
│   └── init-project.sh      # Project initializer
│
└── README.md                # This file
```

## Configuration

### Backend Configuration

Key environment variables (in `.env`):

| Variable | Description | Example |
|----------|-------------|---------|
| `SERVER_PORT` | Backend server port | `8080` |
| `DATABASE_URL` | PostgreSQL connection string | `jdbc:postgresql://localhost:5432/mydb` |
| `AUTH_SERVER_URL` | Central auth server URL | `http://localhost:8081` |
| `JWT_SECRET` | **Must match auth server** | `your-256-bit-secret` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:3001` |

### Frontend Configuration

Key environment variables (in `.env`):

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Your backend API URL | `http://localhost:8080` |
| `NEXT_PUBLIC_AUTH_SERVER_URL` | Auth server URL (client-side) | `http://localhost:8081` |
| `AUTH_SERVER_URL` | Auth server URL (server-side) | `http://localhost:8081` |
| `NEXT_PUBLIC_APP_URL` | Your frontend URL | `http://localhost:3001` |

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
   - Your backend validates JWT using the shared secret
   - No need to call auth server for every request

## Key Components

### Backend

- **JwtTokenValidator**: Validates JWT tokens from the auth server
- **JwtAuthenticationFilter**: Extracts and validates tokens from requests
- **AuthenticatedUser**: Represents the logged-in user (from JWT claims)
- **@CurrentUser**: Annotation to inject the current user into controllers
- **AuthServerClient**: HTTP client to fetch additional user data

### Frontend

- **lib/auth.ts**: Server-side authentication helpers
- **lib/api.ts**: API client for backend and auth server requests
- **middleware.ts**: Route protection middleware
- **OAuthButtons**: OAuth provider login buttons
- **UserMenu**: User dropdown with logout

## Security Considerations

1. **JWT Secret**: The `JWT_SECRET` must be identical across the auth server and all client projects

2. **CORS**: Configure `CORS_ALLOWED_ORIGINS` to only allow your frontend domains

3. **HTTP-Only Cookies**: Tokens are stored in HTTP-only cookies to prevent XSS attacks

4. **Token Expiration**: Access tokens expire in 15 minutes; refresh tokens in 7 days

5. **HTTPS**: Always use HTTPS in production

## Development Workflow

1. **Start the auth server first** (port 8081):
   ```bash
   cd /path/to/auth
   docker-compose up
   # or
   cd backend && ./mvnw spring-boot:run
   ```

2. **Start your project's backend** (port 8080):
   ```bash
   cd your-project/backend
   ./mvnw spring-boot:run
   ```

3. **Start your project's frontend** (port 3001):
   ```bash
   cd your-project/frontend
   npm install
   npm run dev
   ```

## Adding New Features

### Adding a new protected endpoint

```kotlin
@RestController
@RequestMapping("/api/projects")
class ProjectController {

    @GetMapping
    fun getProjects(@CurrentUser user: AuthenticatedUser): List<Project> {
        // user.id, user.email, user.roles are available
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

## Troubleshooting

### "Invalid JWT signature"
- Ensure `JWT_SECRET` matches exactly between auth server and your project

### "CORS error"
- Add your frontend URL to `CORS_ALLOWED_ORIGINS` in your backend

### "Redirect loop on login"
- Check that the auth server's OAuth callback URL includes your app's callback URL
- Verify `NEXT_PUBLIC_AUTH_SERVER_URL` is correct

### "Token expired"
- The frontend should automatically refresh tokens
- Check that the refresh token endpoint is accessible
