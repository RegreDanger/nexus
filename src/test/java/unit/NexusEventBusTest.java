package unit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.nexus.core.bus.NexusEventBus;

import dummy.EventDummy;

class NexusEventBusTest {
    @Test
    void registerAndConsumesEventByClass() {

        NexusEventBus eventBus = new NexusEventBus();
        @SuppressWarnings("unchecked")
        Consumer<EventDummy> listener = mock(Consumer.class);
        EventDummy event = new EventDummy();

        eventBus.register(EventDummy.class, listener);
        eventBus.publish(EventDummy.class, event);

        verify(listener).accept(event);
    }

    @Test
    void registerAndConsumesEventByObject() {
        NexusEventBus eventBus = new NexusEventBus();
        @SuppressWarnings("unchecked")
        Consumer<EventDummy> listener = mock(Consumer.class);
        EventDummy event = new EventDummy();

        eventBus.register(EventDummy.class, listener);
        eventBus.publish(event);

        verify(listener).accept(event);

    }

    @Test
    void registerMultipleListenersInSameTypeAndConsumesByClass() {
        NexusEventBus eventBus = new NexusEventBus();
        @SuppressWarnings("unchecked")
        Consumer<EventDummy> listener1 = mock(Consumer.class);
        Consumer<EventDummy> listener2 = mock(Consumer.class);
        Consumer<EventDummy> listener3 = mock(Consumer.class);
        Consumer<EventDummy> listener4 = mock(Consumer.class);
        EventDummy event = new EventDummy();
        
        eventBus.register(EventDummy.class, listener1);
        eventBus.register(EventDummy.class, listener2);
        eventBus.register(EventDummy.class, listener3);
        eventBus.register(EventDummy.class, listener4);
        eventBus.publish(EventDummy.class, event);

    }
}
