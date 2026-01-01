-- V3: Fix OIDC boolean columns for existing users
-- This migration repairs data from V2 where boolean columns may have NULL values

-- Update any NULL values to defaults
UPDATE users SET email_verified = TRUE WHERE email_verified IS NULL;
UPDATE users SET phone_number_verified = FALSE WHERE phone_number_verified IS NULL;

-- Ensure columns are NOT NULL (may already be, but this is idempotent)
DO $$
BEGIN
    -- Check if email_verified allows NULL and fix it
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users'
        AND column_name = 'email_verified'
        AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE users ALTER COLUMN email_verified SET NOT NULL;
    END IF;

    -- Check if phone_number_verified allows NULL and fix it
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users'
        AND column_name = 'phone_number_verified'
        AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE users ALTER COLUMN phone_number_verified SET NOT NULL;
    END IF;
END $$;

-- Set default values for future inserts
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT FALSE;
ALTER TABLE users ALTER COLUMN phone_number_verified SET DEFAULT FALSE;
