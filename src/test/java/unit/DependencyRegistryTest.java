package unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.RegistryProvider;
import com.nexus.exceptions.DependencyNotFoundException;

class DependencyRegistryTest {

    @Test
    void shouldRegisterAndGetInstance() {
        DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);

        SimpleBean sb = new SimpleBean("x");
        Map<Class<?>, Object> m = new HashMap<>();
        m.put(SimpleBean.class, sb);

        DependencyRegistry reg = base.registry(m);

        SimpleBean got = reg.get(SimpleBean.class);
        assertSame(sb, got, "Registry must return the exact same instance that was registered");
    }

    @Test
    void shouldCombineMapsWhenRegisteringMultipleTimes() {
        DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);

        SimpleBean a = new SimpleBean("a");
        Map<Class<?>, Object> m1 = new HashMap<>();
        m1.put(SimpleBean.class, a);
        DependencyRegistry reg1 = base.registry(m1);

        AnotherBean b = new AnotherBean(42);
        Map<Class<?>, Object> m2 = new HashMap<>();
        m2.put(AnotherBean.class, b);
        DependencyRegistry reg2 = reg1.registry(m2);

        // both should be available from the new registry
        assertSame(a, reg2.get(SimpleBean.class), "Previously registered bean should remain available after combining");
        assertSame(b, reg2.get(AnotherBean.class), "Newly registered bean should be available after combining");
    }

    @Test
    void getShouldThrowWhenDependencyNotFound() {
        DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);
        assertThrows(DependencyNotFoundException.class, () -> base.get(SimpleBean.class),
                "Requesting an unregistered dependency must throw DependencyNotFoundException");
    }

    @Test
    void registeringWithNonClassKeyShouldThrow() {
        DependencyRegistry base = RegistryProvider.getRegistry(DependencyRegistry.class);
        Map<Object, Object> bad = new HashMap<>();
        bad.put("notAClassKey", new SimpleBean("x"));

        // Because ClassValidator will allow Map but the registry implementation will check keys and throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> base.registry(bad),
                "Registering a map with non-Class keys must throw IllegalArgumentException");
    }

    // --- Helper dummy beans used by the tests ---
    public static class SimpleBean {
        private final String name;
        public SimpleBean(String name) { this.name = name; }
        public String getName() { return name; }
    }

    public static class AnotherBean {
        private final int value;
        public AnotherBean(int value) { this.value = value; }
        public int getValue() { return value; }
    }
}
