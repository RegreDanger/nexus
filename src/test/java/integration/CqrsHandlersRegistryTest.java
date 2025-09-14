package integration;

import static org.junit.jupiter.api.Assertions.*;

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
import dummy.SingletonDummy;
import io.github.classgraph.ScanResult;
import com.nexus.core.cqrs.Handler;

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

        ScanResult sr = packagesRegistry.registry("dummy");
        registry = managedRegistry.registry(registry, sr);
        registry = injectableRegistry.registry(registry, sr);
        cqrsHandlersRegistry.registry(registry, sr);
    }

    @Test
    void getCommandHandlerShouldExistAndBeCallable() {
        CommandDummy handler = cqrsHandlersRegistry.getCQRSHandler(CommandDummy.class);
        assertNotNull(handler, "Command handler returned by registry should not be null.");

        // verify handle works
        String out = handler.handle("payload");
        assertEquals("Hello from CommandDummy! input:payload", out, "Command handler should return expected message.");

        // verify dependency injection chain: CommandDummy -> InjectableFirstLevelDummy -> InjectableBaseLevelDummy -> SingletonDummy
        assertNotNull(handler.getFirstLevel(), "First level injectable must be injected into CommandDummy.");
        assertNotNull(handler.getFirstLevel().getBaseLevelInstanceFromFirstLevelInjectable(),
                "Base level injectable must be injected into InjectableFirstLevelDummy.");
        assertNotNull(handler.getFirstLevel().getBaseLevelInstanceFromFirstLevelInjectable().getSingletonFromBaseLevelInjectable(),
                "Singleton must be injected into InjectableBaseLevelDummy.");

        // the singleton must be the actual SingletonDummy.getInstance()
        assertSame(SingletonDummy.getInstance(),
                handler.getFirstLevel().getBaseLevelInstanceFromFirstLevelInjectable().getSingletonFromBaseLevelInjectable(),
                "Injected singleton should be the same instance returned by SingletonDummy.getInstance().");
    }

    @Test
    void getQueryHandlerShouldExistAndBeCallable() {
        QueryDummy handler = cqrsHandlersRegistry.getCQRSHandler(QueryDummy.class);
        assertNotNull(handler, "Query handler returned by registry should not be null.");

        // verify handle works
        String out = handler.handle("q");
        assertEquals("Hello from QueryDummy! input:q", out, "Query handler should return expected message.");

        // QueryDummy depends on InjectableBaseLevelDummy directly
        assertNotNull(handler.getBaseLevel(), "InjectableBaseLevelDummy must be injected into QueryDummy.");
        assertNotNull(handler.getBaseLevel().getSingletonFromBaseLevelInjectable(), "Singleton must be injected into base-level injectable.");
        assertSame(SingletonDummy.getInstance(), handler.getBaseLevel().getSingletonFromBaseLevelInjectable(),
                "Injected singleton should be the same instance returned by SingletonDummy.getInstance().");
    }

    @Test
    void getCQRSHandlerWhenNotRegisteredShouldThrow() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> cqrsHandlersRegistry.getCQRSHandler(DummyHandler.class),
                "Requesting a handler that is not registered should throw NullPointerException.");
        assertTrue(ex.getMessage().contains(DummyHandler.class.getName()),
                "Exception message should include the missing handler class name.");
    }

    /**
     * Dummy handler class used only for the 'not registered' negative test.
     * It implements Handler but is NOT in the scanned 'dummy' package, so it won't be registered.
     */
    public static class DummyHandler implements Handler<String, String> {
        @Override
        public String handle(String input) {
            return "i shouldn't be called";
        }
    }
}
