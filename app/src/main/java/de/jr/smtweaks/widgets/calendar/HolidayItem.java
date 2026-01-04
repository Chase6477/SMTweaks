package de.jr.smtweaks.widgets.calendar;

import java.time.LocalDate;

public class HolidayItem {
    private final String startDate;
    private final String endDate;

    public HolidayItem(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getEndDate() {
        return LocalDate.parse(endDate);
    }

    public LocalDate getStartDate() {
        return LocalDate.parse(startDate);
    }


    public boolean containsDate(LocalDate date) {
        return (!getStartDate().isAfter(date) && !getEndDate().isBefore(date));
    }
}
