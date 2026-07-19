package com.crm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CalendarControllerTest {
    @Test
    void miniCalendarDaysUseAllAvailableWidthAtNormalSizes() {
        assertEquals(29.0, CalendarController.miniCalendarCellSize(221, 3), 0.0001);
        assertEquals(34.0, CalendarController.miniCalendarCellSize(256, 3), 0.0001);
    }

    @Test
    void miniCalendarDaysStayInsideNarrowGridsAndStopGrowingAtTheVisualMaximum() {
        assertEquals(10.0, CalendarController.miniCalendarCellSize(88, 3), 0.0001);
        assertEquals(40.0, CalendarController.miniCalendarCellSize(400, 3), 0.0001);
    }

    @Test
    void timelineUsesTheActualTimeColumnWidth() {
        assertEquals(928.0, CalendarController.timelineWidthFor(1000, 72), 0.0001);
    }

    @Test
    void taskEntryStaysInsideItsDayMargins() {
        double dayWidth = 140;
        double margin = 5;
        double left = 6 * dayWidth + margin;
        double right = left + CalendarController.taskEntryWidth(dayWidth, margin);

        assertEquals(130.0, CalendarController.taskEntryWidth(dayWidth, margin), 0.0001);
        assertEquals(7 * dayWidth - margin, right, 0.0001);
    }
}
