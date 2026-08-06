-- Token is nullable so existing devices remain valid until authentication is implemented.
ALTER TABLE devices
    ADD COLUMN IF NOT EXISTS token VARCHAR(255);

COMMENT ON COLUMN devices.token IS
    'Reserved for future device authentication';
