package integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;

import io.github.classgraph.ScanResult;

class PackagesRegistryTest {
    private PackagesRegistry registryPkgs;

    @BeforeEach
    void setUp() {
        registryPkgs = RegistryProvider.getRegistry(PackagesRegistry.class);
    }

    @Test
    void shouldThrowNullException() {
        assertThrows(NullPointerException.class, () -> registryPkgs.registry((Object[]) null),
                "Calling registry with null varargs should throw NullPointerException");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> registryPkgs.registry(""),
                "Calling registry with an empty package string should throw IllegalArgumentException");
    }

    @Test
    void shouldScanDummyPackageSuccessfully() {
        ScanResult sr = registryPkgs.registry("dummy");
        assertNotNull(sr, "ScanResult should not be null for a valid package");
        assertTrue(sr.getAllClasses().size() > 0, "The dummy package should contain at least one scanned class");
    }

    @Test
    void shouldAcceptPackageArrayVarargs() {
        String[] pkgs = new String[] { "dummy" };
        // cast to Object[] so varargs expands the String[] elements as separate arguments
        ScanResult sr = registryPkgs.registry((Object[]) pkgs);
        assertNotNull(sr, "ScanResult should not be null when passing packages as an array");
        assertTrue(sr.getAllClasses().size() > 0, "Scanning via array varargs should find classes in the dummy package");
    }

    @Test
    void scanningUnknownPackageShouldReturnEmptyScanResult() {
        ScanResult sr = registryPkgs.registry("no.such.package.exists");
        assertNotNull(sr, "ScanResult should never be null even for unknown packages");
        assertEquals(0, sr.getAllClasses().size(), "Unknown package should yield an empty scan result");
    }
}
