package com.nexus.core.cqrs;

public interface CqrsBus {
    <C, R, T extends Handler<C, R>> R send(Class<T> handlerClass, C input);
}
