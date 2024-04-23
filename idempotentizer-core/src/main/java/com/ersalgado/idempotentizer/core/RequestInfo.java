package com.ersalgado.idempotentizer.core;

import java.time.Instant;
import java.util.UUID;

public class RequestInfo {

    private final UUID idempotencyKey;
    private final String consumerId;
    private final boolean processed;
    private final Instant processedAt;
    private final Object returnedValue;

    public RequestInfo(UUID idempotencyKey, String consumerId, Instant processedAt, Object returnedValue) {
        this.idempotencyKey = idempotencyKey;
        this.consumerId = consumerId;
        this.processed = true;
        this.processedAt = processedAt;
        this.returnedValue = returnedValue;
    }

    public RequestInfo() {
        this.idempotencyKey = null;
        this.consumerId = null;
        this.processed = false;
        this.processedAt = null;
        this.returnedValue = null;
    }

    public UUID getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public String getConsumerId() {
        return this.consumerId;
    }

    public boolean getProcessed() {
        return this.processed;
    }

    public Instant getProcessedAt() {
        return this.processedAt;
    }

    public Object getReturnedValue() {
        return this.returnedValue;
    }
}
