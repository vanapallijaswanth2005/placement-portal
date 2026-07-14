ALTER TABLE verification_token RENAME TO verification_tokens;
ALTER TABLE revoked_token RENAME TO revoked_tokens;
ALTER TABLE revoked_tokens MODIFY COLUMN token VARCHAR(512) NOT NULL;
