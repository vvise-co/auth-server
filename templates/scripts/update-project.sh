#!/bin/bash

# ============================================
# Update an existing project from the template
# ============================================
# Usage: ./update-project.sh <target-directory> [options]
# Example: ./update-project.sh ~/Projects/@vvise-co/my-app
# Example: ./update-project.sh ~/Projects/@vvise-co/my-app --all
# Example: ./update-project.sh ~/Projects/@vvise-co/my-app --docker --readme
#
# Options:
#   --all       Update everything (default if no options specified)
#   --docker    Update Dockerfile, nginx config, docker-compose
#   --readme    Update README.md
#   --env       Update .env.example
#   --mvnw      Update Maven wrapper (mvnw and .mvn)
#   --frontend  Update frontend pages and components
#   --backend   Update backend source files (security, config)
#   --scripts   Update shared lib files (auth.ts, api.ts, types.ts)

set -e

TARGET_DIR=${1:-""}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_DIR="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Parse options
UPDATE_ALL=false
UPDATE_DOCKER=false
UPDATE_README=false
UPDATE_ENV=false
UPDATE_MVNW=false
UPDATE_FRONTEND=false
UPDATE_BACKEND=false
UPDATE_SCRIPTS=false

if [ -z "$TARGET_DIR" ]; then
  echo -e "${RED}Error: Target directory is required${NC}"
  echo ""
  echo "Usage: ./update-project.sh <target-directory> [options]"
  echo ""
  echo "Options:"
  echo "  --all       Update everything (default if no options specified)"
  echo "  --docker    Update Dockerfile, nginx config, docker-compose"
  echo "  --readme    Update README.md"
  echo "  --env       Update .env.example files"
  echo "  --mvnw      Update Maven wrapper (mvnw and .mvn)"
  echo "  --frontend  Update frontend pages and components"
  echo "  --backend   Update backend source files (security, config)"
  echo "  --scripts   Update shared lib files (auth.ts, api.ts, types.ts)"
  exit 1
fi

# Check if target directory exists
if [ ! -d "$TARGET_DIR" ]; then
  echo -e "${RED}Error: Target directory does not exist: $TARGET_DIR${NC}"
  exit 1
fi

# Shift past the target directory argument
shift

# Parse remaining arguments
if [ $# -eq 0 ]; then
  UPDATE_ALL=true
else
  for arg in "$@"; do
    case $arg in
      --all)
        UPDATE_ALL=true
        ;;
      --docker)
        UPDATE_DOCKER=true
        ;;
      --readme)
        UPDATE_README=true
        ;;
      --env)
        UPDATE_ENV=true
        ;;
      --mvnw)
        UPDATE_MVNW=true
        ;;
      --frontend)
        UPDATE_FRONTEND=true
        ;;
      --backend)
        UPDATE_BACKEND=true
        ;;
      --scripts)
        UPDATE_SCRIPTS=true
        ;;
      *)
        echo -e "${YELLOW}Warning: Unknown option: $arg${NC}"
        ;;
    esac
  done
fi

# If --all is set, enable all updates
if [ "$UPDATE_ALL" = true ]; then
  UPDATE_DOCKER=true
  UPDATE_README=true
  UPDATE_ENV=true
  UPDATE_MVNW=true
  UPDATE_FRONTEND=true
  UPDATE_BACKEND=true
  UPDATE_SCRIPTS=true
fi

echo "============================================"
echo "Updating project: $TARGET_DIR"
echo "============================================"
echo ""

# Track what was updated
UPDATED=()

# Update Docker files
if [ "$UPDATE_DOCKER" = true ]; then
  echo -e "${GREEN}Updating Docker configuration...${NC}"

  # Update root Dockerfile
  if [ -f "$TEMPLATE_DIR/Dockerfile" ]; then
    cp "$TEMPLATE_DIR/Dockerfile" "$TARGET_DIR/"
    UPDATED+=("Dockerfile")
  fi

  # Update nginx config
  if [ -f "$TEMPLATE_DIR/nginx/nginx.conf.template" ]; then
    mkdir -p "$TARGET_DIR/nginx"
    cp "$TEMPLATE_DIR/nginx/nginx.conf.template" "$TARGET_DIR/nginx/"
    UPDATED+=("nginx/nginx.conf.template")
  fi

  # Update docker directory
  if [ -d "$TEMPLATE_DIR/docker" ]; then
    mkdir -p "$TARGET_DIR/docker"
    [ -f "$TEMPLATE_DIR/docker/Dockerfile.backend" ] && cp "$TEMPLATE_DIR/docker/Dockerfile.backend" "$TARGET_DIR/docker/"
    [ -f "$TEMPLATE_DIR/docker/Dockerfile.frontend" ] && cp "$TEMPLATE_DIR/docker/Dockerfile.frontend" "$TARGET_DIR/docker/"
    [ -f "$TEMPLATE_DIR/docker/docker-compose.yml" ] && cp "$TEMPLATE_DIR/docker/docker-compose.yml" "$TARGET_DIR/"
    UPDATED+=("docker/Dockerfile.backend" "docker/Dockerfile.frontend" "docker-compose.yml")
  fi
