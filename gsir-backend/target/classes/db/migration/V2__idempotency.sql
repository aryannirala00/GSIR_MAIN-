CREATE TABLE idempotency_records (
    user_id UUID NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, idempotency_key)
);

