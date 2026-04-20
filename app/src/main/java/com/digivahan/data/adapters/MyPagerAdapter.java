package com.digivahan.data.adapters;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.digivahan.data.model.GarageItemModel;
import com.digivahan.ui.Fragments.myGarageInfoPages.DetailsFragment;
import com.digivahan.ui.Fragments.myGarageInfoPages.DocumentsFragment;
import com.digivahan.ui.Fragments.myGarageInfoPages.VirtualRCFragment;

public class MyPagerAdapter extends FragmentStateAdapter {
    GarageItemModel vehicleInfo;
    Activity activity;
    int tabCount;
    public MyPagerAdapter(@NonNull FragmentActivity fragmentActivity, Activity activity, GarageItemModel vehicleInfo, int tabCount) {
        super(fragmentActivity);
        this.vehicleInfo = vehicleInfo;
        this.tabCount = tabCount;
        this.activity = activity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 1 -> new DocumentsFragment(vehicleInfo, activity);
            case 2 -> new VirtualRCFragment();
            default -> new DetailsFragment(vehicleInfo);
        };
    }

    @Override
    public int getItemCount() {
        return tabCount; // Total number of tabs
    }
}

