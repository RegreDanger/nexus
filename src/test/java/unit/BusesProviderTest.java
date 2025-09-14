package unit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.CqrsHandlersRegistry;
import com.nexus.boot.EventHandlersRegistry;
import com.nexus.core.bus.BusesProvider;
import com.nexus.core.bus.NexusCqrsBus;
import com.nexus.core.bus.NexusEventBus;

class BusesProviderTest {

    @BeforeEach
    void resetStatics() throws Exception {
        // Reset private static fields cqrsBus and eventBus to null between tests
        Field cqrsField = BusesProvider.class.getDeclaredField("cqrsBus");
        cqrsField.setAccessible(true);
        cqrsField.set(null, null);

        Field eventField = BusesProvider.class.getDeclaredField("eventBus");
        eventField.setAccessible(true);
        eventField.set(null, null);
    }

    @Test
    void getNexusCqrsBus_returnsSingletonInstance() {
        CqrsHandlersRegistry reg1 = mock(CqrsHandlersRegistry.class);
        CqrsHandlersRegistry reg2 = mock(CqrsHandlersRegistry.class);

        NexusCqrsBus first = BusesProvider.getNexusCqrsBus(reg1);
        assertNotNull(first, "First call should return a non-null NexusCqrsBus");
        assertTrue(first instanceof NexusCqrsBus, "Returned instance must be NexusCqrsBus");

        NexusCqrsBus second = BusesProvider.getNexusCqrsBus(reg2);
        assertSame(first, second, "Subsequent calls must return the same singleton instance even if registry differs");
    }

    @Test
    void getNexusEventBusReturnsSingletonInstance() {
        EventHandlersRegistry reg1 = mock(EventHandlersRegistry.class);
        EventHandlersRegistry reg2 = mock(EventHandlersRegistry.class);

        NexusEventBus first = BusesProvider.getNexusEventBus(reg1);
        assertNotNull(first, "First call should return a non-null NexusEventBus");
        assertTrue(first instanceof NexusEventBus, "Returned instance must be NexusEventBus");

        NexusEventBus second = BusesProvider.getNexusEventBus(reg2);
        assertSame(first, second, "Subsequent calls must return the same singleton instance even if registry differs");
    }

    @Test
    void cqrsAndEventBusesAreIndependentSingletons() {
        CqrsHandlersRegistry cqrsReg = mock(CqrsHandlersRegistry.class);
        EventHandlersRegistry eventReg = mock(EventHandlersRegistry.class);

        NexusCqrsBus cqrs = BusesProvider.getNexusCqrsBus(cqrsReg);
        NexusEventBus event = BusesProvider.getNexusEventBus(eventReg);

        assertNotNull(cqrs, "CQRS bus should not be null");
        assertNotNull(event, "Event bus should not be null");
        assertNotSame(cqrs, event, "CQRS and Event buses must be independent instances");
        assertTrue(cqrs instanceof NexusCqrsBus);
        assertTrue(event instanceof NexusEventBus);
    }
}
