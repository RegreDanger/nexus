package com.nexus.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public final class ClassValidator {
    private static final String CLASSES_MUST_NOT_BE_NULL = "classes must not be null";
    private static final String ARGS_MUST_NOT_BE_NULL = "args must not be null";

    private ClassValidator() {}

    public static void validateArgs(Object[] args, Class<?>[] classes) {
        Objects.requireNonNull(args, ARGS_MUST_NOT_BE_NULL);
        Objects.requireNonNull(classes, CLASSES_MUST_NOT_BE_NULL);
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
        Objects.requireNonNull(args, ARGS_MUST_NOT_BE_NULL);
        Objects.requireNonNull(expectedElementType, "expectedElementType must not be null");
        boolean hasInvalidType = Arrays.stream(args).anyMatch(o -> (o != null && !expectedElementType.isInstance(o)));
        if(hasInvalidType) {
            throw new IllegalArgumentException(String.format("All elements must be instances of %s", getName(expectedElementType)));
        }
    }

    public static <T> void validateArgsWithContent(Object[] args, Class<T> expectedElementType, Predicate<T> predicate) {
        Objects.requireNonNull(args, ARGS_MUST_NOT_BE_NULL);
        Objects.requireNonNull(expectedElementType, "expectedElementType must not be null");
        boolean hasInvalidType = Arrays.stream(args).anyMatch(o ->  {
            boolean isInvalidType = (o != null && !expectedElementType.isInstance(o));
            if(isInvalidType) {
                return isInvalidType; //Tests first the type so doesn't throw a ClassCastException when it runs the predicate
            }
            return predicate.negate().test(expectedElementType.cast(o));
        });
        if(hasInvalidType) {
            throw new IllegalArgumentException(String.format("All elements must be instances of %s and satisfy the predicate", getName(expectedElementType)));
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
