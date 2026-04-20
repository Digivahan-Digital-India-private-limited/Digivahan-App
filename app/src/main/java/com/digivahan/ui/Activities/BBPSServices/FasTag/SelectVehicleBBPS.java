package com.digivahan.ui.Activities.BBPSServices.FasTag;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.digivahan.R;
import com.digivahan.data.adapters.FasTagAdapter;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.FasTagCardModel;
import com.digivahan.databinding.ActivitySelectVehicleFasTagBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagAppDatabase;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagDao;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagEntity;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagMapper;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;
import java.util.List;

public class SelectVehicleBBPS extends BaseActivity {

    ActivitySelectVehicleFasTagBinding binding;

    PreferencesManager manager;

    AshDialog loadingDialog;
    FasTagAppDatabase database;
    FasTagDao dao;

    ArrayList<FasTagCardModel> selectedFasTagCardList = new ArrayList<>();

    FasTagAdapter fasTagAdapter;

    String categoryKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectVehicleFasTagBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Find FasTag");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        getOnBackPressedDispatcher().addCallback(SelectVehicleBBPS.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        try {
            categoryKey = getIntent().getStringExtra("categoryKey");
        } catch (Exception e) {
//            throw new RuntimeException(e);
        }

        manager = new PreferencesManager(SelectVehicleBBPS.this);

        database = FasTagAppDatabase.getInstance(SelectVehicleBBPS.this);
        dao = database.fasTagDao();

        loadingDialog = new AshDialog(SelectVehicleBBPS.this, "Please wait", "");

        binding.checkVehicle.setOnClickListener(view -> {
            String vehicleNumber = binding.vehicleNumber.getText().toString();
            if (vehicleNumber.isEmpty()){
                binding.vehicleNumber.setError("Filed can't be empty");
                return;
            }

            disableHideContentSecureForNextNavigation();
            Intent FasTagWalletPage = new Intent(SelectVehicleBBPS.this, BillerIdList.class);
            FasTagWalletPage.putExtra("vehicleNumber", vehicleNumber);
            FasTagWalletPage.putExtra("categoryKey", categoryKey);
            startActivity(FasTagWalletPage);

        });

        List<FasTagEntity> entityList = dao.getAll();

        selectedFasTagCardList.clear();
        for (FasTagEntity entity : entityList) {
            FasTagCardModel model = FasTagMapper.toModel(entity);
            if (model.getCategoryKey().equalsIgnoreCase(categoryKey)) {
                selectedFasTagCardList.add(model);
            }
        }

        fasTagAdapter = new FasTagAdapter(this, selectedFasTagCardList, new FasTagAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(FasTagCardModel model) {
                disableHideContentSecureForNextNavigation();
                Intent FasTagWalletPage = new Intent(SelectVehicleBBPS.this, FasTagWallet.class);
                FasTagWalletPage.putExtra("selectedBank", model);
                startActivity(FasTagWalletPage);
            }

            @Override
            public void onDeleteClick(FasTagCardModel model, int position) {

                // 🔹 Delete from DB
                FasTagEntity entity = FasTagMapper.toEntity(model);
                FasTagEntity existing = dao.getByVehicle(entity.vehicleNumber);

                if (existing != null) {
                    dao.delete(existing);
                }

                // 🔹 Remove from list
                selectedFasTagCardList.remove(position);
                fasTagAdapter.notifyItemRemoved(position);

                Toast.makeText(SelectVehicleBBPS.this, "FASTag removed", Toast.LENGTH_SHORT).show();

                /*new AlertDialog.Builder(FasTagWallet.this)
                        .setTitle("Remove FASTag")
                        .setMessage("Are you sure you want to remove this FASTag card?")
                        .setPositiveButton("Yes", (dialog, which) -> {


                        })
                        .setNegativeButton("Cancel", null)
                        .show();*/
            }
        });

        binding.recyclerFastag.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFastag.setAdapter(fasTagAdapter);
    }

    public void showServiceDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_password_changed);
        dialog.setCancelable(false);

        // Transparent background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView btnLogin = dialog.findViewById(R.id.btnLogin);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        ImageView imgStatus = dialog.findViewById(R.id.imgStatus);
        imgStatus.setImageResource(R.drawable.app_logo);

        btnLogin.setText("Ok");

        tvTitle.setText("No Service");
        tvMessage.setText("No service available for this option at this time, Please choose any other option.");

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }
}