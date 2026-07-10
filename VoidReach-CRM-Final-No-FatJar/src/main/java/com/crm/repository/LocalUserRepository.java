package com.crm.repository;

import com.crm.model.UserAccount;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

/** Local stand-in for a users table. It deliberately persists columns, not UI state. */
public class LocalUserRepository implements UserRepository {
    private final Path file = applicationDataDirectory().resolve("users.properties");

    public static Path applicationDataDirectory() { return Path.of(System.getProperty("user.home"), ".voidreach-crm"); }

    @Override public synchronized Optional<UserAccount> findByEmail(String email) {
        Properties p = load();
        String normalized = email.trim().toLowerCase();
        for (String key : p.stringPropertyNames()) {
            if (key.endsWith(".email") && normalized.equals(decode(p.getProperty(key)))) {
                String id = key.substring("user.".length(), key.length() - ".email".length());
                return Optional.of(read(p, id));
            }
        }
        return Optional.empty();
    }

    @Override public synchronized void save(UserAccount user) {
        Properties p = load(); String prefix = "user." + user.getId() + ".";
        put(p, prefix + "fullName", user.getFullName()); put(p, prefix + "email", user.getEmail());
        put(p, prefix + "passwordHash", user.getPasswordHash()); put(p, prefix + "passwordSalt", user.getPasswordSalt());
        put(p, prefix + "createdAt", user.getCreatedAt().toString()); put(p, prefix + "resetCodeHash", user.getResetCodeHash());
        put(p, prefix + "resetCodeExpiresAt", user.getResetCodeExpiresAt() == null ? null : user.getResetCodeExpiresAt().toString());
        put(p, prefix + "avatarFileName", user.getAvatarFileName());
        try { Files.createDirectories(file.getParent()); try (OutputStream out = Files.newOutputStream(file)) { p.store(out, "VoidReach CRM local users"); } }
        catch (IOException e) { throw new IllegalStateException("Impossibile salvare gli account locali", e); }
    }

    private Properties load() { Properties p = new Properties(); if (Files.exists(file)) try (InputStream in = Files.newInputStream(file)) { p.load(in); } catch (IOException e) { throw new IllegalStateException("Impossibile leggere gli account locali", e); } return p; }
    private UserAccount read(Properties p, String id) { String x = "user." + id + "."; UserAccount u = new UserAccount(); u.setId(id); u.setFullName(decode(p.getProperty(x + "fullName"))); u.setEmail(decode(p.getProperty(x + "email"))); u.setPasswordHash(decode(p.getProperty(x + "passwordHash"))); u.setPasswordSalt(decode(p.getProperty(x + "passwordSalt"))); u.setCreatedAt(Instant.parse(decode(p.getProperty(x + "createdAt")))); u.setResetCodeHash(decode(p.getProperty(x + "resetCodeHash"))); String expires = decode(p.getProperty(x + "resetCodeExpiresAt")); if (expires != null && !expires.isBlank()) u.setResetCodeExpiresAt(Instant.parse(expires)); u.setAvatarFileName(decode(p.getProperty(x + "avatarFileName"))); return u; }
    private void put(Properties p, String key, String value) { p.setProperty(key, Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
    private String decode(String value) { return value == null ? null : new String(Base64.getDecoder().decode(value), java.nio.charset.StandardCharsets.UTF_8); }
}
