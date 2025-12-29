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

### Update an existing project

If you have an existing project created from this template and want to update it with the latest changes:

```bash
cd templates/scripts
./update-project.sh ~/Projects/@vvise-co/my-app
```

Update specific components only:

```bash
# Update everything
./update-project.sh ~/Projects/@vvise-co/my-app --all

# Update Docker files only (Dockerfile, nginx, docker-compose)
./update-project.sh ~/Projects/@vvise-co/my-app --docker

# Update Maven wrapper only (fixes "mvnw not found" errors)
./update-project.sh ~/Projects/@vvise-co/my-app --mvnw

# Update multiple components
./update-project.sh ~/Projects/@vvise-co/my-app --docker --readme --env
```

Available options:
- `--all` - Update everything (default if no options specified)
- `--docker` - Update Dockerfile, nginx config, docker-compose
- `--readme` - Update README.md
- `--env` - Update .env.example files
- `--mvnw` - Update Maven wrapper (mvnw and .mvn)
- `--frontend` - Update frontend pages (login, callback) and components
- `--backend` - Update backend source files (security, config, controller)
- `--scripts` - Update shared lib files (auth.ts, api.ts, types.ts)

## Directory Structure

```
templates/
├── Dockerfile               # Unified Dockerfile for Koyeb
├── .env.example             # Unified environment template
├── README.md                # This documentation
├── nginx/
│   └── nginx.conf.template  # Nginx reverse proxy config
│
├── backend-client/          # Spring Boot backend template
│   ├── src/main/kotlin/
│   ├── pom.xml
│   ├── mvnw                 # Maven wrapper
│   ├── .mvn/                # Maven wrapper config
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
    ├── init-project.sh      # Project initializer
    └── update-project.sh    # Update existing projects
```

---

## Local Development

### Option A: Unified Local Development (Docker)

Run your entire project (frontend + backend) in a single container, mimicking the production environment.

#### Prerequisites
- Docker installed
- PostgreSQL database running locally or remotely
- Auth server running (locally or deployed)

#### Step 1: Create your `.env` file

```bash
cd your-project
cp .env.example .env
```

#### Step 2: Configure environment variables

Edit `.env` with your local settings:

```bash
# ===========================================
# DATABASE (Required)
# ===========================================
DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/your_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# ===========================================
# AUTH SERVER (Required)
# ===========================================
# If auth server runs locally on host machine
AUTH_SERVER_URL=http://host.docker.internal:8081

# If auth server is deployed
# AUTH_SERVER_URL=https://your-auth-server.koyeb.app

# ===========================================
# APPLICATION URLs (Required)
# ===========================================
CORS_ALLOWED_ORIGINS=http://localhost:8000
# NEXT_PUBLIC_APP_URL is optional - OAuth callbacks use browser origin automatically

# ===========================================
# OPTIONAL
# ===========================================
# PORT=8000  # Default port
# AUTH_CACHE_TTL=300  # Token cache TTL in seconds
```

#### Step 3: Build and run

```bash
# Build the unified image
docker build -t my-app .

# Run on default port (8000)
docker run -p 8000:8000 --env-file .env my-app

# Or run on custom port
docker run -e PORT=3000 -p 3000:3000 --env-file .env my-app
```

#### Step 4: Access your app

- App: http://localhost:8000
- API: http://localhost:8000/api
- Health check: http://localhost:8000/health

---

### Option B: Separate Services (Development Mode)

Run frontend and backend as separate processes for faster development iteration with hot reload.

#### Prerequisites
- Node.js 18+ installed
- Java 21+ installed
- PostgreSQL database running
- Auth server running (locally or deployed)

#### Step 1: Start the Auth Server

If running locally:
```bash
cd /path/to/auth
docker-compose up
# Auth server runs on http://localhost:8081
```

Or use a deployed auth server (e.g., `https://your-auth-server.koyeb.app`).

#### Step 2: Configure Backend Environment

```bash
cd your-project/backend
cp .env.example .env
```

Edit `backend/.env`:

```bash
# =================================
# Backend Environment
# =================================

# Server Configuration
SERVER_PORT=8080
APP_NAME=my-app

# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/your_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Auth Server Configuration
AUTH_SERVER_URL=http://localhost:8081

# Cache TTL (optional)
# AUTH_CACHE_TTL=300

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3001
```

#### Step 3: Start the Backend

```bash
cd your-project/backend
./mvnw spring-boot:run
# Backend runs on http://localhost:8080
```

#### Step 4: Configure Frontend Environment

```bash
cd your-project/frontend
cp .env.example .env.local
```

Edit `frontend/.env.local`:

```bash
# =================================
# Frontend Environment (Local Dev)
# =================================

# Your project's backend API URL
NEXT_PUBLIC_API_URL=http://localhost:8080

# Central auth server URL (for browser redirects)
NEXT_PUBLIC_AUTH_SERVER_URL=http://localhost:8081

# Central auth server URL (for server-side API calls)
AUTH_SERVER_URL=http://localhost:8081

# Your app's public URL (for OAuth callbacks)
NEXT_PUBLIC_APP_URL=http://localhost:3001

# Node environment
NODE_ENV=development
```

#### Step 5: Start the Frontend

```bash
cd your-project/frontend
npm install
npm run dev
# Frontend runs on http://localhost:3001
```

#### Step 6: Access your app

- Frontend: http://localhost:3001
- Backend API: http://localhost:8080/api
- Auth Server: http://localhost:8081

