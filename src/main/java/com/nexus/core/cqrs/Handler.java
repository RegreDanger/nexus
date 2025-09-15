package com.nexus.core.cqrs;

public interface Handler<I, O> {
	O handle(I input);
}
