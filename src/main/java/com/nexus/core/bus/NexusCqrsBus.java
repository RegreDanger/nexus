package com.nexus.core.bus;

import com.nexus.boot.CqrsHandlersRegistry;
import com.nexus.core.cqrs.CqrsBus;
import com.nexus.core.cqrs.Handler;
import com.nexus.exceptions.BusInitializationException;

public class NexusCqrsBus implements CqrsBus {
	private final CqrsHandlersRegistry cqrsRegistry;

	protected NexusCqrsBus(CqrsHandlersRegistry cqrsRegistry) {
		this.cqrsRegistry = cqrsRegistry;
	}

	@Override
	public <C, R, T extends Handler<C, R>> R send(Class<T> handlerClass, C input) {
		if(cqrsRegistry == null) {
			throw new BusInitializationException(String.format(
													"CqrsHandlersRegistry is not initialized. " +
													"This indicates an internal error in NexusContext initialization. " +
													"Try rebuilding the NexusContext or contact support if the problem persists."
												));
		}
		return cqrsRegistry.getCQRSHandler(handlerClass).handle(input);
	}
	
}
