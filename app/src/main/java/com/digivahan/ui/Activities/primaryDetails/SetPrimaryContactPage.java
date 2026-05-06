package com.digivahan.ui.Activities.primaryDetails;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.databinding.ActivitySetPrimaryContactPageBinding;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetPrimaryContactPage extends BaseActivity {

    String TAG = "SetPrimaryContactPageData";

    ActivitySetPrimaryContactPageBinding binding;

    PreferencesManager manager;

    String setPrimary = "phone", value;

    boolean isEmailSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetPrimaryContactPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        manager = new PreferencesManager(SetPrimaryContactPage.this);

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Set Primary");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        binding.userContact.setText( "+91 " + manager.getUser().getPhone_number());
        binding.userEmail.setText( manager.getUser().getEmail());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        setSelectedPrimaryData("/api/v1/user/"+manager.getUserId()+"/primary-contact-status");

        binding.llPhone.setOnClickListener(v -> {
            isEmailSelected = false;
            binding.rbEmail.setChecked(false);
            binding.rbPhone.setChecked(true);
        });

        binding.llEmail.setOnClickListener(v -> {
            isEmailSelected = true;
            binding.rbEmail.setChecked(true);
            binding.rbPhone.setChecked(false);
        });


        binding.btnSetPrimary.setOnClickListener(v -> {

            JsonObject jsonObjectForgetPassword = new JsonObject();
            jsonObjectForgetPassword.addProperty("user_id", manager.getUserId());

            if (isEmailSelected) {
                setPrimary = "email";
                value = manager.getUser().getEmail();
            } else {
                setPrimary = "phone";
                value = manager.getUser().getPhone_number();
            }

            jsonObjectForgetPassword.addProperty("set_primary", setPrimary);
            jsonObjectForgetPassword.addProperty("value", value);


            // --- Step 2: Make API call ---
            ApiClient.getApiService(SetPrimaryContactPage.this).setPrimary(jsonObjectForgetPassword).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    try {
                        JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                        boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                        String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

//                    Toast.makeText(SetPrimaryContactPage.this, message, Toast.LENGTH_SHORT).show();

                        if (status) {
                            try {

                                JSONObject userJson = responseBody.getJSONObject("data");

                                if (userJson.getJSONObject("email").getBoolean("is_primary") && userJson.getJSONObject("email").getBoolean("is_primary")) {
                                    isEmailSelected = true;
                                    binding.rbEmail.setChecked(true);
                                    binding.rbPhone.setChecked(false);
                                } else if (userJson.getJSONObject("phone").getBoolean("is_primary") && userJson.getJSONObject("email").getBoolean("is_primary")) {
                                    isEmailSelected = false;
                                    binding.rbEmail.setChecked(false);
                                    binding.rbPhone.setChecked(true);
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
                    CommonLogic.showTestLog(TAG, "onFailure:- Updation failed. Please try again.");
                }
            });
        });

    }

    private void setSelectedPrimaryData(String url) {
        // --- Step 2: Make API call ---
        ApiClient.getApiService(SetPrimaryContactPage.this).getPrimary(url).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                    boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

//                    Toast.makeText(SetPrimaryContactPage.this, message, Toast.LENGTH_SHORT).show();

                    if (status) {
                        try {

                            JSONObject userJson = responseBody.getJSONObject("data");

                            if (userJson.getJSONObject("email").getBoolean("is_primary") && userJson.getJSONObject("email").getBoolean("is_primary")) {
                                isEmailSelected = true;
                                binding.rbEmail.setChecked(true);
                                binding.rbPhone.setChecked(false);
                            } else if (userJson.getJSONObject("phone").getBoolean("is_primary") && userJson.getJSONObject("email").getBoolean("is_primary")) {
                                isEmailSelected = false;
                                binding.rbEmail.setChecked(false);
                                binding.rbPhone.setChecked(true);
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
                CommonLogic.showTestLog(TAG, "onFailure:- Updation failed. Please try again.");
            }
        });
    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

}