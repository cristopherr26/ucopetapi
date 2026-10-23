package com.uco.ucopetapi.util;

public interface MessageSender <T>{
    void execute(T message, String idMessage);
}
