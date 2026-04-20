package com.digivahan.ui.Activities.qr;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.digivahan.R;
import com.digivahan.data.adapters.QRTypeAdapter;
import com.digivahan.data.adapters.StatusAdapter;
import com.digivahan.data.model.QRTypeModel;
import com.digivahan.databinding.ActivityCheckQrstatusBinding;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;

public class CheckQRStatus extends BaseActivity {

    ActivityCheckQrstatusBinding binding;
    private ArrayList<String> qrTypeList = new ArrayList<>();
    private ArrayList<QRTypeModel> qrItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckQrstatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );


        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Check QR Status");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });


        // Initialize status list
        qrTypeList.add("Vehicle");
        qrTypeList.add("Keys");
        qrTypeList.add("Pet");
        qrTypeList.add("Child");
        qrTypeList.add("Luggage");

// Set LayoutManager
        binding.qrTypeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

// Set Adapter
        StatusAdapter adapter = new StatusAdapter(qrTypeList, 0, (position, status) -> {
            // Handle selected status here
            Toast.makeText(this, "Selected: " + status, Toast.LENGTH_SHORT).show();
        });

        binding.qrTypeRecyclerView.setAdapter(adapter);


        QRTypeAdapter qrTypeAdapter = new QRTypeAdapter(CheckQRStatus.this , qrItemList);

        binding.qrItemRecyclerView.setAdapter(qrTypeAdapter);

    }
}