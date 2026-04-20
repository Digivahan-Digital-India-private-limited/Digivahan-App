package com.digivahan.data.model;

import java.util.List;

public class ComparisonSection {
    public String title;
    public List<ComparisonItemModel> items;
    public boolean expanded = true;

    public ComparisonSection(String title, List<ComparisonItemModel> items) {
        this.title = title;
        this.items = items;
    }
}

