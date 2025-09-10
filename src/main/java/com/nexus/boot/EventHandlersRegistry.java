package com.nexus.boot;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
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
            throw new IllegalArgumentException("The first arg must be instace of " + DependencyRegistry.class.getName() + "class");
        }
        initRegistry(di, new ClassGraph().enableClassInfo().scan());
        return null;
    }
    
    public void initRegistry(DependencyRegistry di, ScanResult sr) {
        List<Class<?>> a = sr.getClassesImplementing(EventHandler.class).loadClasses();
        a.forEach(cls -> {
            EventHandler<?> handler = (EventHandler<?>) DependencyResolver.resolve(di, cls);
            
            Class<?> eventType = (Class<?>) ((ParameterizedType) cls
                        .getGenericInterfaces()[0])
                        .getActualTypeArguments()[0];
            addHandler(eventType, handler);
        });
    }

    private void addHandler(Class<?> eventType, EventHandler<?> handler) {
        handlersMap.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <E extends DomainEvent> List<EventHandler<E>> getHandlers(Class<E> eventType) {
        return (List<EventHandler<E>>) (List<?>) handlersMap.get(eventType);
    }

}
