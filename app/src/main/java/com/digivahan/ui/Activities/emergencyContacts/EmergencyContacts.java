package com.digivahan.ui.Activities.emergencyContacts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.adapters.EmergencyContactListAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.EmergencyContactModel;
import com.digivahan.databinding.ActivityEmergencyContactsBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyContacts extends BaseActivity {

    String TAG = "EmergencyContactsData";
    ActivityEmergencyContactsBinding binding;

    EmergencyContactListAdapter emergencyContactListItemAdapter;
    ArrayList<EmergencyContactModel> contactList = new ArrayList<>();
    AshDialog loadingDialog;
    PreferencesManager manager;
    boolean isEditable = false;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        isEditable = false;
        emergencyContactListItemAdapter.setEditable(isEditable);
        emergencyContactListItemAdapter.notifyDataSetChanged();
        binding.addEmergencyContact.setVisibility(View.VISIBLE);
        getEmergencyContactList();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setImageResource(R.drawable.edit_icon2);
        binding.toolbarLayout.tvTitle.setText("Emergency Contacts");


        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            back();
        });

        getOnBackPressedDispatcher().addCallback(EmergencyContacts.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        loadingDialog = new AshDialog(EmergencyContacts.this, "Please wait", "");

        manager = new PreferencesManager(EmergencyContacts.this);

        binding.addEmergencyContact.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent addEmergencyContactPage = new Intent(EmergencyContacts.this, EditEmergencyContacts.class);
            addEmergencyContactPage.putExtra("hit_type" , "add");
            startActivity(addEmergencyContactPage);
        });

        emergencyContactListItemAdapter = new EmergencyContactListAdapter(EmergencyContacts.this, contactList, isEditable,
                loadingDialog, new EmergencyContactListAdapter.OnContactDeleteListener() {
            @Override
            public void onContactDeleted(int listSize) {
                isEditable = false;
                binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
                emergencyContactListItemAdapter.notifyDataSetChanged();
                if (contactList.isEmpty()){
                    binding.toolbarLayout.ivBell.setVisibility(View.GONE);
                    binding.emptyLayout.setVisibility(View.VISIBLE);
                    binding.rvEmergencyContact.setVisibility(View.GONE);
                    binding.addEmergencyContact.setVisibility(View.VISIBLE);
                }else {
                    binding.emptyLayout.setVisibility(View.GONE);
                    binding.rvEmergencyContact.setVisibility(View.VISIBLE);
                    binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onContactDeleteFailed(String errorMessage) {
                Toast.makeText(
                        EmergencyContacts.this,
                        errorMessage,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        binding.rvEmergencyContact.setAdapter(emergencyContactListItemAdapter);

        binding.toolbarLayout.ivBell.setOnClickListener(v -> {
            isEditable = true;
            binding.toolbarLayout.ivBell.setVisibility(View.GONE);
            binding.addEmergencyContact.setVisibility(View.GONE);
            binding.toolbarLayout.tvTitle.setText("Edit Emergency Contacts");
            emergencyContactListItemAdapter.setEditable(isEditable);
            emergencyContactListItemAdapter.notifyDataSetChanged();
        });

    }

    private void back() {
        if (isEditable){
            isEditable = false;
            binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
            binding.addEmergencyContact.setVisibility(View.VISIBLE);
            binding.toolbarLayout.tvTitle.setText("Emergency Contacts");
            emergencyContactListItemAdapter.setEditable(isEditable);
            emergencyContactListItemAdapter.notifyDataSetChanged();

            if (contactList.isEmpty()){
                binding.toolbarLayout.ivBell.setVisibility(View.GONE);
                binding.emptyLayout.setVisibility(View.VISIBLE);
                binding.rvEmergencyContact.setVisibility(View.GONE);
            }else {
                binding.emptyLayout.setVisibility(View.GONE);
                binding.rvEmergencyContact.setVisibility(View.VISIBLE);
                binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
            }
        }
        else {
            disableHideContentSecureForNextNavigation();
            finish();
        }
    }

    public void getEmergencyContactList(){
        JsonObject jsonObjectDeliveryAddress = new JsonObject();
        jsonObjectDeliveryAddress.addProperty("user_id", manager.getUserId());
        jsonObjectDeliveryAddress.addProperty("details_type", "emergency_contacts");

        loadingDialog.show();

        // --- Step 2: Make API call ---
        ApiClient.getApiService(EmergencyContacts.this).commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_USER_DETAILS, jsonObjectDeliveryAddress).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                // Always dismiss loader
                loadingDialog.dismiss();

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                    CommonLogic.showTestLog(TAG, responseBody.toString());

                    boolean status = responseBody.has("success") && responseBody.getBoolean("success");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "";

//                    Toast.makeText(EmergencyContacts.this, message, Toast.LENGTH_SHORT).show();

                    if (status) {
                            try {
                                contactList.clear();
                                CommonLogic.showTestLog(TAG, responseBody.getJSONArray("data").toString());
                                contactList.addAll(APIHelper.convertJsonArrayToList(responseBody.getJSONArray("data"), EmergencyContactModel.class));
                                CommonLogic.showTestLog(TAG, "list size " +String.valueOf(contactList.size()));
                                emergencyContactListItemAdapter.notifyDataSetChanged();

                                if (contactList.isEmpty()){
                                    binding.toolbarLayout.ivBell.setVisibility(View.GONE);
                                    binding.emptyLayout.setVisibility(View.VISIBLE);
                                    binding.rvEmergencyContact.setVisibility(View.GONE);
                                }else {
                                    binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
                                    binding.emptyLayout.setVisibility(View.GONE);
                                    binding.rvEmergencyContact.setVisibility(View.VISIBLE);
                                }

                            } catch (Exception e) {
                                CommonLogic.showTestLog(TAG, e.getMessage());
                            }
                    }
                } catch (Exception e) {
                    CommonLogic.showTestLog(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                CommonLogic.showTestLog(TAG, "onFailure:- Updation failed. Please try again.");
            }
        });
    }
}