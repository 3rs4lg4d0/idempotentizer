package com.ersalgado.idempotentizer.sql;

public class SerializedObjectWrapperForTesting {

    private String className;
    private byte[] serializedObject;

    public SerializedObjectWrapperForTesting() {}

    public SerializedObjectWrapperForTesting(String className, byte[] serializedObject) {
        this.className = className;
        this.serializedObject = serializedObject;
    }

    public String getClassName() {
        return className;
    }

    public byte[] getSerializedObject() {
        return serializedObject;
    }
}
