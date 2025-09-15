package com.nexus.boot;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.nexus.exceptions.RegistryInstantiationException;

public class RegistryProvider {
	private static final Map<Class<? extends Registry<?>>, Registry<?>> registries = new HashMap<>();
	
	private RegistryProvider() {}

	public static <T extends Registry<?>> T getRegistry(Class<T> clazz) {
		return clazz.cast(registries.computeIfAbsent(clazz, c -> {
			try {
				return c.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
				throw new RegistryInstantiationException(String.format(
															"Cannot instantiate registry class %s: %s. " +
															"Ensure the registry class has a public no-argument constructor and is properly designed.",
															clazz.getSimpleName(),
															e.getClass().getSimpleName()
														), e);
			} catch (InvocationTargetException e) {
				throw new RegistryInstantiationException(String.format(
															"Registry constructor invocation failed for class %s. " +
															"The constructor threw an exception during initialization: %s",
															clazz.getSimpleName(),
															e.getCause() != null ? e.getCause().getMessage() : "Unknown error"
														), e);
			}
		}));
	}
}
