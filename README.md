# Full-Stack Authentication Demo

A secure full-stack application with Spring Boot (JWT + OAuth2) backend and Next.js frontend, containerized with Docker for Koyeb deployment.

## Features

- OAuth2 authentication with Google, GitHub, and Microsoft
- JWT-based session management
- Role-based access control (RBAC)
- Server-side authentication in Next.js
- HTTP-only cookies for secure token storage
- Docker support for easy deployment

## Tech Stack

- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Frontend**: Next.js 14, React, Tailwind CSS
- **Database**: PostgreSQL (Koyeb hosted)
- **Containerization**: Docker, Docker Compose

## Project Structure

```
demo/
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   │   └── com/vvise/demo/
│   │       ├── config/      # Security, CORS configs
│   │       ├── controller/  # REST controllers
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── entity/      # JPA entities
│   │       ├── repository/  # Data repositories
│   │       ├── service/     # Business logic
│   │       └── security/    # JWT, OAuth2 handlers
│   └── Dockerfile
├── frontend/                # Next.js application
│   ├── src/
│   │   ├── app/            # App Router pages
│   │   ├── components/     # React components
│   │   └── lib/            # Utilities
│   └── Dockerfile
├── docker-compose.yml       # Local development
├── docker-compose.prod.yml  # Production deployment
└── .env.example            # Environment template
```

## Setup

### Prerequisites

- Docker and Docker Compose
- Node.js 20+ (for local development)
- Java 17+ (for local development)
- OAuth2 credentials from Google, GitHub, and Microsoft

### 1. Clone and Configure

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your OAuth2 credentials and database URL
```

### 2. Set Up OAuth2 Providers

#### Google
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable OAuth consent screen
4. Create OAuth 2.0 credentials
5. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

#### GitHub
1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Create a new OAuth App
3. Set Homepage URL: `http://localhost:3000`
4. Set Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`

#### Microsoft
1. Go to [Azure Portal](https://portal.azure.com/)
2. Register a new application in Azure AD
3. Add redirect URI: `http://localhost:8080/login/oauth2/code/microsoft`
4. Create a client secret

### 3. Run with Docker

```bash
# Build and run all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

The application will be available at:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

### 4. Local Development (without Docker)

#### Backend
```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

## API Endpoints

### Authentication
- `GET /oauth2/authorization/{provider}` - Initiate OAuth2 login
- `GET /api/auth/me` - Get current user info
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout and invalidate tokens
- `GET /api/auth/providers` - Get available OAuth2 providers

### Users (Admin)
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users/{id}/admin` - Grant admin role
- `DELETE /api/users/{id}/admin` - Revoke admin role

## Deployment to Koyeb

### 1. Push to GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin <your-repo-url>
git push -u origin main
```

### 2. Deploy Backend on Koyeb

1. Create a new Web Service
2. Connect to your GitHub repository
3. Select the `backend` directory
4. Set environment variables from `.env.example`
5. Update `CORS_ALLOWED_ORIGINS` with your frontend Koyeb URL
6. Update `OAUTH2_REDIRECT_URI` with your frontend callback URL

### 3. Deploy Frontend on Koyeb

1. Create another Web Service
2. Connect to the same repository
3. Select the `frontend` directory
4. Set `NEXT_PUBLIC_API_URL` to your backend Koyeb URL

### 4. Update OAuth2 Providers

Update the callback URLs in each OAuth2 provider console:
- Google: `https://your-backend.koyeb.app/login/oauth2/code/google`
- GitHub: `https://your-backend.koyeb.app/login/oauth2/code/github`
- Microsoft: `https://your-backend.koyeb.app/login/oauth2/code/microsoft`

## Security Notes

- JWT tokens are stored in HTTP-only cookies
- Access tokens expire in 15 minutes
- Refresh tokens expire in 7 days
- CSRF protection is enabled
- Passwords are never stored (OAuth2 only)

## License

MIT
