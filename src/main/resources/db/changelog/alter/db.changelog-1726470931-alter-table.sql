ALTER TABLE PUBLIC.Users
ADD COLUMN user_phone_number VARCHAR,
DROP COLUMN rules_opt_in,
DROP COLUMN mktg_email_consent,
ADD COLUMN consent_marketing BOOLEAN;
