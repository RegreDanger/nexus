package unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.nexus.util.ClassValidator;

import java.util.function.Predicate;

class ClassValidatorTest {

    @Test
    void validateArgsWithCorrectTypesShouldPass() {
        Object[] args = { "hello", 123 };
        Class<?>[] types = { String.class, Integer.class };
        assertDoesNotThrow(() -> ClassValidator.validateArgumentTypes(args, types));
    }

    @Test
    void validateArgsWithNullArgsShouldThrow() {
        Class<?>[] types = { String.class };
        assertThrows(NullPointerException.class, () -> ClassValidator.validateArgumentTypes(null, types));
    }

    @Test
    void validateArgsWithNullTypesShouldThrow() {
        Object[] args = { "test" };
        assertThrows(NullPointerException.class, () -> ClassValidator.validateArgumentTypes(args, (Class<Object>[]) null));
    }

    @Test
    void validateArgsWithWrongLengthShouldThrow() {
        Object[] args = { "hello" };
        Class<?>[] types = { String.class, Integer.class };
        assertThrows(IllegalArgumentException.class, () -> ClassValidator.validateArgumentTypes(args, types));
    }

    @Test
    void validateArgsWithWrongTypeShouldThrow() {
        Object[] args = { "hello", "oops" };
        Class<?>[] types = { String.class, Integer.class };
        assertThrows(IllegalArgumentException.class, () -> ClassValidator.validateArgumentTypes(args, types));
    }

    @Test
    void validateArgsSingleTypeWithCorrectInstancesShouldPass() {
        Object[] args = { "a", "b" };
        assertDoesNotThrow(() -> ClassValidator.validateArgumentTypes(args, String.class));
    }

    @Test
    void validateArgsSingleTypeWithWrongInstanceShouldThrow() {
        Object[] args = { "a", 123 };
        assertThrows(IllegalArgumentException.class, () -> ClassValidator.validateArgumentTypes(args, String.class));
    }

    @Test
    void validateArgsWithContentAndValidPredicateShouldPass() {
        Object[] args = { "one", "two" };
        Predicate<String> predicate = s -> s.length() >= 3;
        assertDoesNotThrow(() -> ClassValidator.validateArgumentsWithPredicate(args, String.class, predicate));
    }

    @Test
    void validateArgsWithContentAndInvalidPredicateShouldThrow() {
        Object[] args = { "ok", "no" };
        Predicate<String> predicate = s -> s.length() >= 3;
        assertThrows(IllegalArgumentException.class, () -> ClassValidator.validateArgumentsWithPredicate(args, String.class, predicate));
    }

    @Test
    void validateArgsWithContentAndWrongTypeShouldThrow() {
        Object[] args = { "hello", 123 };
        Predicate<String> predicate = s -> !s.isEmpty();
        assertThrows(IllegalArgumentException.class, () -> ClassValidator.validateArgumentsWithPredicate(args, String.class, predicate));
    }

    @Test
    void castWithCorrectTypeShouldReturnObject() {
        String s = "hello";
        String result = ClassValidator.cast(s, String.class);
        assertEquals(s, result);
    }

    @Test
    void castWithWrongTypeShouldThrow() {
        Integer i = 123;
        assertThrows(IllegalArgumentException.class, () -> ClassValidator.cast(i, String.class));
    }

    @Test
    void castArrayShouldReturnTypedArray() {
        Object[] objs = { "a", "b" };
        String[] typed = new String[objs.length];
        String[] result = ClassValidator.castArray(objs, typed);
        assertArrayEquals(new String[] { "a", "b" }, result);
    }

    @Test
    void castArrayWithNullSourceShouldThrow() {
        assertThrows(NullPointerException.class, () -> ClassValidator.castArray(null, new String[0]));
    }

    @Test
    void castArrayWithNullTargetShouldThrow() {
        Object[] objs = { "x" };
        assertThrows(NullPointerException.class, () -> ClassValidator.castArray(objs, null));
    }
}
