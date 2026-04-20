package com.digivahan.data.model;

public class ComparisonItemModel {

    public String label;
    public String leftText;
    public String rightText;
    public boolean highlightLeft;   // green pill on left
    public boolean highlightRight;  // green pill on right
    public boolean isBoolean;       // use tick/cross instead of text

    public ComparisonItemModel(String label, String leftText, String rightText) {
        this.label = label;
        this.leftText = leftText;
        this.rightText = rightText;
    }

    public static ComparisonItemModel bool(String label, boolean left, boolean right) {
        ComparisonItemModel i = new ComparisonItemModel(label, left ? "true" : "false", right ? "true" : "false");
        i.isBoolean = true;
        i.highlightLeft = left;
        i.highlightRight = right;
        return i;
    }

    public ComparisonItemModel highlightLeft() { this.highlightLeft = true; return this; }
    public ComparisonItemModel highlightRight() { this.highlightRight = true; return this; }
}