fi

# Update README
if [ "$UPDATE_README" = true ]; then
  echo -e "${GREEN}Updating README.md...${NC}"
  if [ -f "$TEMPLATE_DIR/README.md" ]; then
    cp "$TEMPLATE_DIR/README.md" "$TARGET_DIR/"
    UPDATED+=("README.md")
  fi
fi

# Update .env.example
if [ "$UPDATE_ENV" = true ]; then
  echo -e "${GREEN}Updating .env.example...${NC}"
  if [ -f "$TEMPLATE_DIR/.env.example" ]; then
    cp "$TEMPLATE_DIR/.env.example" "$TARGET_DIR/"
    UPDATED+=(".env.example")
  fi

  # Update backend .env.example
  if [ -f "$TEMPLATE_DIR/backend-client/.env.example" ] && [ -d "$TARGET_DIR/backend" ]; then
    cp "$TEMPLATE_DIR/backend-client/.env.example" "$TARGET_DIR/backend/"
    UPDATED+=("backend/.env.example")
  fi

  # Update frontend .env.example
  if [ -f "$TEMPLATE_DIR/frontend-client/.env.example" ] && [ -d "$TARGET_DIR/frontend" ]; then
    cp "$TEMPLATE_DIR/frontend-client/.env.example" "$TARGET_DIR/frontend/"
    UPDATED+=("frontend/.env.example")
  fi
fi

# Update Maven wrapper
if [ "$UPDATE_MVNW" = true ]; then
  echo -e "${GREEN}Updating Maven wrapper...${NC}"
  if [ -d "$TARGET_DIR/backend" ]; then
    # Copy mvnw
    if [ -f "$TEMPLATE_DIR/backend-client/mvnw" ]; then
      cp "$TEMPLATE_DIR/backend-client/mvnw" "$TARGET_DIR/backend/"
      chmod +x "$TARGET_DIR/backend/mvnw"
      UPDATED+=("backend/mvnw")
    fi

    # Copy .mvn directory
    if [ -d "$TEMPLATE_DIR/backend-client/.mvn" ]; then
      rm -rf "$TARGET_DIR/backend/.mvn"
      cp -r "$TEMPLATE_DIR/backend-client/.mvn" "$TARGET_DIR/backend/"
      UPDATED+=("backend/.mvn/")
    fi
  fi
fi

