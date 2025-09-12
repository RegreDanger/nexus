package com.nexus.core.bus;

import com.nexus.boot.CqrsHandlersRegistry;
import com.nexus.core.cqrs.Bus;
import com.nexus.core.cqrs.Handler;

public class NexusCqrsBus implements Bus {
    private final CqrsHandlersRegistry cqrsRegistry;

    protected NexusCqrsBus(CqrsHandlersRegistry cqrsRegistry) {
        this.cqrsRegistry = cqrsRegistry;
    }

    @Override
    public <C, R, T extends Handler<C, R>> R send(Class<T> handlerClass, C input) {
        if(cqrsRegistry == null) {
            throw new IllegalStateException("CqrsHandlersRegistry not initialized");
        }
        return cqrsRegistry.getCQRSHandler(handlerClass).handle(input);
    }
    
}
