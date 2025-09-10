package com.nexus.boot;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexus.core.event.DomainEvent;
import com.nexus.core.event.EventHandler;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public final class EventHandlersRegistry implements Registry<Void> {

    private Map<Class<?>, List<EventHandler<?>>> handlersMap = new HashMap<>();

    @Override
    public Void registry(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument: DependencyRegistry instance");
        }
        if(!(args[0] instanceof DependencyRegistry di)) {
            throw new IllegalArgumentException("The first arg must be instance of " + DependencyRegistry.class.getName() + " class");
        }
        initRegistry(di, new ClassGraph().enableClassInfo().scan());
        return null;
    }
    
    public void initRegistry(DependencyRegistry di, ScanResult sr) {
        List<Class<?>> a = sr.getClassesImplementing(EventHandler.class).loadClasses();
        a.forEach(cls -> {
            EventHandler<?> handler = (EventHandler<?>) DependencyResolver.resolve(di, cls);
            
            Class<?> eventType = extractEventType(cls);
            addHandler(eventType, handler);
        });
    }

    private Class<?> extractEventType(Class<?> handlerClass) {
    return Arrays.stream(handlerClass.getGenericInterfaces())
        .filter(ParameterizedType.class::isInstance)
        .map(ParameterizedType.class::cast)
        .filter(pt -> pt.getRawType().equals(EventHandler.class))
        .findFirst()
        .map(pt -> (Class<?>) pt.getActualTypeArguments()[0])
        .orElseThrow(() -> new IllegalStateException(
            "Cannot extract event type from handler: " + handlerClass.getName()));
}

    private void addHandler(Class<?> eventType, EventHandler<?> handler) {
        handlersMap.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <E extends DomainEvent> List<EventHandler<E>> getHandlers(Class<E> eventType) {
        List<EventHandler<E>> handlers = (List<EventHandler<E>>) (List<?>) handlersMap.get(eventType);
        return handlers != null ? handlers : Collections.emptyList();
    }

}
