package com.digivahan.ui.Fragments.virtualQR;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digivahan.data.adapters.VirtualQRItemAdapter;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.FragmentVirtualQRListPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.other.HideContentLayout;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;


public class VirtualQRListPage extends Fragment {

    String TAG = "VirtualQRListPageData";

    FragmentVirtualQRListPageBinding binding;

    ArrayList<GarageItemModel> notificationItemList = new ArrayList<>();

    VirtualQRItemAdapter notificationItemAdapter;

    PreferencesManager preferencesManager;
    AshDialog loadingDialog;

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<GarageItemModel> cachedList = CommonMethods.loadGarageCache(getActivity());

        if (cachedList != null && !cachedList.isEmpty()){
            updateGarageUI(cachedList);
        }else {
            getGarageVehicleList();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVirtualQRListPageBinding.inflate(getLayoutInflater(), container, false);

        preferencesManager = new PreferencesManager(requireContext());

        loadingDialog = new AshDialog(getContext(), "Please wait", "");

        notificationItemAdapter = new VirtualQRItemAdapter(getContext(), notificationItemList);
        binding.rvVirtualQRList.setAdapter(notificationItemAdapter);


        return binding.getRoot();
    }

    private void getGarageVehicleList() {
        loadingDialog.show();

        CommonMethods.getGarageVehicleList(TAG,
                getActivity(),
                preferencesManager.getUserId(),
                new CommonMethods.GarageListCallback() {

                    @Override
                    public void onSuccess(ArrayList<GarageItemModel> garageList) {

                        loadingDialog.dismiss();

                        updateGarageUI(garageList);

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
                    }
                }
        );
    }

    private void updateGarageUI(ArrayList<GarageItemModel> garageList) {
        notificationItemList.clear();
        notificationItemList.addAll(garageList);
        notificationItemAdapter.notifyDataSetChanged();

        if (garageList.isEmpty()){
            binding.emptyLayout.setVisibility(View.VISIBLE);
            binding.rvVirtualQRList.setVisibility(View.GONE);
        }else {
            binding.emptyLayout.setVisibility(View.GONE);
            binding.rvVirtualQRList.setVisibility(View.VISIBLE);
        }
    }
}