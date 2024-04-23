package com.ersalgado.idempotentizer.core;

import java.io.IOException;

public interface ObjectSerde {

    byte[] serialize(Object object) throws IOException;

    Object deserialize(byte[] wrapperBytes) throws IOException, ClassNotFoundException;
}
