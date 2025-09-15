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
		// Arrange: obtain real registries and run wiring over the dummy package
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
		InjectableBaseLevelDummy baseFromRegistry = registry.get(InjectableBaseLevelDummy.class);
		InjectableFirstLevelDummy firstLevel = registry.get(InjectableFirstLevelDummy.class);

		assertNotNull(firstLevel, "First level injectable must be available");
		assertNotNull(baseFromRegistry, "Base level injectable must be available");

		// The first-level injectable should have the base-level injected
		InjectableBaseLevelDummy baseFromFirstLevel = firstLevel.getBaseLevelInstanceFromFirstLevelInjectable();
		assertNotNull(baseFromFirstLevel, "Base-level instance must be injected into the first-level injectable");

		// The instance injected into the first-level must be the same object that the registry stores for the base class
		assertSame(baseFromRegistry, baseFromFirstLevel,
				"InjectableFirstLevelDummy should receive the same instance of InjectableBaseLevelDummy that the registry stores");

		// Singleton sanity: base-level injectable should have the singleton injected and it must match SingletonDummy.getInstance()
		assertNotNull(baseFromRegistry.getSingletonFromBaseLevelInjectable(), "Singleton must be injected into base-level injectable");
		assertSame(SingletonDummy.getInstance(), baseFromRegistry.getSingletonFromBaseLevelInjectable(),
				"Injected singleton should be the same as SingletonDummy.getInstance()");
	}
}
