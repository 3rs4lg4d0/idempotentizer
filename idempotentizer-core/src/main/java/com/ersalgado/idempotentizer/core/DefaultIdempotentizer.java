package com.ersalgado.idempotentizer.core;

import java.util.UUID;

public class DefaultIdempotentizer implements Idempotentizer {

    private Repository repository;

    public DefaultIdempotentizer(Repository repository) {
        this.repository = repository;
    }

    @Override
    public RequestInfo checkProcessed(UUID idempotencyKey, String consumerId) {
        checkNotNull(idempotencyKey, "idempotencyKey");
        checkNotNull(idempotencyKey, "consumerId");
        return repository.findRequestInfo(idempotencyKey, consumerId);
    }

    @Override
    public void markAsProcessed(UUID idempotencyKey, String consumerId) {
        checkNotNull(idempotencyKey, "idempotencyKey");
        checkNotNull(idempotencyKey, "consumerId");
        repository.persistRequestInfo(idempotencyKey, consumerId);
    }

    private void checkNotNull(Object paramValue, String paramName) {
        if (paramValue == null)
            throw new IdempotentizerException(String.format("parameter '%s' is mandatory", paramName));
    }
}
