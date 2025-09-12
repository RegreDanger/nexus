package com.nexus.core.event;

public interface EventPublisher {
    public <T extends DomainEvent> void publish(Class<T> eventType, T event);
}
