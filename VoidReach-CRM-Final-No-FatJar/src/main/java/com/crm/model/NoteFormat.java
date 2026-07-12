package com.crm.model;

public enum NoteFormat {
    TEXT("Plain text", ".txt"),
    MARKDOWN("Markdown", ".md");

    private final String displayName;
    private final String extension;

    NoteFormat(String displayName, String extension) {
        this.displayName = displayName;
        this.extension = extension;
    }

    public String displayName() { return displayName; }
    public String extension() { return extension; }

    @Override public String toString() { return displayName; }
}