# Update frontend pages and components
if [ "$UPDATE_FRONTEND" = true ]; then
  echo -e "${GREEN}Updating frontend pages and components...${NC}"

  if [ -d "$TARGET_DIR/frontend/src" ]; then
    # Update app pages
    TEMPLATE_APP_DIR="$TEMPLATE_DIR/frontend-client/src/app"
    TARGET_APP_DIR="$TARGET_DIR/frontend/src/app"

    if [ -d "$TEMPLATE_APP_DIR" ] && [ -d "$TARGET_APP_DIR" ]; then
      # Update auth callback page
      if [ -f "$TEMPLATE_APP_DIR/auth/callback/page.tsx" ]; then
        mkdir -p "$TARGET_APP_DIR/auth/callback"
        cp "$TEMPLATE_APP_DIR/auth/callback/page.tsx" "$TARGET_APP_DIR/auth/callback/"
        UPDATED+=("frontend/src/app/auth/callback/page.tsx")
      fi

      # Update login page
      if [ -f "$TEMPLATE_APP_DIR/login/page.tsx" ]; then
        mkdir -p "$TARGET_APP_DIR/login"
        cp "$TEMPLATE_APP_DIR/login/page.tsx" "$TARGET_APP_DIR/login/"
        UPDATED+=("frontend/src/app/login/page.tsx")
      fi

      # Update API routes
      if [ -d "$TEMPLATE_APP_DIR/api/auth" ]; then
        mkdir -p "$TARGET_APP_DIR/api/auth"
        for route in callback logout; do
          if [ -f "$TEMPLATE_APP_DIR/api/auth/$route/route.ts" ]; then
            mkdir -p "$TARGET_APP_DIR/api/auth/$route"
            cp "$TEMPLATE_APP_DIR/api/auth/$route/route.ts" "$TARGET_APP_DIR/api/auth/$route/"
            UPDATED+=("frontend/src/app/api/auth/$route/route.ts")
          fi
        done
      fi
    fi

    # Update components
    TEMPLATE_COMPONENTS_DIR="$TEMPLATE_DIR/frontend-client/src/components"
    TARGET_COMPONENTS_DIR="$TARGET_DIR/frontend/src/components"

    if [ -d "$TEMPLATE_COMPONENTS_DIR" ]; then
      mkdir -p "$TARGET_COMPONENTS_DIR"
      for file in "$TEMPLATE_COMPONENTS_DIR"/*.tsx; do
        if [ -f "$file" ]; then
          filename=$(basename "$file")
          cp "$file" "$TARGET_COMPONENTS_DIR/"
          UPDATED+=("frontend/src/components/$filename")
        fi
      done
    fi

    # Update middleware
    if [ -f "$TEMPLATE_DIR/frontend-client/src/middleware.ts" ]; then
      cp "$TEMPLATE_DIR/frontend-client/src/middleware.ts" "$TARGET_DIR/frontend/src/"
      UPDATED+=("frontend/src/middleware.ts")
    fi
  fi
fi

# Update backend source files
if [ "$UPDATE_BACKEND" = true ]; then
  echo -e "${GREEN}Updating backend source files...${NC}"

  if [ -d "$TARGET_DIR/backend/src/main/kotlin" ]; then
    # Find the project's package directory
    PROJECT_PKG_DIR=$(find "$TARGET_DIR/backend/src/main/kotlin" -type d -name "com" -exec find {} -mindepth 2 -maxdepth 2 -type d \; 2>/dev/null | head -1)

    if [ -n "$PROJECT_PKG_DIR" ]; then
      # Get the target package name from directory structure
      TARGET_PKG=$(echo "$PROJECT_PKG_DIR" | sed 's|.*/kotlin/||' | tr '/' '.')

      TEMPLATE_PKG_DIR="$TEMPLATE_DIR/backend-client/src/main/kotlin/com/vvise/template"

      # Update security files
      if [ -d "$TEMPLATE_PKG_DIR/security" ]; then
        mkdir -p "$PROJECT_PKG_DIR/security"
        for file in "$TEMPLATE_PKG_DIR/security"/*.kt; do
          if [ -f "$file" ]; then
            filename=$(basename "$file")
            sed "s/com.vvise.template/$TARGET_PKG/g" "$file" > "$PROJECT_PKG_DIR/security/$filename"
            UPDATED+=("backend/.../security/$filename")
          fi
        done
      fi

      # Update config files
      if [ -d "$TEMPLATE_PKG_DIR/config" ]; then
        mkdir -p "$PROJECT_PKG_DIR/config"
        for file in "$TEMPLATE_PKG_DIR/config"/*.kt; do
          if [ -f "$file" ]; then
            filename=$(basename "$file")
            sed "s/com.vvise.template/$TARGET_PKG/g" "$file" > "$PROJECT_PKG_DIR/config/$filename"
            UPDATED+=("backend/.../config/$filename")
          fi
        done
      fi

      # Update controller files
      if [ -d "$TEMPLATE_PKG_DIR/controller" ]; then
        mkdir -p "$PROJECT_PKG_DIR/controller"
        for file in "$TEMPLATE_PKG_DIR/controller"/*.kt; do
          if [ -f "$file" ]; then
            filename=$(basename "$file")
            sed "s/com.vvise.template/$TARGET_PKG/g" "$file" > "$PROJECT_PKG_DIR/controller/$filename"
            UPDATED+=("backend/.../controller/$filename")
          fi
        done
      fi

      # Update service files
      if [ -d "$TEMPLATE_PKG_DIR/service" ]; then
        mkdir -p "$PROJECT_PKG_DIR/service"
        for file in "$TEMPLATE_PKG_DIR/service"/*.kt; do
          if [ -f "$file" ]; then
            filename=$(basename "$file")
            sed "s/com.vvise.template/$TARGET_PKG/g" "$file" > "$PROJECT_PKG_DIR/service/$filename"
            UPDATED+=("backend/.../service/$filename")
          fi
        done
      fi

      # Update dto files
      if [ -d "$TEMPLATE_PKG_DIR/dto" ]; then
        mkdir -p "$PROJECT_PKG_DIR/dto"
        for file in "$TEMPLATE_PKG_DIR/dto"/*.kt; do
          if [ -f "$file" ]; then
            filename=$(basename "$file")
            sed "s/com.vvise.template/$TARGET_PKG/g" "$file" > "$PROJECT_PKG_DIR/dto/$filename"
            UPDATED+=("backend/.../dto/$filename")
          fi
        done
      fi
    else
      echo -e "${YELLOW}Warning: Could not find backend package directory${NC}"
    fi
  fi
fi

# Update shared lib files
if [ "$UPDATE_SCRIPTS" = true ]; then
  echo -e "${GREEN}Updating shared lib files...${NC}"

  # Update frontend lib files (auth.ts, api.ts, types.ts)
  if [ -d "$TARGET_DIR/frontend/src/lib" ]; then
    for file in auth.ts api.ts types.ts; do
      if [ -f "$TEMPLATE_DIR/frontend-client/src/lib/$file" ]; then
        cp "$TEMPLATE_DIR/frontend-client/src/lib/$file" "$TARGET_DIR/frontend/src/lib/"
        UPDATED+=("frontend/src/lib/$file")
      fi
    done
  fi
fi

echo ""
echo "============================================"
echo -e "${GREEN}Update complete!${NC}"
echo "============================================"
echo ""

if [ ${#UPDATED[@]} -gt 0 ]; then
  echo "Updated files:"
  for file in "${UPDATED[@]}"; do
    echo "  - $file"
  done
else
  echo "No files were updated."
fi

echo ""
echo -e "${YELLOW}Note:${NC} Review the changes and test your application."
echo "You may need to:"
echo "  1. Rebuild Docker images: docker build -t your-app ."
echo "  2. Restart services if running locally"
echo "  3. Redeploy to Koyeb if deployed"
echo ""
