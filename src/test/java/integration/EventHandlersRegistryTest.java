package integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.EventHandlersRegistry;
import com.nexus.boot.InjectableRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;
import com.nexus.core.event.EventHandler;
import com.nexus.core.event.DomainEvent;

import dummy.EventDummy;
import dummy.EventHandlerInterfacesDummy;
import dummy.EventHandlerDummy;
import dummy.InjectableFirstLevelDummy;
import dummy.InjectableBaseLevelDummy;
import dummy.SingletonDummy;
import io.github.classgraph.ScanResult;

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

        ScanResult sr = packagesRegistry.registry("dummy");
        registry = managedRegistry.registry(registry, sr);
        registry = injectableRegistry.registry(registry, sr);
        eventHandlersRegistry.registry(registry, sr);
    }

    @Test
    void getHandlersShouldReturnNonEmptyList() {
        List<EventHandler<EventDummy>> handlers = eventHandlersRegistry.getHandlers(EventDummy.class);
        assertNotNull(handlers, "getHandlers must not return null");
        assertFalse(handlers.isEmpty(), "There should be at least one EventHandler registered for EventDummy");
    }

    @Test
    void mustContainEventHandlerInterfacesDummy() {
        List<EventHandler<EventDummy>> handlers = eventHandlersRegistry.getHandlers(EventDummy.class);
        boolean found = handlers.stream().anyMatch(h -> h.getClass().equals(EventHandlerInterfacesDummy.class));
        assertTrue(found, "EventHandlerInterfacesDummy must be present among handlers for EventDummy");
    }

    @Test
    void handlerInstancesShouldHaveDependenciesInjectedAndBeCallable() {
        List<EventHandler<EventDummy>> handlers = eventHandlersRegistry.getHandlers(EventDummy.class);

        // Find the handler instance that is EventHandlerInterfacesDummy (it implements EventHandler<EventDummy>)
        Optional<EventHandler<EventDummy>> opt = handlers.stream()
            .filter(h -> h.getClass().equals(EventHandlerInterfacesDummy.class))
            .findFirst();

        assertTrue(opt.isPresent(), "EventHandlerInterfacesDummy instance should be registered and present");

        EventHandlerInterfacesDummy handler = (EventHandlerInterfacesDummy) opt.get();

        // Ensure dependencies are injected
        InjectableFirstLevelDummy firstLevel = handler.getFirstlevel();
        assertNotNull(firstLevel, "First level injectable must be injected into EventHandlerInterfacesDummy");

        InjectableBaseLevelDummy baseLevel = firstLevel.getBaseLevelInstanceFromFirstLevelInjectable();
        assertNotNull(baseLevel, "Base level injectable must be injected into InjectableFirstLevelDummy");

        assertNotNull(baseLevel.getSingletonFromBaseLevelInjectable(), "Singleton must be injected into base-level injectable");
        assertSame(SingletonDummy.getInstance(), baseLevel.getSingletonFromBaseLevelInjectable(),
                "Injected singleton should match SingletonDummy.getInstance()");

        // Call the handler to ensure it's invocable (should not throw)
        assertDoesNotThrow(() -> handler.on(new EventDummy()), "Handler.on(...) should be invocable without throwing");
    }

    @Test
    void handlersShouldIncludeEventHandlerDummyAndItsDependenciesShouldBeInjected() {
        // Also check the other dummy handler (class EventHandlerDummy)
        List<EventHandler<EventDummy>> handlers = eventHandlersRegistry.getHandlers(EventDummy.class);
        Optional<EventHandler<EventDummy>> opt = handlers.stream()
            .filter(h -> h.getClass().equals(EventHandlerDummy.class))
            .findFirst();

        assertTrue(opt.isPresent(), "EventHandlerDummy should be registered");

        EventHandlerDummy handler = (EventHandlerDummy) opt.get();
        assertNotNull(handler.getFirstlevel(), "InjectableFirstLevelDummy must be injected into EventHandlerDummy");
        assertDoesNotThrow(() -> handler.on(new EventDummy()), "EventHandlerDummy.on(...) should not throw");
    }

    @Test
    void getHandlersForUnknownEventShouldReturnEmptyList() {
        List<EventHandler<UnknownEvent>> handlers = eventHandlersRegistry.getHandlers(UnknownEvent.class);
        assertNotNull(handlers, "Should return an empty list, not null, for unknown events");
        assertTrue(handlers.isEmpty(), "Handlers list must be empty for events that have no registered handlers");
    }

    // Helper test event used only to assert empty result for unknown events
    public static class UnknownEvent implements DomainEvent {}
}
