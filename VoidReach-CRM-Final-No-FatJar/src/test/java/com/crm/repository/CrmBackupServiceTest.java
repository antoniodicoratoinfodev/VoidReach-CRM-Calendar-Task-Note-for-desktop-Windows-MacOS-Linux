package com.crm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.crm.model.Contact;
import com.crm.model.CrmDataSnapshot;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CrmBackupServiceTest {
    @TempDir Path directory;

    @Test void keepsThreeCompatiblePasswordFreeBackupsPerUser() throws Exception {
        Path dataDirectory = directory.resolve("data");
        Path backupRoot = directory.resolve("backup");
        LocalCrmDataRepository repository = new LocalCrmDataRepository(dataDirectory);
        CrmBackupService backups = new CrmBackupService(repository, backupRoot, Duration.ofMinutes(2));

        for (int version = 1; version <= 4; version++) {
            repository.saveForUser("account-a", snapshot("Contact " + version));
            backups.createBackupNow("account-a");
        }
        repository.saveForUser("account-b", snapshot("Other account"));
        backups.createBackupNow("account-b");

        Path accountA = backupRoot.resolve("account-a");
        Path accountB = backupRoot.resolve("account-b");
        List<Path> accountABackups;
        try (var files = Files.list(accountA)) {
            accountABackups = files.filter(Files::isRegularFile).sorted().toList();
        }
        assertEquals(3, accountABackups.size());
        try (var files = Files.list(accountB)) {
            assertEquals(1, files.filter(Files::isRegularFile).count());
        }

        Path newest = accountABackups.getLast();
        Properties rawBackup = load(newest);
        assertEquals("voidreach.crm-data", rawBackup.getProperty(AtomicPropertiesStore.FILE_TYPE_KEY));
        assertFalse(rawBackup.stringPropertyNames().stream().anyMatch(key -> key.toLowerCase().contains("password")));

        Path restoredDirectory = directory.resolve("restored-data");
        Files.createDirectories(restoredDirectory);
        Files.copy(newest, restoredDirectory.resolve("account-a.properties"));
        CrmDataSnapshot restored = new LocalCrmDataRepository(restoredDirectory).loadForUser("account-a");
        assertEquals("Contact 4", restored.contacts().getFirst().nameProperty().get());
        assertTrue(Files.isDirectory(backupRoot));
        assertEquals(Duration.ofMinutes(2), CrmBackupService.DEFAULT_INTERVAL);
    }

    private static CrmDataSnapshot snapshot(String contactName) {
        Contact contact = new Contact("contact-id", contactName, "VoidReach", "", "", "", "", "", "");
        return new CrmDataSnapshot(List.of(contact), new HashMap<>(), LocalDate.of(2026, 7, 11), "Day", 1.0);
    }

    private static Properties load(Path file) throws Exception {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) { properties.load(input); }
        return properties;
    }
}
