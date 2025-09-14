package integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.InjectableRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;

import dummy.InjectableBaseLevelDummy;
import dummy.InjectableFirstLevelDummy;

class InjectableRegistryTest {

    private PackagesRegistry packagesRegistry;
    private DependencyRegistry registry;
    private ManagedRegistry managedRegistry;
    private InjectableRegistry injectableRegistry;

    @BeforeEach
    void setUp() {
        packagesRegistry = RegistryProvider.getRegistry(PackagesRegistry.class);
        registry = RegistryProvider.getRegistry(DependencyRegistry.class);
        managedRegistry = RegistryProvider.getRegistry(ManagedRegistry.class);
        injectableRegistry = RegistryProvider.getRegistry(InjectableRegistry.class);
        packagesRegistry.registry("dummy");
        registry = managedRegistry.registry(registry, packagesRegistry);
        registry = injectableRegistry.registry(registry, packagesRegistry);
        
    }

    @Test
    void hasInjectableBaseBean() {
        assertNotNull(registry.get(InjectableBaseLevelDummy.class), "The bean should not be null.");
    }

    @Test
    void hasInjectableFirstLevelBean() {
        assertNotNull(registry.get(InjectableFirstLevelDummy.class), "The bean should not be null.");
    }

}
