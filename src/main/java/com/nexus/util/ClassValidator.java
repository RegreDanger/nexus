package com.nexus.util;

import java.util.Arrays;
import java.util.Objects;

public final class ClassValidator {

    private ClassValidator() {}

    public static void validateArgs(Object[] args, Class<?>[] classes) {
        Objects.requireNonNull(args, "args must not be null");
        Objects.requireNonNull(classes, "classes must not be null");
        if(args.length != classes.length) {
            throw new IllegalArgumentException(String.format("Expected %d arguments: %s (got: %d)", classes.length, Arrays.toString(classes), args.length));
        }
        for(int i = 0; i < args.length; i++) {
            if(args[i] != null && !classes[i].isInstance(args[i])) {
                throw new IllegalArgumentException(String.format("Expected: %s instance at %d but it was %s instance", getName(classes[i]), i, getName(args[i].getClass())));
            }
        }
    }

    public static <T> void validateArgs(Object[] args, Class<T> expectedElementType) {
        Objects.requireNonNull(args, "args must not be null");
        Objects.requireNonNull(expectedElementType, "expectedElementType must not be null");
        boolean hasInvalidType = Arrays.stream(args).anyMatch(o -> (o != null && !expectedElementType.isInstance(o)));
        if(hasInvalidType) {
            throw new IllegalArgumentException(String.format("All elements must be instances of %s", getName(expectedElementType)));
        }
    }

    public static <T> T cast(Object obj, Class<T> cls) {
        Objects.requireNonNull(cls, "cls must not be null");
        if(!cls.isInstance(obj)) {
            throw new IllegalArgumentException(String.format("Object is not instance of %s", getName(cls)));
        }
        return cls.cast(obj);
    }

    public static <T> T[] castArray(Object[] source, T[] typed) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(typed, "typed must not be null");
        return Arrays.asList(source).toArray(typed);
    }

    public static String getName(Class<?> cls) {
        return cls.getName() + ".class";
    }
}
