package com.digivahan.ui.Activities.changPassword;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.PasswordValidationResult;
import com.digivahan.databinding.ActivityChangePasswordPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.gson.JsonObject;

import org.json.JSONObject;

public class ChangePasswordPage extends BaseActivity {

    String TAG = "ChangePasswordPageData";

    ActivityChangePasswordPageBinding binding;

    PreferencesManager manager;

    AshDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        manager = new PreferencesManager(ChangePasswordPage.this);

        loadingDialog = new AshDialog(ChangePasswordPage.this, "Please wait", "");

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Change Password");

        getOnBackPressedDispatcher().addCallback(ChangePasswordPage.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        ImageHelperMethods.loadImage(TAG, ChangePasswordPage.this, manager.getUser().getProfile_pic(), binding.imgProfile, R.drawable.temp_profile_icon);
        binding.tvName.setText(manager.getUser().getFirst_name() + " " + manager.getUser().getLast_name());

        CommonLogic.setUserInPutFiledData(binding.currentPasswordField, R.drawable.password_icon1, "Password", "password", false, binding.scrollView, binding.btnUpdate);
        CommonLogic.setUserInPutFiledData(binding.newPasswordField, R.drawable.password_icon1, "New Password", "password", false, binding.scrollView, binding.btnUpdate);
        CommonLogic.setUserInPutFiledData(binding.confirmPasswordField, R.drawable.password_icon1, "Confirm Password", "password", true, binding.scrollView, binding.btnUpdate);

        binding.newPasswordField.etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 🔹 Remove error from confirm password field
                binding.confirmPasswordField.etInput.setError(null);
                binding.newPasswordField.etInput.setError(null); // if using TextInputLayout
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        binding.confirmPasswordField.etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 🔹 Remove error from confirm password field
                binding.newPasswordField.etInput.setError(null);
                binding.confirmPasswordField.etInput.setError(null); // if using TextInputLayout
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });


        binding.btnUpdate.setOnClickListener(v -> {

            String password = binding.newPasswordField.etInput.getText().toString();
            String currentPassword = binding.currentPasswordField.etInput.getText().toString();
            String confirmPassword = binding.confirmPasswordField.etInput.getText().toString();

            PasswordValidationResult result =
                    CommonLogic.validatePassword(password, 3);

            if (currentPassword.isEmpty()){
                Toast.makeText(this, "Please enter current password", Toast.LENGTH_SHORT).show();
                binding.currentPasswordField.etInput.setError(getString(R.string.empty_field_string));
                return;
            }else if (password.isEmpty()){
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
                binding.newPasswordField.etInput.setError(getString(R.string.empty_field_string));
                return;
            } else if (!result.isValid) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                binding.newPasswordField.etInput.setError(result.message);
                return;
            } else if (confirmPassword.isEmpty()){
                Toast.makeText(this, "Please enter confirm password", Toast.LENGTH_SHORT).show();
                binding.confirmPasswordField.etInput.setError(getString(R.string.empty_field_string));
                return;
            } else if (!password.equals(confirmPassword)){
                binding.newPasswordField.etInput.setError("Password does not match");
                binding.confirmPasswordField.etInput.setError("Password does not match");
                Toast.makeText(this, "password and confirm password does not match", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject jsonObjectForVerification = new JsonObject();
            jsonObjectForVerification.addProperty("user_id", manager.getUserId());
            jsonObjectForVerification.addProperty("old_password", currentPassword);
            jsonObjectForVerification.addProperty("new_password", password);

            CommonLogic.showTestLog(TAG, "change password params: " +jsonObjectForVerification.toString());

            loadingDialog.show();

            ApiCall.callApi(TAG,
                    ChangePasswordPage.this,
                    APIData.CHANGE_PASSWORD,
                    jsonObjectForVerification, "post",
                    new ApiCall.ApiResponseCallback() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(JSONObject responseBody, boolean status, String message) {

                            try {
                                if (status) {
                                    showPasswordChangedDialog();
                                } else {
                                    loadingDialog.dismiss();
                                    CommonLogic.showTestLog(TAG, "Failed: " + message);
                                    if (responseBody.has("error_type") && responseBody.getString("error_type").equalsIgnoreCase("old_password")){
                                        binding.currentPasswordField.etInput.setError(message);
                                    }else if (responseBody.has("error_type") && responseBody.getString("error_type").equalsIgnoreCase("new_password")){
                                        binding.newPasswordField.etInput.setError(message);
                                    }
                                    Toast.makeText(ChangePasswordPage.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                CommonLogic.showTestLog(TAG, e.getMessage());
                            }

                        }

                        @Override
                        public void onError(String errorMessage) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, errorMessage);
                        }
                    }
            );

            /*// --- Step 2: Make API call ---
            ApiClient.getApiService(ChangePasswordPage.this).changePassword(jsonObjectForVerification).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                    // Always dismiss loader
                    loadingDialog.dismiss();

                    try {
                        JSONObject responseBody = APIHelper.getResponseData(TAG, response);


                        boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                        String message = responseBody.has("message") ? responseBody.getString("message") : "";

                        Toast.makeText(ChangePasswordPage.this, message, Toast.LENGTH_SHORT).show();

                        if (status) {
                            try {
                                showPasswordChangedDialog();

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
                    CommonLogic.showTestLog(TAG, "onFailure:- Registration failed. Please try again.");
                }
            });*/
        });
    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    private void showPasswordChangedDialog() {
        Dialog dialog = new Dialog(this);
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

        btnLogin.setText("OK");
        tvTitle.setText("Password Changed");
        tvMessage.setText("Your password has been changed. Please use the new password next time you log in.");

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
            // 👉 Redirect to login activity here
            back();
        });

        dialog.show();
    }
}