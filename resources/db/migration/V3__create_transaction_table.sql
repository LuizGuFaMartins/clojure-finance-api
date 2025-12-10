CREATE TABLE transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  from_user UUID NOT NULL REFERENCES users(id),
  to_user UUID NOT NULL REFERENCES users(id),
  amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
  status TEXT NULL CHECK (status IN ('pending','completed','failed')),
  created_at TIMESTAMP NOT NULL DEFAULT now()
);
