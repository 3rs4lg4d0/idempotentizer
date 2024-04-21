package com.ersalgado.idempotentizer.core;

public class IdempotentizerException extends RuntimeException {

    public IdempotentizerException(String msg) {
        super(msg);
    }
}
