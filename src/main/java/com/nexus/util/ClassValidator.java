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
		if(args.length != classes.length) throw new IllegalArgumentException(String.format(
																				"Argument count mismatch: expected %d arguments of types %s, but received %d arguments. " +
																				"Check that you're passing the correct number of parameters.",
																				classes.length, Arrays.toString(classes), args.length
																			));
		IntStream.range(0, Math.min(args.length, classes.length))
		.filter(i -> isInvalidType(args[i], classes[i], null))
		.findFirst()
		.ifPresent(invalidIndex -> {
			throw new IllegalArgumentException(String.format(
												"Type mismatch at argument %d: expected %s but received %s. " +
												"Ensure all arguments match their expected types.",
												invalidIndex,
												classes[invalidIndex].getSimpleName(),
												args[invalidIndex].getClass().getSimpleName()
											));
		});
	}

	public static <T> void validateArgumentTypes(Object[] args, Class<T> expectedElementType) {
		Objects.requireNonNull(args, ARGS_MUST_NOT_BE_NULL);
		Objects.requireNonNull(expectedElementType, "expectedElementType must not be null");
		boolean hasInvalidType = Arrays.stream(args).anyMatch(o -> isInvalidType(o, expectedElementType, null));
		if(hasInvalidType) throw new IllegalArgumentException(String.format(
																"Type validation failed: all arguments must be instances of %s. " +
																"Check that no null values or incompatible types are being passed.",
																expectedElementType.getSimpleName()
															 ));
	}

	public static <T> void validateArgumentsWithPredicate(Object[] args, Class<T> expectedElementType, Predicate<T> predicate) {
		Objects.requireNonNull(args, ARGS_MUST_NOT_BE_NULL);
		Objects.requireNonNull(expectedElementType, "expectedElementType must not be null");
		Objects.requireNonNull(predicate, "predicate must not be null");
		boolean hasInvalidType = Arrays.stream(args).anyMatch(o -> isInvalidType(o, expectedElementType, predicate));
		if(hasInvalidType) throw new IllegalArgumentException(String.format(
																"Validation failed: all arguments must be instances of %s and satisfy the provided predicate. " +
																"One or more arguments failed the custom validation logic.",
																expectedElementType.getSimpleName()
															));
	}

	public static <T> T cast(Object obj, Class<T> clazz) {
		Objects.requireNonNull(obj, "obj must not be null");
		Objects.requireNonNull(clazz, "clazz must not be null");
		if(!clazz.isInstance(obj)) throw new IllegalArgumentException(String.format(
																		"Cannot cast object to %s: the provided object is of type %s. " +
																		"Verify that the object is of the expected type before casting.",
																		clazz.getSimpleName(),
																		obj.getClass().getSimpleName()
																	));
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
												"Array casting failed at index %d: expected %s but found %s. " +
												"Ensure all array elements are of the target type.",
												invalidIndex,
												typed.getClass().getComponentType().getSimpleName(),
												source[invalidIndex].getClass().getSimpleName()
											));
		});
		return Arrays.asList(source).toArray(typed);
	}

	private static <T> boolean isInvalidType(Object obj, Class<T> expectedElementType, Predicate<T> predicate) {
		if (obj == null) return true; // Reject nulls
		if (!expectedElementType.isInstance(obj)) return true; // Wrong type
		if(predicate != null) return !predicate.test(expectedElementType.cast(obj)); // Test predicate
		return false;
	}

}
