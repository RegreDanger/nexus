package integration;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;

import dummy.SingletonDummy;

class WiringConfigRegistryTest {
    private PackagesRegistry packagesRegistry;
    private DependencyRegistry registry;
    private ManagedRegistry managedRegistry;

    @BeforeEach
    void setUp() {
        packagesRegistry = RegistryProvider.getRegistry(PackagesRegistry.class);
        registry = RegistryProvider.getRegistry(DependencyRegistry.class);
        managedRegistry = RegistryProvider.getRegistry(ManagedRegistry.class);
    }

    @Test
    void hasManagedBean() {
        packagesRegistry.registry("dummy");
        registry = managedRegistry.registry(registry, packagesRegistry);
        assertNotNull(registry.get(SingletonDummy.class), "The bean should not be null.");
        assertInstanceOf(SingletonDummy.class, registry.get(SingletonDummy.class), "The bean should be an instance of WiringConfigDummy.");
    }

}
