package unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.DependencyResolver;
import com.nexus.boot.RegistryProvider;
import com.nexus.exceptions.DependencyInstantiationException;
import com.nexus.exceptions.DependencyNotFoundException;

class DependencyResolverTest {

	@Test
	void resolveShouldThrowWhenNoAnnotatedConstructor() {
		// Arrange
		DependencyRegistry di = RegistryProvider.getRegistry(DependencyRegistry.class);

		DependencyInstantiationException ex = assertThrows(DependencyInstantiationException.class,
				() -> DependencyResolver.resolve(di, NoInjectConstructor.class),
				"Classes without an @Inject annotated constructor must cause DependencyInstantiationException");

		// Message should mention the class name to help debugging
		assertTrue(ex.getMessage().contains(NoInjectConstructor.class.getSimpleName()),
				"Exception message should include the class name with missing @Inject constructor");
	}

	@Test
	void resolveShouldThrowWhenMultipleAnnotatedConstructors() {
		DependencyRegistry di = RegistryProvider.getRegistry(DependencyRegistry.class);

		DependencyInstantiationException ex = assertThrows(DependencyInstantiationException.class,
				() -> DependencyResolver.resolve(di, MultipleInjectConstructors.class),
				"Classes with multiple @Inject constructors should cause DependencyInstantiationException");

		assertTrue(ex.getMessage().toLowerCase().contains("multiple constructors") ||
				   ex.getMessage().contains(MultipleInjectConstructors.class.getSimpleName()),
				"Exception message should indicate multiple annotated constructors and include the class name");
	}

	@Test
	void resolveShouldCreateInstanceWhenNoDependencies() {
		DependencyRegistry di = RegistryProvider.getRegistry(DependencyRegistry.class);

		Object obj = DependencyResolver.resolve(di, NoDeps.class);

		assertNotNull(obj, "Resolver should create instance for class with no-arg @Inject constructor");
		assertTrue(obj instanceof NoDeps, "Returned object must be of the requested type");
	}

	@Test
	void resolveShouldInjectDependenciesFromRegistry() {
		DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);
		DepA a = new DepA("hello");
		Map<Class<?>, Object> map = new HashMap<>();
		map.put(DepA.class, a);
		DependencyRegistry withA = base.registry(map);

		DepB b = (DepB) DependencyResolver.resolve(withA, DepB.class);

		assertNotNull(b, "DepB must be instantiated");
		assertSame(a, b.getA(), "DepA injected into DepB must be the same instance stored in the registry");
	}

	@Test
	void resolveShouldThrowWhenDependencyMissingInRegistry() {
		DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);

		assertThrows(DependencyNotFoundException.class,
				() -> DependencyResolver.resolve(base, DepB.class),
				"Resolving a class with a missing dependency must throw DependencyNotFoundException");
	}

	@Test
	void resolveShouldWrapConstructorExceptionInDependencyInstantiationException() {
		DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);

		DependencyInstantiationException ex = assertThrows(DependencyInstantiationException.class,
				() -> DependencyResolver.resolve(base, ThrowingCtor.class),
				"If constructor throws, resolver must wrap it into DependencyInstantiationException");

		// The original cause (the runtime exception thrown by the constructor) should be preserved as the cause
		assertNotNull(ex.getCause(), "DependencyInstantiationException should contain the original cause");
		assertTrue(ex.getCause() instanceof RuntimeException, "Cause should be the runtime exception thrown by constructor");
		assertEquals("boom", ex.getCause().getMessage(), "The inner exception message should be preserved");
	}

	// -------------------------
	// Test helper classes
	// -------------------------

	// No @Inject constructor
	public static class NoInjectConstructor {
		public NoInjectConstructor() {
			// For testing
		}
	}

	// Two constructors annotated with @Inject (invalid)
	public static class MultipleInjectConstructors {
		@com.nexus.core.annotations.Inject
		public MultipleInjectConstructors() {
			// For testing
		}
		@com.nexus.core.annotations.Inject
		public MultipleInjectConstructors(String s) {
			// For testing
		}
	}

	// Class with public no-arg @Inject
	public static class NoDeps {
		@com.nexus.core.annotations.Inject
		public NoDeps() {
			// For testing
		}
	}

	// Dependency class A
	public static class DepA {
		private final String value;
		public DepA(String value) { this.value = value; }
		public String getValue() { return value; }
	}

	// Class depending on DepA
	public static class DepB {
		private final DepA a;
		@com.nexus.core.annotations.Inject
		public DepB(DepA a) { this.a = a; }
		public DepA getA() { return a; }
	}

	// Class whose constructor throws
	public static class ThrowingCtor {
		@com.nexus.core.annotations.Inject
		public ThrowingCtor() {
			throw new RuntimeException("boom");
		}
	}
}
