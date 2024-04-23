package com.ersalgado.idempotentizer.core;

import java.util.UUID;

public interface Repository {

    RequestInfo findRequestInfo(UUID idempotencyKey, String consumerId);

    void persistRequestInfo(UUID idempotencyKey, String consumerId, Object returnedValue);
}
