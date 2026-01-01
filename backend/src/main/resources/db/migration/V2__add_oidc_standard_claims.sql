-- V2: Add OpenID Connect Standard Claims to users table
-- Reference: https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims

-- Add OIDC standard claim columns (nullable first)
ALTER TABLE users ADD COLUMN IF NOT EXISTS given_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS family_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS middle_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS nickname VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_username VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile VARCHAR(1024);
ALTER TABLE users ADD COLUMN IF NOT EXISTS picture VARCHAR(1024);
ALTER TABLE users ADD COLUMN IF NOT EXISTS website VARCHAR(1024);
ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS birthdate DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS zoneinfo VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS locale VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS address TEXT;

-- Add boolean columns as nullable first
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN;
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number_verified BOOLEAN;

-- Set default values for existing rows (OAuth users have verified emails)
UPDATE users SET email_verified = TRUE WHERE email_verified IS NULL;
UPDATE users SET phone_number_verified = FALSE WHERE phone_number_verified IS NULL;

-- Now make the columns NOT NULL with defaults for future inserts
ALTER TABLE users ALTER COLUMN email_verified SET NOT NULL;
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT FALSE;
ALTER TABLE users ALTER COLUMN phone_number_verified SET NOT NULL;
ALTER TABLE users ALTER COLUMN phone_number_verified SET DEFAULT FALSE;

-- Migrate existing image_url data to picture column (OIDC standard name)
UPDATE users SET picture = image_url WHERE picture IS NULL AND image_url IS NOT NULL;

-- Add comment explaining the address column format
COMMENT ON COLUMN users.address IS 'OIDC Address Claim stored as JSON. Format: {"formatted": "...", "street_address": "...", "locality": "...", "region": "...", "postal_code": "...", "country": "..."}';
