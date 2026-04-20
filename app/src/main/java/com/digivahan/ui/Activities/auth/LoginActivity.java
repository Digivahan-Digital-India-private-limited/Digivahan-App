package com.digivahan.ui.Activities.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.PasswordValidationResult;
import com.digivahan.data.model.User;
import com.digivahan.data.repository.RegisterUserRepository;
import com.digivahan.databinding.ActivityLoginBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.ui.Activities.infoPages.PrivacyPolicyPage;
import com.digivahan.ui.Activities.infoPages.TermsConditionPage;
import com.digivahan.ui.Activities.infoPages.WebViewerActivity;
import com.digivahan.ui.Activities.verification.VerifyPhoneEmailActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    String TAG = "LoginActivityData";
    ActivityLoginBinding binding;

    private LoginViewModel loginViewModel;
    private RegisterUserRepository registerUserRepository;

    private boolean isOldUser = true, isLoginWithOTP = true;

    boolean isEmailLogin = false;

    PreferencesManager prefs;


    AshDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        initializeData();

        setLoginData();

        setEvents();

        CommonLogic.checkAppVersion(TAG, LoginActivity.this);


    }

    @SuppressLint("ClickableViewAccessibility")
    private void setEvents() {
        binding.btnOldUser.setOnClickListener(v -> setOldUser());
        binding.btnNewUser.setOnClickListener(v -> setNewUser());

        binding.scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                CommonLogic.hideKeyboard(LoginActivity.this);
                v.requestFocus(); // move focus away from EditText
            }
            return false;
        });


        binding.btnLogin.setOnClickListener(v -> {
            if (isOldUser) {
                String user = binding.loginTypeField.etInput.getText().toString().trim();

                if (isLoginWithOTP){

                    String userNumber = binding.userNumber.getText().toString().trim();

                    if (userNumber.isEmpty()) {
                        binding.userNumber.setError("Please enter your phone number");
                        Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (userNumber.length() != 10) {
                        binding.userNumber.setError("Invalid phone number");
                        Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent mainPage = new Intent(LoginActivity.this, VerifyPhoneEmailActivity.class);
                    mainPage.putExtra("phone", userNumber);
                    mainPage.putExtra("verificationType", "otpLogin");
                    startActivity(mainPage);

                    return;
                }

                String password = binding.loginPasswordField.etInput.getText().toString().trim();


                if (user.isEmpty()) {
                    if (isEmailLogin) {
                        binding.loginTypeField.etInput.setError("Please enter your email");
                    } else {
                        binding.loginTypeField.etInput.setError("Please enter your phone number");
                    }
                    Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEmailLogin && !CommonLogic.isValidEmail(user)) {
                    binding.loginTypeField.etInput.setError("Invalid email");
                    Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isEmailLogin && user.length() != 10) {
                    binding.loginTypeField.etInput.setError("Invalid phone number");
                    Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (password.isEmpty()) {
                    binding.loginPasswordField.etInput.setError("Please enter your password");
                    Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                prefs.setBoolean(PreferencesManager.KEY_IS_LOGGED_IN, true);
                startActivity(intent);
                finish();*/

                JsonObject jsonObjectLogin = new JsonObject();
                jsonObjectLogin.addProperty("login_type", isEmailLogin ? "email" : "phone");
                jsonObjectLogin.addProperty("login_value", user);
                jsonObjectLogin.addProperty("password", password);

                loadingDialog.show();

                CommonLogic.showTestLog(TAG, "Login params:- " + jsonObjectLogin);

                ApiCall.callApi(TAG, LoginActivity.this, APIData.LOGIN, jsonObjectLogin, "post", new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            loadingDialog.dismiss();
                            if (status) {
                                // --- Step 2: Extract User JSON ---
                                JSONObject userJson = responseBody.getJSONObject("user");

                                if (userJson != null) {

                                    String userId = CommonMethods.getUserIdFromToken(userJson.getString("token"));

                                    if (userId != null && !userId.isEmpty()) {
                                        if (userJson.has("token") && !userJson.getString("token").isEmpty()) {
                                            prefs.setAuthToken(userJson.getString("token"));
                                            CommonLogic.showTestLog(TAG, "access token: " + prefs.getAuthToken());

                                            prefs.setUserId(userId);

                                            CommonMethods.onUserLogin(TAG, userId);

                                            // --- Step 3: Map to User Entity ---
                                            User userEntity = CommonLogic.parseUserFromJson(TAG, userJson);

//                                            CommonLogic.addAndRemoveDeviceData(TAG, LoginActivity.this, prefs.getUserId(), "add", Objects.requireNonNull(OneSignal.getDeviceState()).getUserId());

                                            prefs.saveUser(userEntity);

                                            prefs.setBoolean(PreferencesManager.NOTIFICATION_SOUND, userJson.has("is_notification_sound_on") &&
                                                    userJson.getBoolean("is_notification_sound_on"));

                                            prefs.setBoolean(PreferencesManager.LIVE_TRACKING, userJson.has("is_tracking_on") &&
                                                    userJson.getBoolean("is_tracking_on"));

                                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            prefs.setBoolean(PreferencesManager.KEY_IS_LOGGED_IN, true);
                                            startActivity(intent);
                                            finish();
                                        }else {
                                            Toast.makeText(LoginActivity.this, "Unable to login, Please try after some time", Toast.LENGTH_SHORT).show();
                                        }

                                    }else {
                                        binding.loginTypeField.etInput.setError("User Not Found");
                                        Toast.makeText(LoginActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                                    }
                                }else {Toast.makeText(LoginActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                                    binding.loginTypeField.etInput.setError("User Not Found");}
                            }else {Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                if (responseBody.has("error_type") && responseBody.getString("error_type").equalsIgnoreCase("phone")){
                                    binding.loginTypeField.etInput.setError(message);
                                }else if (responseBody.has("error_type") && responseBody.getString("error_type").equalsIgnoreCase("password")){
                                    binding.loginPasswordField.etInput.setError(message);
                                }else {
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            CommonLogic.showTestLog(TAG, "onFailure:- "+e);
                            loadingDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                            binding.loginTypeField.etInput.setError("User Not Found");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(LoginActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                        CommonLogic.showTestLog(TAG, "onFailure:- Registration failed. Please try again.");
                    }
                });

            } else {
                String firstName = binding.firstNameField.etInput.getText().toString().trim();
                String lastName = binding.lastNameField.etInput.getText().toString().trim();
                String email = binding.emailField.etInput.getText().toString().trim();
                String phone = binding.phoneField.etInput.getText().toString().trim();
                String password = binding.passwordField.etInput.getText().toString().trim();
                String confirmPassword = binding.passwordConfirmField.etInput.getText().toString().trim();
                String otpChannel = "phone"; //email or "phone"
                String hit_type = "check"; // or "phone"

                PasswordValidationResult result =
                        CommonLogic.validatePassword(password, 3);

                // Basic validation
                if (firstName.isEmpty()) {
                    binding.firstNameField.etInput.setError("Please enter your first name");
                    CommonLogic.focusToWidget(binding.scrollView, binding.firstNameField.etInput);
                    // Smooth scroll to the field
                    Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!CommonLogic.isValidName(firstName)) {
                    binding.firstNameField.etInput.setError(getString(R.string.name_length_error));
                    CommonLogic.focusToWidget(binding.scrollView, binding.firstNameField.etInput);
                    Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
                    return;
                } else if (lastName.isEmpty()) {
                    binding.lastNameField.etInput.setError("Please enter your last name");
                    CommonLogic.focusToWidget(binding.scrollView, binding.lastNameField.etInput);
                    Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!CommonLogic.isValidName(lastName)) {
                    binding.lastNameField.etInput.setError(getString(R.string.name_length_error));
                    CommonLogic.focusToWidget(binding.scrollView, binding.lastNameField.etInput);
                    Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
                    return;
                } else if (email.isEmpty()) {
                    binding.emailField.etInput.setError("Please enter your email");
                    CommonLogic.focusToWidget(binding.scrollView, binding.emailField.etInput);
                    Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!CommonLogic.isValidEmail(email)) {
                    binding.emailField.etInput.setError("Invalid email");
                    CommonLogic.focusToWidget(binding.scrollView, binding.emailField.etInput);
                    Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                    return;
                } else if (phone.isEmpty()) {
                    binding.phoneField.etInput.setError("Please enter your phone number");
                    CommonLogic.focusToWidget(binding.scrollView, binding.phoneField.etInput);
                    Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!CommonLogic.isValidPhone(phone)) {
                    binding.phoneField.etInput.setError("Invalid phone number");
                    CommonLogic.focusToWidget(binding.scrollView, binding.phoneField.etInput);
                    Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                } else if (password.isEmpty()) {
                    binding.passwordField.etInput.setError("Please enter your password");
//                    CommonLogic.focusToWidget(binding.scrollView, binding.passwordField.etInput);
                    Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!result.isValid) {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
//                    CommonLogic.focusToWidget(binding.scrollView, binding.passwordField.etInput);
                    binding.passwordField.etInput.setError(result.message);
                    return;
                } else if (confirmPassword.isEmpty()) {
                    binding.passwordConfirmField.etInput.setError("Please enter your password");
//                    CommonLogic.focusToWidget(binding.scrollView, binding.passwordConfirmField.etInput);
                    Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!password.equals(confirmPassword)) {
                    binding.passwordField.etInput.setError("Password does not match");
                    binding.passwordConfirmField.etInput.setError("Password does not match");
                    Toast.makeText(this, "password not match", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!binding.rbAgree.isChecked()) {
                    Toast.makeText(this, "Please agree terms and condition", Toast.LENGTH_SHORT).show();
                    return;
                }



                // --- Step 1: Prepare request JSON ---
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("first_name", firstName);
                requestBody.addProperty("last_name", lastName);
                requestBody.addProperty("email", email);
                requestBody.addProperty("phone", phone);
                requestBody.addProperty("password", password);
                requestBody.addProperty("otp_channel", otpChannel);
                requestBody.addProperty("hit_type", hit_type);

                loadingDialog.show();

                CommonLogic.showTestLog(TAG, "registerUser params:- " + requestBody.toString());

                // --- Step 2: Make API call ---
                ApiClient.getApiService(LoginActivity.this).commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.CHECK_USER_REGISTER, requestBody).enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                            CommonLogic.showTestLog(TAG, "registerUser response:- " + responseBody);

                            boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                            String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

                            if (status) {
                                // Go to OTP screen and pass required data
                                Intent mainPage = new Intent(LoginActivity.this, VerifyPhoneEmailActivity.class);
                                mainPage.putExtra("firstName", firstName);
                                mainPage.putExtra("lastName", lastName);
                                mainPage.putExtra("email", email);
                                mainPage.putExtra("phone", phone);
                                mainPage.putExtra("newPassword", password);
                                mainPage.putExtra("otpChannel", otpChannel);
                                mainPage.putExtra("hit_type", hit_type);
                                mainPage.putExtra("verificationType", "createAccount");
                                startActivity(mainPage);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                                String error_type = responseBody.has("error_type") ? responseBody.getString("error_type") : "Server error, Please try after some time.";
                                if (error_type.equalsIgnoreCase("email")) {
                                    binding.emailField.etInput.setError(message);
                                } else if (error_type.equalsIgnoreCase("phone")) {
                                    binding.phoneField.etInput.setError(message);
                                }
                            }
                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "Registration check error:- " + e.getMessage());
                        }

                        // Always dismiss loader
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        // Always dismiss loader
                        loadingDialog.dismiss();

                        // ❌ Network or unexpected failure
                        CommonLogic.showTestLog(TAG, "onFailure:- " + t.getMessage());
                    }
                });
            }

        });

        binding.btnLoginWithOTP.setOnClickListener(view -> {
            if (isLoginWithOTP){
                binding.loginWithOtpLayout.setVisibility(View.GONE);
                binding.loginWithPasswordLayout.setVisibility(View.VISIBLE);
                binding.btnLoginWithOTP.setText("Login With OTP");
            }else {
                binding.loginWithOtpLayout.setVisibility(View.VISIBLE);
                binding.loginWithPasswordLayout.setVisibility(View.GONE);
                binding.btnLoginWithOTP.setText("Login With Password");
            }

            isLoginWithOTP = !isLoginWithOTP;
        });

        binding.forgotPassword.setOnClickListener(v -> {
            Intent mainPage = new Intent(LoginActivity.this, VerifyPhoneEmailActivity.class);
            mainPage.putExtra("verificationType", "changePassword");
            startActivity(mainPage);
            finish();
        });

        binding.changeLoginType.setOnClickListener(v -> {
            binding.loginTypeField.etInput.setText("");
            if (isEmailLogin) {
                binding.changeLoginType.setText("Login with email");
                binding.changeLoginType.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.email_icon2, 0, 0, 0);
                binding.loginTypeText.setText("Phone Number");
                binding.loginTypeField.etInput.setHint("Phone Number");
                binding.loginTypeField.ivIcon.setImageResource(R.drawable.call_icon2);
                CommonLogic.setupField(binding.loginTypeField, "phone", null);
            } else {
                binding.changeLoginType.setText("Login with number");
                binding.changeLoginType.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.call_icon1, 0, 0, 0);
                binding.loginTypeText.setText("Email Address");
                binding.loginTypeField.etInput.setHint("Enter your email");
                binding.loginTypeField.ivIcon.setImageResource(R.drawable.email_icon1);
                CommonLogic.setupField(binding.loginTypeField, "email", null);
            }
            isEmailLogin = !isEmailLogin;
        });


        binding.passwordField.etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 🔹 Remove error from confirm password field
                binding.passwordField.etInput.setError(null);
                binding.passwordConfirmField.etInput.setError(null); // if using TextInputLayout
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        binding.passwordConfirmField.etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 🔹 Remove error from confirm password field
                binding.passwordField.etInput.setError(null);
                binding.passwordConfirmField.etInput.setError(null); // if using TextInputLayout
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });
    }

    private void setLoginData() {
        String text = "I Agree to Terms & Condition and Privacy Policy.";
        SpannableString ss = new SpannableString(text);

// Bold
        ss.setSpan(new StyleSpan(Typeface.BOLD), 10, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Terms
        ss.setSpan(new StyleSpan(Typeface.BOLD), 19, 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Condition
        ss.setSpan(new StyleSpan(Typeface.BOLD), 33, 47, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // privacy policy

// Clickable
        ClickableSpan termsClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Open Terms activity / WebView
                Intent termConditionPage = new Intent(LoginActivity.this, WebViewerActivity.class);
                termConditionPage.putExtra("extra_policy_type", "terms_condition");
                startActivity(termConditionPage);
            }
        };
        ss.setSpan(termsClick, 10, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan condClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Open Condition page
                Intent termConditionPage = new Intent(LoginActivity.this, WebViewerActivity.class);
                termConditionPage.putExtra("extra_policy_type", "terms_condition");
                startActivity(termConditionPage);
            }
        };
        ss.setSpan(condClick, 19, 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan privacyClick = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Open Privacy Policy page
                Intent privacyPolicyPage = new Intent(LoginActivity.this, WebViewerActivity.class);
                privacyPolicyPage.putExtra("extra_policy_type", "privacy_policy");
                startActivity(privacyPolicyPage);
            }
        };
        ss.setSpan(privacyClick, 33, 47, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvTerms.setText(ss);
        binding.tvTerms.setMovementMethod(LinkMovementMethod.getInstance());

        CommonLogic.setUserInPutFiledData(binding.loginPasswordField, R.drawable.password_icon1, getString(R.string.password_hint), "password", true, binding.scrollView, binding.btnLogin);

        // comment it when it start email otp.
        binding.changeLoginType.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.email_icon2, 0, 0, 0);
        binding.changeLoginType.setText("Login with email");
        binding.loginTypeText.setText("Phone Number");
        CommonLogic.setUserInPutFiledData(binding.loginTypeField, R.drawable.call_icon2, "Phone Number", "phone", true, binding.scrollView, binding.btnLogin);


        CommonLogic.setUserInPutFiledData(binding.firstNameField, R.drawable.profile_icon1, "First Name", "name", false, binding.scrollView, binding.btnLogin);
        CommonLogic.setUserInPutFiledData(binding.lastNameField, R.drawable.profile_icon1, "Last Name", "name", false, binding.scrollView, binding.btnLogin);
        CommonLogic.setUserInPutFiledData(binding.emailField, R.drawable.email_icon1, "Email", "email", false, binding.scrollView, binding.btnLogin);
        CommonLogic.setUserInPutFiledData(binding.phoneField, R.drawable.call_icon2, "Phone Number", "phone", true, binding.scrollView, binding.btnLogin);
        CommonLogic.setUserInPutFiledData(binding.passwordField, R.drawable.password_icon1, getString(R.string.password_hint), "password", true, binding.scrollView, binding.btnLogin);
        CommonLogic.setUserInPutFiledData(binding.passwordConfirmField, R.drawable.password_icon1, "Confirm Password", "password", true, binding.scrollView, binding.btnLogin);

    }

    private void initializeData() {
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        registerUserRepository = new RegisterUserRepository(this);
        prefs = new PreferencesManager(this);
        loadingDialog = new AshDialog(LoginActivity.this, "Please wait", "login user...");
    }

    private void setOldUser() {
        CommonLogic.hideKeyboard(LoginActivity.this);

        isOldUser = true;
        isEmailLogin = false;
        binding.btnOldUser.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_button));
        binding.btnNewUser.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.unselected_1));
        binding.loginLayout.setVisibility(View.VISIBLE);
        binding.loginWithOtpBtnLayout.setVisibility(View.VISIBLE);
        binding.termBtnLayout.setVisibility(View.GONE);
        binding.registerLayout.setVisibility(View.GONE);

        binding.btnLogin.setText("Login");
        binding.changeLoginType.setText("Login with email");
        binding.changeLoginType.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.email_icon2, 0, 0, 0);

        binding.loginTypeText.setText("Phone Number");
        binding.loginTypeField.etInput.setHint("Phone Number");
        binding.loginTypeField.ivIcon.setImageResource(R.drawable.call_icon2);
    }

    private void setNewUser() {
        CommonLogic.hideKeyboard(LoginActivity.this);

        isOldUser = false;
        binding.btnNewUser.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_button));
        binding.btnOldUser.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.unselected_1));
        binding.loginLayout.setVisibility(View.GONE);
        binding.termBtnLayout.setVisibility(View.VISIBLE);
        binding.registerLayout.setVisibility(View.VISIBLE);
        binding.loginWithOtpBtnLayout.setVisibility(View.GONE);
        binding.btnLogin.setText("Create Account");
    }

}
