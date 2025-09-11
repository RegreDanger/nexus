package com.nexus.boot;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class RegistryProvider {
    private static final Map<Class<? extends Registry<?>>, Registry<?>> registries = new HashMap<>();
    
    private RegistryProvider() {}

    public static <T extends Registry<?>> T getRegistry(Class<T> clazz) {
        return clazz.cast(registries.computeIfAbsent(clazz, c -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new IllegalStateException("Cannot instantiate/access registry class " + clazz.getName(), e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Registry constructor invocation failed for " + clazz.getName(), e);
            }
        }));
    }
}
