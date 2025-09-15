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
		if (annotatedConstructors.isEmpty()) throw new DependencyInstantiationException(String.format(
																							"No constructor annotated with @Inject found in class %s. " +
																							"Add @Inject annotation to exactly one public constructor to enable dependency injection.",
																							clazz.getSimpleName()
																						));

		Constructor<?> ctor = annotatedConstructors.get(0);
		Class<?>[] deps = ctor.getParameterTypes();
		Object[] depInstances = new Object[deps.length];

		for (int i = 0; i < deps.length; i++) {
			depInstances[i] = di.get(deps[i]);
		}
		try {
			return clazz.cast(ctor.newInstance(depInstances));
		} catch (InvocationTargetException e) {
			throw new DependencyInstantiationException(String.format(
														"Constructor invocation failed for class %s: %s. " +
														"Check that the constructor logic doesn't throw exceptions and all dependencies are properly configured.",
														clazz.getSimpleName(),
														ctor.toString()
													), e.getCause());
		} catch (IllegalAccessException e) {
			throw new DependencyInstantiationException(String.format(
														"Cannot access constructor for class %s: %s. " +
														"Ensure the constructor is public and the class is accessible.",
														clazz.getSimpleName(),
														ctor.toString()
													), e);
		} catch (InstantiationException e) {
			throw new DependencyInstantiationException(String.format(
														"Cannot instantiate class %s: it appears to be abstract or an interface. " +
														"Only concrete classes with public constructors can be instantiated through dependency injection.",
														clazz.getSimpleName()
													), e);
		}
	}

	private static List<Constructor<?>> getAnnotatedConstructors(Class<?> clazz) {
		return Arrays.stream(clazz.getConstructors()).filter(con -> con.isAnnotationPresent(Inject.class)).toList();
	}

	private static void hasMultipleAnnotatedConstructors(List<Constructor<?>> annotatedConstructors) {
		if(annotatedConstructors.size() > 1) throw new DependencyInstantiationException(String.format(
																							"Multiple constructors annotated with @Inject found in class %s. " +
																							"Only one constructor per class should be annotated with @Inject. " +
																							"Remove @Inject from all but one constructor.",
																							annotatedConstructors.get(0).getDeclaringClass().getSimpleName()
																						));
	}

}
