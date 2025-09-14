package com.nexus.boot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexus.core.annotations.Injectable;
import com.nexus.util.ClassValidator;

import io.github.classgraph.ScanResult;

public final class InjectableRegistry implements Registry<DependencyRegistry> {

    protected InjectableRegistry() {}

    @Override
    public DependencyRegistry registry(Object... args) {
        ClassValidator.validateArgs(args, new Class<?>[] {DependencyRegistry.class, ScanResult.class});
        DependencyRegistry di = ClassValidator.cast(args[0], DependencyRegistry.class);
        ScanResult sr = ClassValidator.cast(args[1], ScanResult.class);
        List<Class<?>> sorted = sortByLevel(sr);
        return registerAll(sorted, di);
    }

    private List<Class<?>> sortByLevel(ScanResult sr) {
        List<Class<?>> injectables = sr.getClassesWithAnnotation(Injectable.class).loadClasses();
        injectables.sort(Comparator.comparingInt(cls -> 
            cls.getAnnotation(Injectable.class).level()
        ));
        return injectables;
    }

    private DependencyRegistry registerAll(List<Class<?>> sorted, DependencyRegistry di) {
        int currentLevel = -1;
        Map<Class<?>, Object> collecting = new HashMap<>();

        for (Class<?> injectable : sorted) {
            int level = injectable.getAnnotation(Injectable.class).level();
            if (level != currentLevel) {
                if (!collecting.isEmpty()) {
                    di = di.registry(collecting);
                    collecting.clear();
                }
                currentLevel = level;
            }
            collecting.put(injectable, DependencyResolver.resolve(di, injectable));
        }

        if (!collecting.isEmpty()) {
            di = di.registry(collecting);
        }

        return di;

    }

}
