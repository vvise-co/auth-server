-- V2: Add OpenID Connect Standard Claims to users table
-- Reference: https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims

-- Add OIDC standard claim columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS given_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS family_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS middle_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS nickname VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_username VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile VARCHAR(1024);
ALTER TABLE users ADD COLUMN IF NOT EXISTS picture VARCHAR(1024);
ALTER TABLE users ADD COLUMN IF NOT EXISTS website VARCHAR(1024);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS birthdate DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS zoneinfo VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS locale VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS address TEXT;

-- Migrate existing image_url data to picture column (OIDC standard name)
UPDATE users SET picture = image_url WHERE picture IS NULL AND image_url IS NOT NULL;

-- Drop the old image_url column (optional - uncomment if you want to remove it)
-- ALTER TABLE users DROP COLUMN IF EXISTS image_url;

-- Set email_verified to true for existing users (they authenticated via OAuth)
UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE;

-- Add comment explaining the address column format
COMMENT ON COLUMN users.address IS 'OIDC Address Claim stored as JSON. Format: {"formatted": "...", "street_address": "...", "locality": "...", "region": "...", "postal_code": "...", "country": "..."}';
