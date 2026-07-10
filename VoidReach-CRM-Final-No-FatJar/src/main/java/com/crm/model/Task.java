package com.crm.model;

import java.util.UUID;

public class Task {
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
    public void setStartMin(int startMin) { this.startMin = startMin; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getColor() { return color; }
    public String getId() { return id; }
}
