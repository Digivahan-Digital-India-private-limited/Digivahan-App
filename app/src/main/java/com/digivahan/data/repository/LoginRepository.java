package com.digivahan.data.repository;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.User;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;


import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;

public class LoginRepository {

    String TAG = "LoginRepositoryData";
    private final Context context;
    PreferencesManager prefs;

    public LoginRepository(Context context) {
        this.context = context;
        prefs = new PreferencesManager(context);
    }

    public LiveData<JSONObject> login(String user, String password, boolean isOldUser) {
        MutableLiveData<JSONObject> result = new MutableLiveData<>();

        // --- Step 1: Prepare JSON body ---
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("login_type", isOldUser ? "phone" : "email");
        requestBody.addProperty("login_value", user);
        requestBody.addProperty("password", password);

        ApiClient.getApiService(context).login(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                    boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    if (status) {
                        try {
                            // --- Step 2: Extract User JSON ---
                            JSONObject userJson = responseBody.getJSONObject("user");

                            if (userJson != null) {
                                // --- Step 3: Map to User Entity ---
                                User userEntity = CommonLogic.parseUserFromJson(TAG, userJson);

                                prefs.saveUser(userEntity);
                            }

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }
                    }

                    // Always return API response to UI
                    result.postValue(responseBody);
                } catch (Exception e) {
                    CommonLogic.showTestLog(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                CommonLogic.showTestLog(TAG, "onFailure:- Registration failed. Please try again.");
            }
        });

        return result;
    }


}