package de.jr.smtweaks.widgets.calendar;

public class TableItem {

    private final String leftTop;
    private final String rightTop;
    private String rightTopAlternate;
    private final String bottom;
    private String bottomAlternate;
    private boolean isCancelled;
    private final int col;
    private final int row;

    public TableItem(String leftTop, String rightTop, String rightTopAlternate, String bottom, String bottomAlternate, boolean isCancelled, int row, int col) {
        this.leftTop = leftTop;
        this.rightTopAlternate = rightTopAlternate;
        this.rightTop = rightTop;
        this.bottom = bottom;
        this.bottomAlternate = bottomAlternate;
        this.isCancelled = isCancelled;
        this.row = row;
        this.col = col;
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

    public int getCol() {
        return col;
    }
}
