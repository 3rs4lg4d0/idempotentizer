CREATE TABLE processed_messages (
    idempotency_key UUID NOT NULL,
    consumer_id     VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    returned_value  BYTEA,
    PRIMARY KEY (idempotency_key, consumer_id)
);
