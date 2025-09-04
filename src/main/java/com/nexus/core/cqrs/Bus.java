package com.nexus.core.cqrs;

public interface Bus {
    <C, R, T extends Handler<C, R>> R send(Class<T> handlerClass, C input);
}
