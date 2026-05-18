CREATE TABLE idempotency_keys (
    idempotency_key  VARCHAR(255) PRIMARY KEY,
    user_id          UUID NOT NULL,
    response_body    TEXT NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_idempotency_user ON idempotency_keys(user_id);