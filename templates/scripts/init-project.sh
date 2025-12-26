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

# Copy docker templates
echo "Copying Docker templates..."
mkdir -p "$TARGET_DIR/docker"
cp "$TEMPLATE_DIR/docker/"* "$TARGET_DIR/docker/"
cp "$TEMPLATE_DIR/docker/docker-compose.yml" "$TARGET_DIR/"

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

# Create .env files from examples
echo "Creating environment files..."
cp "$TARGET_DIR/backend/.env.example" "$TARGET_DIR/backend/.env"
cp "$TARGET_DIR/frontend/.env.example" "$TARGET_DIR/frontend/.env"

# Initialize git
echo "Initializing git repository..."
cd "$TARGET_DIR"
git init
echo "node_modules/" >> .gitignore
echo ".env" >> .gitignore
echo "target/" >> .gitignore
echo ".next/" >> .gitignore

echo ""
echo "============================================"
echo "Project '$PROJECT_NAME' created successfully!"
echo "============================================"
echo ""
echo "Next steps:"
echo "1. cd $TARGET_DIR"
echo "2. Update .env files with your configuration"
echo "3. Make sure the auth server is running on port 8081"
echo "4. Start the backend: cd backend && ./mvnw spring-boot:run"
echo "5. Start the frontend: cd frontend && npm install && npm run dev"
echo ""
echo "Important: Ensure JWT_SECRET matches the auth server's secret!"
