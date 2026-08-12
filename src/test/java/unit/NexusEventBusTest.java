package unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.nexus.core.bus.NexusEventBus;
import com.nexus.core.event.DomainEvent;

import dummy.EventDummy;

class NexusEventBusTest {
    @Test
    @SuppressWarnings("unchecked")
    void publishByClass_withRegisteredListener_invokesListener() {
        NexusEventBus eventBus = new NexusEventBus();
        Consumer<EventDummy> listener = mock(Consumer.class);
        EventDummy event = new EventDummy();

        eventBus.register(EventDummy.class, listener);
        eventBus.publish(EventDummy.class, event);

        verify(listener).accept(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishByObject_withRegisteredListener_invokesListener() {
        NexusEventBus eventBus = new NexusEventBus();
        Consumer<EventDummy> listener = mock(Consumer.class);
        EventDummy event = new EventDummy();

        eventBus.register(EventDummy.class, listener);
        eventBus.publish(event);

        verify(listener).accept(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishByClass_withMultipleListeners_invokesInRegistrationOrder() {
        NexusEventBus eventBus = new NexusEventBus();
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

        InOrder inOrder = inOrder(listener1, listener2, listener3, listener4);

        inOrder.verify(listener1).accept(event);
        inOrder.verify(listener2).accept(event);
        inOrder.verify(listener3).accept(event);
        inOrder.verify(listener4).accept(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishByObject_withMultipleListeners_invokesInRegistrationOrder() {
        NexusEventBus eventBus = new NexusEventBus();
        Consumer<EventDummy> listener1 = mock(Consumer.class);
        Consumer<EventDummy> listener2 = mock(Consumer.class);
        Consumer<EventDummy> listener3 = mock(Consumer.class);
        Consumer<EventDummy> listener4 = mock(Consumer.class);
        EventDummy event = new EventDummy();

        eventBus.register(EventDummy.class, listener1);
        eventBus.register(EventDummy.class, listener2);
        eventBus.register(EventDummy.class, listener3);
        eventBus.register(EventDummy.class, listener4);
        eventBus.publish(event);

        InOrder inOrder = inOrder(listener1, listener2, listener3, listener4);

        inOrder.verify(listener1).accept(event);
        inOrder.verify(listener2).accept(event);
        inOrder.verify(listener3).accept(event);
        inOrder.verify(listener4).accept(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishByClass_withDifferentEventType_doesNotInvokeListener() {
        NexusEventBus eventBus = new NexusEventBus();
        Consumer<EventDummy> listener = mock(Consumer.class);
        class OtherEventDummy implements DomainEvent {}

        eventBus.register(EventDummy.class, listener);
        eventBus.publish(OtherEventDummy.class, new OtherEventDummy());

        verify(listener, never()).accept(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishByObject_withDifferentEventType_doesNotInvokeListener() {
        NexusEventBus eventBus = new NexusEventBus();
        Consumer<EventDummy> listener = mock(Consumer.class);
        class OtherEventDummy implements DomainEvent {}

        eventBus.register(EventDummy.class, listener);
        eventBus.publish(new OtherEventDummy());

        verify(listener, never()).accept(any());
    }

    @Test
    void publishByClass_withNoRegisteredListeners_doesNotThrow() {
        NexusEventBus eventBus = new NexusEventBus();
        EventDummy event = new EventDummy();
        assertDoesNotThrow(() -> eventBus.publish(EventDummy.class, event));
    }

    @Test
    void publishByObject_withNoRegisteredListeners_doesNotThrow() {
        NexusEventBus eventBus = new NexusEventBus();
        EventDummy event = new EventDummy();
        assertDoesNotThrow(() -> eventBus.publish(event));
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishByClass_whenListenerThrows_propagatesAndSkipsRemainingListeners() {
        NexusEventBus eventBus = new NexusEventBus();
        Consumer<EventDummy> listener1 = mock(Consumer.class);
        Consumer<EventDummy> listener2 = mock(Consumer.class);
        EventDummy event = new EventDummy();
        doThrow(new RuntimeException("Should Crash")).when(listener1).accept(event);
        InOrder inOrder = inOrder(listener1, listener2);

        eventBus.register(EventDummy.class, listener1);
        eventBus.register(EventDummy.class, listener2);

        assertThrows(RuntimeException.class, () -> eventBus.publish(EventDummy.class, event));
        inOrder.verify(listener1).accept(event);
        inOrder.verify(listener2, never()).accept(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishByObject_whenListenerThrows_propagatesAndSkipsRemainingListeners() {
        NexusEventBus eventBus = new NexusEventBus();
        Consumer<EventDummy> listener1 = mock(Consumer.class);
        Consumer<EventDummy> listener2 = mock(Consumer.class);
        EventDummy event = new EventDummy();
        doThrow(new RuntimeException("Should Crash")).when(listener1).accept(event);
        InOrder inOrder = inOrder(listener1, listener2);

        eventBus.register(EventDummy.class, listener1);
        eventBus.register(EventDummy.class, listener2);

        assertThrows(RuntimeException.class, () -> eventBus.publish(event));
        inOrder.verify(listener1).accept(event);
        inOrder.verify(listener2, never()).accept(any());
    }

    @Test
    void publishByClass_withNullEvent_throwsNullPointerException() {
        NexusEventBus eventBus = new NexusEventBus();
        assertThrows(NullPointerException.class, () -> eventBus.publish(EventDummy.class, null));
    }

    @Test
    void publishByClass_withNullEventType_throwsNullPointerException() {
        NexusEventBus eventBus = new NexusEventBus();
        EventDummy event = new EventDummy();
        assertThrows(NullPointerException.class, () -> eventBus.publish(null, event));
    }

    @Test
    void publishByClass_withNullEventAndEventType_throwsNullPointerException() {
        NexusEventBus eventBus = new NexusEventBus();
        assertThrows(NullPointerException.class, () -> eventBus.publish(null, null));
    }

    @Test
    void publishByObject_withNullEvent_throwsNullPointerException() {
        NexusEventBus eventBus = new NexusEventBus();
        assertThrows(NullPointerException.class, () -> eventBus.publish(null));
    }
}
