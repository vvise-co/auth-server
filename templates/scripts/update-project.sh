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
#   --docker    Update Dockerfile, docker-compose
#   --readme    Update README.md
#   --env       Update .env.example
#   --mvnw      Update Maven wrapper (mvnw and .mvn)
#   --frontend  Update frontend to React SPA (replaces Next.js if present)
#   --backend   Update backend source files (security, config)
#   --scripts   Update shared lib files (api.ts, types.ts)

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
  echo "  --docker    Update Dockerfile, docker-compose"
  echo "  --readme    Update README.md"
  echo "  --env       Update .env.example files"
  echo "  --mvnw      Update Maven wrapper (mvnw and .mvn)"
  echo "  --frontend  Update frontend to React SPA (replaces Next.js if present)"
  echo "  --backend   Update backend source files (security, config)"
  echo "  --scripts   Update shared lib files (api.ts, types.ts)"
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

  # Update docker directory
  if [ -d "$TEMPLATE_DIR/docker" ]; then
    mkdir -p "$TARGET_DIR/docker"
    [ -f "$TEMPLATE_DIR/docker/Dockerfile.backend" ] && cp "$TEMPLATE_DIR/docker/Dockerfile.backend" "$TARGET_DIR/docker/"
    [ -f "$TEMPLATE_DIR/docker/Dockerfile.frontend" ] && cp "$TEMPLATE_DIR/docker/Dockerfile.frontend" "$TARGET_DIR/docker/"
    [ -f "$TEMPLATE_DIR/docker/docker-compose.yml" ] && cp "$TEMPLATE_DIR/docker/docker-compose.yml" "$TARGET_DIR/"
    UPDATED+=("docker/Dockerfile.backend" "docker/Dockerfile.frontend" "docker-compose.yml")
  fi

  # Remove old nginx directory if exists (no longer needed)
  if [ -d "$TARGET_DIR/nginx" ]; then
    echo -e "${YELLOW}Removing old nginx directory (no longer needed)...${NC}"
    rm -rf "$TARGET_DIR/nginx"
    UPDATED+=("nginx/ (removed)")
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

