package com.crm.repository;

import com.crm.model.Contact;
import com.crm.model.CrmDataSnapshot;
import com.crm.model.Task;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** One file per user: local equivalent of owner_user_id filtering in SQL. */
public class LocalCrmDataRepository implements CrmDataRepository {
    private final Path dataDirectory = LocalUserRepository.applicationDataDirectory().resolve("data");

    @Override public synchronized CrmDataSnapshot loadForUser(String userId) {
        Properties p = load(userId); List<Contact> contacts = new ArrayList<>(); Map<LocalDate, List<Task>> tasks = new HashMap<>();
        int contactsCount = integer(p, "contacts.count");
        for (int i = 0; i < contactsCount; i++) { String key = "contact." + i + "."; contacts.add(new Contact(value(p, key + "id"), value(p, key + "name"), value(p, key + "company"), value(p, key + "title"), value(p, key + "email"), value(p, key + "phone"), value(p, key + "lastInteraction"), value(p, key + "tags"), value(p, key + "description"))); }
        int tasksCount = integer(p, "tasks.count");
        for (int i = 0; i < tasksCount; i++) { String key = "task." + i + "."; LocalDate date = LocalDate.parse(value(p, key + "date")); Task task = new Task(value(p, key + "id"), value(p, key + "title"), value(p, key + "description"), integer(p, key + "startMin"), integer(p, key + "duration"), value(p, key + "color")); tasks.computeIfAbsent(date, d -> new ArrayList<>()).add(task); }
        LocalDate selected = p.containsKey("calendar.selectedDate") ? LocalDate.parse(value(p, "calendar.selectedDate")) : LocalDate.now();
        String mode = p.containsKey("calendar.viewMode") ? value(p, "calendar.viewMode") : "Day";
        double zoom = p.containsKey("calendar.zoom") ? Double.parseDouble(value(p, "calendar.zoom")) : 1.0;
        return new CrmDataSnapshot(contacts, tasks, selected, mode, zoom);
    }

    @Override public synchronized void saveForUser(String userId, CrmDataSnapshot data) {
        Properties p = new Properties(); p.setProperty("schema.version", "1");
        p.setProperty("contacts.count", String.valueOf(data.contacts().size()));
        for (int i = 0; i < data.contacts().size(); i++) { Contact c = data.contacts().get(i); String key = "contact." + i + "."; put(p, key + "id", c.getId()); put(p, key + "name", c.nameProperty().get()); put(p, key + "company", c.companyProperty().get()); put(p, key + "title", c.titleProperty().get()); put(p, key + "email", c.emailProperty().get()); put(p, key + "phone", c.phoneProperty().get()); put(p, key + "lastInteraction", c.lastInteractionProperty().get()); put(p, key + "tags", c.tagsProperty().get()); put(p, key + "description", c.descriptionProperty().get()); }
        List<Map.Entry<LocalDate, Task>> allTasks = new ArrayList<>(); data.tasksByDate().forEach((date, tasks) -> tasks.forEach(task -> allTasks.add(Map.entry(date, task))));
        p.setProperty("tasks.count", String.valueOf(allTasks.size()));
        for (int i = 0; i < allTasks.size(); i++) { LocalDate date = allTasks.get(i).getKey(); Task t = allTasks.get(i).getValue(); String key = "task." + i + "."; put(p, key + "id", t.getId()); put(p, key + "date", date.toString()); put(p, key + "title", t.getTitle()); put(p, key + "description", t.getDescription()); put(p, key + "startMin", String.valueOf(t.getStartMin())); put(p, key + "duration", String.valueOf(t.getDuration())); put(p, key + "color", t.getColor()); }
        put(p, "calendar.selectedDate", data.selectedDate().toString()); put(p, "calendar.viewMode", data.calendarViewMode()); put(p, "calendar.zoom", String.valueOf(data.calendarZoom()));
        Path file = dataDirectory.resolve(userId + ".properties");
        try { Files.createDirectories(dataDirectory); try (OutputStream out = Files.newOutputStream(file)) { p.store(out, "VoidReach CRM data for one account"); } }
        catch (IOException e) { throw new IllegalStateException("Impossibile salvare i dati locali", e); }
    }

    private Properties load(String userId) { Properties p = new Properties(); Path file = dataDirectory.resolve(userId + ".properties"); if (Files.exists(file)) try (InputStream in = Files.newInputStream(file)) { p.load(in); } catch (IOException e) { throw new IllegalStateException("Impossibile leggere i dati locali", e); } return p; }
    private int integer(Properties p, String key) { String value = p.getProperty(key); if (value == null) return 0; try { return Integer.parseInt(decode(value)); } catch (IllegalArgumentException ex) { return Integer.parseInt(value); } }
    private void put(Properties p, String key, String value) { p.setProperty(key, Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8))); }
    private String value(Properties p, String key) { return decode(p.getProperty(key)); }
    private String decode(String value) { return value == null ? "" : new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8); }
}
