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
#   --scripts   Update shared scripts only (not project-specific code)

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
  echo "  --env       Update .env.example"
  echo "  --mvnw      Update Maven wrapper (mvnw and .mvn)"
  echo "  --scripts   Update shared scripts only"
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

# Update shared scripts (lib files, utility functions)
if [ "$UPDATE_SCRIPTS" = true ]; then
  echo -e "${GREEN}Updating shared scripts...${NC}"

  # Update frontend lib files (auth.ts, api.ts, types.ts)
  if [ -d "$TARGET_DIR/frontend/src/lib" ]; then
    for file in auth.ts api.ts types.ts; do
      if [ -f "$TEMPLATE_DIR/frontend-client/src/lib/$file" ]; then
        cp "$TEMPLATE_DIR/frontend-client/src/lib/$file" "$TARGET_DIR/frontend/src/lib/"
        UPDATED+=("frontend/src/lib/$file")
      fi
    done
  fi

  # Update backend security files
  if [ -d "$TARGET_DIR/backend/src/main/kotlin" ]; then
    # Find the package directory
    BACKEND_PKG_DIR=$(find "$TARGET_DIR/backend/src/main/kotlin" -type d -name "security" 2>/dev/null | head -1)
    if [ -n "$BACKEND_PKG_DIR" ]; then
      TEMPLATE_SECURITY_DIR="$TEMPLATE_DIR/backend-client/src/main/kotlin/com/vvise/template/security"
      if [ -d "$TEMPLATE_SECURITY_DIR" ]; then
        # Get the target package name from directory structure
        TARGET_PKG=$(echo "$BACKEND_PKG_DIR" | sed 's|.*/kotlin/||' | tr '/' '.')
        TARGET_PKG=${TARGET_PKG%%.security}

        for file in "$TEMPLATE_SECURITY_DIR"/*.kt; do
          if [ -f "$file" ]; then
            filename=$(basename "$file")
            # Copy and update package name
            sed "s/com.vvise.template/$TARGET_PKG/g" "$file" > "$BACKEND_PKG_DIR/$filename"
            UPDATED+=("backend/.../$filename")
          fi
        done
      fi
    fi
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
