package com.crm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NoteFolderTest {
    @Test void trimsNamesAndKeepsStableIdWhenRenamed() {
        NoteFolder folder = new NoteFolder("folder-1", "  Projects  ", "parent-1");

        folder.setName(" Archive ");

        assertEquals("folder-1", folder.getId());
        assertEquals("Archive", folder.getName());
        assertEquals("parent-1", folder.getParentFolderId());
    }

    @Test void rejectsMissingIdentityAndBlankNames() {
        assertThrows(IllegalArgumentException.class, () -> new NoteFolder("", "Projects"));
        assertThrows(IllegalArgumentException.class, () -> new NoteFolder("folder-1", "  "));
        assertThrows(IllegalArgumentException.class, () -> new NoteFolder("folder-1", "Projects", "folder-1"));
    }
}
