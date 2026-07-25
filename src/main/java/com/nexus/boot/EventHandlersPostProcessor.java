package com.nexus.boot;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.nexus.core.annotations.NexusEventListener;

public class EventHandlersPostProcessor {

    private final List<Class<?>> subscribersList;
    private final Map<Class<?>, Object> cachedInstances;

    public EventHandlersPostProcessor(List<Class<?>> subscribersList, Map<Class<?>, Object> cachedInstances) {
        this.subscribersList = subscribersList;
        this.cachedInstances = cachedInstances;
    }

    public void process() {
        subscribersList.forEach(subscriberClass -> {
            cachedInstances.get(subscriberClass);
            for(Method m : subscriberClass.getMethods()) {
                if (m.isAnnotationPresent(NexusEventListener.class)) {
                    Class<?>[] parameterTypes = m.getParameterTypes();
                    if(parameterTypes.length > 1) {
                    }
                }
            }
        });
    }

}