package unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.nexus.api.NexusContext;
import com.nexus.boot.CqrsHandlersRegistry;
import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.EventHandlersRegistry;
import com.nexus.boot.InjectableRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;
import com.nexus.core.bus.BusesProvider;
import com.nexus.core.bus.NexusCqrsBus;
import com.nexus.core.bus.NexusEventBus;
import com.nexus.core.cqrs.CqrsBus;
import com.nexus.core.event.EventBus;

import io.github.classgraph.ScanResult;

class NexusContextTest {

    private MockedStatic<RegistryProvider> registryProviderStatic;
    private MockedStatic<BusesProvider> busesProviderStatic;

    @AfterEach
    void teardown() {
        if (registryProviderStatic != null) registryProviderStatic.close();
        if (busesProviderStatic != null) busesProviderStatic.close();
    }

    @Test
    void buildWithDefaultsCreatesBothBuses() {
        // mocks for registries
        PackagesRegistry pkgReg = mock(PackagesRegistry.class);
        DependencyRegistry di = mock(DependencyRegistry.class);
        ManagedRegistry managed = mock(ManagedRegistry.class);
        InjectableRegistry inject = mock(InjectableRegistry.class);
        CqrsHandlersRegistry cqrsHandlers = mock(CqrsHandlersRegistry.class);
        EventHandlersRegistry eventHandlers = mock(EventHandlersRegistry.class);
        ScanResult sr = mock(ScanResult.class);

        // concrete bus types that BusesProvider actually returns
        NexusCqrsBus expectedCqrsBus = mock(NexusCqrsBus.class);
        NexusEventBus expectedEventBus = mock(NexusEventBus.class);

        // static providers - mock BEFORE building context
        registryProviderStatic = Mockito.mockStatic(RegistryProvider.class);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(PackagesRegistry.class)).thenReturn(pkgReg);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(DependencyRegistry.class)).thenReturn(di);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(ManagedRegistry.class)).thenReturn(managed);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(InjectableRegistry.class)).thenReturn(inject);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(CqrsHandlersRegistry.class)).thenReturn(cqrsHandlers);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(EventHandlersRegistry.class)).thenReturn(eventHandlers);

        // behaviour for registry methods
        when(pkgReg.registry(any())).thenReturn(sr);
        when(managed.registry(di, sr)).thenReturn(di);
        when(inject.registry(di, sr)).thenReturn(di);

        // cqrsHandlers.registry and eventHandlers.registry return themselves
        when(cqrsHandlers.registry(di, sr)).thenReturn(cqrsHandlers);
        when(eventHandlers.registry(di, sr)).thenReturn(eventHandlers);

        // BusesProvider static mock BEFORE build
        busesProviderStatic = Mockito.mockStatic(BusesProvider.class);
        busesProviderStatic.when(() -> BusesProvider.getNexusCqrsBus(cqrsHandlers)).thenReturn(expectedCqrsBus);
        busesProviderStatic.when(() -> BusesProvider.getNexusEventBus(eventHandlers)).thenReturn(expectedEventBus);

        // build context
        NexusContext ctx = new NexusContext.NexusContextBuilder()
                .packagesToScan("com.example")
                .build();

        // asserts
        Assertions.assertSame(expectedCqrsBus, ctx.getCqrsBus(), "CqrsBus should be the one provided by BusesProvider");
        Assertions.assertSame(expectedEventBus, ctx.getEventBus(), "EventBus should be the one provided by BusesProvider");

        // verify interactions
        registryProviderStatic.verify(() -> RegistryProvider.getRegistry(PackagesRegistry.class));
        registryProviderStatic.verify(() -> RegistryProvider.getRegistry(CqrsHandlersRegistry.class));
        registryProviderStatic.verify(() -> RegistryProvider.getRegistry(EventHandlersRegistry.class));
        busesProviderStatic.verify(() -> BusesProvider.getNexusCqrsBus(cqrsHandlers), times(1));
        busesProviderStatic.verify(() -> BusesProvider.getNexusEventBus(eventHandlers), times(1));
    }

    @Test
    void buildOnlyCqrsNoEventBusGetEventThrows() {
        // mocks
        PackagesRegistry pkgReg = mock(PackagesRegistry.class);
        DependencyRegistry di = mock(DependencyRegistry.class);
        ManagedRegistry managed = mock(ManagedRegistry.class);
        InjectableRegistry inject = mock(InjectableRegistry.class);
        CqrsHandlersRegistry cqrsHandlers = mock(CqrsHandlersRegistry.class);
        ScanResult sr = mock(ScanResult.class);

        NexusCqrsBus expectedCqrsBus = mock(NexusCqrsBus.class);

        // static - register necessary registries
        registryProviderStatic = Mockito.mockStatic(RegistryProvider.class);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(PackagesRegistry.class)).thenReturn(pkgReg);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(DependencyRegistry.class)).thenReturn(di);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(ManagedRegistry.class)).thenReturn(managed);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(InjectableRegistry.class)).thenReturn(inject);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(CqrsHandlersRegistry.class)).thenReturn(cqrsHandlers);
        // Note: we DO NOT stub EventHandlersRegistry here on purpose

        // behaviour
        when(pkgReg.registry(any())).thenReturn(sr);
        when(managed.registry(di, sr)).thenReturn(di);
        when(inject.registry(di, sr)).thenReturn(di);
        when(cqrsHandlers.registry(di, sr)).thenReturn(cqrsHandlers);

        // BusesProvider static mock BEFORE build
        busesProviderStatic = Mockito.mockStatic(BusesProvider.class);
        busesProviderStatic.when(() -> BusesProvider.getNexusCqrsBus(cqrsHandlers)).thenReturn(expectedCqrsBus);
        // do NOT stub getNexusEventBus - we expect it NOT to be called

        // build context with onlyCqrs
        NexusContext ctx = new NexusContext.NexusContextBuilder()
                .packagesToScan("com.example")
                .onlyCqrs()
                .build();

        // cqrs exists, event should throw
        Assertions.assertSame(expectedCqrsBus, ctx.getCqrsBus());
        Assertions.assertThrows(UnsupportedOperationException.class, ctx::getEventBus, "getEventBus should throw when onlyCqrs() used");

        // verify that event registry / bus provider were never called
        registryProviderStatic.verify(() -> RegistryProvider.getRegistry(EventHandlersRegistry.class), never());
        busesProviderStatic.verify(() -> BusesProvider.getNexusCqrsBus(cqrsHandlers), times(1));
        busesProviderStatic.verify(() -> BusesProvider.getNexusEventBus(any()), never());
    }

    @Test
    void buildWithCustomBusesUsesCustomInstancesAndRespectsFlags() {
        // mocks for registries (shouldn't be used for custom buses)
        PackagesRegistry pkgReg = mock(PackagesRegistry.class);
        DependencyRegistry di = mock(DependencyRegistry.class);
        ManagedRegistry managed = mock(ManagedRegistry.class);
        InjectableRegistry inject = mock(InjectableRegistry.class);
        ScanResult sr = mock(ScanResult.class);

        // custom buses provided by user (can be interfaces or concrete impls)
        CqrsBus customCqrs = mock(CqrsBus.class);
        EventBus customEvent = mock(EventBus.class);

        // registry provider static - still needs to supply basic registries used for scanning
        registryProviderStatic = Mockito.mockStatic(RegistryProvider.class);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(PackagesRegistry.class)).thenReturn(pkgReg);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(DependencyRegistry.class)).thenReturn(di);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(ManagedRegistry.class)).thenReturn(managed);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(InjectableRegistry.class)).thenReturn(inject);

        when(pkgReg.registry(any())).thenReturn(sr);
        when(managed.registry(di, sr)).thenReturn(di);
        when(inject.registry(di, sr)).thenReturn(di);

        // BusesProvider static mock BEFORE build - we expect NO interactions because user provides custom buses
        busesProviderStatic = Mockito.mockStatic(BusesProvider.class);

        // Build context with custom buses
        NexusContext ctx = new NexusContext.NexusContextBuilder()
                .packagesToScan("com.example")
                .withCqrsBus(customCqrs)
                .withEventBus(customEvent)
                .build();

        // should use exactly custom instances
        Assertions.assertSame(customCqrs, ctx.getCqrsBus());
        Assertions.assertSame(customEvent, ctx.getEventBus());

        // BusesProvider shouldn't be used when custom buses are provided
        busesProviderStatic.verifyNoInteractions();
    }

    @Test
    void buildPassCustomCqrsButOnlyEventBusGetCqrsThrowsGetEventWorks() {
        // This tests the "absurd" case: user supplies a custom CQRS bus but then calls onlyEventBus().
        PackagesRegistry pkgReg = mock(PackagesRegistry.class);
        DependencyRegistry di = mock(DependencyRegistry.class);
        ManagedRegistry managed = mock(ManagedRegistry.class);
        InjectableRegistry inject = mock(InjectableRegistry.class);
        EventHandlersRegistry eventHandlers = mock(EventHandlersRegistry.class);
        ScanResult sr = mock(ScanResult.class);

        NexusEventBus expectedEventBus = mock(NexusEventBus.class);
        CqrsBus providedCqrs = mock(CqrsBus.class);

        // static
        registryProviderStatic = Mockito.mockStatic(RegistryProvider.class);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(PackagesRegistry.class)).thenReturn(pkgReg);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(DependencyRegistry.class)).thenReturn(di);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(ManagedRegistry.class)).thenReturn(managed);
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(InjectableRegistry.class)).thenReturn(inject);
        // EventHandlersRegistry will be requested because onlyEventBus() is used
        registryProviderStatic.when(() -> RegistryProvider.getRegistry(EventHandlersRegistry.class)).thenReturn(eventHandlers);

        when(pkgReg.registry(any())).thenReturn(sr);
        when(managed.registry(di, sr)).thenReturn(di);
        when(inject.registry(di, sr)).thenReturn(di);

        when(eventHandlers.registry(di, sr)).thenReturn(eventHandlers);

        // BusesProvider static mock BEFORE build
        busesProviderStatic = Mockito.mockStatic(BusesProvider.class);
        busesProviderStatic.when(() -> BusesProvider.getNexusEventBus(eventHandlers)).thenReturn(expectedEventBus);

        // build with customCqrs but onlyEventBus() -> hasCqrsBus = false
        NexusContext ctx = new NexusContext.NexusContextBuilder()
                .packagesToScan("com.example")
                .withCqrsBus(providedCqrs) // provided but should be ignored since onlyEventBus() is set
                .onlyEventBus()
                .build();

        // getCqrsBus must throw because hasCqrsBus == false
        Assertions.assertThrows(UnsupportedOperationException.class, ctx::getCqrsBus, "getCqrsBus should throw because onlyEventBus() disabled cqrs");
        // event bus should be the one created by provider
        Assertions.assertSame(expectedEventBus, ctx.getEventBus());

        // verify that cqrs provider was never called and event provider was called once
        busesProviderStatic.verify(() -> BusesProvider.getNexusCqrsBus(any()), never());
        busesProviderStatic.verify(() -> BusesProvider.getNexusEventBus(eventHandlers), times(1));
    }
}
