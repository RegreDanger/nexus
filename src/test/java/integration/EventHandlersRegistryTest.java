package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.EventHandlersRegistry;
import com.nexus.boot.InjectableRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;
import com.nexus.core.event.EventHandler;

import dummy.EventDummy;
import dummy.EventHandlerInterfacesDummy;

class EventHandlersRegistryTest {
    private PackagesRegistry packagesRegistry;
    private DependencyRegistry registry;
    private ManagedRegistry managedRegistry;
    private InjectableRegistry injectableRegistry;
    private EventHandlersRegistry eventHandlersRegistry;

    @BeforeEach
    void setUp() {
        packagesRegistry = RegistryProvider.getRegistry(PackagesRegistry.class);
        registry = RegistryProvider.getRegistry(DependencyRegistry.class);
        managedRegistry = RegistryProvider.getRegistry(ManagedRegistry.class);
        injectableRegistry = RegistryProvider.getRegistry(InjectableRegistry.class);
        eventHandlersRegistry = RegistryProvider.getRegistry(EventHandlersRegistry.class);
        packagesRegistry.registry("dummy");
        registry = managedRegistry.registry(registry, packagesRegistry);
        registry = injectableRegistry.registry(registry, packagesRegistry);
        eventHandlersRegistry.registry(registry, packagesRegistry);
        
    }

    @Test
    void hasEventHandler() {
        assertNotNull(eventHandlersRegistry.getHandlers(EventDummy.class), "The bean should not be null.");
    }

    @Test
    void hasEventHandlerSpecific() {
        List<EventHandler<EventDummy>> handlers = eventHandlersRegistry.getHandlers(EventDummy.class);
        assertEquals(true, handlers.stream().anyMatch(h -> h.getClass().equals(EventHandlerInterfacesDummy.class)));
    }

}
