package com.digivahan.ui.Fragments.myGarageInfoPages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digivahan.data.adapters.VehicleDocumentListAdapter;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.FragmentDocumentsBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.garage.MyGarageDocumentPage;
import com.digivahan.utils.CommonLogic;

public class DocumentsFragment extends Fragment {

    String TAG = "DocumentsFragmentData";

    private FragmentDocumentsBinding binding;
    private GarageItemModel vehicleInfo;
    Activity activity;

    // ✅ Constructor
    public DocumentsFragment(GarageItemModel vehicleInfo, Activity activity) {
        this.vehicleInfo = vehicleInfo;
        this.activity = activity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDocumentsBinding.inflate(inflater, container, false);

        // ✅ Set listeners for each document type
//        binding.uploadInsuranceDocument.setOnClickListener(v -> openDocumentPage(true, "insurance", ""));
//        binding.uploadPollutionDocument.setOnClickListener(v -> openDocumentPage(true, "pollution", ""));
//        binding.uploadRCDocument.setOnClickListener(v -> openDocumentPage(true, "registration", ""));

        CommonLogic.showTestLog(TAG, "vehicle_id: " + vehicleInfo.getVehicle_id());

        binding.uploadOtherDocument.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent uploadDocument = new Intent(getContext(), MyGarageDocumentPage.class);
            uploadDocument.putExtra("vehicleInfo", vehicleInfo);
            startActivity(uploadDocument);
            activity.finish();
        });

        if (vehicleInfo == null || vehicleInfo.getVehicleDocumentsArrayList() == null || vehicleInfo.getVehicleDocumentsArrayList().isEmpty()) {
            binding.otherDocumentList.setVisibility(View.GONE);
        } else {
            binding.otherDocumentList.setVisibility(View.VISIBLE);
            VehicleDocumentListAdapter documentListAdapter = new VehicleDocumentListAdapter(getActivity(), vehicleInfo.getVehicle_id(), true, vehicleInfo.getVehicleDocumentsArrayList());
            binding.otherDocumentList.setAdapter(documentListAdapter);
        }

        return binding.getRoot();
    }
}
