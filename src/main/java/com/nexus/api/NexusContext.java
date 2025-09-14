package com.nexus.api;

import com.nexus.boot.CqrsHandlersRegistry;
import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.EventHandlersRegistry;
import com.nexus.boot.InjectableRegistry;
import com.nexus.boot.ManagedRegistry;
import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;
import com.nexus.core.bus.BusesProvider;
import com.nexus.core.cqrs.CqrsBus;
import com.nexus.core.event.EventBus;

import io.github.classgraph.ScanResult;

public class NexusContext {
    private String[] pkgs;
    
    private PackagesRegistry pkgRegistry;
    private DependencyRegistry di;
    private ManagedRegistry managedRegistry;
    private InjectableRegistry injectRegistry;
    private CqrsHandlersRegistry cqrsHandlersRegistry;
    private EventHandlersRegistry eventHandlersRegistry;

    private CqrsBus cqrsBus;
    private EventBus eventBus;

    private boolean hasCqrsBus;
    private boolean hasEventBus;

    private ScanResult sr;

    private NexusContext(NexusContextBuilder builder) {
        this.pkgs = builder.getPkgs();
        this.cqrsBus = builder.getCqrsBus();
        this.eventBus = builder.getEventBus();
        this.hasCqrsBus = builder.hasCqrsBus();
        this.hasEventBus = builder.hasEventBus();
        setMainRegistry();
        setBusesRegistry();
        scanMainClasses();
        buildCqrsBus();
        buildEventBus();
    }

    private void setMainRegistry() {
        this.pkgRegistry = RegistryProvider.getRegistry(PackagesRegistry.class);
        this.di = RegistryProvider.getRegistry(DependencyRegistry.class);
        this.managedRegistry = RegistryProvider.getRegistry(ManagedRegistry.class);
        this.injectRegistry = RegistryProvider.getRegistry(InjectableRegistry.class);
    }

    private void setBusesRegistry() {
        if (this.hasCqrsBus) this.cqrsHandlersRegistry = RegistryProvider.getRegistry(CqrsHandlersRegistry.class);
        if (this.hasEventBus) this.eventHandlersRegistry = RegistryProvider.getRegistry(EventHandlersRegistry.class);
    }


    private void scanMainClasses() {
        this.sr = pkgRegistry.registry((Object[]) this.pkgs);
        this.di = managedRegistry.registry(this.di, this.sr);
        this.di = injectRegistry.registry(this.di, this.sr);
    }

    private void buildCqrsBus() {
        if(this.cqrsBus == null && this.hasCqrsBus) {
            this.cqrsHandlersRegistry = cqrsHandlersRegistry.registry(this.di, this.sr);
            this.cqrsBus = BusesProvider.getNexusCqrsBus(this.cqrsHandlersRegistry);
        }
    }

    private void buildEventBus() {
        if(this.eventBus == null && this.hasEventBus) {
            this.eventHandlersRegistry = eventHandlersRegistry.registry(this.di, this.sr);
            this.eventBus = BusesProvider.getNexusEventBus(this.eventHandlersRegistry);
        }
    }

    public CqrsBus getCqrsBus() {
        if(!this.hasCqrsBus) {
            throw new UnsupportedOperationException("CQRS bus not enabled. Did you mean to call onlyEventBus() in the builder?");
        }
        return this.cqrsBus;
    }

    public EventBus getEventBus() {
        if(!this.hasEventBus) {
            throw new UnsupportedOperationException("Event bus not enabled in this context. Did you mean to call onlyCqrsBus() in the builder?");
        }
        return this.eventBus;
    }

    public static class NexusContextBuilder {
        private String[] pkgs;
        private CqrsBus cqrsBus;
        private EventBus eventBus;
        private boolean hasCqrsBus = true;
        private boolean hasEventBus = true;

        public NexusContextBuilder packagesToScan(String... pkgs) {
            this.pkgs = pkgs;
            return this;
        }

        public NexusContextBuilder withCqrsBus(CqrsBus cqrsBus) { 
            this.cqrsBus = cqrsBus;
            return this;
        }

        public NexusContextBuilder withEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public NexusContextBuilder onlyCqrs() {
            this.hasCqrsBus = true;
            this.hasEventBus = false;
            return this;
        }

        public NexusContextBuilder onlyEventBus() {
            this.hasCqrsBus = false;
            this.hasEventBus = true;
            return this;
        }

        private String[] getPkgs() {
            return this.pkgs;
        }

        private CqrsBus getCqrsBus() {
            return this.cqrsBus;
        }

        private EventBus getEventBus() {
            return this.eventBus;
        }

        private boolean hasCqrsBus() {
            return this.hasCqrsBus;
        }

        private boolean hasEventBus() {
            return this.hasEventBus;
        }

        public NexusContext build() {
            return new NexusContext(this);
        }

    }

}
