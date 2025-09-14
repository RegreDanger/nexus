package integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.InjectableRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;

import dummy.InjectableBaseLevelDummy;
import dummy.InjectableFirstLevelDummy;
import dummy.SingletonDummy;
import io.github.classgraph.ScanResult;

class InjectableRegistryTest {

    private PackagesRegistry packagesRegistry;
    private DependencyRegistry registry;
    private ManagedRegistry managedRegistry;
    private InjectableRegistry injectableRegistry;
    private ScanResult scanResult;

    @BeforeEach
    void setUp() {
        packagesRegistry = RegistryProvider.getRegistry(PackagesRegistry.class);
        registry = RegistryProvider.getRegistry(DependencyRegistry.class);
        managedRegistry = RegistryProvider.getRegistry(ManagedRegistry.class);
        injectableRegistry = RegistryProvider.getRegistry(InjectableRegistry.class);

        scanResult = packagesRegistry.registry("dummy");
        registry = managedRegistry.registry(registry, scanResult);
        registry = injectableRegistry.registry(registry, scanResult);
    }

    @Test
    void shouldRegisterBaseAndFirstLevelBeans() {
        InjectableBaseLevelDummy base = registry.get(InjectableBaseLevelDummy.class);
        InjectableFirstLevelDummy firstLevel = registry.get(InjectableFirstLevelDummy.class);

        assertNotNull(base, "InjectableBaseLevelDummy should be registered and retrievable from the registry");
        assertNotNull(firstLevel, "InjectableFirstLevelDummy should be registered and retrievable from the registry");
    }

    @Test
    void shouldInjectDependenciesAcrossLevels() {
        // get instances from registry
        InjectableBaseLevelDummy baseFromRegistry = registry.get(InjectableBaseLevelDummy.class);
        InjectableFirstLevelDummy firstLevel = registry.get(InjectableFirstLevelDummy.class);

        assertNotNull(firstLevel, "First level injectable must be available");
        // the first level holds a reference to base-level injectable
        InjectableBaseLevelDummy baseFromFirstLevel = firstLevel.getBaseLevelInstanceFromFirstLevelInjectable();
        assertNotNull(baseFromFirstLevel, "Base-level instance must be injected into the first-level injectable");

        // The injected base inside firstLevel must be the same instance that the registry returns for the base class
        assertSame(baseFromRegistry, baseFromFirstLevel,
                "InjectableFirstLevelDummy should receive the same instance of InjectableBaseLevelDummy that the registry stores");

        // singleton sanity check: base-level injectable should have the singleton injected
        assertNotNull(baseFromRegistry.getSingletonFromBaseLevelInjectable(), "Singleton must be injected into base-level injectable");
        assertSame(SingletonDummy.getInstance(), baseFromRegistry.getSingletonFromBaseLevelInjectable(),
                "Injected singleton should be the same as SingletonDummy.getInstance()");
    }

}
