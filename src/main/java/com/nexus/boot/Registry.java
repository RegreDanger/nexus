package com.nexus.boot;

public interface Registry<T> {
    public T registry(Object... args);
}

