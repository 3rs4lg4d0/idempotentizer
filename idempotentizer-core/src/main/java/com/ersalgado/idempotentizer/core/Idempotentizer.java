package com.ersalgado.idempotentizer.core;

import java.util.UUID;

public interface Idempotentizer {

    RequestInfo checkProcessed(UUID idempotencyKey, String consumerId);

    void markAsProcessed(UUID idempotencyKey, String consumerId);
}