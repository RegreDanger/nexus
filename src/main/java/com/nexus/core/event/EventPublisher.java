package com.nexus.core.event;

public interface EventPublisher {
    public <T extends DomainEvent> void register(Class<T> eventType, EventHandler<T> handler);
}
