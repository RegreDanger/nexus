package com.nexus.boot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexus.core.annotations.Injectable;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public final class InjectableRegistry implements Registry<DependencyRegistry> {

    protected InjectableRegistry() {}

    @Override
    public DependencyRegistry registry(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument: DependencyRegistry instance");
        }
        if(!(args[0] instanceof DependencyRegistry di)) {
            throw new IllegalArgumentException("The first arg must be instace of " + DependencyRegistry.class.getName() + "class");
        }
        List<Class<?>> sorted = sortByLevel(new ClassGraph().enableClassInfo().scan());
        return registerAll(sorted, di);
    }

    private List<Class<?>> sortByLevel(ScanResult sr) {
        List<Class<?>> injectables = sr.getClassesWithAnnotation("Injectable").loadClasses();
        injectables.sort(Comparator.comparingInt(cls -> {
            Injectable annotation = cls.getAnnotation(Injectable.class);
            return annotation != null ? annotation.level() : 0;
        }));
        return injectables;
    }

    private DependencyRegistry registerAll(List<Class<?>> sorted, DependencyRegistry di) {
        int currentLevel = -1;
        Map<Class<?>, Object> collecting = new HashMap<>();

        for (Class<?> injectable : sorted) {
            int level = injectable.getAnnotation(Injectable.class).level();
            if (level != currentLevel) {
                if (!collecting.isEmpty()) {
                    di.registry(collecting);
                    collecting.clear();
                }
                currentLevel = level;
            }
            collecting.put(injectable, DependencyResolver.resolve(di, injectable));
        }

        if (!collecting.isEmpty()) {
            di.registry(collecting);
        }

        return di;

    }

}
