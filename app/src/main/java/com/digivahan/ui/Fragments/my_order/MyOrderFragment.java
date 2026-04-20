package com.digivahan.ui.Fragments.my_order;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.digivahan.data.adapters.MyOrderItemAdapter;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.FragmentMyOrderBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;

public class MyOrderFragment extends Fragment {

    String TAG = "MyOrderFragmentData";

    FragmentMyOrderBinding binding;

    ArrayList<GarageItemModel> myOrderItemList = new ArrayList<>();

    MyOrderItemAdapter orderItemAdapter;

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
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        binding = FragmentMyOrderBinding.inflate(getLayoutInflater(), container, false);

        preferencesManager = new PreferencesManager(requireContext());

        loadingDialog = new AshDialog(getContext(), "Please wait", "");

        orderItemAdapter = new MyOrderItemAdapter(getContext(), myOrderItemList);
        binding.rvNotificationList.setAdapter(orderItemAdapter);

        return binding.getRoot();
    }


    private void getGarageVehicleList() {
        loadingDialog.show();

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
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void updateGarageUI(ArrayList<GarageItemModel> garageList) {
        myOrderItemList.clear();
        myOrderItemList.addAll(garageList);
        orderItemAdapter.notifyDataSetChanged();

        if (garageList.isEmpty()) {
            binding.emptyLayout.setVisibility(View.VISIBLE);
            binding.rvNotificationList.setVisibility(View.GONE);
        } else {
            binding.emptyLayout.setVisibility(View.GONE);
            binding.rvNotificationList.setVisibility(View.VISIBLE);
        }
    }
}