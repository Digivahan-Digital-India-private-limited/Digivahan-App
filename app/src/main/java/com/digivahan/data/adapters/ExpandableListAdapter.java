package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import com.digivahan.R;
import com.digivahan.data.model.MenuModel;
import com.digivahan.ui.Activities.MainActivity;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final List<MenuModel> listDataHeader;
    private final HashMap<MenuModel, List<MenuModel>> listDataChild;

    public ExpandableListAdapter(Context context, List<MenuModel> listDataHeader,
                                 HashMap<MenuModel, List<MenuModel>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }

    @Override
    public MenuModel getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView,
                             ViewGroup parent) {

        final String childText = getChild(groupPosition, childPosition).menuName;
        final Drawable childDrawable = getChild(groupPosition, childPosition).icon;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_child, null);
        }

        TextView txtListChild = convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText);
        childDrawable.setBounds(0, 0, 60, 60);
        txtListChild.setCompoundDrawables(childDrawable, null, null, null);
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        if (this.listDataChild.get(this.listDataHeader.get(groupPosition)) == null)
            return 0;
        else
            return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                    .size();
    }

    @Override
    public MenuModel getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = getGroup(groupPosition).menuName;
        Drawable headerIcon = getGroup(groupPosition).icon;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_header, null);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        ImageView nav_icon = convertView.findViewById(R.id.nav_icon);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchBtn = convertView.findViewById(R.id.switchBtn);
        if(getGroup(groupPosition).isWallet) {
            lblListHeader.setText(getGroup(groupPosition).menuNameSpan, TextView.BufferType.SPANNABLE);
        }else{
            lblListHeader.setText(headerTitle);
        }


        headerIcon.setBounds(0, 0, 60, 60);
        nav_icon.setImageDrawable(headerIcon);

        if (getGroup(groupPosition).showSwitchBtn) {
            switchBtn.setVisibility(View.VISIBLE);

            // ✅ Handle switch change
            switchBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ((MainActivity) context).drawerClicked(getGroup(groupPosition), true, isChecked);
            });
        } else {
            switchBtn.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(v -> {
            ((MainActivity) context).drawerClicked(getGroup(groupPosition), false, false);
        });

//        lblListHeader.setCompoundDrawables(headerIcon, null, null, null);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}