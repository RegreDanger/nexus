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

import io.github.classgraph.ScanResult;

public final class ManagedRegistry implements Registry<DependencyRegistry> {

	protected ManagedRegistry() {}

	@Override
	public DependencyRegistry registry(Object... args) {
		ClassValidator.validateArgumentTypes(args, new Class<?>[] {DependencyRegistry.class, ScanResult.class});
		DependencyRegistry di = ClassValidator.cast(args[0], DependencyRegistry.class);
		ScanResult sr = ClassValidator.cast(args[1], ScanResult.class);
		
		List<Class<?>> classes = sr.getClassesWithAnnotation(WiringConfig.class).loadClasses();
		Map<Class<?>, Object> manageds = new HashMap<>();
		initMethodRegistry(classes, manageds);
		
		return di.registry(manageds);
	}

	private void initMethodRegistry(List<Class<?>> classes, Map<Class<?>, Object> manageds) {
		classes.forEach(clazz -> 
			Arrays.stream(clazz.getMethods())
			.filter(m -> m.isAnnotationPresent(Managed.class))
			.forEach(m -> {
				validateManagedMethod(m);
				manageds.put(m.getReturnType(), secureInvoke(m));
			})
		);
	}

	private void validateManagedMethod(Method m) {
		boolean hasParameters = hasParameters(m);
		boolean isVoid = isVoid(m);
		if(hasParameters) throw new DependencyInstantiationException(String.format(
																		"Invalid @Managed method %s in class %s: methods annotated with @Managed must have no parameters. " +
																		"Remove all parameters from the method or handle dependencies differently.",
																		m.getName(),
																		m.getDeclaringClass().getSimpleName()
																	));
		if(isVoid) throw new DependencyInstantiationException(String.format(
																"Invalid @Managed method %s in class %s: methods annotated with @Managed cannot return void. " +
																"Change the method to return the instance that should be managed as a dependency.",
																m.getName(),
																m.getDeclaringClass().getSimpleName()
															));
	}

	private Object secureInvoke(Method m) {
		try {
			return m.invoke(null);
		} catch (IllegalAccessException e) {
			throw new DependencyInstantiationException(String.format(
														"Cannot access @Managed method %s in class %s: method must be public and static. " +
														"Ensure the method is declared as 'public static YourType methodName()'.",
														m.getName(),
														m.getDeclaringClass().getSimpleName()
													), e);
		} catch (InvocationTargetException e) {
			throw new DependencyInstantiationException(String.format(
														"Method invocation failed for @Managed method %s in class %s: %s. " +
														"Check that the method logic doesn't throw exceptions.",
														m.getName(),
														m.getDeclaringClass().getName(),
														e.getCause() != null ? e.getCause().getMessage() : "Unknown error"
													), e.getCause());
		}
	}

	private boolean hasParameters(Method m) {
		return m.getParameterTypes().length != 0;
	}

	private boolean isVoid(Method m) {
		return m.getReturnType() == void.class;
	}

}
