package com.nexus.core.cqrs;

public interface Command<T, R> extends Handler<T, R> {
    @Override
    R handle(T input);
}
