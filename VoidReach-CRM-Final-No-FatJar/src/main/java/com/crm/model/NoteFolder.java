package com.crm.model;

import java.util.UUID;

public final class NoteFolder {
    private final String id;
    private String name;
    private String parentFolderId;

    public NoteFolder(String name) {
        this(UUID.randomUUID().toString(), name, "");
    }

    public NoteFolder(String id, String name) {
        this(id, name, "");
    }

    public NoteFolder(String id, String name, String parentFolderId) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("A note folder must have an ID.");
        this.id = id;
        setName(name);
        setParentFolderId(parentFolderId);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getParentFolderId() { return parentFolderId; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Enter a folder name.");
        this.name = name.trim();
    }

    public void setParentFolderId(String parentFolderId) {
        String parent = parentFolderId == null ? "" : parentFolderId.trim();
        if (id.equals(parent)) throw new IllegalArgumentException("A folder cannot contain itself.");
        this.parentFolderId = parent;
    }
}
