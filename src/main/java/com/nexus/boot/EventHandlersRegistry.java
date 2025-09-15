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
import com.nexus.exceptions.RegistryResolutionException;
import com.nexus.util.ClassValidator;

import io.github.classgraph.ScanResult;

public final class EventHandlersRegistry implements Registry<EventHandlersRegistry> {

	private Map<Class<?>, List<EventHandler<?>>> handlersMap = new HashMap<>();

	@Override
	public EventHandlersRegistry registry(Object... args) {
		ClassValidator.validateArgumentTypes(args, new Class<?>[] {DependencyRegistry.class, ScanResult.class});
		DependencyRegistry di = ClassValidator.cast(args[0], DependencyRegistry.class);
		ScanResult sr = ClassValidator.cast(args[1], ScanResult.class);
		initRegistry(di, sr);
		return this;
	}
	
	public void initRegistry(DependencyRegistry di, ScanResult sr) {
		List<Class<?>> eventHandlersClasses = sr.getClassesImplementing(EventHandler.class).loadClasses();
		eventHandlersClasses.forEach(clazz -> {
			EventHandler<?> handler = (EventHandler<?>) DependencyResolver.resolve(di, clazz);
			
			Class<?> eventType = extractEventType(clazz);
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
			.orElseThrow(() -> new RegistryResolutionException(String.format(
																"Cannot extract event type from EventHandler: %s. " +
																"Ensure the class properly implements EventHandler<YourEventType> with a concrete event type parameter.",
																handlerClass.getSimpleName()
															))
			);
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