# Update frontend (React SPA - replaces Next.js if present)
if [ "$UPDATE_FRONTEND" = true ]; then
  echo -e "${GREEN}Updating frontend to React SPA...${NC}"

  # Check if this is a Next.js project that needs migration
  IS_NEXTJS=false
  if [ -f "$TARGET_DIR/frontend/next.config.js" ] || [ -d "$TARGET_DIR/frontend/src/app" ] || [ -f "$TARGET_DIR/frontend/src/middleware.ts" ]; then
    IS_NEXTJS=true
    echo -e "${YELLOW}Detected Next.js project - migrating to React SPA...${NC}"
  fi

  if [ -d "$TARGET_DIR/frontend" ]; then
    # Backup package.json name if exists
    FRONTEND_NAME="frontend"
    if [ -f "$TARGET_DIR/frontend/package.json" ]; then
      FRONTEND_NAME=$(grep -o '"name": *"[^"]*"' "$TARGET_DIR/frontend/package.json" | sed 's/"name": *"\([^"]*\)"/\1/' || echo "frontend")
    fi

    if [ "$IS_NEXTJS" = true ]; then
      # Remove Next.js specific files and directories
      echo -e "${YELLOW}Removing Next.js files...${NC}"
      rm -f "$TARGET_DIR/frontend/next.config.js" 2>/dev/null || true
      rm -f "$TARGET_DIR/frontend/next.config.mjs" 2>/dev/null || true
      rm -f "$TARGET_DIR/frontend/next-env.d.ts" 2>/dev/null || true
      rm -rf "$TARGET_DIR/frontend/.next" 2>/dev/null || true
      rm -rf "$TARGET_DIR/frontend/src/app" 2>/dev/null || true
      rm -f "$TARGET_DIR/frontend/src/middleware.ts" 2>/dev/null || true
      rm -rf "$TARGET_DIR/frontend/src/lib/auth.ts" 2>/dev/null || true
      rm -rf "$TARGET_DIR/frontend/src/lib/auth-utils.ts" 2>/dev/null || true
      UPDATED+=("Next.js files (removed)")
    fi

    # Copy all template frontend files
    TEMPLATE_FRONTEND="$TEMPLATE_DIR/frontend-client"

    # Copy root config files
    cp "$TEMPLATE_FRONTEND/package.json" "$TARGET_DIR/frontend/"
    cp "$TEMPLATE_FRONTEND/vite.config.ts" "$TARGET_DIR/frontend/"
    cp "$TEMPLATE_FRONTEND/tsconfig.json" "$TARGET_DIR/frontend/"
    cp "$TEMPLATE_FRONTEND/tsconfig.node.json" "$TARGET_DIR/frontend/" 2>/dev/null || true
    cp "$TEMPLATE_FRONTEND/tailwind.config.ts" "$TARGET_DIR/frontend/"
    cp "$TEMPLATE_FRONTEND/postcss.config.js" "$TARGET_DIR/frontend/"
    cp "$TEMPLATE_FRONTEND/index.html" "$TARGET_DIR/frontend/"
    UPDATED+=("frontend/package.json" "frontend/vite.config.ts" "frontend/tsconfig.json" "frontend/tailwind.config.ts" "frontend/index.html")

    # Update package.json name
    sed -i "s/\"name\": *\"[^\"]*\"/\"name\": \"$FRONTEND_NAME\"/" "$TARGET_DIR/frontend/package.json"

    # Copy src files
    mkdir -p "$TARGET_DIR/frontend/src"

    # Copy main entry files
    cp "$TEMPLATE_FRONTEND/src/main.tsx" "$TARGET_DIR/frontend/src/"
    cp "$TEMPLATE_FRONTEND/src/App.tsx" "$TARGET_DIR/frontend/src/"
    cp "$TEMPLATE_FRONTEND/src/index.css" "$TARGET_DIR/frontend/src/"
    cp "$TEMPLATE_FRONTEND/src/vite-env.d.ts" "$TARGET_DIR/frontend/src/" 2>/dev/null || true
    UPDATED+=("frontend/src/main.tsx" "frontend/src/App.tsx" "frontend/src/index.css")

    # Copy pages
    mkdir -p "$TARGET_DIR/frontend/src/pages"
    for file in "$TEMPLATE_FRONTEND/src/pages"/*.tsx; do
      if [ -f "$file" ]; then
        filename=$(basename "$file")
        cp "$file" "$TARGET_DIR/frontend/src/pages/"
        UPDATED+=("frontend/src/pages/$filename")
      fi
    done

    # Copy components
    mkdir -p "$TARGET_DIR/frontend/src/components"
    for file in "$TEMPLATE_FRONTEND/src/components"/*.tsx; do
      if [ -f "$file" ]; then
        filename=$(basename "$file")
        cp "$file" "$TARGET_DIR/frontend/src/components/"
        UPDATED+=("frontend/src/components/$filename")
      fi
    done

    # Copy context
    mkdir -p "$TARGET_DIR/frontend/src/context"
    for file in "$TEMPLATE_FRONTEND/src/context"/*.tsx; do
      if [ -f "$file" ]; then
        filename=$(basename "$file")
        cp "$file" "$TARGET_DIR/frontend/src/context/"
        UPDATED+=("frontend/src/context/$filename")
      fi
    done

    # Copy lib
    mkdir -p "$TARGET_DIR/frontend/src/lib"
    for file in "$TEMPLATE_FRONTEND/src/lib"/*.ts; do
      if [ -f "$file" ]; then
        filename=$(basename "$file")
        cp "$file" "$TARGET_DIR/frontend/src/lib/"
        UPDATED+=("frontend/src/lib/$filename")
      fi
    done

    # Copy public directory
    if [ -d "$TEMPLATE_FRONTEND/public" ]; then
      mkdir -p "$TARGET_DIR/frontend/public"
      cp -r "$TEMPLATE_FRONTEND/public/"* "$TARGET_DIR/frontend/public/" 2>/dev/null || true
    fi

    echo -e "${YELLOW}Note: Run 'npm install' in frontend directory to install new dependencies${NC}"
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

# Update shared lib files (only if --scripts is used without --frontend)
if [ "$UPDATE_SCRIPTS" = true ] && [ "$UPDATE_FRONTEND" = false ]; then
  echo -e "${GREEN}Updating shared lib files...${NC}"

  # Update frontend lib files (api.ts, types.ts)
  if [ -d "$TARGET_DIR/frontend/src/lib" ]; then
    for file in api.ts types.ts; do
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
echo "  1. Run 'cd frontend && npm install' to install dependencies"
echo "  2. Rebuild Docker images: docker build --build-arg AUTH_SERVER_URL=https://your-auth.koyeb.app -t your-app ."
echo "  3. Restart services if running locally"
echo "  4. Redeploy to Koyeb if deployed"
echo ""
echo -e "${YELLOW}Koyeb Deployment:${NC}"
echo "  Set AUTH_SERVER_URL as BOTH environment variable AND build argument!"
echo ""
