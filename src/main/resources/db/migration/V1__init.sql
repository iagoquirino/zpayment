CREATE TABLE user_order (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    order_id UUID NOT NULL
);
