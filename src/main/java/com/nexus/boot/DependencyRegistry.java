package com.nexus.boot;

import java.util.HashMap;
import java.util.Map;

import com.nexus.exceptions.DependencyNotFoundException;
import com.nexus.util.ClassValidator;

public final class DependencyRegistry implements Registry<DependencyRegistry> {
    private Map<Class<?>, Object> instances = Map.of();

    protected DependencyRegistry() {}

    private DependencyRegistry(Map<Class<?>, Object> init) {
        if (init != null && !init.isEmpty()) this.instances = Map.copyOf(init);
    }

    @Override
    public DependencyRegistry registry(Object... args) {
        ClassValidator.validateArgumentTypes(args, new Class<?>[] {Map.class});
        Map<?, ?> map = ClassValidator.cast(args[0], Map.class);

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof Class<?>)) throw new IllegalArgumentException("All map keys must be Class<?> instances");
        }
        @SuppressWarnings("unchecked")
        Map<Class<?>, Object> typedMap = (Map<Class<?>, Object>) map;
        return new DependencyRegistry(combine(this.instances, typedMap));
    }

    private Map<Class<?>, Object> combine(Map<Class<?>, Object> a, Map<Class<?>, Object> b) {
        Map<Class<?>, Object> combined = new HashMap<>(a);
        combined.putAll(b);
        return Map.copyOf(combined);
    }

    public <T> T get(Class<T> clazz) {
        Object instance = instances.get(clazz);
        if (instance == null) throw new DependencyNotFoundException("Dependency null: " + clazz);
        return clazz.cast(instance);
    }

    public Map<Class<?>, Object> getInstances() {
        return instances;
    }

}
