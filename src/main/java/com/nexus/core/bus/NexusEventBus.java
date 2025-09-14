package com.nexus.core.bus;

import com.nexus.boot.EventHandlersRegistry;
import com.nexus.core.event.DomainEvent;
import com.nexus.core.event.EventBus;

public class NexusEventBus implements EventBus {
    private final EventHandlersRegistry eventRegistry;

    protected NexusEventBus(EventHandlersRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

    @Override
    public <T extends DomainEvent> void publish(Class<T> eventType, T event) {
        if(eventRegistry == null) {
            throw new IllegalStateException("EventHandlersRegistry not initialized");
        }
        eventRegistry.getHandlers(eventType).forEach(handler -> handler.on(event));   
    }
    
}
