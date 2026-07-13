package com.crm.controller;

import com.crm.model.NoteFolder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotesControllerTest {
    @Test void markdownParserAndDragFormatInitialize() {
        assertDoesNotThrow(() -> Class.forName(NotesController.class.getName()));
    }

    @Test void folderMovePolicyAllowsReparentingWithoutCyclesOrDuplicateNames() {
        NoteFolder projects = new NoteFolder("projects", "Projects", "");
        NoteFolder child = new NoteFolder("child", "Client A", "projects");
        NoteFolder grandchild = new NoteFolder("grandchild", "Assets", "child");
        NoteFolder archive = new NoteFolder("archive", "Archive", "");
        NoteFolder duplicate = new NoteFolder("duplicate", "Projects", "archive");
        List<NoteFolder> folders = List.of(projects, child, grandchild, archive, duplicate);

        assertTrue(NotesController.canMoveFolder(child, "archive", folders));
        assertTrue(NotesController.canMoveFolder(child, "", folders));
        assertFalse(NotesController.canMoveFolder(projects, "projects", folders));
        assertFalse(NotesController.canMoveFolder(projects, "grandchild", folders));
        assertFalse(NotesController.canMoveFolder(projects, "", folders));
        assertFalse(NotesController.canMoveFolder(projects, "archive", folders));
        assertFalse(NotesController.canMoveFolder(child, "missing", folders));
    }
}
