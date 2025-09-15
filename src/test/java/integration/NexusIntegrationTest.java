package integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.nexus.api.NexusContext;
import com.nexus.boot.CqrsHandlersRegistry;
import com.nexus.boot.EventHandlersRegistry;
import com.nexus.boot.RegistryProvider;
import com.nexus.core.cqrs.CqrsBus;
import com.nexus.core.event.EventBus;
import com.nexus.core.event.EventHandler;

import dummy.CommandDummy;
import dummy.EventDummy;
import dummy.QueryDummy;

class NexusIntegrationTest {

	/**
	 * Full integration test:
	 * - Builds a real NexusContext scanning the "dummy" package used by the other tests.
	 * - Verifies both buses are created.
	 * - Sends a command and a query through the CQRS bus and checks returned values.
	 * - Checks that CQRS handler instance was registered and dependencies were injected.
	 * - Verifies event handlers are registered and publishes an event (should not throw).
	 */
	@Test
	void fullIntegrationFlow_buildsContext_executesCqrsAndPublishesEvents() {
		//Build a real context scanning the test "dummy" package (same used by other integration tests)
		NexusContext ctx = new NexusContext.NexusContextBuilder()
				.packagesToScan("dummy")
				.build();

		//--- CQRS: via bus ---
		CqrsBus cqrsBus = ctx.getCqrsBus();
		assertNotNull(cqrsBus, "CqrsBus should be available from NexusContext");

		//Send command (CommandDummy.handle returns a string in dummy classes)
		String cmdResult = cqrsBus.send(CommandDummy.class, "payload");
		assertEquals("Hello from CommandDummy! input:payload", cmdResult, "Command execution via bus should return expected result");

		//Send query (QueryDummy.handle returns a string in dummy classes)
		String queryResult = cqrsBus.send(QueryDummy.class, "q");
		assertEquals("Hello from QueryDummy! input:q", queryResult, "Query execution via bus should return expected result");

		//Verify the handler instance was registered and DI chain is present
		CqrsHandlersRegistry cqrsRegistry = RegistryProvider.getRegistry(CqrsHandlersRegistry.class);
		CommandDummy commandHandler = cqrsRegistry.getCQRSHandler(CommandDummy.class);
		assertNotNull(commandHandler, "CommandDummy handler must be registered in CqrsHandlersRegistry");
		assertNotNull(commandHandler.getFirstLevel(), "First level injectable must be injected into CommandDummy");
		assertNotNull(commandHandler.getFirstLevel().getBaseLevelInstanceFromFirstLevelInjectable(),
				"Base level injectable must be injected into the first-level injectable");

		//--- Events: via bus ---
		EventBus eventBus = ctx.getEventBus();
		assertNotNull(eventBus, "EventBus should be available from NexusContext");

		//Confirm that the event handlers registry contains handlers for EventDummy
		EventHandlersRegistry evRegistry = RegistryProvider.getRegistry(EventHandlersRegistry.class);
		List<EventHandler<EventDummy>> handlers = evRegistry.getHandlers(EventDummy.class);
		assertNotNull(handlers, "getHandlers must not return null");
		assertFalse(handlers.isEmpty(), "There should be at least one EventHandler registered for EventDummy");

		//Publishing should not throw (handlers may perform their own validation)
		assertDoesNotThrow(() -> eventBus.publish(EventDummy.class, new EventDummy()), "Publishing EventDummy should not throw");

		//Extra sanity: make sure we can still use cqrs bus after event publish
		String cmdResult2 = cqrsBus.send(CommandDummy.class, "another");
		assertEquals("Hello from CommandDummy! input:another", cmdResult2, "Cqrs bus should continue working after event publish");
	}
}
