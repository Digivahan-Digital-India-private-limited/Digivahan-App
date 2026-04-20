package com.digivahan.ui.Activities.forgotPassword;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.databinding.ActivityForgotPasswordBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.auth.LoginActivity;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.google.gson.JsonObject;

import org.json.JSONObject;

public class ForgotPasswordActivity extends BaseActivity {
    String TAG = "ForgotPasswordActivityData";

    ActivityForgotPasswordBinding binding;
    AshDialog loadingDialog;
    PreferencesManager prefs;

    String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            userId = getIntent().getStringExtra("userId");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        loadingDialog = new AshDialog(ForgotPasswordActivity.this, "Please wait", "Registering user...");
        prefs = new PreferencesManager(ForgotPasswordActivity.this);

        getOnBackPressedDispatcher().addCallback(ForgotPasswordActivity.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setData();

        setEvents();


        binding.loginPasswordField.etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 🔹 Remove error from confirm password field
                binding.loginPasswordField.etInput.setError(null);
                binding.loginConfirmPasswordField.etInput.setError(null); // if using TextInputLayout
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        binding.loginConfirmPasswordField.etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 🔹 Remove error from confirm password field
                binding.loginPasswordField.etInput.setError(null);
                binding.loginConfirmPasswordField.etInput.setError(null); // if using TextInputLayout
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

    }

    private void setData() {
        binding.loginPasswordField.etInput.setHint("Enter your password");
        binding.loginPasswordField.ivIcon.setImageResource(R.drawable.password_icon1);
        CommonLogic.setupField(binding.loginPasswordField, "password", null);
        CommonLogic.setScrolling(binding.loginPasswordField.etInput, binding.scrollView, binding.btnLayout);

        binding.loginConfirmPasswordField.etInput.setHint("Confirm your password");
        binding.loginConfirmPasswordField.ivIcon.setImageResource(R.drawable.password_icon1);
        CommonLogic.setupField(binding.loginConfirmPasswordField, "password", null);
        CommonLogic.setScrolling(binding.loginConfirmPasswordField.etInput, binding.scrollView, binding.btnLayout);
    }

    private void setEvents() {
        binding.btnSave.setOnClickListener(v -> {

            String password = binding.loginPasswordField.etInput.getText().toString();
            String confirmPassword = binding.loginConfirmPasswordField.etInput.getText().toString();

            if (!CommonLogic.validateField(binding.loginPasswordField.etInput, "password")){
                return;
            } else if (!CommonLogic.validateField(binding.loginConfirmPasswordField.etInput, "password")){
                return;
            } else if (!password.equals(confirmPassword)){
                binding.loginPasswordField.etInput.setError("Password does not match");
                binding.loginConfirmPasswordField.etInput.setError("Password does not match");
                Toast.makeText(this, "password and confirm password does not match", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            JsonObject jsonObjectForChangePassword = new JsonObject();
            jsonObjectForChangePassword.addProperty("user_id", userId);
            jsonObjectForChangePassword.addProperty("new_password", password);

            ApiCall.callApi(TAG, ForgotPasswordActivity.this, APIData.NEW_PASSWORD, jsonObjectForChangePassword, "post", new ApiCall.ApiResponseCallback() {
                @Override
                public void onSuccess(JSONObject responseBody, boolean status, String message) {
                    loadingDialog.dismiss();
                    try {
                        if (status) {
                            CommonMethods.showSuccessDialog(ForgotPasswordActivity.this, "");
                        }else {
                            if (responseBody.has("error_type") && responseBody.getString("error_type").equalsIgnoreCase("password")){
                                binding.loginPasswordField.etInput.setError(message);
                            }
                            Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ForgotPasswordActivity.this, "Unable to change password", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onError(String errorMessage) {
                    loadingDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, "Unable to change password", Toast.LENGTH_SHORT).show();
                }
            });

        });

        binding.btnLogin.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
}