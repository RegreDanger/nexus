package unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.DependencyResolver;
import com.nexus.boot.RegistryProvider;
import com.nexus.core.annotations.Inject;
import com.nexus.exceptions.DependencyInstantiationException;
import com.nexus.exceptions.DependencyNotFoundException;

class DependencyResolverTest {

    @Test
    void resolveShouldThrowWhenNoAnnotatedConstructor() {
        DependencyRegistry di = RegistryProvider.getRegistry(DependencyRegistry.class);
        assertThrows(IllegalStateException.class, () -> DependencyResolver.resolve(di, NoInjectConstructor.class),
                "Classes without an @Inject annotated constructor must cause IllegalStateException");
    }

    @Test
    void resolveShouldThrowWhenMultipleAnnotatedConstructors() {
        DependencyRegistry di = RegistryProvider.getRegistry(DependencyRegistry.class);
        assertThrows(IllegalStateException.class, () -> DependencyResolver.resolve(di, MultipleInjectConstructors.class),
                "Classes with multiple @Inject constructors should cause IllegalStateException");
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
        // register dependency
        Map<Class<?>, Object> map = new HashMap<>();
        DepA a = new DepA("hello");
        map.put(DepA.class, a);
        DependencyRegistry withA = base.registry(map);

        // resolve DepB which depends on DepA
        DepB b = (DepB) DependencyResolver.resolve(withA, DepB.class);
        assertNotNull(b, "DepB must be instantiated");
        assertSame(a, b.getA(), "DepA injected into DepB must be the same instance stored in the registry");
    }

    @Test
    void resolveShouldThrowWhenDependencyMissingInRegistry() {
        DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);
        // DepB depends on DepA but registry is empty -> should throw DependencyNotFoundException when di.get is called
        assertThrows(DependencyNotFoundException.class, () -> DependencyResolver.resolve(base, DepB.class),
                "Resolving a class with a missing dependency must bubble up DependencyNotFoundException");
    }

    @Test
    void resolveShouldWrapConstructorExceptionInDependencyInstantiationException() {
        DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);
        assertThrows(DependencyInstantiationException.class, () -> DependencyResolver.resolve(base, ThrowingCtor.class),
                "If constructor throws, resolver must wrap it into DependencyInstantiationException");
        try {
            DependencyResolver.resolve(base, ThrowingCtor.class);
            fail("Expected DependencyInstantiationException");
        } catch (DependencyInstantiationException ex) {
            assertNotNull(ex.getCause(), "DependencyInstantiationException should contain the original cause");
            assertTrue(ex.getCause() instanceof RuntimeException, "Cause should be the runtime exception thrown by constructor");
            assertEquals("boom", ex.getCause().getMessage(), "The inner exception message should be preserved");
        }
    }

    // -------------------------
    // Test helper classes
    // -------------------------

    // No @Inject constructor
    public static class NoInjectConstructor {
        public NoInjectConstructor() {
            //For testing
        }
    }

    // Two constructors annotated with @Inject (invalid)
    public static class MultipleInjectConstructors {
        @Inject
        public MultipleInjectConstructors() {
            //For testing
        }
        @Inject
        public MultipleInjectConstructors(String s) {
            //For testing
        }
    }

    // Class with public no-arg @Inject
    public static class NoDeps {
        @Inject
        public NoDeps() {
            //For testing
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
        @Inject
        public DepB(DepA a) { this.a = a; }
        public DepA getA() { return a; }
    }

    // Class whose constructor throws
    public static class ThrowingCtor {
        @Inject
        public ThrowingCtor() {
            throw new RuntimeException("boom");
        }
    }
}
