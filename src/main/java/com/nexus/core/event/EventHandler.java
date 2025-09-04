package com.nexus.core.event;

public interface EventHandler<T> {
    public void on(T event);
}
