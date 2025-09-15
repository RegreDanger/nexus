package com.nexus.core.event;

public interface EventBus {
	public <T extends DomainEvent> void publish(Class<T> eventType, T event);
}
