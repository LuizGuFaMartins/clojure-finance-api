CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NULL,
  email TEXT UNIQUE NULL,
  password TEXT NULL,
  cpf CHAR(11) NOT NULL,
  phone CHAR(11) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  balance NUMERIC(12,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
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

ALTER TABLE bank_data ENABLE ROW LEVEL SECURITY;

CREATE POLICY user_bank_data_isolation ON bank_data
    FOR ALL
    USING (user_id = NULLIF(current_setting('app.current_user_id', TRUE), '')::UUID);