package com.ersalgado.idempotentizer.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class JacksonObjectSerde implements ObjectSerde {

    private final ObjectMapper objectMapper;

    public JacksonObjectSerde() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public byte[] serialize(Object object) throws IOException {
        if (object == null) return new byte[0];

        String className = object.getClass().getName();
        byte[] objectBytes = objectMapper.writeValueAsBytes(object);

        SerializedObjectWrapper wrapper = new SerializedObjectWrapper(className, objectBytes);
        return objectMapper.writeValueAsBytes(wrapper);
    }

    @Override
    public Object deserialize(byte[] wrapperBytes) throws IOException, ClassNotFoundException {
        if (wrapperBytes == null || wrapperBytes.length == 0) return null;

        SerializedObjectWrapper wrapper =
                objectMapper.readValue(new ByteArrayInputStream(wrapperBytes), SerializedObjectWrapper.class);

        String className = wrapper.getClassName();
        byte[] serializedObject = wrapper.getSerializedObject();

        return objectMapper.readValue(new ByteArrayInputStream(serializedObject), Class.forName(className));
    }

    private static class SerializedObjectWrapper {

        private String className;
        private byte[] serializedObject;

        @SuppressWarnings("unused")
        public SerializedObjectWrapper() {}

        public SerializedObjectWrapper(String className, byte[] serializedObject) {
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
}
