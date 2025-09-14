package integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.CqrsHandlersRegistry;
import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.InjectableRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;

import dummy.CommandDummy;
import dummy.QueryDummy;

class CqrsHandlersRegistryTest {
    private PackagesRegistry packagesRegistry;
    private DependencyRegistry registry;
    private ManagedRegistry managedRegistry;
    private InjectableRegistry injectableRegistry;
    private CqrsHandlersRegistry cqrsHandlersRegistry;

    @BeforeEach
    void setUp() {
        packagesRegistry = RegistryProvider.getRegistry(PackagesRegistry.class);
        registry = RegistryProvider.getRegistry(DependencyRegistry.class);
        managedRegistry = RegistryProvider.getRegistry(ManagedRegistry.class);
        injectableRegistry = RegistryProvider.getRegistry(InjectableRegistry.class);
        cqrsHandlersRegistry = RegistryProvider.getRegistry(CqrsHandlersRegistry.class);
        packagesRegistry.registry("dummy");
        registry = managedRegistry.registry(registry, packagesRegistry);
        registry = injectableRegistry.registry(registry, packagesRegistry);
        cqrsHandlersRegistry.registry(registry, packagesRegistry);
        
    }

    @Test
    void hasCommandHandler() {
        assertNotNull(cqrsHandlersRegistry.getCQRSHandler(CommandDummy.class), "The bean should not be null.");
    } 

    @Test
    void hasQueryHandler() {
        assertNotNull(cqrsHandlersRegistry.getCQRSHandler(QueryDummy.class), "The bean should be not null.");
    }   
}       
