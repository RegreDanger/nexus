package com.nexus.core.cqrs;

public interface Query <T, R> extends Handler<T, R> {
	@Override
	R handle(T input);
}
