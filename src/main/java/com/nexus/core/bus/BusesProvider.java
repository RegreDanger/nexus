package com.nexus.core.bus;

import com.nexus.boot.CqrsHandlersRegistry;
import com.nexus.boot.EventHandlersRegistry;

public class BusesProvider {
	private static NexusCqrsBus cqrsBus;
	private static NexusEventBus eventBus;

	private BusesProvider() {}

	public static NexusCqrsBus getNexusCqrsBus(CqrsHandlersRegistry registry) {
		if(cqrsBus == null) {
			cqrsBus = new NexusCqrsBus(registry);
		}
		return cqrsBus;
	}

	public static NexusEventBus getNexusEventBus(EventHandlersRegistry registry) {
		if(eventBus == null) {
			eventBus = new NexusEventBus(registry);
		}
		return eventBus;
	}
}
