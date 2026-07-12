package com.crm.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class NotesControllerTest {
    @Test void markdownParserAndDragFormatInitialize() {
        assertDoesNotThrow(() -> Class.forName(NotesController.class.getName()));
    }
}
