package com.nexus.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public final class ClassValidator {
    private static final String CLASSES_MUST_NOT_BE_NULL = "classes must not be null";
    private static final String ARGS_MUST_NOT_BE_NULL = "args must not be null";

    private ClassValidator() {}

    public static void validateArgumentTypes(Object[] args, Class<?>[] classes) {
        Objects.requireNonNull(args, ARGS_MUST_NOT_BE_NULL);
        Objects.requireNonNull(classes, CLASSES_MUST_NOT_BE_NULL);
        if(args.length != classes.length) throw new IllegalArgumentException(String.format("Expected %d arguments: %s (got: %d)", classes.length, Arrays.toString(classes), args.length));
        IntStream.range(0, Math.min(args.length, classes.length))
        .filter(i -> isInvalidType(args[i], classes[i], null))
        .findFirst()
        .ifPresent(invalidIndex -> {
            throw new IllegalArgumentException(String.format(
                "Expected: %s instance at %d but it was %s instance",
                getReadableName(classes[invalidIndex]),
                invalidIndex,
                getReadableName(args[invalidIndex].getClass())
            ));
        });
    }

    public static <T> void validateArgumentTypes(Object[] args, Class<T> expectedElementType) {
        Objects.requireNonNull(args, ARGS_MUST_NOT_BE_NULL);
        Objects.requireNonNull(expectedElementType, "expectedElementType must not be null");
        boolean hasInvalidType = Arrays.stream(args).anyMatch(o -> isInvalidType(o, expectedElementType, null));
        if(hasInvalidType) throw new IllegalArgumentException(String.format("All elements must be instances of %s", getReadableName(expectedElementType)));
    }

    public static <T> void validateArgumentsWithPredicate(Object[] args, Class<T> expectedElementType, Predicate<T> predicate) {
        Objects.requireNonNull(args, ARGS_MUST_NOT_BE_NULL);
        Objects.requireNonNull(expectedElementType, "expectedElementType must not be null");
        Objects.requireNonNull(predicate, "predicate must not be null");
        boolean hasInvalidType = Arrays.stream(args).anyMatch(o -> isInvalidType(o, expectedElementType, predicate));
        if(hasInvalidType) throw new IllegalArgumentException(String.format("All elements must be instances of %s and satisfy the predicate", getReadableName(expectedElementType)));
    }

    public static <T> T cast(Object obj, Class<T> clazz) {
        Objects.requireNonNull(obj, "obj must not be null");
        Objects.requireNonNull(clazz, "clazz must not be null");
        if(!clazz.isInstance(obj)) throw new IllegalArgumentException(String.format("Object is not instance of %s", getReadableName(clazz)));
        return clazz.cast(obj);
    }

    public static <T> T[] castArray(Object[] source, T[] typed) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(typed, "typed must not be null");
        IntStream.range(0, source.length)
        .filter(i -> isInvalidType(source[i], typed.getClass().getComponentType(), null))
        .findFirst()
        .ifPresent(invalidIndex -> {
            throw new IllegalArgumentException(String.format(
                "Expected: %s instance at %d but it was %s instance",
                getReadableName(typed.getClass().getComponentType()),
                invalidIndex,
                getReadableName(source[invalidIndex].getClass())
            ));
        });
        return Arrays.asList(source).toArray(typed);
    }

    public static String getReadableName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    private static <T> boolean isInvalidType(Object obj, Class<T> expectedElementType, Predicate<T> predicate) {
        if (obj == null) return true; // Reject nulls
        if (!expectedElementType.isInstance(obj)) return true; // Wrong type
        if(predicate != null) return !predicate.test(expectedElementType.cast(obj)); // Test predicate
        return false;
    }

}
