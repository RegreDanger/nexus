package com.nexus.boot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import com.nexus.core.annotations.Inject;
import com.nexus.core.annotations.Injectable;
import com.nexus.exceptions.DependencyInstantiationException;

public final class DependencyResolver {

    private DependencyResolver() {}

    public static Object resolve(DependencyRegistry di, Class<?> cls) {
        
        List<Constructor<?>> annotatedConstructors = getAnnotatedConstructors(cls);
        hasMultipleAnnotatedConstructors(annotatedConstructors);

        Constructor<?> ctor = annotatedConstructors.get(0);
        Class<?>[] deps = ctor.getParameterTypes();
        Object[] depInstances = new Object[deps.length];

        for (int i = 0; i < deps.length; i++) {
            depInstances[i] = di.get(deps[i]);
        }
        try {
            return cls.cast(ctor.newInstance(depInstances));
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

    private static List<Constructor<?>> getAnnotatedConstructors(Class<?> cls) {
        return Arrays.stream(cls.getConstructors()).filter(con -> con.isAnnotationPresent(Inject.class)).toList();
    }

    private static void hasMultipleAnnotatedConstructors(List<Constructor<?>> annotatedConstructors) {
        if(annotatedConstructors.size() > 1) {
            throw new IllegalStateException("Multiple constructors annotated with: " + Injectable.class.getName());
        }
    }

}
