package com.nexus.boot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.nexus.core.annotations.NexusEventListener;
import com.nexus.core.bus.NexusEventBus;
import com.nexus.exceptions.InvalidListenerSignatureException;

public class EventHandlersPostProcessor {

    private static final System.Logger LOGGER = System.getLogger(EventHandlersPostProcessor.class.getName());

    private final List<Class<?>> subscribersList;
    private final Map<Class<?>, Object> cachedInstances;
    private final NexusEventBus eventBus = new NexusEventBus();

    public EventHandlersPostProcessor(List<Class<?>> subscribersList, Map<Class<?>, Object> cachedInstances) {
        this.subscribersList = subscribersList;
        this.cachedInstances = cachedInstances;
    }

    public void process() {
        subscribersList.forEach(subscriberClass -> {
            Object eventInstance = cachedInstances.get(subscriberClass);
            for (Method m : subscriberClass.getMethods()) {
                if (m.isAnnotationPresent(NexusEventListener.class) || (m.getName().equals("on") && !m.isBridge())) {
                    Class<?>[] parameterTypes = m.getParameterTypes();
                    if (parameterTypes.length != 1) {
                        throw new InvalidListenerSignatureException(String.format(
                            "Method %s in class %s was discovered as an event listener but declares %d parameter(s). " +
                            "Event listener methods must declare exactly one parameter — the event instance to handle.",
                            m.getName(), subscriberClass.getSimpleName(), parameterTypes.length));
                    }
                    eventBus.register(parameterTypes[0], event -> invokeListener(m, eventInstance, event));
                }
            }
        });
    }

    private void invokeListener(Method m, Object eventInstance, Object event) {
        try {
            m.invoke(eventInstance, event);
        } catch (IllegalAccessException e) {
            LOGGER.log(System.Logger.Level.ERROR, () -> String.format(
                    "Cannot invoke event handler method %s on %s: %s. " +
                            "Ensure the method annotated with @NexusEventListener is public.",
                    m.getName(), eventInstance.getClass().getSimpleName(), e.getClass().getSimpleName()), e);
        } catch (InvocationTargetException e) {
            LOGGER.log(System.Logger.Level.ERROR, () -> String.format(
                    "Event handler %s.%s threw an exception while handling %s: %s",
                    eventInstance.getClass().getSimpleName(), m.getName(),
                    event.getClass().getSimpleName(),
                    e.getCause() != null ? e.getCause().getMessage() : "Unknown error"), e);
        }
    }
}