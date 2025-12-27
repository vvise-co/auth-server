#!/bin/bash

# ============================================
# Initialize a new project from the template
# ============================================
# Usage: ./init-project.sh <project-name> <target-directory>
# Example: ./init-project.sh map-animator ~/Projects/@vvise-co/map-animator

set -e

PROJECT_NAME=${1:-"my-project"}
TARGET_DIR=${2:-"./$PROJECT_NAME"}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_DIR="$(dirname "$SCRIPT_DIR")"

echo "Creating new project: $PROJECT_NAME"
echo "Target directory: $TARGET_DIR"
echo ""

# Create target directory
mkdir -p "$TARGET_DIR"

# Copy backend template
echo "Copying backend template..."
cp -r "$TEMPLATE_DIR/backend-client" "$TARGET_DIR/backend"

# Copy frontend template
echo "Copying frontend template..."
cp -r "$TEMPLATE_DIR/frontend-client" "$TARGET_DIR/frontend"

# Copy nginx directory for unified deployment
echo "Copying nginx configuration..."
mkdir -p "$TARGET_DIR/nginx"
cp "$TEMPLATE_DIR/nginx/nginx.conf.template" "$TARGET_DIR/nginx/"

# Copy root Dockerfile for Koyeb deployment
echo "Copying Dockerfile..."
cp "$TEMPLATE_DIR/Dockerfile" "$TARGET_DIR/"

# Copy docker directory for development
echo "Copying Docker development templates..."
mkdir -p "$TARGET_DIR/docker"
cp "$TEMPLATE_DIR/docker/Dockerfile.backend" "$TARGET_DIR/docker/"
cp "$TEMPLATE_DIR/docker/Dockerfile.frontend" "$TARGET_DIR/docker/"
cp "$TEMPLATE_DIR/docker/docker-compose.yml" "$TARGET_DIR/"

# Copy unified .env.example
echo "Copying environment template..."
cp "$TEMPLATE_DIR/.env.example" "$TARGET_DIR/"

# Replace placeholders in backend
echo "Customizing backend..."
find "$TARGET_DIR/backend" -type f \( -name "*.kt" -o -name "*.xml" -o -name "*.yml" -o -name "*.properties" \) -exec \
  sed -i "s/your-project-name/$PROJECT_NAME/g" {} \;
find "$TARGET_DIR/backend" -type f \( -name "*.kt" -o -name "*.xml" -o -name "*.yml" -o -name "*.properties" \) -exec \
  sed -i "s/your_db/${PROJECT_NAME//-/_}_db/g" {} \;

# Rename package directories
PROJECT_PACKAGE=$(echo "$PROJECT_NAME" | tr '-' '_' | tr '[:upper:]' '[:lower:]')
if [ -d "$TARGET_DIR/backend/src/main/kotlin/com/vvise/template" ]; then
  mkdir -p "$TARGET_DIR/backend/src/main/kotlin/com/vvise/$PROJECT_PACKAGE"
  cp -r "$TARGET_DIR/backend/src/main/kotlin/com/vvise/template/"* "$TARGET_DIR/backend/src/main/kotlin/com/vvise/$PROJECT_PACKAGE/"
  rm -rf "$TARGET_DIR/backend/src/main/kotlin/com/vvise/template"
  find "$TARGET_DIR/backend/src/main/kotlin/com/vvise/$PROJECT_PACKAGE" -type f -name "*.kt" -exec \
    sed -i "s/com.vvise.template/com.vvise.$PROJECT_PACKAGE/g" {} \;
fi

# Replace placeholders in frontend
echo "Customizing frontend..."
find "$TARGET_DIR/frontend" -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.json" \) -exec \
  sed -i "s/your-project-frontend/$PROJECT_NAME-frontend/g" {} \;
find "$TARGET_DIR/frontend" -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.json" \) -exec \
  sed -i "s/Your Project/${PROJECT_NAME}/g" {} \;

# Replace placeholders in Docker files
echo "Customizing Docker configuration..."
find "$TARGET_DIR" -maxdepth 2 -type f \( -name "docker-compose.yml" -o -name "Dockerfile*" \) -exec \
  sed -i "s/your-project/$PROJECT_NAME/g" {} \;
find "$TARGET_DIR" -maxdepth 2 -type f \( -name "docker-compose.yml" -o -name "Dockerfile*" \) -exec \
  sed -i "s/your_project/${PROJECT_NAME//-/_}/g" {} \;

# Create .env from example
echo "Creating environment file..."
cp "$TARGET_DIR/.env.example" "$TARGET_DIR/.env"

# Initialize git
echo "Initializing git repository..."
cd "$TARGET_DIR"
git init
cat > .gitignore << 'EOF'
# Dependencies
node_modules/
target/

# Build output
.next/
*.jar

# Environment
.env
.env.local
.env.*.local

# IDE
.idea/
.vscode/
*.iml

# Logs
*.log
logs/

# OS
.DS_Store
Thumbs.db
EOF

echo ""
echo "============================================"
echo "Project '$PROJECT_NAME' created successfully!"
echo "============================================"
echo ""
echo "Project structure:"
echo "  $TARGET_DIR/"
echo "  ├── backend/          # Spring Boot backend"
echo "  ├── frontend/         # Next.js frontend"
echo "  ├── nginx/            # Nginx config for unified deployment"
echo "  ├── docker/           # Docker files for development"
echo "  ├── Dockerfile        # Unified Dockerfile for Koyeb"
echo "  ├── docker-compose.yml"
echo "  ├── .env.example"
echo "  └── .env"
echo ""
echo "============================================"
echo "DEPLOYMENT OPTIONS"
echo "============================================"
echo ""
echo "Option 1: Koyeb/Railway (Unified - Recommended)"
echo "  1. Push to GitHub"
echo "  2. Connect to Koyeb/Railway"
echo "  3. Set environment variables (see .env.example)"
echo "  4. Deploy - Koyeb will use the root Dockerfile"
echo ""
echo "Option 2: Local Development (Separate Services)"
echo "  1. cd $TARGET_DIR"
echo "  2. Update .env with local settings"
echo "  3. Start auth server on port 8081"
echo "  4. Start backend: cd backend && ./mvnw spring-boot:run"
echo "  5. Start frontend: cd frontend && npm install && npm run dev"
echo ""
echo "============================================"
echo "REQUIRED ENVIRONMENT VARIABLES"
echo "============================================"
echo ""
echo "| Variable                    | Description                    |"
echo "|-----------------------------|--------------------------------|"
echo "| DATABASE_URL                | PostgreSQL JDBC URL            |"
echo "| DATABASE_USERNAME           | Database user                  |"
echo "| DATABASE_PASSWORD           | Database password              |"
echo "| AUTH_SERVER_URL             | Central auth server URL        |"
echo "| JWT_SECRET                  | Must match auth server         |"
echo "| CORS_ALLOWED_ORIGINS        | Your app URL                   |"
echo "| NEXT_PUBLIC_AUTH_SERVER_URL | Auth server URL (client-side)  |"
echo "| NEXT_PUBLIC_APP_URL         | Your app URL                   |"
echo ""
echo "Important: JWT_SECRET must match the auth server's secret!"
echo ""
