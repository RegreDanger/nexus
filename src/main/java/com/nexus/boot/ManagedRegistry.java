package com.nexus.boot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexus.core.annotations.Managed;
import com.nexus.core.annotations.WiringConfig;
import com.nexus.exceptions.DependencyInstantiationException;
import com.nexus.util.ClassValidator;

public final class ManagedRegistry implements Registry<DependencyRegistry> {

    protected ManagedRegistry() {}

    @Override
    public DependencyRegistry registry(Object... args) {
        ClassValidator.validateArgs(args, new Class<?>[] {DependencyRegistry.class, PackagesRegistry.class});
        DependencyRegistry di = ClassValidator.cast(args[0], DependencyRegistry.class);
        PackagesRegistry pr = ClassValidator.cast(args[1], PackagesRegistry.class);
        
        List<Class<?>> classes = pr.getScanResult().getClassesWithAnnotation(WiringConfig.class).loadClasses();
        Map<Class<?>, Object> manageds = new HashMap<>();

        classes.forEach(cls -> 
            Arrays.stream(cls.getMethods()).forEach(m -> {
                if(m.isAnnotationPresent(Managed.class)) {
                    try {
                        hasParameters(m);
                        isVoid(m);
                        manageds.put(m.getReturnType(), m.invoke(null));
                    } catch (IllegalAccessException e) {
                        throw new DependencyInstantiationException(
                            "Cannot access method, should be public and static " + m, e
                        );
                    } catch (InvocationTargetException e) {
                        throw new DependencyInstantiationException(
                            "Method threw an exception: " + m, e.getCause()
                        );
                    }
                }
            })
        );
        return di.registry(manageds);
    }

    private void hasParameters(Method m) {
        if(m.getParameterTypes().length != 0) {
            throw new DependencyInstantiationException(
            "Cannot register injectable " + m + " depends on " + m.getParameterTypes()
            );
        }
    }

    private void isVoid(Method m) {
        if(m.getReturnType() == void.class) {
            throw new DependencyInstantiationException(
            "Cannot register injectable, returns void " + m
            );
        }
    }

}
