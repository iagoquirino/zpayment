CREATE TABLE payments (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    idempotency_key UUID NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    amount INTEGER NOT NULL,
    currency varchar(3),
    state varchar(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);