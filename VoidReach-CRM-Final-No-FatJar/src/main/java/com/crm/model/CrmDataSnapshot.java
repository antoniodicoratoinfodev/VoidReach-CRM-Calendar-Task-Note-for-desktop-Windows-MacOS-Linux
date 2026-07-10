package com.crm.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Aggregate for one owner. SQL version maps to contacts, calendar_tasks and user_preferences tables. */
public record CrmDataSnapshot(
        List<Contact> contacts,
        Map<LocalDate, List<Task>> tasksByDate,
        LocalDate selectedDate,
        String calendarViewMode,
        double calendarZoom) { }
