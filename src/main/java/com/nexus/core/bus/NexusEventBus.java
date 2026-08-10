	package com.nexus.core.bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.nexus.core.event.DomainEvent;
import com.nexus.core.event.EventBus;

public class NexusEventBus implements EventBus {
	
	private final Map<Class<?>, List<Consumer<Object>>> listeners = new HashMap<>();

	public NexusEventBus() {}
	
	@SuppressWarnings("unchecked")
	public <T> void register(Class<T> eventType, Consumer<T> handler) {
		listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add((Consumer<Object>) handler);
	}
	
	@Override
	public <T extends DomainEvent> void publish(Class<T> eventType, T event) {
		List<Consumer<Object>> targets = listeners.get(eventType);
		if (targets != null) {
			for(Consumer<Object> t : targets) {
				t.accept(event);
			}
		}
	}
	
	public <T> void publish(T event) {
		List<Consumer<Object>> targets = listeners.get(event.getClass());
		if (targets != null) {
			for(Consumer<Object> t : targets) {
				t.accept(event);
			}
		}
	}
	
}


