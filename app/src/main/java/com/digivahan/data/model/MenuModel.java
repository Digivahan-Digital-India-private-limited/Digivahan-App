package com.digivahan.data.model;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;

public class MenuModel {

    public String menuName;
    public SpannableStringBuilder menuNameSpan;
    public Drawable icon;
    public boolean hasChildren, isGroup, isWallet, showSwitchBtn = false;

    public MenuModel(String menuName, Drawable icon, boolean isGroup, boolean hasChildren) {
        this.menuName = menuName;
        this.icon = icon;
        this.isGroup = isGroup;
        this.hasChildren = hasChildren;
    }

    public MenuModel(String menuName, Drawable icon, boolean isGroup, boolean hasChildren, boolean showSwitchBtn) {
        this.menuName = menuName;
        this.icon = icon;
        this.isGroup = isGroup;
        this.hasChildren = hasChildren;
        this.showSwitchBtn = showSwitchBtn;
    }

    public MenuModel(SpannableStringBuilder menuName, Drawable icon, boolean isGroup, boolean hasChildren, boolean isWallet) {
        this.menuNameSpan = menuName;
        this.icon = icon;
        this.isGroup = isGroup;
        this.hasChildren = hasChildren;
        this.isWallet = isWallet;
    }
}
