package com.nexus.boot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexus.core.cqrs.Command;
import com.nexus.core.cqrs.Handler;
import com.nexus.core.cqrs.Query;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public final class CqrsHandlersRegistry implements Registry<Void> {

    private Map<Class<?>, Query<?, ?>> queries = new HashMap<>();
    private Map<Class<?>, Command<?, ?>> commands = new HashMap<>();
    private Map<Class<?>, Handler<?, ?>> handlers = new HashMap<>();

    protected CqrsHandlersRegistry() {}

    @Override
    public Void registry(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument: DependencyRegistry instance");
        }
        if(!(args[0] instanceof DependencyRegistry di)) {
            throw new IllegalArgumentException("The first arg must be instace of " + DependencyRegistry.class.getName() + "class");
        }
        initRegistry(di, new ClassGraph().enableClassInfo().scan());
        fillHandlers();
        return null;
    }

    private void initRegistry(DependencyRegistry di, ScanResult sr) {
        List<Class<?>> cmdClasses = sr.getClassesImplementing(Command.class).loadClasses();
        List<Class<?>> queryClasses = sr.getClassesImplementing(Query.class).loadClasses();

        cmdClasses.forEach(cls -> commands.put(cls, (Command <?, ?>) DependencyResolver.resolve(di, cls)));
        queryClasses.forEach(cls -> queries.put(cls, (Query<?, ?>) DependencyResolver.resolve(di, cls)));

    }

    private void fillHandlers() {
        if(handlers.isEmpty()) {
            Map<Class<?>, Handler<?, ?>> tmp = new HashMap<>();
            tmp.putAll(commands);
            tmp.putAll(queries);
            handlers = Map.copyOf(tmp);
        }
    }

    public <C, R, T extends Handler<C, R>> T getCQRSHandler(Class<T> handlerClass) {
        T cmd = handlerClass.cast(handlers.get(handlerClass));
        if(cmd == null) {
            throw new NullPointerException("Handler not found for class: " + handlerClass.getName());
        }
        return cmd;
    }

}