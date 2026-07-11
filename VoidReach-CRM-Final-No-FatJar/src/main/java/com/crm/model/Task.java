package com.crm.model;

import java.util.UUID;

public class Task {
    public static final int MINUTES_PER_DAY = 24 * 60;
    public static final int MIN_DURATION_MINUTES = 5;

    private String title;
    private String description;
    private int startMin;
    private int duration;
    private String color;
    private final String id;

    public Task(String title, String description, int startMin, int duration, String color) {
        this(UUID.randomUUID().toString(), title, description, startMin, duration, color);
    }

    public Task(String id, String title, String description, int startMin, int duration, String color) {
        validateSchedule(startMin, duration);
        this.id = id;
        this.title = title;
        this.description = description;
        this.startMin = startMin;
        this.duration = duration;
        this.color = color;
    }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public int getStartMin() { return startMin; }
    public void setStartMin(int startMin) {
        validateSchedule(startMin, duration);
        this.startMin = startMin;
    }

    public int getDuration() { return duration; }
    public void setDuration(int duration) {
        validateSchedule(startMin, duration);
        this.duration = duration;
    }

    public String getColor() { return color; }
    public String getId() { return id; }

    public static void validateSchedule(int startMin, int duration) {
        if (startMin < 0 || startMin >= MINUTES_PER_DAY) {
            throw new IllegalArgumentException("L'orario di inizio deve essere compreso tra 00:00 e 23:59.");
        }
        if (duration < MIN_DURATION_MINUTES) {
            throw new IllegalArgumentException("Un'attività deve durare almeno 5 minuti.");
        }
        if (duration > MINUTES_PER_DAY - startMin) {
            throw new IllegalArgumentException("Un'attività non può terminare dopo le 24:00.");
        }
    }
}
