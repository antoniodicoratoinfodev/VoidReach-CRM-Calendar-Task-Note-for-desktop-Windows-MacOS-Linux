package com.crm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AtomicPropertiesStoreTest {
    private static final String TYPE = "test.properties";

    @TempDir Path directory;

    @Test void preservesThePreviousCompleteRevisionAsBackup() throws Exception {
        Path file = directory.resolve("data.properties");
        AtomicPropertiesStore.store(file, current("first"), "test");
        AtomicPropertiesStore.store(file, current("second"), "test");

        assertEquals("second", AtomicPropertiesStore.load(file, TYPE, 1, p -> p.containsKey("value")).getProperty("value"));
        assertEquals("first", AtomicPropertiesStore.load(AtomicPropertiesStore.backupPath(file), TYPE, 1, p -> p.containsKey("value")).getProperty("value"));
    }

    @Test void recoversFromTheBackupWhenThePrimaryFileIsCorrupt() throws Exception {
        Path file = directory.resolve("data.properties");
        AtomicPropertiesStore.store(file, current("first"), "test");
        AtomicPropertiesStore.store(file, current("second"), "test");
        Files.writeString(file, "not-a-valid-properties-file-without-schema");

        Properties recovered = AtomicPropertiesStore.load(file, TYPE, 1, p -> p.containsKey("value"));
        assertEquals("first", recovered.getProperty("value"));
        assertTrue(Files.exists(AtomicPropertiesStore.backupPath(file)));
    }

    @Test void acceptsAnExistingV1FileWithoutTheNewTypeMarker() throws Exception {
        Path file = directory.resolve("legacy.properties");
        Properties legacy = new Properties();
        legacy.setProperty(AtomicPropertiesStore.SCHEMA_VERSION_KEY, "1");
        legacy.setProperty("value", "legacy");
        try (var output = Files.newOutputStream(file)) { legacy.store(output, "legacy"); }

        assertEquals("legacy", AtomicPropertiesStore.load(file, TYPE, 1, p -> p.containsKey("value")).getProperty("value"));
    }

    private Properties current(String value) {
        Properties properties = new Properties();
        properties.setProperty(AtomicPropertiesStore.SCHEMA_VERSION_KEY, "1");
        properties.setProperty(AtomicPropertiesStore.FILE_TYPE_KEY, TYPE);
        properties.setProperty("value", value);
        return properties;
    }
}
