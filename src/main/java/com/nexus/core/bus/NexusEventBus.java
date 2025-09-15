package com.nexus.core.bus;

import com.nexus.boot.EventHandlersRegistry;
import com.nexus.core.event.DomainEvent;
import com.nexus.core.event.EventBus;
import com.nexus.exceptions.BusInitializationException;

public class NexusEventBus implements EventBus {
	private final EventHandlersRegistry eventRegistry;

	protected NexusEventBus(EventHandlersRegistry eventRegistry) {
		this.eventRegistry = eventRegistry;
	}

	@Override
	public <T extends DomainEvent> void publish(Class<T> eventType, T event) {
		if(eventRegistry == null) {
			throw new BusInitializationException(String.format(
												"EventHandlersRegistry is not initialized. " +
												"This indicates an internal error in NexusContext initialization. " +
												"Try rebuilding the NexusContext or contact support if the problem persists."
											));
		}
		eventRegistry.getHandlers(eventType).forEach(handler -> handler.on(event));   
	}
	
}
