package com.uco.ucopetapi.util.gson;

import java.util.Optional;

public interface MapperJsonObject {

    Optional<String> execute(Object object);

    <T> Optional<T> execute(String json, Class<T> destinationClass);

    Optional<String> executeGson(Object object);
}
