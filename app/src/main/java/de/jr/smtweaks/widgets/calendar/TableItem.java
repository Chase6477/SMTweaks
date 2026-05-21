package de.jr.smtweaks.widgets.calendar;

import java.time.LocalDate;

public class TableItem {

    private final String leftTop;
    private final String rightTop;
    private final String bottom;
    private final LocalDate date;
    private final int row;
    private String rightTopAlternate;
    private String bottomAlternate;
    private boolean isCancelled;

    public TableItem(String leftTop, String rightTop, String rightTopAlternate, String bottom, String bottomAlternate, boolean isCancelled, int row, LocalDate date) {
        this.leftTop = leftTop;
        this.rightTopAlternate = rightTopAlternate;
        this.rightTop = rightTop;
        this.bottom = bottom;
        this.bottomAlternate = bottomAlternate;
        this.isCancelled = isCancelled;
        this.row = row;
        this.date = date;
    }

    public String getLeftTop() {
        return leftTop;
    }

    public String getRightTop() {
        return rightTop;
    }

    public String getRightTopAlternate() {
        return rightTopAlternate;
    }

    public void setRightTopAlternate(String rightTopAlternate) {
        this.rightTopAlternate = rightTopAlternate;
    }

    public String getBottom() {
        return bottom;
    }

    public String getBottomAlternate() {
        return bottomAlternate;
    }

    public void setBottomAlternate(String bottomAlternate) {
        this.bottomAlternate = bottomAlternate;
    }

    public boolean getIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public int getRow() {
        return row;
    }

    public LocalDate getDate() {
        return date;
    }
}
