package com.nexus.core.event;

public interface EventPublisher {
    public void publish(DomainEvent event);
}
