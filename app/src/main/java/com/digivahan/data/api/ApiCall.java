package com.digivahan.data.api;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.ashu.ashuutils.NetworkUtils;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface ApiCall {

    public interface ApiResponseCallback {
        void onSuccess(JSONObject responseBody, boolean status, String message);
        void onError(String errorMessage);
    }

    public static Call<JsonObject> callApi(String TAG, Activity context, String url, JsonObject params, String method, ApiResponseCallback callback) {



        Call<JsonObject> call = ApiClient.getApiService(context).commonPOSTMethodToHitAllAPIsWithUrlBody(url, params);

        if (params == null){
            if (method.equalsIgnoreCase("post")) {
                call = ApiClient.getApiService(context).commonPOSTMethodToHitAllAPIsWithUrl(url);
            }else if (method.equalsIgnoreCase("get")){
                call = ApiClient.getApiService(context).commonGETMethodToHitAllAPIs(url);
            }
        }else {
            if (method.equalsIgnoreCase("put")) {
                call = ApiClient.getApiService(context).commonPUTMethodToHitAllAPIsWithUrlBody(url, params);
            }
        }

        CommonLogic.showTestLog(TAG, "FULL API URL: " + call.request().url());

        // ✅ Check internet first
        if (!NetworkUtils.isInternetAvailable(context)) {
            if (callback != null)
                callback.onError("No internet connection");
            return null;
        }


        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                    if (responseBody == null) {
                        if (callback != null)
                            callback.onError("Empty response from server.");
                        return;
                    }

                    boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                    if (responseBody.has("success")){
                        status = responseBody.getBoolean("success");
                    }

                    String message = responseBody.optString("message", "Unknown server message");

                    if (!status && message.equalsIgnoreCase("Access token required")){
                        CommonMethods.logout(context, false);
                    }

                    if (callback != null)
                        callback.onSuccess(responseBody, status, message);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.onError("Response parsing error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (callback != null)
                    callback.onError("Network error: " + t.getMessage());
            }
        });

        return call;
    }
}

