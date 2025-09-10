package com.nexus.core.event;

public interface EventHandler<T extends DomainEvent> {
    public void on(T event);
}
