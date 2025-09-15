package com.nexus.boot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexus.core.cqrs.Command;
import com.nexus.core.cqrs.Handler;
import com.nexus.core.cqrs.Query;
import com.nexus.exceptions.RegistryResolutionException;
import com.nexus.util.ClassValidator;

import io.github.classgraph.ScanResult;

public final class CqrsHandlersRegistry implements Registry<CqrsHandlersRegistry> {

	private Map<Class<?>, Query<?, ?>> queries = new HashMap<>();
	private Map<Class<?>, Command<?, ?>> commands = new HashMap<>();
	private Map<Class<?>, Handler<?, ?>> handlers = new HashMap<>();

	protected CqrsHandlersRegistry() {}

	@Override
	public CqrsHandlersRegistry registry(Object... args) {
		ClassValidator.validateArgumentTypes(args, new Class<?>[] {DependencyRegistry.class, ScanResult.class});
		DependencyRegistry di = ClassValidator.cast(args[0], DependencyRegistry.class);
		ScanResult sr = ClassValidator.cast(args[1], ScanResult.class);
		initRegistry(di, sr);
		fillHandlers();
		return this;
	}

	private void initRegistry(DependencyRegistry di, ScanResult sr) {
		List<Class<?>> cmdClasses = sr.getClassesImplementing(Command.class).loadClasses();
		List<Class<?>> queryClasses = sr.getClassesImplementing(Query.class).loadClasses();

		cmdClasses.forEach(clazz -> commands.put(clazz, (Command <?, ?>) DependencyResolver.resolve(di, clazz)));
		queryClasses.forEach(clazz -> queries.put(clazz, (Query<?, ?>) DependencyResolver.resolve(di, clazz)));

	}

	private void fillHandlers() {
	if(handlers.isEmpty()) {
		Map<Class<?>, Handler<?, ?>> tmp = HashMap.newHashMap(commands.size() + queries.size());
		tmp.putAll(commands);
		tmp.putAll(queries);
		handlers = Map.copyOf(tmp);
		}
	}

	public <C, R, T extends Handler<C, R>> T getCQRSHandler(Class<T> handlerClass) {
		T cmd = handlerClass.cast(handlers.get(handlerClass));
		if(cmd == null) throw new RegistryResolutionException(String.format(
																"CQRS handler not found: no handler of type %s is registered. " +
																"Ensure the class implements Command<T, R> or Query<T, R>, is annotated with @Inject, " +
																"and is located in a package being scanned by NexusContext.",
																handlerClass.getSimpleName()
															));
		return cmd;
	}

}