---

## Koyeb Deployment

Deploy your project to Koyeb with a single unified container.

### Prerequisites
- GitHub repository with your project
- Koyeb account
- PostgreSQL database (Koyeb, Neon, Supabase, etc.)
- Auth server deployed (e.g., on Koyeb)

### Step 1: Push to GitHub

```bash
cd your-project
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/your-org/your-project.git
git push -u origin main
```

### Step 2: Create Koyeb Service

1. Go to [Koyeb Dashboard](https://app.koyeb.com)
2. Click **Create Service** → **GitHub**
3. Select your repository
4. Koyeb auto-detects the root `Dockerfile`

### Step 3: Configure Environment Variables

In the Koyeb service settings, add these environment variables:

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `DATABASE_URL` | Yes | PostgreSQL JDBC URL | `jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/mydb` |
| `DATABASE_USERNAME` | Yes | Database user | `my_user` |
| `DATABASE_PASSWORD` | Yes | Database password | `my_password` |
| `AUTH_SERVER_URL` | Yes | Your deployed auth server URL | `https://auth-server-xxx.koyeb.app` |
| `CORS_ALLOWED_ORIGINS` | Yes | Your Koyeb app URL | `https://my-app-xxx.koyeb.app` |
| `AUTH_CACHE_TTL` | No | Token cache TTL in seconds | `300` (default) |

**Important Notes:**
- `PORT` is automatically set by Koyeb - do not set it manually
- No `JWT_SECRET` required - token validation uses introspection
- No `NEXT_PUBLIC_API_URL` required - Nginx proxies `/api` to backend
- No `NEXT_PUBLIC_APP_URL` required - OAuth callbacks use browser origin automatically

### Step 4: Deploy

1. Click **Deploy**
2. Wait for the build to complete
3. Your app is live at `https://your-app-xxx.koyeb.app`

### Step 5: Configure Auth Server Callback

Add your app's callback URL to the auth server's allowed redirects:

In the auth server's environment variables, ensure `OAUTH2_REDIRECT_URI` includes your app:
```
OAUTH2_REDIRECT_URI=https://my-app-xxx.koyeb.app/auth/callback
```

Or if supporting multiple apps:
```
OAUTH2_REDIRECT_URI=https://app1.koyeb.app/auth/callback,https://app2.koyeb.app/auth/callback
```

---

## Environment Variables Reference

### Unified Deployment (Koyeb/Railway/Docker)

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `PORT` | No | Server port (auto-set by Koyeb) | `8000` |
| `DATABASE_URL` | Yes | PostgreSQL JDBC URL | `jdbc:postgresql://host/db` |
| `DATABASE_USERNAME` | Yes | Database user | `postgres` |
| `DATABASE_PASSWORD` | Yes | Database password | `secret` |
| `AUTH_SERVER_URL` | Yes | Central auth server URL | `https://auth.koyeb.app` |
| `CORS_ALLOWED_ORIGINS` | Yes | Your app URL | `https://my-app.koyeb.app` |
| `AUTH_CACHE_TTL` | No | Token cache TTL in seconds | `300` (default) |

**Note:** `NEXT_PUBLIC_APP_URL` is not required - OAuth callbacks automatically use the browser's origin.

### Separate Services (Local Development)

#### Backend (`backend/.env`)

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `SERVER_PORT` | No | Backend server port | `8080` (default) |
| `APP_NAME` | No | Application name for logging | `my-app` |
| `DATABASE_URL` | Yes | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/mydb` |
| `DATABASE_USERNAME` | Yes | Database user | `postgres` |
| `DATABASE_PASSWORD` | Yes | Database password | `postgres` |
| `AUTH_SERVER_URL` | Yes | Auth server URL | `http://localhost:8081` |
| `AUTH_CACHE_TTL` | No | Token cache TTL in seconds | `300` (default) |
| `CORS_ALLOWED_ORIGINS` | Yes | Frontend URL(s), comma-separated | `http://localhost:3001` |

#### Frontend (`frontend/.env.local`)

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Yes | Backend API URL | `http://localhost:8080` |
| `NEXT_PUBLIC_AUTH_SERVER_URL` | Yes | Auth server URL (browser) | `http://localhost:8081` |
| `AUTH_SERVER_URL` | Yes | Auth server URL (server-side) | `http://localhost:8081` |
| `NEXT_PUBLIC_APP_URL` | Yes | Frontend URL | `http://localhost:3001` |
| `NODE_ENV` | No | Node environment | `development` |

---

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
- Verify the auth server's `/api/auth/introspect` endpoint is accessible

### "CORS error"
- Add your frontend URL to `CORS_ALLOWED_ORIGINS` in your backend
- For local dev, ensure the URL includes the port (e.g., `http://localhost:3001`)

### "Redirect loop on login"
- Check that the auth server's `OAUTH2_REDIRECT_URI` includes your app's callback URL
- Verify `AUTH_SERVER_URL` is correct

### "Token expired"
- The frontend should automatically refresh tokens
- Check that the refresh token endpoint is accessible

### "Database connection failed"
- Verify `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` are correct
- For Docker, use `host.docker.internal` instead of `localhost` to connect to host machine

### "Cannot connect to auth server from Docker"
- Use `host.docker.internal:8081` instead of `localhost:8081`
- Or use the deployed auth server URL

### Cache issues
- Token introspection results are cached for 5 minutes
- Set `AUTH_CACHE_TTL=60` for shorter cache during development
