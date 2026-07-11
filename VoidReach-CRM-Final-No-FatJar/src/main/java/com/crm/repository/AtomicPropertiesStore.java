package com.crm.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.function.Predicate;

/** Durable storage for small Properties files, with a recoverable previous revision. */
public final class AtomicPropertiesStore {
    public static final String SCHEMA_VERSION_KEY = "schema.version";
    public static final String FILE_TYPE_KEY = "file.type";

    private AtomicPropertiesStore() { }

    public static void store(Path target, Properties properties, String comment) throws IOException {
        Path directory = target.getParent();
        if (directory == null) throw new IOException("Percorso dati non valido: " + target);
        Files.createDirectories(directory);

        Path newFile = Files.createTempFile(directory, target.getFileName().toString() + ".", ".tmp");
        try {
            writeAndSync(newFile, properties, comment);
            if (Files.exists(target)) replaceBackup(target);
            moveReplacing(newFile, target);
        } finally {
            Files.deleteIfExists(newFile);
        }
    }

    /** Returns the primary file, or its last complete backup if the primary cannot be read. */
    public static Properties load(Path target, String expectedType, int currentSchema, Predicate<Properties> legacyFormat) throws IOException {
        if (!Files.exists(target)) return new Properties();
        try {
            return readAndValidate(target, expectedType, currentSchema, legacyFormat);
        } catch (IOException | IllegalArgumentException primaryFailure) {
            Path backup = backupPath(target);
            if (!Files.exists(backup)) {
                throw new IOException("Il file dati non è leggibile e non esiste una copia di backup: " + target, primaryFailure);
            }
            try {
                return readAndValidate(backup, expectedType, currentSchema, legacyFormat);
            } catch (IOException | IllegalArgumentException backupFailure) {
                primaryFailure.addSuppressed(backupFailure);
                throw new IOException("Il file dati e la sua copia di backup non sono leggibili: " + target, primaryFailure);
            }
        }
    }

    public static Path backupPath(Path target) { return target.resolveSibling(target.getFileName() + ".bak"); }

    private static Properties readAndValidate(Path path, String expectedType, int currentSchema, Predicate<Properties> legacyFormat) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) { properties.load(input); }
        if (properties.isEmpty()) throw new IOException("Il file dati è vuoto: " + path);

        String version = properties.getProperty(SCHEMA_VERSION_KEY);
        if (version == null) {
            if (!legacyFormat.test(properties)) throw new IOException("Formato dati non riconosciuto: " + path);
            return properties; // Schema precedente: verrà aggiornato al prossimo salvataggio.
        }

        int schema;
        try { schema = Integer.parseInt(version); }
        catch (NumberFormatException e) { throw new IOException("Versione schema non valida: " + version, e); }
        if (schema < 1 || schema > currentSchema) throw new IOException("Versione schema non supportata: " + schema);
        String fileType = properties.getProperty(FILE_TYPE_KEY);
        if (fileType == null && legacyFormat.test(properties)) return properties; // Early v1 files did not include a type marker.
        if (!expectedType.equals(fileType)) throw new IOException("Tipo di file dati non valido: " + path);
        return properties;
    }

    private static void replaceBackup(Path target) throws IOException {
        Path backup = backupPath(target);
        Path newBackup = Files.createTempFile(target.getParent(), target.getFileName().toString() + ".backup.", ".tmp");
        try {
            Files.copy(target, newBackup, StandardCopyOption.REPLACE_EXISTING);
            force(newBackup);
            moveReplacing(newBackup, backup);
        } finally {
            Files.deleteIfExists(newBackup);
        }
    }

    private static void writeAndSync(Path path, Properties properties, String comment) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            OutputStream output = Channels.newOutputStream(channel);
            properties.store(output, comment);
            output.flush();
            channel.force(true);
        }
    }

    private static void force(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) { channel.force(true); }
    }

    private static void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupported) {
            // The temporary file is still complete; this only affects uncommon filesystems without atomic rename support.
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
