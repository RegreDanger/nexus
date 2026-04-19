package com.nexus.boot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nexus.core.annotations.NexusBean;
import com.nexus.core.annotations.NexusComponent;

public class DependencyNode {

    private final Class<?> instanceType;
    private final Executable executable;
    private final List<Parameter> parameterArgs;
    private final List<Class<?>> dependencyTypes;
    private final Class<?> wrapperDeclaringClass;
    private final boolean isStatic;
    private final String id;

    public DependencyNode(Executable executable) {
        this.executable = executable;

        this.parameterArgs = Arrays.asList(executable.getParameters());
        this.dependencyTypes = Arrays.asList(executable.getParameterTypes());
        this.isStatic = Modifier.isStatic(executable.getModifiers());
        this.wrapperDeclaringClass = executable.getDeclaringClass();

        switch (executable) {
            case Method method -> {
                this.instanceType = method.getReturnType();
                this.id = method.getAnnotation(NexusBean.class).id();
            }
            case Constructor<?> constructor -> {
                this.instanceType = this.wrapperDeclaringClass;
                this.id = constructor.getAnnotation(NexusComponent.class).id();
            }
            default -> throw new IllegalArgumentException("Executable must be either constructor or method");
        }

    }

    public Object invoke(Object instance, Object... args) {
        try {
            return switch (executable) {
                case Method method -> {
                    if(!isStatic) {
                        yield method.invoke(instance, args);
                    } else {
                        yield method.invoke(null, args);
                    }
                }
                case Constructor<?> constructor -> constructor.newInstance(args);
            };
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | IllegalArgumentException e) {
            throw new IllegalStateException("Cannot invoke the method or constructor", e);
        }
    }

    public int getInDegree() {
        int degree = parameterArgs.size();
        if (executable instanceof Method && !isStatic) {
            degree += 1;
        }
        return degree;
    }

    public List<Class<?>> getAllDependencyTypes() {
        List<Class<?>> allDeps = new ArrayList<>(dependencyTypes);
        if (executable instanceof Method && !isStatic) {
            allDeps.add(wrapperDeclaringClass);
        }
        return allDeps;
    }

    public List<Parameter> getAllActualParametersArgs() {
        return parameterArgs;
    }

    public Class<?> getInstanceType() {
        return instanceType;
    }

    public Class<?> getWrapperDeclaringClass() {
        return wrapperDeclaringClass;
    }

    public String getId() {
        return this.id;
    }

}
