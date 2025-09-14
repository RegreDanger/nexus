package integration;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;

import dummy.SingletonDummy;
import io.github.classgraph.ScanResult;

class WiringConfigRegistryTest {
    private PackagesRegistry packagesRegistry;
    private DependencyRegistry registry;
    private ManagedRegistry managedRegistry;
    private ScanResult scanResult;

    @BeforeEach
    void setUp() {
        packagesRegistry = RegistryProvider.getRegistry(PackagesRegistry.class);
        registry = RegistryProvider.getRegistry(DependencyRegistry.class);
        managedRegistry = RegistryProvider.getRegistry(ManagedRegistry.class);
        scanResult = packagesRegistry.registry("dummy");
        registry = managedRegistry.registry(registry, scanResult);
    }

    @Test
    void hasManagedBean() {
        Object bean = registry.get(SingletonDummy.class);
        assertNotNull(bean, "The bean should not be null.");
        assertInstanceOf(SingletonDummy.class, bean, "The bean should be an instance of SingletonDummy.");
        // Verify it's the exact Singleton instance expected
        assertSame(SingletonDummy.getInstance(), bean,
            "Registered managed bean should be the same instance returned by SingletonDummy.getInstance().");
    }

    @Test
    void reRegisteringManagedShouldKeepValidSingletonInstance() {
        // Run managed registry again (simulate repeated registration)
        DependencyRegistry after = managedRegistry.registry(registry, scanResult);

        Object beanAfter = after.get(SingletonDummy.class);
        assertNotNull(beanAfter, "Bean should still be present after re-registration.");
        assertSame(SingletonDummy.getInstance(), beanAfter,
            "After re-registering, the managed bean must still match SingletonDummy.getInstance().");
    }
}
