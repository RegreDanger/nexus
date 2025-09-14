package com.nexus.boot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import com.nexus.core.annotations.Inject;
import com.nexus.exceptions.DependencyInstantiationException;

public final class DependencyResolver {

    private DependencyResolver() {}

    public static Object resolve(DependencyRegistry di, Class<?> clazz) {
        
        List<Constructor<?>> annotatedConstructors = getAnnotatedConstructors(clazz);
        hasMultipleAnnotatedConstructors(annotatedConstructors);
        if (annotatedConstructors.isEmpty()) throw new IllegalStateException("No constructor annotated with @Inject found in class: " + clazz.getName());

        Constructor<?> ctor = annotatedConstructors.get(0);
        Class<?>[] deps = ctor.getParameterTypes();
        Object[] depInstances = new Object[deps.length];

        for (int i = 0; i < deps.length; i++) {
            depInstances[i] = di.get(deps[i]);
        }
        try {
            return clazz.cast(ctor.newInstance(depInstances));
        } catch (InvocationTargetException e) {
            throw new DependencyInstantiationException(
                "Constructor threw an exception: " + ctor, e.getCause()
            );
        } catch (IllegalAccessException e) {
            throw new DependencyInstantiationException(
                "Cannot access constructor " + ctor, e
            );
        } catch (InstantiationException e) {
            throw new DependencyInstantiationException(
                "Cannot instantiate abstract/interface class: " + ctor, e
            );
        }
    }

    private static List<Constructor<?>> getAnnotatedConstructors(Class<?> clazz) {
        return Arrays.stream(clazz.getConstructors()).filter(con -> con.isAnnotationPresent(Inject.class)).toList();
    }

    private static void hasMultipleAnnotatedConstructors(List<Constructor<?>> annotatedConstructors) {
        if(annotatedConstructors.size() > 1) throw new IllegalStateException("Multiple constructors annotated with: " + Inject.class.getName());
    }

}
