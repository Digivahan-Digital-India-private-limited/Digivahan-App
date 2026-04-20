package com.digivahan.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.User;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterUserRepository {
    private final Context context;
    PreferencesManager prefs;

    public RegisterUserRepository(Context context) {
        this.context = context;
        prefs = new PreferencesManager(context);
    }

    public LiveData<JsonObject> registerUser(String firstName, String lastName, String email,
                                             String phone, String password, String otpChannel, String hit_type) {

        MutableLiveData<JsonObject> result = new MutableLiveData<>();

        // --- Step 1: Prepare request JSON ---
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("first_name", firstName);
        requestBody.addProperty("last_name", lastName);
        requestBody.addProperty("email", email);
        requestBody.addProperty("phone", phone);
        requestBody.addProperty("password", password);
        requestBody.addProperty("otp_channel", otpChannel);
        requestBody.addProperty("hit_type", hit_type);

        // --- Step 2: Make API call ---
        ApiClient.getApiService(context).registerUser(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject responseBody = response.body();

                    boolean status = responseBody.has("status") && responseBody.get("status").getAsBoolean();

                    if (status && hit_type.equalsIgnoreCase("register")) {
                        try {
                            // --- Step 2: Extract User JSON ---
                            JsonObject userJson = responseBody.getAsJsonObject("user");

                            if (userJson != null) {
                                // --- Step 3: Map to User Entity ---
                                User userEntity = parseUserFromJson(userJson);

                                prefs.saveUser(userEntity);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Always return API response to UI
                    result.postValue(responseBody);
                } else {
                    // ❌ Non-200 response
                    JsonObject errorObj = new JsonObject();
                    errorObj.addProperty("status", false);
                    errorObj.addProperty("message", "Registration failed. Please try again.");
                    result.postValue(errorObj);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // ❌ Network or unexpected failure
                JsonObject errorObj = new JsonObject();
                errorObj.addProperty("status", false);
                errorObj.addProperty("message", t.getMessage());
                result.postValue(errorObj);
            }
        });

        return result;
    }

    public LiveData<JsonObject> verifyRegisterUser(String userRegisterId, String OTP) {

        MutableLiveData<JsonObject> result = new MutableLiveData<>();

        // --- Step 1: Prepare request JSON ---
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("temp_user_id", userRegisterId);
        requestBody.addProperty("otp", OTP);

        // --- Step 2: Make API call ---
        ApiClient.getApiService(context).registerUser(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject responseBody = response.body();

                    boolean status = responseBody.has("status") && responseBody.get("status").getAsBoolean();

                    if (status) {
                        try {
                            // --- Step 2: Extract User JSON ---
                            JsonObject userJson = responseBody.getAsJsonObject("user");

                            if (userJson != null) {
                                // --- Step 3: Map to User Entity ---
                                User userEntity = parseUserFromJson(userJson);

                                prefs.saveUser(userEntity);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Always return API response to UI
                    result.postValue(responseBody);
                } else {
                    // ❌ Non-200 response
                    JsonObject errorObj = new JsonObject();
                    errorObj.addProperty("status", false);
                    errorObj.addProperty("message", "Registration failed. Please try again.");
                    result.postValue(errorObj);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // ❌ Network or unexpected failure
                JsonObject errorObj = new JsonObject();
                errorObj.addProperty("status", false);
                errorObj.addProperty("message", t.getMessage());
                result.postValue(errorObj);
            }
        });

        return result;
    }


    private User parseUserFromJson(JsonObject userJson) {
        User user = new User();

        // Extract basic_details
        JsonObject basicDetails = userJson.getAsJsonObject("basic_details");
        JsonObject publicDetails = userJson.getAsJsonObject("public_details");

        if (basicDetails != null) {
            user.setFirst_name(basicDetails.has("first_name") ? basicDetails.get("first_name").getAsString() : "");
            user.setLast_name(basicDetails.has("last_name") ? basicDetails.get("last_name").getAsString() : "");
            user.setEmail(basicDetails.has("email") ? basicDetails.get("email").getAsString() : "");
            user.setPhone_number_verified(basicDetails.has("phone_number_verified") ? basicDetails.get("phone_number_verified").getAsString() : "");
            user.setIs_phone_number_primary(basicDetails.has("is_phone_number_primary") ? basicDetails.get("is_phone_number_primary").getAsString() : "");
            user.setEmail(basicDetails.has("email") ? basicDetails.get("email").getAsString() : "");
            user.setIs_email_verified(basicDetails.has("is_email_verified") ? basicDetails.get("is_email_verified").getAsString() : "");
            user.setIs_email_primary(basicDetails.has("is_email_primary") ? basicDetails.get("is_email_primary").getAsString() : "");
            user.setPassword(basicDetails.has("password") ? basicDetails.get("password").getAsString() : "");
            user.setOccupation(basicDetails.has("occupation") ? basicDetails.get("occupation").getAsString() : "");
            user.setProfile_completion_percent(basicDetails.has("profile_completion_percent") ? basicDetails.get("profile_completion_percent").getAsString() : "");
            user.setPhone_number(basicDetails.has("phone_number") ? basicDetails.get("phone_number").getAsString() : "");
            user.setPassword(basicDetails.has("password") ? basicDetails.get("password").getAsString() : "");
            user.setProfile_pic(basicDetails.has("profile_pic_url") ? basicDetails.get("profile_pic_url").getAsString() : "");
        }


        if (publicDetails != null){
            user.setNick_name(publicDetails.has("nick_name") ? publicDetails.get("nick_name").getAsString() : "");
            user.setAddress(publicDetails.has("address") ? publicDetails.get("address").getAsString() : "");
            user.setAge(publicDetails.has("age") ? publicDetails.get("age").getAsString() : "");
            user.setGender(publicDetails.has("gender") ? publicDetails.get("gender").getAsString() : "");
        }

        // You can extract public_details, address_book, garage, etc. if needed
        // For now, only the basic details are saved

        return user;
    }


}