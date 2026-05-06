package com.digivahan.ui.Activities.verification;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.User;
import com.digivahan.databinding.ActivityVerifyPhoneEmailBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.ui.Activities.auth.LoginActivity;
import com.digivahan.ui.Activities.forgotPassword.ForgotPasswordActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyPhoneEmailActivity extends BaseActivity {

    String TAG = "VerifyPhoneEmailActivityData";

    ActivityVerifyPhoneEmailBinding binding;

    boolean isPhoneSelected = true;

    String verificationType = "", otp_channel = "phone", verify_otp_url = "";

    PreferencesManager prefs;

    AshDialog loadingDialog;

    private CountDownTimer resendOtpTimer;


    String firstName = "", lastName = "", newPassword = "", email = "", phone = "",
            otpChannel = "",
            user_register_id = "", verify_to = "";

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (resendOtpTimer != null) {
            resendOtpTimer.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyPhoneEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                String message = "Are you sure you want to cancel the process?";

        /*if (verificationType.equalsIgnoreCase("changePassword")) {
            message = "Are you sure you want to cancel the change password process?";
        }else if (binding.btnSendCode.getText().toString().equalsIgnoreCase("Verify") && verificationType.equalsIgnoreCase("changePassword")) {
            message = "Please enter otp to complete change password process. Are you sure you want to cancel it?";
        }*/

                // Show confirmation dialog before exiting registration
                new AlertDialog.Builder(VerifyPhoneEmailActivity.this)
                        .setTitle("Cancel process?")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            if (binding.btnSendCode.getText().toString().equalsIgnoreCase("Verify")) {
                                setVerifyOTPLayout(false);
                            } else {
                                disableHideContentSecureForNextNavigation();
                                // 👉 Redirect to login activity here
                                Intent intent = new Intent(VerifyPhoneEmailActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finishAffinity();
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        initializeData();

        setData();

        setEvents();
    }

    private void initializeData() {
        prefs = new PreferencesManager(VerifyPhoneEmailActivity.this);
        loadingDialog = new AshDialog(VerifyPhoneEmailActivity.this, "Please wait", "Registering user...");
    }

    private void setData() {
        CommonLogic.setupField(binding.loginTypeField, "phone", null);
        binding.title.setText("Verify Your Number");
        binding.description.setText("Please verify the number associated with your account & don’t share your OTP with anyone.");
        binding.loginTypeField.etInput.setHint("Enter your phone number");
        binding.loginTypeField.etInput.setText("");
        binding.loginTypeField.ivIcon.setImageResource(R.drawable.call_icon2);
        CommonLogic.setScrolling(binding.loginTypeField.etInput, binding.scrollView, binding.btnSendCode);
        binding.btnPhone.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.widgetColor));
        binding.btnEmail.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.paragraph));

        try {
            if (getIntent().hasExtra("newPassword")) {
                newPassword = getIntent().getStringExtra("newPassword");
            }
            if (getIntent().hasExtra("firstName")) {
                firstName = getIntent().getStringExtra("firstName");
            }
            if (getIntent().hasExtra("lastName")) {
                lastName = getIntent().getStringExtra("lastName");
            }
            if (getIntent().hasExtra("email")) {
                email = getIntent().getStringExtra("email");
            }
            if (getIntent().hasExtra("phone")) {
                phone = getIntent().getStringExtra("phone");
            }
            if (getIntent().hasExtra("otpChannel")) {
                otpChannel = getIntent().getStringExtra("otpChannel");
            }
            if (getIntent().hasExtra("verificationType")) {
                verificationType = getIntent().getStringExtra("verificationType");
            }


//            hit_type = getIntent().getStringExtra("hit_type");


            // in case of verification of email and number
            if (getIntent().hasExtra("isPhoneSelected")) {
                isPhoneSelected = getIntent().getBooleanExtra("isPhoneSelected", true);
            }
            if (getIntent().hasExtra("verify_to")) {
                verify_to = getIntent().getStringExtra("verify_to");
            }


        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        if (verificationType.equalsIgnoreCase("createAccount")) {
            binding.loginTypeField.etInput.setFocusable(false);
            binding.loginTypeField.etInput.setClickable(false);
            binding.loginTypeField.etInput.setText(phone);
        } else if (verificationType.equalsIgnoreCase("changePassword")) {
            binding.loginTypeField.etInput.setFocusable(true);
            binding.loginTypeField.etInput.setClickable(true);
            if (isPhoneSelected) {
                binding.loginTypeField.etInput.setHint("Enter your registered number");
            } else {
                binding.loginTypeField.etInput.setHint("Enter your registered email");
            }
        } else if (verificationType.equalsIgnoreCase("otpLogin")) {
            binding.loginTypeField.etInput.setText(phone);
            sendOTPRequests();
        }
    }

    private void setEvents() {
        binding.btnPhone.setOnClickListener(v -> {
            CommonLogic.setupField(binding.loginTypeField, "phone", null);
            binding.title.setText("Verify Your Number");
            binding.description.setText("Please verify the number associated with your account & don’t share your OTP with anyone.");
            binding.loginTypeField.etInput.setHint("Enter your phone number");
            binding.loginTypeField.etInput.setText("");
            binding.loginTypeField.ivIcon.setImageResource(R.drawable.call_icon2);
            binding.btnPhone.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.widgetColor));
            binding.btnEmail.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.paragraph));
            isPhoneSelected = true;
            otp_channel = "phone";
            binding.loginTypeField.etInput.setText(phone);
        });

        binding.btnEmail.setOnClickListener(v -> {
            CommonLogic.setupField(binding.loginTypeField, "email", null);
            binding.title.setText("Verify Your Email");
            binding.description.setText("Please verify the email associated with your account & don’t share your OTP with anyone.");
            binding.loginTypeField.etInput.setHint("Enter your email");
            binding.loginTypeField.etInput.setText("");
            binding.loginTypeField.ivIcon.setImageResource(R.drawable.email_icon1);
            binding.btnPhone.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.paragraph));
            binding.btnEmail.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.widgetColor));
            isPhoneSelected = false;
            otp_channel = "email";
            binding.loginTypeField.etInput.setText(email);
        });


        binding.resendOTP.setOnClickListener(v -> {

            /*JsonObject jsonObjectForResendOtp = new JsonObject();
            jsonObjectForResendOtp.addProperty("user_register_id", user_register_id);

            CommonLogic.showTestLog(TAG, "resendOtp params:- " + jsonObjectForResendOtp);

            // --- Step 2: Make API call ---
            ApiClient.getApiService(VerifyPhoneEmailActivity.this).resendUserRegisterOTP(jsonObjectForResendOtp).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    try {

                        CommonLogic.showTestLog(TAG, "Raw Response Code: " + response.code());
                        CommonLogic.showTestLog(TAG, "Raw Response Body: " + response.body());
                        CommonLogic.showTestLog(TAG, "Raw Error Body: " + (response.errorBody() != null ? response.errorBody().toString() : "null"));

                        JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                        try {

                            boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                            String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

                            Toast.makeText(VerifyPhoneEmailActivity.this, message, Toast.LENGTH_SHORT).show();


                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, e.getMessage());
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


            sendOTPRequests();
        });


        binding.btnSendCode.setOnClickListener(v -> {
            if (binding.btnSendCode.getText().toString().equalsIgnoreCase("Verify")) {

                loadingDialog.setTitle("Verifying OTP");
                loadingDialog.setMessage("Please wait, we are verifying otp");
                loadingDialog.show();

                verify_to = binding.loginTypeField.etInput.getText().toString();


                JsonObject jsonObjectForVerification = new JsonObject();

                if (verificationType.equalsIgnoreCase("createAccount")) {
                    jsonObjectForVerification.addProperty("user_register_id", user_register_id);
                } else if (verificationType.equalsIgnoreCase("otpLogin") || verificationType.equalsIgnoreCase("changePassword")) {
                    jsonObjectForVerification.addProperty("login_via", otp_channel);
                    jsonObjectForVerification.addProperty("value", verify_to);
                } else if (verificationType.equalsIgnoreCase("verify")) {
                    jsonObjectForVerification.addProperty("verify_to", verify_to);
                    jsonObjectForVerification.addProperty("otp_channel", otp_channel);
                }

                jsonObjectForVerification.addProperty("otp", binding.otpView.getOtp());

                CommonLogic.showTestLog(TAG, "registerUser params:- " + jsonObjectForVerification);

                String url = APIData.API_FOLDER + verify_otp_url;

                // Remove starting "/" if present
                if (verify_otp_url.startsWith("/")) {
                    String apiPath = verify_otp_url.substring(1);
                    url = APIData.API_FOLDER + apiPath;
                }

                Call<JsonObject> apiCall = ApiClient.getApiService(VerifyPhoneEmailActivity.this)
                        .verifyRegisterUser(url, jsonObjectForVerification);

                CommonLogic.showTestLog(TAG, "👉 Full API URL: " + apiCall.request().url());
                CommonLogic.showTestLog(TAG, "👉 Method: " + apiCall.request().method());

                // --- Step 2: Make API call ---
                apiCall.enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        CommonLogic.showTestLog(TAG, "btnSendCode Raw Response Code: " + response.code());
                        CommonLogic.showTestLog(TAG, "btnSendCode Raw Response Body: " + response.body());
                        CommonLogic.showTestLog(TAG, "btnSendCode Raw Error Body: " + (response.errorBody() != null ? response.errorBody().toString() : "null"));

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                            // Log full response
                            CommonLogic.showTestLog(TAG, "registerUser FULL response:- " + responseBody.toString());

                            boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                            String message = responseBody.has("message")
                                    ? responseBody.getString("message")
                                    : "Server error, Please try after some time.";

                            // Log status and message
                            CommonLogic.showTestLog(TAG, "registerUser status:- " + status);
                            CommonLogic.showTestLog(TAG, "registerUser message:- " + message);

                            Toast.makeText(VerifyPhoneEmailActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (status) {

                                // Step 2: Extract User JSON
                                JSONObject userJson = responseBody.getJSONObject("user");

                                CommonLogic.showTestLog(TAG, "registerUser userJson:- " + userJson.toString());

                                String token = userJson.has("token") ? userJson.getString("token") : "";
                                CommonLogic.showTestLog(TAG, "registerUser token:- " + token);

                                String userId = CommonMethods.getUserIdFromToken(token);

                                CommonLogic.showTestLog(TAG, "registerUser extracted userId:- " + userId);

                                if (!userId.isEmpty()) {

                                    if (!token.isEmpty()) {
                                        prefs.setAuthToken(token);
                                        CommonLogic.showTestLog(TAG, "registerUser token saved in prefs");
                                    }

                                    prefs.setUserId(userId);
                                    CommonLogic.showTestLog(TAG, "registerUser userId saved in prefs");

                                    CommonMethods.onUserLogin(TAG, userId);
                                    CommonLogic.showTestLog(TAG, "registerUser onUserLogin called");

                                    // Map to User Entity
                                    User userEntity = CommonLogic.parseUserFromJson(TAG, userJson);

                                    prefs.saveUser(userEntity);
                                    CommonLogic.showTestLog(TAG, "registerUser userEntity saved");

                                    boolean isTrackingOn = userJson.has("is_tracking_on") &&
                                            userJson.getBoolean("is_tracking_on");

                                    boolean isNotificationSoundOn = userJson.has("is_notification_sound_on") &&
                                            userJson.getBoolean("is_notification_sound_on");

                                    prefs.setBoolean(PreferencesManager.LIVE_TRACKING, isTrackingOn);
                                    prefs.setBoolean(PreferencesManager.NOTIFICATION_SOUND, isNotificationSoundOn);

                                    CommonLogic.showTestLog(TAG, "registerUser tracking:- " + isTrackingOn);
                                    CommonLogic.showTestLog(TAG, "registerUser notificationSound:- " + isNotificationSoundOn);

                                    CommonLogic.showTestLog(TAG, "registerUser verificationType:- " + verificationType);

                                    if (verificationType.equalsIgnoreCase("createAccount") ||
                                            verificationType.equalsIgnoreCase("verify")) {

                                        loadingDialog.dismiss();

                                        CommonLogic.showTestLog(TAG, "registerUser showing success dialog");

                                        CommonMethods.showSuccessDialog(
                                                VerifyPhoneEmailActivity.this,
                                                verificationType
                                        );
                                    }
                                    else if (verificationType.equalsIgnoreCase("changePassword")) {

                                        loadingDialog.dismiss();

                                        CommonLogic.showTestLog(TAG, "registerUser navigating to ForgotPasswordActivity");

                                        disableHideContentSecureForNextNavigation();

                                        Intent mainPage = new Intent(
                                                VerifyPhoneEmailActivity.this,
                                                ForgotPasswordActivity.class
                                        );

                                        mainPage.putExtra("userId", userId);
                                        startActivity(mainPage);
                                        finishAffinity();
                                    }
                                    else if (verificationType.equalsIgnoreCase("otpLogin")) {

                                        loadingDialog.dismiss();

                                        CommonLogic.showTestLog(TAG, "registerUser navigating to MainActivity");

                                        disableHideContentSecureForNextNavigation();

                                        prefs.setBoolean(PreferencesManager.KEY_IS_LOGGED_IN, true);

                                        Intent intent = new Intent(
                                                VerifyPhoneEmailActivity.this,
                                                MainActivity.class
                                        );

                                        startActivity(intent);
                                        finishAffinity();
                                    }

                                } else {
                                    loadingDialog.dismiss();
                                    CommonLogic.showTestLog(TAG, "registerUser ERROR: userId empty");
                                }

                            } else {
                                loadingDialog.dismiss();
                                CommonLogic.showTestLog(TAG, "registerUser failed status false");
                            }

                        } catch (Exception e) {

                            loadingDialog.dismiss();

                            CommonLogic.showTestLog(TAG, "registerUser EXCEPTION:- " + e.toString());
                            CommonLogic.showTestLog(TAG, "registerUser EXCEPTION MESSAGE:- " + e.getMessage());
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        loadingDialog.dismiss();
                        // ❌ Network or unexpected failure
                        CommonLogic.showTestLog(TAG, "onFailure:- " + t.getMessage());
                    }
                });

                /*if (verificationType.equalsIgnoreCase("createAccount") || verificationType.equalsIgnoreCase("otpLogin")) {



                } else

                if (verificationType.equalsIgnoreCase("verify")) {

                    JsonObject jsonObjectForVerification = new JsonObject();
                    jsonObjectForVerification.addProperty("verify_to", verify_to);
                    jsonObjectForVerification.addProperty("otp_channel", otp_channel);
                    jsonObjectForVerification.addProperty("otp", binding.otpView.getOtp());

                    CommonLogic.showTestLog(TAG, "verifyUser params:- " + jsonObjectForVerification);


                    // --- Step 2: Make API call ---
                    ApiClient.getApiService(VerifyPhoneEmailActivity.this).verifyUserPhoneEmail(APIData.API_FOLDER + verify_otp_url, jsonObjectForVerification).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                            loadingDialog.dismiss();

                            try {

                                JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                                boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                                String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

                                Toast.makeText(VerifyPhoneEmailActivity.this, message, Toast.LENGTH_SHORT).show();

                                if (status) {
                                    try {
                                        // --- Step 2: Extract User JSON ---
                                        JSONObject userJson = responseBody.getJSONObject("user");

                                        if (userJson != null) {
                                            showSuccessDialog();
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
                            CommonLogic.showTestLog(TAG, "onFailure:- Registration failed. Please try again.");
                        }
                    });
                }

                else if (verificationType.equalsIgnoreCase("changePassword")) {

                    JsonObject jsonObjectForgetPassword = new JsonObject();
                    jsonObjectForgetPassword.addProperty("forget_with", verify_to);
                    jsonObjectForgetPassword.addProperty("otp_channel", otp_channel);
                    jsonObjectForgetPassword.addProperty("otp", binding.otpView.getOtp());
                    jsonObjectForgetPassword.addProperty("new_password", newPassword);


                    CommonLogic.showTestLog(TAG, "verifyForget params:- " + jsonObjectForgetPassword);

                    // --- Step 2: Make API call ---
                    ApiClient.getApiService(VerifyPhoneEmailActivity.this).verifyForgetUserPassword(
//                            verify_otp_url
                            "/api/auth/verify-reset-otp", jsonObjectForgetPassword).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            loadingDialog.dismiss();
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                            try {

                                boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                                String message = responseBody.has("message") ? responseBody.getString("message") : "";

                                Toast.makeText(VerifyPhoneEmailActivity.this, message, Toast.LENGTH_SHORT).show();

                                if (status) {
                                    try { 
                                        *//*JSONObject userJson = responseBody.getJSONObject("user");

                                        if (userJson != null) {
                                            // --- Step 3: Map to User Entity ---
                                            User userEntity = parseUserFromJson(userJson);

                                            prefs.saveUser(userEntity);
                                        }*//*

                                        showSuccessDialog();
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
                            CommonLogic.showTestLog(TAG, "onFailure:- Registration failed. Please try again.");
                        }
                    });
                }*/
            } else {
                verify_to = binding.loginTypeField.etInput.getText().toString();
                sendOTPRequests();
            }
        });

    }

    private void sendOTPRequests() {

        verify_to = binding.loginTypeField.etInput.getText().toString();

        if (!verificationType.equalsIgnoreCase("otpLogin")) {

            if (verify_to.isEmpty()) {
                if (!isPhoneSelected) {
                    binding.loginTypeField.etInput.setError("Please enter your email");
                } else {
                    binding.loginTypeField.etInput.setError("Please enter your phone number");
                }
                CommonLogic.focusToWidget(binding.scrollView, binding.loginTypeField.etInput);
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (isPhoneSelected && verify_to.length() != 10) {
                binding.loginTypeField.etInput.setError("Invalid phone number");
                Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        loadingDialog.setTitle("Sending OTP");
        loadingDialog.setMessage("Please wait, we are verifying to send otp");

        if (isPhoneSelected) {
            binding.description.setText("Please enter the OTP received on your registered phone & don’t share your OTP with anyone.");
        } else {
            binding.description.setText("Please enter the OTP received on your registered email & don’t share your OTP with anyone.");
        }

        loadingDialog.setMessage("Please wait, sending OTP… ");

        loadingDialog.show();

        JsonObject requestBody = new JsonObject();
        if (verificationType.equalsIgnoreCase("createAccount")) {
            requestBody.addProperty("first_name", firstName);
            requestBody.addProperty("last_name", lastName);
            requestBody.addProperty("email", email);
            requestBody.addProperty("phone", phone);
            requestBody.addProperty("password", newPassword);
            requestBody.addProperty("otp_channel", otpChannel);
            requestBody.addProperty("hit_type", "register");

            CommonLogic.showTestLog(TAG, "registerUser params:- " + requestBody);

            sendOTP(APIData.USER_REGISTER, requestBody);
        } else {
            requestBody.addProperty("otp_channel", otp_channel);

            /*if (verificationType.equalsIgnoreCase("changePassword")) {
                requestBody.addProperty("forget_with", verify_to);
                CommonLogic.showTestLog(TAG, "registerUser params:- " + requestBody);
                sendOTP(APIData.FORGET_PASSWORD, requestBody);

            } else */

            if (verificationType.equalsIgnoreCase("verify")) {
                requestBody.addProperty("verify_to", verify_to);
                CommonLogic.showTestLog(TAG, "VerifyUserPhoneEmail params:- " + requestBody);
                sendOTP(APIData.USER_VERIFY_EMAIL_PHONE, requestBody);
            }

            else if (verificationType.equalsIgnoreCase("otpLogin") || verificationType.equalsIgnoreCase("changePassword")) {
                requestBody.addProperty("login_via", otp_channel);
                requestBody.addProperty("value", verify_to);
                CommonLogic.showTestLog(TAG, "loginWithOtp params:- " + requestBody);
                sendOTP(APIData.OTP_BASED_LOGIN, requestBody);
            }
        }
    }


    private void sendOTP(String url, JsonObject requestBody) {
        // --- Step 1: Prepare request JSON ---

        // --- Step 2: Make API call ---
        ApiClient.getApiService(VerifyPhoneEmailActivity.this).commonPOSTMethodToHitAllAPIsWithUrlBody(url, requestBody)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);
                            CommonLogic.showTestLog(TAG, "registerUser response:- " + responseBody);
                            boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                            String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

                            Toast.makeText(VerifyPhoneEmailActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (status) {
                                // ⏱ Start timer again
                                startResendOtpTimer(binding.resendOTP);

                                // Go to OTP screen and pass required data

                                if (verificationType.equalsIgnoreCase("createAccount")) {
                                    user_register_id = responseBody.has("user_register_id") ? responseBody.getString("user_register_id") : "";
                                    verify_otp_url = responseBody.has("otp_verify_endpoint") ? responseBody.getString("otp_verify_endpoint") : "";

                                } else if (verificationType.equalsIgnoreCase("changePassword")|| verificationType.equalsIgnoreCase("otpLogin")) {
                                    verify_otp_url = responseBody.has("verify_otp_url") ? responseBody.getString("verify_otp_url") : "";
                                }

                                else if (verificationType.equalsIgnoreCase("verify")) {
                                    verify_otp_url = responseBody.has("otp_verification_endpoint") ? responseBody.getString("otp_verification_endpoint") : "";
                                }

                                setVerifyOTPLayout(true);


                            }else {
                                setVerifyOTPLayout(false);
                                binding.loginTypeField.etInput.setError(message);
                            }
                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "Registration check error:- " + e.getMessage());
                            setVerifyOTPLayout(false);
                        }
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        // ❌ Network or unexpected failure
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, "onFailure:- " + t.getMessage());
                        setVerifyOTPLayout(false);
                    }
                });
    }

    private void setVerifyOTPLayout(boolean isOTPSend){
        if (isOTPSend){
            binding.btnSendCode.setText("Verify");
            binding.resendOTP.setVisibility(View.VISIBLE);
            binding.otpView.setVisibility(View.VISIBLE);
            binding.etPhoneEmailLayout.setVisibility(View.GONE);
            binding.emailPhoneBtnLayout.setVisibility(View.GONE);
        }else {
            binding.btnSendCode.setText("Send Code");
            binding.resendOTP.setVisibility(View.GONE);
            binding.otpView.setVisibility(View.GONE);
            binding.etPhoneEmailLayout.setVisibility(View.VISIBLE);

            if (isPhoneSelected) {
                binding.description.setText("Please verify the number associated with your account & don’t share your OTP with anyone.");
            } else {
                binding.description.setText("Please verify the email associated with your account & don’t share your OTP with anyone.");
            }
        }
    }



    private void startResendOtpTimer(TextView resendOTP) {

        resendOTP.setVisibility(View.VISIBLE);
        resendOTP.setEnabled(false);
        resendOTP.setClickable(false);

        resendOtpTimer = new CountDownTimer(30_000, 1_000) {

            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                resendOTP.setText("Resend OTP in " + seconds + "s");
                resendOTP.setTextColor(Color.GRAY);
            }

            @Override
            public void onFinish() {
                resendOTP.setEnabled(true);
                resendOTP.setClickable(true);
                resendOTP.setText("Resend OTP");
                resendOTP.setTextColor(
                        ContextCompat.getColor(resendOTP.getContext(), R.color.widgetColor)
                );
            }
        }.start();
    }


}