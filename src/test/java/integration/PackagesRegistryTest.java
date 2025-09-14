package integration;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexus.boot.PackagesRegistry;
import com.nexus.boot.RegistryProvider;

class PackagesRegistryTest {
    private PackagesRegistry registryPkgs;

    @BeforeEach
    void setUp() {
        registryPkgs = RegistryProvider.getRegistry(PackagesRegistry.class);
    }

    @Test
    void shouldThrowNullException() {
        assertThrows(NullPointerException.class, () -> registryPkgs.registry((Object[]) null));
    }

    @Test
    void question() {
        assertThrows(IllegalArgumentException.class, () -> registryPkgs.registry(""));
    }

}
