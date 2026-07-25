package com.nexus.boot;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.nexus.core.annotations.Inject;
import com.nexus.core.annotations.Injectable;
import com.nexus.core.annotations.NexusBean;
import com.nexus.core.annotations.NexusComponent;
import com.nexus.core.annotations.NexusConfiguration;
import com.nexus.core.annotations.NexusEventSubscriber;
import com.nexus.core.annotations.NexusInject;
import com.nexus.core.annotations.NexusQualifier;

import io.github.classgraph.ScanResult;

public class KahnsResolver {

    private final Map<Class<?>, DependencyNode> totalNodes = new HashMap<>();
    private final Map<Class<?>, Integer> inDegreeMap = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> adyancentDeps = new HashMap<>();
    private final Deque<DependencyNode> unresolvedNodes = new ArrayDeque<>();
    private final List<DependencyNode> resolvedNodes = new ArrayList<>();
    private final Map<Class<?>, Object> cachedInstances = new HashMap<>();
    private final Map<Class<?>, Map<String, Object>> cachedInterfaceInstances = new HashMap<>();

    private final List<Class<?>> subscribersList = new ArrayList<>();

    public KahnsResolver(ScanResult sr) {
        List<Class<?>> toSolve = getAnnotatedClasses(sr, Injectable.class, NexusComponent.class,
                NexusConfiguration.class, NexusEventSubscriber.class);
        toSolve.forEach(component -> {
            map(getValidConstructor(component));
            if (component.isAnnotationPresent(NexusConfiguration.class)) {
                Stream.of(component.getMethods())
                        .filter(method -> method.isAnnotationPresent(NexusBean.class))
                        .forEach(this::map);
            }

            if(component.isAnnotationPresent(NexusEventSubscriber.class)) {
                subscribersList.add(component);
            }
        });

        DependencyNode evaluatedNode;
        while ((evaluatedNode = unresolvedNodes.poll()) != null) {
            Object[] args = fillParameters(evaluatedNode);
            initializeInstance(evaluatedNode, args);
            resolvedNodes.add(evaluatedNode);
            notifyDependencies(evaluatedNode);
        }
        checkCyclicDependencies();

    }

    @SafeVarargs
    private List<Class<?>> getAnnotatedClasses(ScanResult sr, Class<? extends Annotation>... annotations) {
        return sr.getClassesWithAnyAnnotation(annotations).loadClasses();
    }

    private Constructor<?> getValidConstructor(Class<?> component) {
        Constructor<?>[] constructors = component.getConstructors();
        if (constructors.length == 0) {
            throw new IllegalStateException("Class must contain at least one constructor");
        }
        List<Constructor<?>> validConstructors = Stream.of(constructors)
                .filter(constructor -> (constructor.isAnnotationPresent(Inject.class)
                        || constructor.isAnnotationPresent(NexusInject.class)))
                .toList();
        if (validConstructors.size() > 1) {
            throw new IllegalStateException("Two or more constructors annotated, Nexus can't differ which to take");
        }
        if (validConstructors.isEmpty()) {
            return constructors[0];
        }
        return validConstructors.get(0);
    }

    private void map(Executable executable) {
        DependencyNode node = new DependencyNode(executable);
        totalNodes.put(node.getInstanceType(), node);
        int degree = node.getInDegree();
        inDegreeMap.put(node.getInstanceType(), degree);
        if (degree == 0) {
            unresolvedNodes.add(node);
        }
        node.getAllDependencyTypes()
                .forEach(dep -> adyancentDeps.computeIfAbsent(dep, k -> new ArrayList<>()).add(node.getInstanceType()));
    }

    private Object[] fillParameters(DependencyNode evaluatedNode) {
        List<Parameter> listNodeActualParameterArgs = evaluatedNode.getAllActualParametersArgs();
        Object[] args = new Object[listNodeActualParameterArgs.size()];
        for (int i = 0; i < listNodeActualParameterArgs.size(); i++) {
            Parameter currentParam = listNodeActualParameterArgs.get(i);
            Class<?> type = currentParam.getType();
            args[i] = fillInterfaceParameter(type, currentParam).orElseGet(() -> cachedInstances.get(type));
        }
        return args;
    }

    private Optional<Object> fillInterfaceParameter(Class<?> type, Parameter currentParam) {
        if (type.isInterface()) {
            if (!cachedInterfaceInstances.containsKey(type) || cachedInterfaceInstances.get(type).isEmpty()) {
                throw new IllegalStateException(
                        "Nexus cannot find any implementation registered for: "
                                + type.getSimpleName()
                                + ", add its id either NexusComponent or NexusBean annotations");
            }

            if (currentParam.isAnnotationPresent(NexusQualifier.class)) {
                String qualifierName = currentParam.getAnnotation(NexusQualifier.class).id();
                Object implementation = cachedInterfaceInstances.get(type).get(qualifierName);

                if (implementation == null) {
                    throw new IllegalStateException("Nexus cannot find any variant with ID '" + qualifierName
                            + "' for the interface:" + type.getSimpleName());
                }
                return Optional.of(implementation);
            } else {
                if (cachedInterfaceInstances.get(type).size() > 1) {
                    String availableIds = String.join(", ", cachedInterfaceInstances.get(type).keySet());
                    throw new IllegalStateException(
                            "Multiple implementations found for " + type.getSimpleName()
                                    + ". Available IDs: [" + availableIds
                                    + "]. Specify one with @NexusQualifier(id=\"...\")");
                }
                return Optional.of(cachedInterfaceInstances.get(type).values().toArray()[0]);
            }
        }
        return Optional.empty();
    }

    private void initializeInstance(DependencyNode evaluatedNode, Object[] args) {
        Object optionalInstanceClassForMethod = cachedInstances.get(evaluatedNode.getWrapperDeclaringClass());
        Object invokedInstance = evaluatedNode.invoke(optionalInstanceClassForMethod, args);
        cachedInstances.put(evaluatedNode.getInstanceType(), invokedInstance);
        List<Class<?>> interfacesInInstance = Arrays.asList(evaluatedNode.getInstanceType().getInterfaces());
        String idNode = evaluatedNode.getId();
        if (!idNode.isBlank()) {
            interfacesInInstance.forEach(interfaceClass -> cachedInterfaceInstances
                    .computeIfAbsent(interfaceClass, k -> new HashMap<>()).put(idNode, invokedInstance));
        }
    }

    private void notifyDependencies(DependencyNode evaluatedNode) {
        List<Class<?>> identitiesToNotify = new ArrayList<>();
        identitiesToNotify.add(evaluatedNode.getInstanceType());
        identitiesToNotify.addAll(Arrays.asList(evaluatedNode.getInstanceType().getInterfaces()));

        identitiesToNotify
                .forEach(identity -> adyancentDeps.getOrDefault(identity, List.of()).forEach(classDependant -> {
                    int currentUnsolvedGrade = inDegreeMap.get(classDependant) - 1;
                    inDegreeMap.put(classDependant, currentUnsolvedGrade);
                    if (currentUnsolvedGrade == 0) {
                        unresolvedNodes.add(totalNodes.get(classDependant));
                    }
                }));
    }

    private void checkCyclicDependencies() {
        if (resolvedNodes.size() != totalNodes.size()) {
            List<String> caught = inDegreeMap.entrySet().stream().filter(entry -> entry.getValue() > 0)
                    .map(entry -> entry.getKey().getSimpleName()).toList();
            throw new IllegalStateException(
                    "Cyclic Dependency Detected! The following classes are caught in a cyclic: " + caught);
        }
    }

}
