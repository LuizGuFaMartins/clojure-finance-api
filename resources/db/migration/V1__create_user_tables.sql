CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NULL,
  email TEXT UNIQUE NULL,
  password_hash TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_balance (
  user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  balance NUMERIC(12,2) NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE bank_data (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  card_holder TEXT NULL,
  card_last4 CHAR(4) NULL,
  card_hash TEXT NULL,
  card_brand TEXT NULL,
  expires_month INT NULL CHECK (expires_month BETWEEN 1 AND 12),
  expires_year INT NULL,
  created_at TIMESTAMP NULL DEFAULT now()
);
