package unit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import com.nexus.boot.Registry;
import com.nexus.boot.RegistryProvider;

class RegistryProviderTest {

    @Test
    void getRegistryShouldReturnInstance() {
        SimpleRegistry instance = RegistryProvider.getRegistry(SimpleRegistry.class);
        assertNotNull(instance, "RegistryProvider should return a non-null instance");
        assertTrue(instance instanceof SimpleRegistry, "Returned instance should be of the requested type");
    }

    @Test
    void getRegistryShouldReturnSameInstanceOnSubsequentCalls() {
        SimpleRegistry first = RegistryProvider.getRegistry(SimpleRegistry.class);
        SimpleRegistry second = RegistryProvider.getRegistry(SimpleRegistry.class);
        assertSame(first, second, "RegistryProvider must return the same (cached) instance for the same registry class");
    }

    @Test
    void getRegistryShouldReturnDifferentInstancesForDifferentRegistryClasses() {
        SimpleRegistry r1 = RegistryProvider.getRegistry(SimpleRegistry.class);
        AnotherRegistry r2 = RegistryProvider.getRegistry(AnotherRegistry.class);
        assertNotSame(r1, r2, "Different registry classes must produce different instances");
    }

    @Test
    void getRegistryShouldWrapConstructorExceptionInIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> RegistryProvider.getRegistry(BadConstructorRegistry.class),
                "When the registry constructor throws, RegistryProvider should throw IllegalStateException");
        assertNotNull(ex.getCause(), "Thrown IllegalStateException should contain a cause");
        assertTrue(ex.getCause() instanceof InvocationTargetException,
                "Cause should be an InvocationTargetException when constructor throws");
    }

    @Test
    void getRegistryShouldWrapNoNoArgConstructorInIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> RegistryProvider.getRegistry(NoNoArgConstructorRegistry.class),
                "When no no-arg constructor exists, RegistryProvider should throw IllegalStateException");
        assertNotNull(ex.getCause(), "Thrown IllegalStateException should contain a cause");
        assertTrue(ex.getCause() instanceof NoSuchMethodException,
                "Cause should be a NoSuchMethodException when there's no no-arg constructor");
    }

    /*
     * Helper test registry classes used only for testing.
     *
     * NOTE: These classes are declared in the same package (com.nexus.boot) so that
     * reflective construction via getDeclaredConstructor().newInstance() works without
     * accessibility surprises.
     */

    // A simple, well-behaved registry with a public no-arg constructor
    public static class SimpleRegistry implements Registry<Object> {
        public SimpleRegistry() {
            //for testing
        }

        @Override
        public Object registry(Object... args) {
            return null;
        }
    }

    // Another different registry class so we can assert different instances per class
    public static class AnotherRegistry implements Registry<Object> {
        public AnotherRegistry() {
            //for testing
        }

        @Override
        public Object registry(Object... args) {
            return null;
        }
    }

    // Registry whose constructor throws -> should lead to InvocationTargetException wrapped by IllegalStateException
    public static class BadConstructorRegistry implements Registry<Object> {
        public BadConstructorRegistry() {
            throw new RuntimeException("constructor boom");
        }

        @Override
        public Object registry(Object... args) {
            return null;
        }
    }

    // Registry without a no-arg constructor -> should lead to NoSuchMethodException wrapped by IllegalStateException
    public static class NoNoArgConstructorRegistry implements Registry<Object> {
        public NoNoArgConstructorRegistry(String param) {}

        @Override
        public Object registry(Object... args) {
            return null;
        }
    }
}
