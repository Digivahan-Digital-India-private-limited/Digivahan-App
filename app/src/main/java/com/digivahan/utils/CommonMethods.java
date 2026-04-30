package com.digivahan.utils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.SavedImageData;
import com.digivahan.data.model.User;
import com.digivahan.databinding.ProfileIncompleteDialogDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.ui.Activities.auth.LoginActivity;
import com.digivahan.ui.Activities.garage.CheckChallan;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.digivahan.ui.Activities.garage.MyGarageActivity;
import com.digivahan.ui.Activities.garage.VehicleInformation;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface CommonMethods {

    String TAG = "CommonMethodsData";

    public interface ImageUploadCallback {
        void onUploadJSON(JSONObject response);

        void onUploadSuccess(SavedImageData uploadedImage);

        void onUploadError(String errorMessage);
    }

    public static void uploadProfileImage(Context context, File publicImageFile, String publicImagePath, ImageUploadCallback callback) {
        if (publicImageFile == null) {
            if (callback != null)
                callback.onUploadError("No image file selected.");
            return;
        }

        MultipartBody.Part profile_pic = null;
        try {

            /*RequestBody requestDlFile = RequestBody.create(publicImageFile, MediaType.parse(publicImagePath.substring(publicImagePath.lastIndexOf(".") + 1)));
            profile_pic = MultipartBody.Part.createFormData("file", publicImagePath.substring(publicImagePath.lastIndexOf("/") + 1), requestDlFile);*/

            RequestBody requestBody = RequestBody.create(publicImageFile, MediaType.parse("image/*"));
            profile_pic = MultipartBody.Part.createFormData("file", publicImageFile.getName(), requestBody);
        } catch (Exception e) {
            if (callback != null)
                callback.onUploadError("Failed to prepare image: " + e.getMessage());
            return;
        }

        ApiClient.getApiService(context, true).uploadProfileImage(profile_pic).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                try {
                    JSONObject responseBody = APIHelper.getResponseData("UploadImageAPI", response, Constants.ENABLE_TESTING);

                    boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                    String message = responseBody.optString("message", "Unknown error");

                    if (status) {
                        JSONObject userJson = responseBody.getJSONObject("data");
                        SavedImageData uploadedImage = APIHelper.convertJsonToModel(userJson, SavedImageData.class);
                        if (callback != null) callback.onUploadSuccess(uploadedImage);
                    } else {
                        if (callback != null) callback.onUploadError(message);
                    }

                    callback.onUploadJSON(responseBody);
                } catch (Exception e) {
                    if (callback != null)
                        callback.onUploadError("Parsing error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (callback != null)
                    callback.onUploadError("Upload failed: " + t.getMessage());
            }
        });
    }

    public static void uploadSingleImage(Context context, File file, String filePath, String folderName, ImageUploadCallback callback) {
        String TAG = "UploadImageProcess";

        Log.i(TAG, "🚀 Starting image upload...");
        Log.i(TAG, "📁 Folder Name: " + folderName);
        Log.i(TAG, "📄 File Path: " + filePath);
        Log.i(TAG, "📦 File Object: " + (file != null ? file.getAbsolutePath() : "null"));

        if (file == null || !file.exists()) {
            Log.e(TAG, "❌ File is null or does not exist. Cannot upload.");
            if (callback != null)
                callback.onUploadError("No image file selected or file missing.");
            return;
        }

        MultipartBody.Part fileBodyRequest;
        try {
            // Extract file extension safely
            String extension = "";
            int dotIndex = filePath.lastIndexOf(".");
            if (dotIndex > 0) extension = filePath.substring(dotIndex + 1).toLowerCase();

            // Detect proper MIME type
            String mimeType = extension.equals("png") ? "image/png"
                    : extension.equals("jpg") || extension.equals("jpeg") ? "image/jpeg"
                    : "application/octet-stream";

            Log.i(TAG, "🧩 File Extension: " + extension + " | MIME Type: " + mimeType);

            // Prepare Multipart request
            RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));
            fileBodyRequest = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            Log.i(TAG, "✅ Multipart file prepared successfully: " + file.getName());
            Log.d(TAG, "🔍 Upload Details -> FileName: " + file.getName() + ", Size: " + file.length() / 1024 + " KB");

        } catch (Exception e) {
            Log.e(TAG, "🔥 Failed to prepare multipart: " + e.getMessage(), e);
            if (callback != null)
                callback.onUploadError("Failed to prepare image: " + e.getMessage());
            return;
        }

        // Folder name field
        RequestBody folderNameRequest = RequestBody.create(MediaType.parse("text/plain"), folderName);

        Log.i(TAG, "🌐 Calling upload API...");
        ApiClient.getApiService(context, true)
                .uploadSingleFile(folderNameRequest, fileBodyRequest)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        Log.i(TAG, "📬 API Response received: " + response);

                        // ✅ Handle HTTP error codes first
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "❌ Server returned error code: " + response.code());

                            try {
                                String errorBody = response.errorBody() != null
                                        ? response.errorBody().string()
                                        : "No error body";

                                Log.d(TAG, "🧾 Error Body: " + errorBody);

                                if (errorBody.startsWith("<!DOCTYPE")) {
                                    if (callback != null)
                                        callback.onUploadError("Server error (HTML page returned, code " + response.code() + ")");
                                    return;
                                }

                                JSONObject errorJson = new JSONObject();
                                errorJson.put("status", false);
                                errorJson.put("message", "Server error " + response.code());

                                if (callback != null)
                                    callback.onUploadError("Server Error: " + errorJson.toString());

                            } catch (Exception ex) {
                                Log.e(TAG, "🔥 Failed to parse error body: " + ex.getMessage(), ex);
                                if (callback != null)
                                    callback.onUploadError("Unexpected server error (" + response.code() + ")");
                            }
                            return;
                        }

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                            Log.i(TAG, "📦 Raw Response: " + responseBody);

                            boolean status = responseBody.optBoolean("status", false);
                            if (responseBody.optBoolean("success", false)) {
                                status = responseBody.optBoolean("success", false);
                            }
                            String message = responseBody.optString("message", "Unknown error");

                            if (status) {
                                JSONObject data = responseBody.optJSONObject("data");
                                if (data != null) {
                                    SavedImageData uploadedImage = APIHelper.convertJsonToModel(data, SavedImageData.class);
                                    Log.i(TAG, "✅ Upload success. Image ID: " + uploadedImage.getPublic_id());
                                    if (callback != null)
                                        callback.onUploadSuccess(uploadedImage);
                                } else {
                                    Log.w(TAG, "⚠️ Upload succeeded but data is null.");
                                    if (callback != null)
                                        callback.onUploadError("Upload success but no data returned.");
                                }
                            } else {
                                Log.e(TAG, "❌ Upload failed. Server message: " + message);
                                if (callback != null)
                                    callback.onUploadError(message);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "🔥 Exception parsing upload response: " + e.getMessage(), e);
                            if (callback != null)
                                callback.onUploadError("Parsing error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Log.e(TAG, "💔 Upload failed: " + t.getMessage(), t);
                        if (callback != null)
                            callback.onUploadError("Upload failed: " + t.getMessage());
                    }
                });
    }

    public static void uploadDocument(
            Context context,
            File file,
            String filePath,
            String userId,
            String vehicleId,
            String docType,
            String docName,
            String docNumber,
            ImageUploadCallback callback
    ) {
        String TAG = "UploadDocumentProcess";

        Log.i(TAG, "🚀 Starting document upload...");
        Log.i(TAG, "📄 File Path: " + filePath);
        Log.i(TAG, "📦 File Object: " + (file != null ? file.getAbsolutePath() : "null"));

        // Validate file
        if (file == null || !file.exists()) {
            Log.e(TAG, "❌ File is null or does not exist. Cannot upload.");
            if (callback != null)
                callback.onUploadError("No document selected or file missing.");
            return;
        }

        try {
            // Detect file extension & MIME type
            String extension = "";
            int dotIndex = filePath.lastIndexOf(".");
            if (dotIndex > 0) extension = filePath.substring(dotIndex + 1).toLowerCase();

            String mimeType = extension.equals("png") ? "image/png"
                    : (extension.equals("jpg") || extension.equals("jpeg")) ? "image/jpeg"
                    : (extension.equals("pdf")) ? "application/pdf"
                    : "application/octet-stream";

            Log.i(TAG, "🧩 File Extension: " + extension + " | MIME Type: " + mimeType);

            // Prepare file request
            RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));
            MultipartBody.Part fileBodyRequest =
                    MultipartBody.Part.createFormData("doc_file", file.getName(), requestFile);

            Log.i(TAG, "✅ Multipart file prepared successfully: " + file.getName());
            Log.d(TAG, "🔍 Upload Details -> FileName: " + file.getName() + ", Size: " + file.length() / 1024 + " KB");

            // 🧾 Prepare text fields
            RequestBody userIdRequest = RequestBody.create(MediaType.parse("text/plain"), userId);
            RequestBody vehicleIdRequest = RequestBody.create(MediaType.parse("text/plain"), vehicleId);
            RequestBody docTypeRequest = RequestBody.create(MediaType.parse("text/plain"), docType);
            RequestBody docNameRequest = RequestBody.create(MediaType.parse("text/plain"), docName);
            RequestBody docNumberRequest = RequestBody.create(MediaType.parse("text/plain"), docNumber);

            // 🌐 Log all request data
            Log.i(TAG, "👤 user_id: " + userId);
            Log.i(TAG, "🚗 vehicle_id: " + vehicleId);
            Log.i(TAG, "📃 doc_type: " + docType);
            Log.i(TAG, "📄 doc_name: " + docName);

            // 🟡 Call API
            Log.i(TAG, "🌐 Calling upload document API...");

            Call<JsonObject> uploadFileCall = ApiClient.getApiService(context, true)
                    .uploadUserVehicleFile(userIdRequest, vehicleIdRequest, docTypeRequest, docNameRequest, docNumberRequest, fileBodyRequest);


            uploadFileCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    Log.i(TAG, "📬 API Response received: " + response);

                    if (!response.isSuccessful()) {
                        JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                        Log.e(TAG, "❌ Server returned error code: " + response.code());
                        try {
                            String errorBody = response.errorBody() != null
                                    ? response.errorBody().string()
                                    : "No error body";
                            Log.d(TAG, "🧾 Error Body: " + errorBody);

                            if (errorBody.startsWith("<!DOCTYPE")) {
                                if (callback != null)
                                    callback.onUploadError("Server error (HTML page returned, code " + response.code() + ")");
                                return;
                            }

                            if (responseBody.has("success") && !responseBody.getBoolean("success")) {
                                callback.onUploadError(responseBody.optString("message", "Unknown error"));
                            } else {
                                callback.onUploadError("Server error: " + errorBody);
                            }

                        } catch (Exception ex) {
                            Log.e(TAG, "🔥 Failed to parse error body: " + ex.getMessage(), ex);
                            if (callback != null)
                                callback.onUploadError("Unexpected server error (" + response.code() + ")");
                        }
                        return;
                    }

                    try {
                        JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                        Log.i(TAG, "📦 Raw Response: " + responseBody);

                        boolean status = responseBody.optBoolean("success", false);
                        String message = responseBody.optString("message", "Unknown error");

//                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                        if (callback != null) {
                            callback.onUploadJSON(responseBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "🔥 Exception parsing upload response: " + e.getMessage(), e);
                        if (callback != null)
                            callback.onUploadError("Parsing error: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    Log.e(TAG, "💔 Upload failed: " + t.getMessage(), t);
                    if (callback != null)
                        callback.onUploadError("Upload failed: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "🔥 Unexpected exception: " + e.getMessage(), e);
            if (callback != null)
                callback.onUploadError("Unexpected error: " + e.getMessage());
        }
    }


    public interface DeleteProfileImageListener {
        void onDeleteSuccess(String message);

        void onDeleteFailed(String message);
    }

    public static void deleteDocument(String TAG, Context context, String file_id, DeleteProfileImageListener listener) {
        try {
            JsonObject jsonObjectDeleteImage = new JsonObject();
            jsonObjectDeleteImage.addProperty("public_id", file_id);

            // 🟢 Step 1: Log request initialization
            CommonLogic.showTestLog(TAG, "🚀 Starting deleteProfileImage request...");
            CommonLogic.showTestLog(TAG, "📎 File ID: " + file_id);

            // 🟡 Step 2: Make the API call
            ApiClient.getApiService(context)
                    .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.DELETE_SINGLE_FILE, jsonObjectDeleteImage)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            try {
                                // 🟢 Step 3: Log raw response
                                CommonLogic.showTestLog(TAG, "📬 Response received: " + response);

                                JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                                CommonLogic.showTestLog(TAG, "📦 Parsed Response: " + responseBody);

                                boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                                if (responseBody.has("success")) {
                                    status = responseBody.getBoolean("success");
                                }

                                String message = responseBody.has("message")
                                        ? responseBody.getString("message")
                                        : "Server error, please try again later.";

                                // 🧭 Step 4: Log status and message
                                CommonLogic.showTestLog(TAG, "✅ Status: " + status + " | Message: " + message);

                                if (status) {
                                    // ✅ Notify success
                                    CommonLogic.showTestLog(TAG, "🗑️ Image deleted successfully from server.");
                                    if (listener != null) listener.onDeleteSuccess(message);
                                } else {
                                    // ❌ Notify failure
                                    CommonLogic.showTestLog(TAG, "⚠️ Failed to delete image on server. Message: " + message);
                                    if (listener != null) listener.onDeleteFailed(message);
                                }

                            } catch (Exception e) {
                                // 🔥 Step 5: Handle and log parsing errors
                                CommonLogic.showTestLog(TAG, "🔥 Exception while parsing response: " + e.getMessage());
                                if (listener != null)
                                    listener.onDeleteFailed("Error: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            // 🔴 Step 6: Handle network failure
                            String msg = "💔 Network failure. Unable to delete image: " + t.getMessage();
                            CommonLogic.showTestLog(TAG, msg);
                            if (listener != null) listener.onDeleteFailed(msg);
                        }
                    });

        } catch (Exception e) {
            // 🔥 Step 7: Catch and log unexpected exceptions
            CommonLogic.showTestLog(TAG, "💥 Unexpected error: " + e.getMessage());
            if (listener != null) listener.onDeleteFailed("Unexpected error: " + e.getMessage());
        }
    }


    public static void loadImage(String TAG, Context context, String imageUrl, ImageView imageView, int tempImage) {
        try {
            // Clear any previous image to prevent overlap
            Glide.with(context).clear(imageView);
            imageView.setImageResource(tempImage);

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(tempImage)
                    .error(tempImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()       // 👈 disables fade animation
                    .dontTransform()     // 👈 ensures clean replacement
                    .centerCrop();

            Glide.with(context)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                Target<Drawable> target,
                                boolean isFirstResource
                        ) {
                            Log.e(TAG, "❌ Failed to load image: " + imageUrl, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                Drawable resource,
                                Object model,
                                Target<Drawable> target,
                                DataSource dataSource,
                                boolean isFirstResource
                        ) {
                            Log.d(TAG, "✅ Image loaded successfully from: " + dataSource.name());
                            return false;
                        }
                    })
                    .into(imageView);

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Exception while loading image: " + imageUrl, e);
        }
    }


    public static void setInfiniteAutoScroll(Context context,
                                             RecyclerView recyclerView,
                                             LinearLayoutManager layoutManager,
                                             int dataSize,
                                             long autoScrollDelay,
                                             Handler handler,
                                             Runnable runnable, SnapHelper snapHelper) {

        if (dataSize <= 0) return;

        recyclerView.setLayoutManager(layoutManager);

        // Start roughly in the middle for infinite illusion
        int middle = Integer.MAX_VALUE / 2;
        int startPosition = middle - (middle % dataSize);
        layoutManager.scrollToPosition(startPosition);

        // Add SnapHelper
        if (snapHelper == null) {
            snapHelper = new PagerSnapHelper();
        }
        try {
            snapHelper.attachToRecyclerView(recyclerView);
        } catch (IllegalStateException ignored) {
        }

        // Initialize handler and runnable
        handler = new Handler();
        Handler finalHandler = handler;

        runnable = new Runnable() {
            @Override
            public void run() {
                if (recyclerView.getLayoutManager() == null) return; // safety

                int nextItem = layoutManager.findFirstVisibleItemPosition() + 1;

                // Smooth scroll safely
                try {
                    SlowLinearSmoothScroller scroller = new SlowLinearSmoothScroller(context);
                    scroller.setTargetPosition(nextItem);
                    layoutManager.startSmoothScroll(scroller);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                finalHandler.postDelayed(this, autoScrollDelay);
            }
        };

        // 🛡️ Start only after RecyclerView is attached to window
        Runnable finalRunnable = runnable;
        recyclerView.post(() -> finalHandler.postDelayed(finalRunnable, autoScrollDelay));


        /*
        also add this code in onBindViewHolder in your adapter.
        int actualPosition = position % dataList.size();
        YourModel model = dataList.get(actualPosition);

        @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }
        */
    }


    /**
     * Stops auto-scrolling if needed (to call in onPause or onDestroy)
     */
    public static void stopAutoScroll(Handler handler, Runnable runnable) {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    // ✅ Start or resume auto scrolling
    public static void startAutoScroll(Handler handler, Runnable runnable, int autoScrollDelay) {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, autoScrollDelay);
        }
    }

    public static class SlowLinearSmoothScroller extends LinearSmoothScroller {
        public SlowLinearSmoothScroller(Context context) {
            super(context);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            // Higher = slower scroll
            return 200f / displayMetrics.densityDpi;
        }
    }

    public static String getUserIdFromToken(String token) {

        final String TAG = "JWT_PARSE_TEST";

        try {
            // 🔍 Log raw token (length only for safety)
            Log.i(TAG, "Token received. Length = " + (token != null ? token.length() : "null"));

            if (token == null || token.isEmpty()) {
                Log.e(TAG, "❌ Token is null or empty");
                return "";
            }

            // 🔍 Split JWT parts
            String[] parts = token.split("\\.");
            Log.i(TAG, "JWT parts count = " + parts.length);

            if (parts.length < 2) {
                Log.e(TAG, "❌ Invalid JWT format. Payload missing");
                return "";
            }

            String payload = parts[1];
            Log.i(TAG, "JWT payload (Base64) = " + payload);

            // 🔍 Decode Base64 payload
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            Log.i(TAG, "Decoded JWT payload JSON = " + json);

            // 🔍 Parse JSON
            JSONObject obj = new JSONObject(json);

            if (!obj.has("userId")) {
                Log.e(TAG, "❌ userId key not found in JWT payload");
                return "";
            }

            String userId = obj.getString("userId");
            Log.i(TAG, "✅ Extracted userId from token = " + userId);

            return userId;

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception while parsing JWT", e);
            return "";
        }
    }


    public static String getSafeString(JSONObject obj, String key) {
        if (obj == null || key == null) return "";

        if (!obj.has(key)) return "";

        String value = obj.optString(key, "");
        if (value.equalsIgnoreCase("null")) return "";

        return value.trim();
    }

    public static String safeValue(String value, String tag) {
        if (value == null || value.trim().isEmpty()) {
            Log.d(tag, "Value is null or empty → setting N/A");
            return "N/A";
        }
        Log.d(tag, "Value: " + value);
        return value;
    }


    public interface GarageListCallback {
        void onSuccess(ArrayList<GarageItemModel> garageList);

        void onFailure(String errorMessage);
    }


    public static Call<JsonObject> getGarageVehicleList(String TAG,
                                                        Activity context,
                                                        String userId,
                                                        GarageListCallback callback
    ) {

        CommonLogic.showTestLog(TAG,
                "🚀 getGarageVehicleList URL: " + APIData.GET_VEHICLE_LIST + userId);

        Call<JsonObject> call = ApiCall.callApi(
                TAG,
                context,
                APIData.GET_VEHICLE_LIST + userId,
                null,
                "get",
                new ApiCall.ApiResponseCallback() {

                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {

                        CommonLogic.showTestLog(TAG,
                                "✅ API Success | Status: " + status + " | Message: " + message);

                        if (!status) {
                            CommonLogic.showTestLog(TAG,
                                    "❌ API returned failure status");
                            callback.onFailure(message);
                            return;
                        }

                        try {

                            if (responseBody.has("http_code") && responseBody.getInt("http_code") == 503){
                                CommonMethods.showMessageDialog(context, "Under Maintenance", "RTO under maintenance, Please try after some time");
                                callback.onFailure(message);
                                return;
                            }

                            ArrayList<GarageItemModel> garageList = new ArrayList<>();

                            JSONObject dataObj = responseBody.getJSONObject("data");
                            JSONArray vehiclesArray = dataObj.getJSONArray("vehicles");

                            CommonLogic.showTestLog(TAG,
                                    "📦 Vehicles Count: " + vehiclesArray.length());

                            for (int i = 0; i < vehiclesArray.length(); i++) {

                                CommonLogic.showTestLog(TAG, "==============================");
                                CommonLogic.showTestLog(TAG, "➡️ Parsing vehicle index: " + i);

                                JSONObject vehicleObj = vehiclesArray.getJSONObject(i);

                                GarageItemModel model = new GarageItemModel();
                                model = fetchVehicleData(model, vehicleObj);

                                if (model != null) {
                                    garageList.add(model);
                                }
                            }

                            CommonLogic.showTestLog(TAG,
                                    "✅ Total Vehicles Parsed Successfully: " + garageList.size());

                            callback.onSuccess(garageList);

                            // Save cache
                            String json = new Gson().toJson(garageList);
                            new PreferencesManager(context).setString(PreferencesManager.KEY_GARAGE_CACHE, json);

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG,
                                    "🔥 Parsing Exception: " + e.getMessage());
                            callback.onFailure("Parsing error: " + e.getMessage());
                            new PreferencesManager(context).setString(PreferencesManager.KEY_GARAGE_CACHE, "");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        CommonLogic.showTestLog(TAG,
                                "❌ API Error: " + errorMessage);
                        callback.onFailure(errorMessage);
                        new PreferencesManager(context).setString(PreferencesManager.KEY_GARAGE_CACHE, "");
                    }
                }
        );

        return call;

    }

    public static ArrayList<GarageItemModel> loadGarageCache(Activity context) {

        ArrayList<GarageItemModel> cachedList = null;

        String cache = new PreferencesManager(context).getString(PreferencesManager.KEY_GARAGE_CACHE, "");

        if (cache != null && !cache.isEmpty()) {

            Type type = new TypeToken<ArrayList<GarageItemModel>>(){}.getType();

            cachedList = new Gson().fromJson(cache, type);

        }

        return cachedList;
    }

    static GarageItemModel fetchVehicleData(GarageItemModel model, JSONObject vehicleObj) {
        try {
            JSONObject vehicleInfoObj = null, info = null, rto_data = null;

            if (vehicleObj.has("api_data")){
                vehicleInfoObj = vehicleObj.getJSONObject("api_data");

                info = vehicleInfoObj.optJSONObject("custom_vehicle_info");
                rto_data = vehicleInfoObj.optJSONObject("rto_data");
            } else {

                if (vehicleObj.has("custom_vehicle_info")){
                    info = vehicleObj.optJSONObject("custom_vehicle_info");
                }

                if (vehicleObj.has("rto_data")){
                    rto_data = vehicleObj.optJSONObject("rto_data");
                }
            }

            //_______________



            if (Objects.requireNonNull(info).has("owner_name") && info.getString("owner_name").equalsIgnoreCase("N/A")) {
                info = Objects.requireNonNull(vehicleInfoObj.optJSONObject("rto_data")).getJSONObject("custom_vehicle_info");
            }

            if (rto_data != null && rto_data.has("permit")) {
                JSONObject permitData = rto_data.getJSONObject("permit");
                model.setPermitNumber(permitData.getString("number"));
                model.setPermitType(permitData.getString("type"));
                model.setPermitValidFrom(permitData.getString("validFrom"));
                model.setPermitValidUpto(permitData.getString("validUpto"));
            }

            if (rto_data != null && rto_data.has("nationalPermit")) {
                JSONObject nationalPermitData = rto_data.getJSONObject("nationalPermit");
                model.setNationalPermitNumber(nationalPermitData.getString("number"));
                model.setNationalPermitIssuedBy(nationalPermitData.getString("issuedBy"));
                model.setNationalPermitValidUpto(nationalPermitData.getString("validUpto"));
            }

            // 🔹 IDs
            /*String vehicleId = CommonMethods.getSafeString(vehicleObj, "vehicle_id");
            model.setVehicle_id(vehicleId);
            CommonLogic.showTestLog(TAG, "🆔 vehicle_id = " + vehicleId);*/


            String internalId = CommonMethods.getSafeString(info, "_id");
            CommonLogic.showTestLog(TAG, "🆔 _id = " + internalId);
            model.set_id(internalId);

            // 🔹 Vehicle Info (log each field)
            model.setVehicle_id(CommonMethods.getSafeString(info, "vehicle_number"));
            CommonLogic.showTestLog(TAG, "👤 vehicle_number = " + model.getVehicle_id());

            model.setOwner_name(CommonMethods.getSafeString(info, "owner_name"));
            CommonLogic.showTestLog(TAG, "👤 owner_name = " + model.getOwner_name());

            model.setVehicle_number(CommonMethods.getSafeString(info, "vehicle_number"));
            CommonLogic.showTestLog(TAG, "🚘 vehicle_number = " + model.getVehicle_number());

            model.setVehicle_name(CommonMethods.getSafeString(info, "vehicle_name"));
            CommonLogic.showTestLog(TAG, "🚗 vehicle_name = " + model.getVehicle_name());

            model.setFuel_type(CommonMethods.getSafeString(info, "fuel_type"));
            CommonLogic.showTestLog(TAG, "⛽ fuel_type = " + model.getFuel_type());

            model.setFinancer_name(CommonMethods.getSafeString(info, "financer_name"));
            CommonLogic.showTestLog(TAG, "⛽ financer_name = " + model.getFinancer_name());

            model.setRc_status(CommonMethods.getSafeString(info, "rc_status"));
            CommonLogic.showTestLog(TAG, "📄 rc_status = " + model.getRc_status());

            model.setRegistration_date(CommonMethods.getSafeString(info, "registration_date"));
            CommonLogic.showTestLog(TAG, "📅 registration_date = " + model.getRegistration_date());

            model.setOwnership_details(CommonMethods.getSafeString(info, "ownership_details"));
            CommonLogic.showTestLog(TAG, "👥 ownership_details = " + model.getOwnership_details());

            model.setRegistered_rto(CommonMethods.getSafeString(info, "registered_rto"));
            CommonLogic.showTestLog(TAG, "🏢 registered_rto = " + model.getRegistered_rto());

            model.setMakers_model(CommonMethods.getSafeString(info, "makers_model"));
            CommonLogic.showTestLog(TAG, "🏭 makers_model = " + model.getMakers_model());

            model.setMakers_name(CommonMethods.getSafeString(info, "makers_name"));
            CommonLogic.showTestLog(TAG, "🏭 makers_name = " + model.getMakers_name());

            model.setVehicle_class(CommonMethods.getSafeString(info, "vehicle_class"));
            CommonLogic.showTestLog(TAG, "🚙 vehicle_class = " + model.getVehicle_class());

            model.setFuel_norms(CommonMethods.getSafeString(info, "fuel_norms"));
            CommonLogic.showTestLog(TAG, "🌱 fuel_norms = " + model.getFuel_norms());

            model.setEngine(CommonMethods.getSafeString(info, "engine"));
            CommonLogic.showTestLog(TAG, "⚙️ engine = " + model.getEngine());

            model.setChassis_number(CommonMethods.getSafeString(info, "chassis_number"));
            CommonLogic.showTestLog(TAG, "🔢 chassis_number = " + model.getChassis_number());

            model.setInsurer_name(CommonMethods.getSafeString(info, "insurer_name"));
            CommonLogic.showTestLog(TAG, "🏥 insurer_name = " + model.getInsurer_name());

            model.setInsurance_type(CommonMethods.getSafeString(info, "insurance_type"));
            CommonLogic.showTestLog(TAG, "📑 insurance_type = " + model.getInsurance_type());

            model.setInsurance_expiry(CommonMethods.getSafeString(info, "insurance_expiry"));
            CommonLogic.showTestLog(TAG, "⏳ insurance_expiry = " + model.getInsurance_expiry());

            model.setInsurance_renewed_date(CommonMethods.getSafeString(info, "insurance_renewed_date"));
            CommonLogic.showTestLog(TAG, "🔄 insurance_renewed_date = " + model.getInsurance_renewed_date());

            model.setVehicle_age(CommonMethods.getSafeString(info, "vehicle_age"));
            CommonLogic.showTestLog(TAG, "🎂 vehicle_age = " + model.getVehicle_age());

            model.setFitness_upto(CommonMethods.getSafeString(info, "fitness_upto"));
            CommonLogic.showTestLog(TAG, "✅ fitness_upto = " + model.getFitness_upto());

            model.setPollution_renew_date(CommonMethods.getSafeString(info, "pollution_renew_date"));
            CommonLogic.showTestLog(TAG, "♻️ pollution_renew_date = " + model.getPollution_renew_date());

            model.setPollution_expiry(CommonMethods.getSafeString(info, "pollution_expiry"));
            CommonLogic.showTestLog(TAG, "🚫 pollution_expiry = " + model.getPollution_expiry());

            model.setColor(CommonMethods.getSafeString(info, "color"));
            CommonLogic.showTestLog(TAG, "🎨 color = " + model.getColor());

            model.setUnloaded_weight(CommonMethods.getSafeString(info, "unloaded_weight"));
            CommonLogic.showTestLog(TAG, "⚖️ unloaded_weight = " + model.getUnloaded_weight());

            model.setCategory(CommonMethods.getSafeString(info, "category"));
            CommonLogic.showTestLog(TAG, "⚖️ category = " + model.getCategory());

            model.setInsurance_policy_number(
                    CommonMethods.getSafeString(info, "insurance_policy_number"));
            CommonLogic.showTestLog(TAG,
                    "📜 insurance_policy_number = " + model.getInsurance_policy_number());

            if (vehicleObj.has("vehicle_doc")) {
                JSONObject vehicleDocObj =
                        vehicleObj.getJSONObject("vehicle_doc");

                // 🔹 Documents
                JSONArray docsArray = vehicleDocObj.getJSONArray("documents");
                CommonLogic.showTestLog(TAG,
                        "📄 documents_count = " + docsArray.length());

                ArrayList<GarageItemModel.vehicleDocuments> docs =
                        APIHelper.convertJsonArrayToList(
                                docsArray,
                                GarageItemModel.vehicleDocuments.class
                        );

                model.setVehicleDocumentsArrayList(docs);
            }

            return model;
        } catch (JSONException e) {
            return null;
        }
    }


    public static void downloadImageIfNotExists(String TAG,
                                                Context context,
                                                String imageUrl,
                                                String appImageFolderName,
                                                AshDialog loadingDialog) {

        // ✅ Show loader once
        if (loadingDialog != null && !loadingDialog.isVisible()) {
            loadingDialog.show();
        }

        new Thread(() -> {
            Uri savedImageUri = null;

            try {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                // 🔹 ANDROID 10+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    String relativePath =
                            Environment.DIRECTORY_PICTURES + "/" + appImageFolderName;

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);

                    savedImageUri = context.getContentResolver()
                            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    if (savedImageUri == null) {
                        throw new Exception("MediaStore URI is null");
                    }

                    OutputStream outputStream =
                            context.getContentResolver().openOutputStream(savedImageUri);

                    writeStream(inputStream, outputStream);

                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    context.getContentResolver()
                            .update(savedImageUri, values, null, null);

                }
                // 🔹 ANDROID 9 AND BELOW
                else {

                    File picturesDir =
                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES);

                    File appDir = new File(picturesDir, appImageFolderName);
                    if (!appDir.exists()) appDir.mkdirs();

                    File imageFile = new File(appDir, fileName);
                    OutputStream outputStream = new FileOutputStream(imageFile);

                    writeStream(inputStream, outputStream);

                    MediaScannerConnection.scanFile(
                            context,
                            new String[]{imageFile.getAbsolutePath()},
                            new String[]{"image/png"},
                            null
                    );

                    savedImageUri = Uri.fromFile(imageFile);
                }

                Uri finalSavedImageUri = savedImageUri;

                ((Activity) context).runOnUiThread(() -> {
                    // ✅ ALWAYS dismiss loader
                    if (loadingDialog != null && loadingDialog.isVisible()) {
                        loadingDialog.dismiss();
                    }

                    if (finalSavedImageUri != null) {
                        showSuccessDialog(context, finalSavedImageUri);
                    }
                });

                CommonLogic.showTestLog(TAG, "Image downloaded successfully");

            } catch (Exception e) {

                CommonLogic.showTestLog(TAG, "Download failed: " + e.getMessage());

                ((Activity) context).runOnUiThread(() -> {
                    // ✅ Dismiss on failure as well
                    if (loadingDialog != null && loadingDialog.isVisible()) {
                        loadingDialog.dismiss();
                    }
                });
            }
        }).start();
    }


    private static void writeStream(InputStream inputStream,
                                    OutputStream outputStream) throws Exception {

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }


    private static void showSuccessDialog(Context context, Uri imageUri) {

        new AlertDialog.Builder(context)
                .setTitle("Image Saved")
                .setMessage("Image downloaded successfully.")
                .setCancelable(false)
                .setPositiveButton("View", (dialog, which) -> {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(imageUri, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    context.startActivity(intent);
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static VehicleType getVehicleType(String vehicleClass, String category) {

        if (vehicleClass == null) return VehicleType.UNKNOWN;

        String cls = vehicleClass.toLowerCase();

        if (category != null && (category.contains("2wn") || category.equalsIgnoreCase("2WN"))) {
            return VehicleType.TWO_WHEELER;
        }

        if (cls.contains("2wn") || cls.contains("m-cycle")) {
            return VehicleType.TWO_WHEELER;
        }

        if (cls.contains("3w") || cls.contains("auto")) {
            return VehicleType.THREE_WHEELER;
        }

        if (cls.contains("lmv") || cls.contains("4w") || cls.contains("lpv")) {
            return VehicleType.FOUR_WHEELER;
        }

        if (cls.contains("truck") || cls.contains("bus") ||
                cls.contains("hgv") || cls.contains("lgv") || cls.contains("mgv")) {
            return VehicleType.HEAVY;
        }

        return VehicleType.UNKNOWN;
    }

    public static int getVehiclePlaceholder(
            String vehicleClass,
            String vehicleName,
            String makersModel,
            String category
    ) {

        String nameModel =
                (vehicleName == null ? "" : vehicleName) + " " +
                        (makersModel == null ? "" : makersModel);

        nameModel = nameModel.toLowerCase();

        VehicleType type = getVehicleType(vehicleClass, category);

        switch (type) {

            case TWO_WHEELER:

                // Bike
                if (nameModel.contains("splendor")
                        || nameModel.contains("pulsar")
                        || nameModel.contains("apache")
                        || nameModel.contains("r15")
                        || nameModel.contains("bullet")
                        || nameModel.contains("bike")
                        || nameModel.contains("motorcycle")) {

                    return R.drawable.ic_vehicle_2w_bike;
                }

                // Scooty
                if (nameModel.contains("activa")
                        || nameModel.contains("jupiter")
                        || nameModel.contains("dio")
                        || nameModel.contains("access")
                        || nameModel.contains("pleasure")
                        || nameModel.contains("vespa")
                        || nameModel.contains("scooter")) {

                    return R.drawable.ic_vehicle_2w_scooty;
                }

                return R.drawable.ic_vehicle_2w;


            case FOUR_WHEELER:

                // Truck
                if (nameModel.contains("truck")) {
                    return R.drawable.ic_vehicle_heavy;
                }

                // Car (default 4W)
                return R.drawable.ic_vehicle_4w;


            case THREE_WHEELER:
                return R.drawable.ic_vehicle_3w;

            case HEAVY:
                return R.drawable.ic_vehicle_heavy;

            default:
                return R.drawable.ic_vehicle_default;
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    public static void setNotificationCount(Activity context, TextView notificationCountTextView, ImageView bellImage, String userId) {
        bellImage.setVisibility(View.VISIBLE);
        notificationCountTextView.setVisibility(View.GONE);
        ApiCall.callApi(TAG,
                context,
                APIData.GET_NOTIFICATION + userId + "?current_page=1",
                null, "get",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {

                                if (responseBody.has("unseen_count")) {
                                    int notificationUnseenCount = responseBody.getInt("unseen_count");
                                    if (notificationUnseenCount > 99) {
                                        notificationCountTextView.setText("99+");
                                    } else {
                                        if (notificationUnseenCount > 0) {
                                            notificationCountTextView.setVisibility(View.VISIBLE);
                                            notificationCountTextView.setText(String.valueOf(notificationUnseenCount));
                                        } else {
                                            notificationCountTextView.setVisibility(View.GONE);
                                        }
                                    }
                                }

                            }
                        } catch (JSONException e) {
                            CommonLogic.showTestLog(TAG, "Error parsing JSON: " + e.getMessage());
                        }

                    }


                    @Override
                    public void onError(String errorMessage) {
                        CommonLogic.showTestLog(TAG, errorMessage);
                    }
                }
        );
    }

    public static String getInsuranceRenewDate(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return "N/A";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);

            if (expiry == null) return "N/A";

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expiry);

            // 🔥 Insurance validity assumed as 1 year
            calendar.add(Calendar.YEAR, -1);

            return sdf.format(calendar.getTime());

        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }


    private User parseUserFromJson(JSONObject userJson) {
        User user = new User();

        try {

            // Extract basic_details
            if (userJson.has("basic_details")) {
                JSONObject basicDetails = userJson.getJSONObject("basic_details");
                user.setFirst_name(basicDetails.has("first_name") ? basicDetails.getString("first_name") : "");
                user.setLast_name(basicDetails.has("last_name") ? basicDetails.getString("last_name") : "");
                user.setEmail(basicDetails.has("email") ? basicDetails.getString("email") : "");
                user.setPhone_number(basicDetails.has("phone_number") ? basicDetails.getString("phone_number") : "");
                user.setPhone_number_verified(basicDetails.has("phone_number_verified") ? basicDetails.getString("phone_number_verified") : "");
                user.setIs_phone_number_primary(basicDetails.has("is_phone_number_primary") ? basicDetails.getString("is_phone_number_primary") : "");

                user.setEmail(basicDetails.has("email") ? basicDetails.getString("email") : "");
                user.setIs_email_verified(basicDetails.has("is_email_verified") ? basicDetails.getString("is_email_verified") : "");
                user.setIs_email_primary(basicDetails.has("is_email_primary") ? basicDetails.getString("is_email_primary") : "");
                user.setPassword(basicDetails.has("password") ? basicDetails.getString("password") : "");
                user.setOccupation(basicDetails.has("occupation") ? basicDetails.getString("occupation") : "");
                user.setProfile_completion_percent(basicDetails.has("profile_completion_percent") ? basicDetails.getString("profile_completion_percent") : "");

                user.setProfile_pic(basicDetails.has("profile_pic") ? basicDetails.getString("profile_pic") : "");
            }


            if (userJson.has("public_details")) {
                JSONObject publicDetails = userJson.getJSONObject("public_details");
                user.setNick_name(publicDetails.has("nick_name") ? publicDetails.getString("nick_name") : "");
                user.setAddress(publicDetails.has("address") ? publicDetails.getString("address") : "");
                user.setAge(publicDetails.has("age") ? publicDetails.getString("age") : "");
                user.setGender(publicDetails.has("gender") ? publicDetails.getString("gender") : "");
                user.setPublic_pic(publicDetails.has("public_pic") ? publicDetails.getString("public_pic") : "");
            }

            // You can extract public_details, address_book, garage, etc. if needed
            // For now, only the basic details are saved

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        return user;
    }


    public static void showProfileUpdateDialog(String TAG, Activity context, boolean isCancelable) {

        CommonLogic.showTestLog(TAG, "🔔 showProfileUpdateDialog() CALLED");

        PreferencesManager manager = new PreferencesManager(context);

        if (manager.getUser() == null) {
            CommonLogic.showTestLog(TAG, "❌ User object is NULL");
            return;
        }

        String profilePercent = manager.getUser().getProfile_completion_percent();
        CommonLogic.showTestLog(TAG, "📊 Profile completion percent = " + profilePercent);

        if (profilePercent != null && !profilePercent.isEmpty()) {

            int percent;
            try {
                percent = Integer.parseInt(profilePercent);
            } catch (NumberFormatException e) {
                CommonLogic.showTestLog(TAG, "❌ Invalid profile percent value: " + profilePercent);
                return;
            }

            if (percent < 90) {

                CommonLogic.showTestLog(TAG, "⚠️ Profile incomplete (<90%), showing dialog");

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(context, R.style.CustomDialogTheme);

                @SuppressLint("InflateParams")
                View view = LayoutInflater.from(context)
                        .inflate(R.layout.profile_incomplete_dialog_design, null);

                ProfileIncompleteDialogDesignBinding dialogBinding = ProfileIncompleteDialogDesignBinding.bind(view);


                builder.setView(view);

                AlertDialog dialog = builder.create();
                dialog.show(); // ⚠️ MUST call show() first

// ✅ Force width to MATCH_PARENT
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }


// 🔒 HARD BLOCK dismiss
//                dialog.setCancelable(false);
//                dialog.setCanceledOnTouchOutside(false);

// 🔒 Block BACK button
                dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        CommonLogic.showTestLog(TAG, "⛔ Back button blocked on dialog");
                        return true;
                    }
                    return false;
                });

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    );
                }

                dialogBinding.vehicleNumberField.rootLayout.setVisibility(View.GONE);

                ImageView closeBtn = view.findViewById(R.id.closeBtn);

                if (isCancelable) {
                    CommonLogic.showTestLog(TAG, "✅ Dialog is cancelable");
                    closeBtn.setVisibility(View.VISIBLE);
                    closeBtn.setOnClickListener(v -> {
                        CommonLogic.showTestLog(TAG, "❎ Dialog dismissed via close button");
                        dialog.dismiss();
                    });
                } else {
                    CommonLogic.showTestLog(TAG, "🚫 Dialog is NOT cancelable");
                    closeBtn.setVisibility(View.GONE);
                }

                Button openProfileBtn = view.findViewById(R.id.openProfileBtn);
                openProfileBtn.setOnClickListener(v -> {

                    CommonLogic.showTestLog(TAG, "➡️ Open Profile button clicked");

                    Intent main = new Intent(context, MainActivity.class);
                    main.putExtra("changeFragment", "profile");
                    context.startActivity(main);
                    dialog.dismiss();
                    context.finish();
                });

//                dialog.setCancelable(isCancelable);
                dialog.show();

                CommonLogic.showTestLog(TAG, "🟢 Profile Update Dialog SHOWN");

            } else {
                CommonLogic.showTestLog(TAG, "✅ Profile completion ≥ 90%, dialog NOT required");
            }

        } else {
            CommonLogic.showTestLog(TAG, "❌ Profile completion percent is NULL or EMPTY");
        }
    }


    public static void logout(Activity context, boolean isLogout) {

        PreferencesManager preferencesManager = new PreferencesManager(context);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.CustomDialogTheme);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context)
                .inflate(R.layout.profile_incomplete_dialog_design, null);

        ProfileIncompleteDialogDesignBinding dialogBinding = ProfileIncompleteDialogDesignBinding.bind(view);

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show(); // ⚠️ MUST call show() first

// ✅ Force width to MATCH_PARENT
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }


// 🔒 HARD BLOCK dismiss
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

// 🔒 Block BACK button
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                CommonLogic.showTestLog(TAG, "⛔ Back button blocked on dialog");
                return true;
            }
            return false;
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        }

        dialogBinding.vehicleNumberField.rootLayout.setVisibility(View.GONE);

        ImageView closeBtn = view.findViewById(R.id.closeBtn);
        if (isLogout) {
            closeBtn.setVisibility(View.VISIBLE);
            closeBtn.setOnClickListener(view1 -> {
                dialog.dismiss();
            });
        } else {
            closeBtn.setVisibility(View.GONE);
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        if (isLogout) {
            tvTitle.setText("Logout from device.");
        } else {
            tvTitle.setText("Login Expired");
        }

        TextView tvSubTitle = view.findViewById(R.id.tvSubTitle);
        if (isLogout) {
            tvSubTitle.setText("Are you sure you want to log out?");
        } else {
            tvSubTitle.setText("Please login again");
        }

        Button openLoginPageBtn = view.findViewById(R.id.openProfileBtn);
        if (isLogout) {
            openLoginPageBtn.setText("Yes");
        } else {
            openLoginPageBtn.setText("Login");
        }
        openLoginPageBtn.setOnClickListener(v -> {

            preferencesManager.clear();

            preferencesManager.setBoolean(PreferencesManager.KEY_FIRST_LAUNCH, false);

            // BREAK OLD LINK
            OneSignal.logout();

            // OPTIONAL BUT STRONG
            OneSignal.getUser().getPushSubscription().optOut();

            // 3. Re-initialize subscription state
            OneSignal.initWithContext(context,
                    context.getString(R.string.one_signal_id));

            CommonMethods.logoutFromServer(TAG, context);
            CommonMethods.logoutFromDeviceDialog(context);
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    public static void logoutFromServer(String TAG, Activity context) {
        ApiCall.callApi(TAG, context, APIData.LOGOUT, null, "post", new ApiCall.ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                CommonLogic.showTestLog(TAG, "User log out from server");

            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    public static void logoutFromDeviceDialog(Activity context) {
        Intent mainActivity = new Intent(context, LoginActivity.class);
        new PreferencesManager(context).setBoolean(PreferencesManager.KEY_IS_LOGGED_IN, false);
        context.startActivity(mainActivity);
        context.finishAffinity();
    }


    public static void setStatusBarColor(@NonNull Window window, @ColorInt int color) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Android 15+

            View decorView = window.getDecorView();

            decorView.setOnApplyWindowInsetsListener((view, insets) -> {

                Insets statusBarInsets =
                        insets.getInsets(WindowInsets.Type.statusBars());

                // Set background color behind status bar
                view.setBackgroundColor(color);

                /*// Adjust padding to avoid overlap
                view.setPadding(
                        0,
                        statusBarInsets.top,
                        0,
                        0
                );*/

                view.setPadding(
                        0,
                        0,
                        0,
                        0
                );

                return insets;
            });

        } else {
            // Android 14 and below
            window.setStatusBarColor(color);
        }
    }

    public static void setHomeStatusBarColor(@NonNull Window window, @ColorInt int color) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Android 15+

            View decorView = window.getDecorView();

            decorView.setOnApplyWindowInsetsListener((view, insets) -> {

                Insets statusBarInsets =
                        insets.getInsets(WindowInsets.Type.statusBars());

                // Set background color behind status bar
                view.setBackgroundColor(color);

                // Adjust padding to avoid overlap
                view.setPadding(
                        0,
                        statusBarInsets.top,
                        0,
                        0
                );

                return insets;
            });

        } else {
            // Android 14 and below
            window.setStatusBarColor(color);
        }
    }


    public interface AddVehicleCallback {
        void onSuccess(GarageItemModel vehicleInfo);

        void onFailure(String message);
    }


    public static void showAddVehiclePopupDialog(Activity context, String serviceType, String userId, String vehicleNumber, String vehicleOwner, AddVehicleCallback callback) {

        AshDialog loadingDialog = new AshDialog(context, "Please wait", "");

        String title = "Check Vehicle";
        String message = "Please enter vehicle number to check details.";
        String textHint = "Enter vehicle number";
        String buttonText = "Check";
        String errorMessage = "No Vehicle Found";

        String apiUrl = APIData.CHECK_VEHICLE;

        if (serviceType.equalsIgnoreCase("verifyVehicle")) {
            title = "Verify Owner";
            message = "Please verify " + vehicleOwner + " vehicle owner, to add vehicle in garage.";
            textHint = "Enter owner name";
            buttonText = "Verify";

            errorMessage = "Invalid Vehicle Owner";

            apiUrl = APIData.ADD_VEHICLE;
        } else if (serviceType.equalsIgnoreCase("challanInfo")) {
            title = "Check Challan";
            message = "Please enter vehicle number to check challan details";
            textHint = "Enter vehicle number";
            buttonText = "Check";

            errorMessage = "No data Found";
            apiUrl = APIData.ADD_VEHICLE;
        }


        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.CustomDialogTheme);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context)
                .inflate(R.layout.profile_incomplete_dialog_design, null);

        ProfileIncompleteDialogDesignBinding dialogBinding = ProfileIncompleteDialogDesignBinding.bind(view);

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show(); // ⚠️ MUST call show() first

// ✅ Force width to MATCH_PARENT
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }


// 🔒 HARD BLOCK dismiss
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

// 🔒 Block BACK button
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                CommonLogic.showTestLog(TAG, "⛔ Back button blocked on dialog");
                return true;
            }
            return false;
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        }


//        ImageView closeBtn = view.findViewById(R.id.closeBtn);
        dialogBinding.closeBtn.setVisibility(View.VISIBLE);
        dialogBinding.closeBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });


//        TextView tvTitle = view.findViewById(R.id.tvTitle);
        dialogBinding.tvTitle.setText(title);

//        TextView tvSubTitle = view.findViewById(R.id.tvSubTitle);
        dialogBinding.tvSubTitle.setText(message);

        dialogBinding.vehicleNumberField.ivIcon.setVisibility(View.GONE);
        dialogBinding.vehicleNumberField.rootLayout.setVisibility(View.VISIBLE);
        dialogBinding.vehicleNumberField.etInput.setHint(textHint);

        if (!serviceType.equalsIgnoreCase("verifyVehicle") || serviceType.equalsIgnoreCase("challanInfo")) {
            dialogBinding.vehicleNumberField.etInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        }


        Button openLoginPageBtn = view.findViewById(R.id.openProfileBtn);
        openLoginPageBtn.setText(buttonText);
        String finalApiUrl = apiUrl;
        String finalErrorMessage = errorMessage;
        openLoginPageBtn.setOnClickListener(v -> {

            String userInput = dialogBinding.vehicleNumberField.etInput.getText().toString().trim();

            if (userInput.isEmpty()) {
                Toast.makeText(context, "Field can't be empty", Toast.LENGTH_SHORT).show();
                dialogBinding.vehicleNumberField.etInput.setError("Field can't be empty");
                return;
            }

            if (serviceType.equalsIgnoreCase("challanInfo")) {
                Intent checkChallan = new Intent(context, CheckChallan.class);
                checkChallan.putExtra("vehicleNumber", userInput);
                checkChallan.putExtra("serviceType", "check");
                context.startActivity(checkChallan);
                dialog.dismiss();
            } else {

                loadingDialog.show();

                ArrayList<GarageItemModel> garageItemList = CommonMethods.loadGarageCache(context);
                if (garageItemList != null && !garageItemList.isEmpty()) {
                    for (GarageItemModel itemModel : garageItemList) {
                        if (itemModel.getVehicle_id().equalsIgnoreCase(userInput)) {
                            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                            Intent vehicleInfoPage = new Intent(context, VehicleInformation.class);
                            vehicleInfoPage.putExtra("vehicleData", itemModel);
                            vehicleInfoPage.putExtra("vehicleDataType", "verity");
                            context.startActivity(vehicleInfoPage);
                            loadingDialog.dismiss();
                            dialog.dismiss();
                            return;
                        }
                    }
                }

                JsonObject jsonObjectFuelPrice = new JsonObject();
                jsonObjectFuelPrice.addProperty("user_id", userId);
                if (serviceType.equalsIgnoreCase("verifyVehicle")) {
                    jsonObjectFuelPrice.addProperty("vehicle_number", vehicleNumber);
                } else {
                    jsonObjectFuelPrice.addProperty("vehicle_number", userInput);
                }
                jsonObjectFuelPrice.addProperty("owner_name", userInput);

                CommonLogic.showTestLog(TAG, "showAddVehicleBottomSheet: " + jsonObjectFuelPrice.toString());

                ApiCall.callApi(TAG,
                        context,
                        finalApiUrl,
                        jsonObjectFuelPrice, "post",
                        new ApiCall.ApiResponseCallback() {
                            @Override
                            public void onSuccess(JSONObject responseBody, boolean status, String message) {

                                try {
                                    CommonLogic.showTestLog(TAG, "showAddVehicleBottomSheet: " + responseBody.toString());
//                            Toast.makeText(VehicleInformation.this, message, Toast.LENGTH_SHORT).show();
                                    if (status) {

                                        JSONObject info = null;

                                        if (serviceType.equalsIgnoreCase("verifyVehicle")) {
                                            info = responseBody.getJSONObject("data").getJSONObject("vehicle").optJSONObject("custom_vehicle_info");
                                        } else {
                                            info = responseBody.getJSONObject("data").getJSONObject("result").optJSONObject("custom_vehicle_info");
                                        }

                                        if (info != null) {

                                            GarageItemModel model = new GarageItemModel();

                                            // 🔹 IDs
                                            if (!serviceType.equalsIgnoreCase("verifyVehicle")) {
                                                model.setVehicle_id(userInput);
                                            } else {
                                                model.setVehicle_id(vehicleNumber);
                                            }

                                            // 🔹 Vehicle Info (log each field)
                                            model.setOwner_name(CommonMethods.getSafeString(info, "owner_name"));
                                            CommonLogic.showTestLog(TAG, "👤 owner_name = " + model.getOwner_name());

                                            model.setVehicle_number(CommonMethods.getSafeString(info, "vehicle_number"));
                                            CommonLogic.showTestLog(TAG, "🚘 vehicle_number = " + model.getVehicle_number());

                                            model.setVehicle_name(CommonMethods.getSafeString(info, "vehicle_name"));
                                            CommonLogic.showTestLog(TAG, "🚗 vehicle_name = " + model.getVehicle_name());

                                            model.setFuel_type(CommonMethods.getSafeString(info, "fuel_type"));
                                            CommonLogic.showTestLog(TAG, "⛽ fuel_type = " + model.getFuel_type());

                                            model.setRc_status(CommonMethods.getSafeString(info, "rc_status"));
                                            CommonLogic.showTestLog(TAG, "📄 rc_status = " + model.getRc_status());

                                            model.setRegistration_date(CommonMethods.getSafeString(info, "registration_date"));
                                            CommonLogic.showTestLog(TAG, "📅 registration_date = " + model.getRegistration_date());

                                            model.setOwnership_details(CommonMethods.getSafeString(info, "ownership_details"));
                                            CommonLogic.showTestLog(TAG, "👥 ownership_details = " + model.getOwnership_details());

                                            model.setRegistered_rto(CommonMethods.getSafeString(info, "registered_rto"));
                                            CommonLogic.showTestLog(TAG, "🏢 registered_rto = " + model.getRegistered_rto());

                                            model.setMakers_model(CommonMethods.getSafeString(info, "makers_model"));
                                            CommonLogic.showTestLog(TAG, "🏭 makers_model = " + model.getMakers_model());

                                            model.setMakers_name(CommonMethods.getSafeString(info, "makers_name"));
                                            CommonLogic.showTestLog(TAG, "🏭 makers_name = " + model.getMakers_name());

                                            model.setVehicle_class(CommonMethods.getSafeString(info, "vehicle_class"));
                                            CommonLogic.showTestLog(TAG, "🚙 vehicle_class = " + model.getVehicle_class());

                                            model.setFuel_norms(CommonMethods.getSafeString(info, "fuel_norms"));
                                            CommonLogic.showTestLog(TAG, "🌱 fuel_norms = " + model.getFuel_norms());

                                            model.setEngine(CommonMethods.getSafeString(info, "engine"));
                                            CommonLogic.showTestLog(TAG, "⚙️ engine = " + model.getEngine());

                                            model.setChassis_number(CommonMethods.getSafeString(info, "chassis_number"));
                                            CommonLogic.showTestLog(TAG, "🔢 chassis_number = " + model.getChassis_number());

                                            model.setInsurer_name(CommonMethods.getSafeString(info, "insurer_name"));
                                            CommonLogic.showTestLog(TAG, "🏥 insurer_name = " + model.getInsurer_name());

                                            model.setInsurance_type(CommonMethods.getSafeString(info, "insurance_type"));
                                            CommonLogic.showTestLog(TAG, "📑 insurance_type = " + model.getInsurance_type());

                                            model.setInsurance_expiry(CommonMethods.getSafeString(info, "insurance_expiry"));
                                            CommonLogic.showTestLog(TAG, "⏳ insurance_expiry = " + model.getInsurance_expiry());

                                            model.setInsurance_renewed_date(CommonMethods.getSafeString(info, "insurance_renewed_date"));
                                            CommonLogic.showTestLog(TAG, "🔄 insurance_renewed_date = " + model.getInsurance_renewed_date());

                                            model.setVehicle_age(CommonMethods.getSafeString(info, "vehicle_age"));
                                            CommonLogic.showTestLog(TAG, "🎂 vehicle_age = " + model.getVehicle_age());

                                            model.setFitness_upto(CommonMethods.getSafeString(info, "fitness_upto"));
                                            CommonLogic.showTestLog(TAG, "✅ fitness_upto = " + model.getFitness_upto());

                                            model.setPollution_renew_date(CommonMethods.getSafeString(info, "pollution_renew_date"));
                                            CommonLogic.showTestLog(TAG, "♻️ pollution_renew_date = " + model.getPollution_renew_date());

                                            model.setPollution_expiry(CommonMethods.getSafeString(info, "pollution_expiry"));
                                            CommonLogic.showTestLog(TAG, "🚫 pollution_expiry = " + model.getPollution_expiry());

                                            model.setColor(CommonMethods.getSafeString(info, "color"));
                                            CommonLogic.showTestLog(TAG, "🎨 color = " + model.getColor());

                                            model.setUnloaded_weight(CommonMethods.getSafeString(info, "unloaded_weight"));
                                            CommonLogic.showTestLog(TAG, "⚖️ unloaded_weight = " + model.getUnloaded_weight());

                                            model.setCategory(CommonMethods.getSafeString(info, "category"));
                                            CommonLogic.showTestLog(TAG, "⚖️ category = " + model.getCategory());

                                            model.setInsurance_policy_number(
                                                    CommonMethods.getSafeString(info, "insurance_policy_number"));
                                            CommonLogic.showTestLog(TAG,
                                                    "📜 insurance_policy_number = " + model.getInsurance_policy_number());

                                            dialog.dismiss();

                                            if (callback != null) {
                                                callback.onSuccess(model);
                                            }
                                        } else {
                                            if (callback != null) {
                                                callback.onFailure("Data not found");
                                                if (dialogBinding.vehicleNumberField.rootLayout.getVisibility() == View.VISIBLE) {
                                                    dialogBinding.vehicleNumberField.etInput.setError(finalErrorMessage);
                                                }
                                            }
                                        }
                                    } else {
                                        if (dialogBinding.vehicleNumberField.rootLayout.getVisibility() == View.VISIBLE) {
                                            dialogBinding.vehicleNumberField.etInput.setError(finalErrorMessage);
                                        }

                                        if (callback != null) {
                                            callback.onFailure("Invalid Owner Name");
                                        }

                                        if (serviceType.equalsIgnoreCase("checkVehicle")) {
                                            CommonMethods.showVehicleNotFoundDialog(context);
                                        }
                                    }
                                    loadingDialog.dismiss();

                                } catch (JSONException e) {
                                    CommonLogic.showTestLog(TAG, e.getMessage());
                                    Toast.makeText(context, "data not found", Toast.LENGTH_SHORT).show();
                                    if (dialogBinding.vehicleNumberField.rootLayout.getVisibility() == View.VISIBLE) {
                                        dialogBinding.vehicleNumberField.etInput.setError(finalErrorMessage);
                                    }

                                    if (serviceType.equalsIgnoreCase("checkVehicle")) {
                                        CommonMethods.showVehicleNotFoundDialog(context);
                                    }
                                }

                            }

                            @Override
                            public void onError(String errorMessage) {
                                dialog.dismiss();
                                loadingDialog.dismiss();
                                CommonLogic.showTestLog(TAG, errorMessage);

                                if (callback != null) {
                                    callback.onFailure(errorMessage);
                                }
                                if (dialogBinding.vehicleNumberField.rootLayout.getVisibility() == View.VISIBLE) {
                                    dialogBinding.vehicleNumberField.etInput.setError(finalErrorMessage);
                                }

                                if (serviceType.equalsIgnoreCase("checkVehicle")) {
                                    CommonMethods.showVehicleNotFoundDialog(context);
                                }
                            }
                        }
                );
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    public static void showSuccessDialog(Activity activity, String verificationType) {
        PreferencesManager preferencesManager = new PreferencesManager(activity);

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

        if (verificationType.equalsIgnoreCase("createAccount")) {
            btnLogin.setText("Login");
            tvTitle.setText("Account Created");
            tvMessage.setText("Welcome to Digivahan – your smart companion for all vehicle-related services. Please log in to explore features like QR scan connect, nearby essentials, and vehicle challan info.");
        } else if (verificationType.equalsIgnoreCase("verify")) {
            btnLogin.setText("Verified");
            tvTitle.setText("Account verified successfully");
            tvMessage.setText("Your account is verified successfully. Please login again.");
        } else {
            btnLogin.setText("Login");
            tvTitle.setText("Password Changed");
            tvMessage.setText("Your password has been changed. For your security, please use the new password next time you log in.");
        }

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
            // 👉 Redirect to login activity here
            ((BaseActivity) activity).disableHideContentSecureForNextNavigation();
            Intent intent = new Intent(activity, LoginActivity.class);
            if (verificationType.equalsIgnoreCase("createAccount")) {
                preferencesManager.setBoolean(PreferencesManager.KEY_IS_LOGGED_IN, true);
                intent = new Intent(activity, MainActivity.class);
            }
            activity.startActivity(intent);
            activity.finish();
        });
        dialog.show();
    }

    public static void showMessageDialog(Activity activity, String title, String message) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_password_changed);
        dialog.setCancelable(false);

        // Transparent background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView imgStatus = dialog.findViewById(R.id.imgStatus);
        imgStatus.setVisibility(View.GONE);

        TextView btnLogin = dialog.findViewById(R.id.btnLogin);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);

        btnLogin.setText("Ok");
        tvTitle.setText(title);
        tvMessage.setText(message);

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }


    public static void getAppInfo(Context context) {
        PreferencesManager manager = new PreferencesManager(context);
        ApiClient.getApiService(context).commonGETMethodToHitAllAPIs(APIData.GET_APP_INFO).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                try {
                    boolean status = responseBody.has("success") && responseBody.getBoolean("success");

                    if (status) {
                        JSONObject appInfoData = responseBody.getJSONObject("data");
                        if (appInfoData.has("currentDate") && !appInfoData.getString("currentDate").isEmpty()
                                && appInfoData.getString("currentDate") != null) {
                            manager.setString(PreferencesManager.CURRENT_DATE, appInfoData.getString("currentDate"));
                        }

                        if (appInfoData.has("appSharingMessage") && !appInfoData.getString("appSharingMessage").isEmpty()
                                && appInfoData.getString("appSharingMessage") != null) {
                            manager.setString(PreferencesManager.APP_SHARING_MESSAGE, appInfoData.getString("appSharingMessage"));
                        }
                    } else {
                        manager.setString(PreferencesManager.CURRENT_DATE, "");
                        manager.setString(PreferencesManager.APP_SHARING_MESSAGE, "");
                    }
                } catch (JSONException e) {
                    manager.setString(PreferencesManager.CURRENT_DATE, "");
                    manager.setString(PreferencesManager.APP_SHARING_MESSAGE, "");
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                manager.setString(PreferencesManager.CURRENT_DATE, "");
                manager.setString(PreferencesManager.APP_SHARING_MESSAGE, "");
            }
        });
    }

    public static String getCurrentDate(Context context, String dateFormat) {
        PreferencesManager manager = new PreferencesManager(context);
        if (manager.getString(PreferencesManager.CURRENT_DATE, "") != null && !manager.getString(PreferencesManager.CURRENT_DATE, "").isEmpty()) {
            return TimeUtils.convertDateFormat(manager.getString(PreferencesManager.CURRENT_DATE, ""), dateFormat);
        }
        return TimeUtils.getCurrentDate(dateFormat);
    }

    public static String getBaseUrl(Context context) {
        PreferencesManager manager = new PreferencesManager(context);
        if (manager.getString(PreferencesManager.BASE_URL, "") != null && !manager.getString(PreferencesManager.BASE_URL, "").isEmpty()) {
            return manager.getString(PreferencesManager.BASE_URL, APIData.BASE_URL);
        }
        return APIData.BASE_URL;
    }

    public static String getAppSharingMessage(Context context) {

        String message =
                "Undi mandi shandi,\n" +
                        "jo is app ko download na kare,\n" +
                        "uski gaadi ki mileage ho jaaye kam… permanently! \uD83D\uDE1C\uD83D\uDE97!\n\n" +
                        "👉 https://play.google.com/store/apps/details?id="
                        + context.getPackageName();

        PreferencesManager manager = new PreferencesManager(context);
        if (manager.getString(PreferencesManager.APP_SHARING_MESSAGE, "") != null && !manager.getString(PreferencesManager.APP_SHARING_MESSAGE, "").isEmpty()) {
            message = manager.getString(PreferencesManager.APP_SHARING_MESSAGE, message);
        }

        return message;
    }


    public static String getDatePartFromDateString(String dateString, String type) {

        Date parsedDate = TimeUtils.parseDateSafely(dateString);
        if (parsedDate == null || type == null) return "";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsedDate);

        switch (type.toLowerCase()) {

            case "year":
                return String.valueOf(calendar.get(Calendar.YEAR));

            case "month":
                return String.valueOf(calendar.get(Calendar.MONTH) + 1); // 1–12

            case "day":
            case "date":
                return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

            case "hour":
                return String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)); // 0–23

            case "minute":
                return String.valueOf(calendar.get(Calendar.MINUTE));

            case "second":
                return String.valueOf(calendar.get(Calendar.SECOND));

            case "age":
                return String.valueOf(calculateAge(calendar));

            default:
                return "";
        }
    }


    private static int calculateAge(Calendar birthDate) {

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // If birthday hasn’t occurred yet this year, reduce age by 1
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return Math.max(age, 0); // Safety check
    }


    public static String getFormattedOwner(String ownershipDetails) {

        if (ownershipDetails == null || ownershipDetails.trim().isEmpty()) {
            return "";
        }

        ownershipDetails = ownershipDetails.trim();

        // If already "First Owner", return as is
        if (ownershipDetails.equalsIgnoreCase("First Owner")) {
            return "First Owner";
        }

        // Handle "Owner X" pattern
        if (ownershipDetails.toLowerCase().startsWith("owner")) {
            try {
                String numberPart = ownershipDetails.replaceAll("[^0-9]", "");
                int ownerNumber = Integer.parseInt(numberPart);

                switch (ownerNumber) {
                    case 1:
                        return "First Owner";
                    case 2:
                        return "Second Owner";
                    case 3:
                        return "Third Owner";
                    case 4:
                        return "Fourth Owner";
                    case 5:
                        return "Fifth Owner";
                    case 6:
                        return "Sixth Owner";
                    case 7:
                        return "Seventh Owner";
                    case 8:
                        return "Eighth Owner";
                    case 9:
                        return "Ninth Owner";
                    default:
                        return ownershipDetails; // beyond 9 → keep original
                }

            } catch (Exception e) {
                return ownershipDetails;
            }
        }

        // Fallback → return same server value
        return ownershipDetails;
    }

    public static void sendWhatsAppAlert(String TAG, Activity context, String agent, String receiver) {
        JsonObject jsonObjectFuelPrice = new JsonObject();
        jsonObjectFuelPrice.addProperty("agent", agent);
        jsonObjectFuelPrice.addProperty("receiver", receiver);

        ApiCall.callApi(TAG, context, APIData.CONTACT_VIA_CALL, null, "post", new ApiCall.ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                CommonLogic.showTestLog(TAG, "User log out from server");

            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }


    public static void sendCallAlert(String TAG, Activity context, String agent, String receiver) {
        JsonObject jsonObjectSendCallAlert = new JsonObject();
        jsonObjectSendCallAlert.addProperty("agent", agent);
        jsonObjectSendCallAlert.addProperty("receiver", receiver);
        CommonLogic.showTestLog(TAG, "sendCallAlert params: " + jsonObjectSendCallAlert.toString());


        ApiCall.callApi(TAG, context, APIData.CONTACT_VIA_CALL, jsonObjectSendCallAlert, "post", new ApiCall.ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                CommonLogic.showTestLog(TAG, "Call initiated successfully");
            }

            @Override
            public void onError(String errorMessage) {
                CommonLogic.showTestLog(TAG, errorMessage);
            }
        });
    }

    public interface SMSAlertCallback {
        void onSuccess(String message);

        void onFailure(String error);
    }

    public static void sendSMSAlert(
            String TAG,
            Activity context,
            String userId,
            String issueType,
            SMSAlertCallback callback
    ) {

        JsonObject jsonObjectSendSMSAlert = new JsonObject();
        jsonObjectSendSMSAlert.addProperty("user_id", userId);
        jsonObjectSendSMSAlert.addProperty("issue_type", issueType);

        CommonLogic.showTestLog(TAG, "sendSMSAlert params: " + jsonObjectSendSMSAlert);

        ApiCall.callApi(
                TAG,
                context,
                APIData.SEND_SMS,
                jsonObjectSendSMSAlert,   // 👈 you were passing null here (bug)
                "post",
                new ApiCall.ApiResponseCallback() {

                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {

                        CommonLogic.showTestLog(TAG, "SMS send successfully");

                        if (status) {
                            if (callback != null) {
                                callback.onSuccess(message);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure(message);
                            }
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {

                        CommonLogic.showTestLog(TAG, "SMS send failed: " + errorMessage);

                        if (callback != null) {
                            callback.onFailure("SMS send failed");
                        }
                    }
                }
        );
    }


    public static void showVehicleNotFoundDialog(Activity activity) {
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
        imgStatus.setImageResource(R.drawable.check_vehicle_icon);

        btnLogin.setText("Ok");
        tvTitle.setText("Vehicle not found");
        tvMessage.setText("Vehicle not found in our database, please check the vehicle number or try after some time");

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }


    public static void handleIvrCall(String TAG, Activity activity, Dialog callRequestDialog, String agent, String receiver) {

        CommonLogic.showTestLog(TAG, "===== IVR CALL FLOW START =====");
        CommonLogic.showTestLog(TAG, "Agent(Login Number): " + agent);
        CommonLogic.showTestLog(TAG, "Receiver: " + receiver);

        // 1. Permission check first
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            CommonLogic.showTestLog(TAG, "❌ Permission NOT granted");

            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_PHONE_NUMBERS
                    }, 101);

            return;
        }

        List<SubscriptionInfo> simList = getAvailableSims(activity);

        CommonLogic.showTestLog(TAG, "Total SIM Found: " + simList.size());

        // If no SIM found
        if (simList.isEmpty()) {
            CommonLogic.showTestLog(TAG, "❌ No SIM available in device");

            Toast.makeText(activity,
                    "No SIM available in device",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if logged-in number exists in device
        boolean exists = isNumberInDevice(agent, simList);

        CommonLogic.showTestLog(TAG, "Is Login Number Exist in Device: " + exists);

        if (exists) {

            CommonLogic.showTestLog(TAG, "✅ Number matched → Direct call flow");

            showCallRequestDialog(TAG, activity, callRequestDialog, agent, receiver);

        } else {

            CommonLogic.showTestLog(TAG, "⚠ Number NOT matched → Showing SIM selection dialog");

            showSimSelectionDialog(TAG, activity, callRequestDialog, receiver, simList);
        }
    }


    public static List<SubscriptionInfo> getAvailableSims(Context context) {

        List<SubscriptionInfo> simList = new ArrayList<>();

        SubscriptionManager subscriptionManager =
                (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            CommonLogic.showTestLog("SIM_CHECK", "❌ READ_PHONE_STATE permission not granted");

            return simList;
        }

        simList = subscriptionManager.getActiveSubscriptionInfoList();

        if (simList == null) {
            CommonLogic.showTestLog("SIM_CHECK", "❌ SIM list is NULL");
            return new ArrayList<>();
        }

        // Log each SIM detail
        for (SubscriptionInfo info : simList) {

            CommonLogic.showTestLog("SIM_CHECK",
                    "SIM Carrier: " + info.getCarrierName());

            CommonLogic.showTestLog("SIM_CHECK",
                    "SIM Number: " + info.getNumber());

            CommonLogic.showTestLog("SIM_CHECK",
                    "Subscription ID: " + info.getSubscriptionId());
        }

        return simList;
    }


    public static boolean isNumberInDevice(String loginNumber, List<SubscriptionInfo> simList) {

        CommonLogic.showTestLog("SIM_MATCH",
                "Matching login number: " + loginNumber);

        for (SubscriptionInfo info : simList) {

            String simNumber = info.getNumber();

            CommonLogic.showTestLog("SIM_MATCH",
                    "Checking with SIM number: " + simNumber);

            if (simNumber != null &&
                    simNumber.contains(loginNumber)) {

                CommonLogic.showTestLog("SIM_MATCH",
                        "✅ MATCH FOUND");

                return true;
            }
        }

        CommonLogic.showTestLog("SIM_MATCH",
                "❌ No matching SIM number found");

        return false;
    }


    public static void showSimSelectionDialog(String TAG, Activity activity, Dialog callRequestDialog, String receiver, List<SubscriptionInfo> simList) {

        CommonLogic.showTestLog(TAG, "Opening SIM Selection Dialog");

        String[] simNames = new String[simList.size()];

        for (int i = 0; i < simList.size(); i++) {

            simNames[i] =
                    "SIM " + (i + 1) +
                            " - " + simList.get(i).getCarrierName() + " (" + simList.get(i).getNumber() + ")";

            CommonLogic.showTestLog(TAG,
                    "Dialog Option: " + simNames[i]+ " (" + simList.get(i).getNumber() + ")" );
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select Number to Make Call");

        builder.setItems(simNames, (dialog, which) -> {

            String simNumber = simList.get(which).getNumber();

            CommonLogic.showTestLog(TAG,
                    "User selected SIM index: " + which);

            CommonLogic.showTestLog(TAG,
                    "Selected SIM Number: " + simNumber);

            showCallRequestDialog(TAG, activity, callRequestDialog, simNumber, receiver);

        });

        builder.show();
    }






    public static void showCallRequestDialog(String TAG, Activity activity, Dialog callRequestDialog, String agent, String receiver) {

        // Transparent background
        if (callRequestDialog.getWindow() != null) {
            callRequestDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            callRequestDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView btnLogin = callRequestDialog.findViewById(R.id.btnLogin);
        TextView tvTitle = callRequestDialog.findViewById(R.id.tvTitle);
        TextView tvMessage = callRequestDialog.findViewById(R.id.tvMessage);
        ImageView imgStatus = callRequestDialog.findViewById(R.id.imgStatus);
        imgStatus.setImageResource(R.drawable.connect_icon);
        ScrollView scrollMessage = callRequestDialog.findViewById(R.id.scrollMessage);

        btnLogin.setText("Request Call");
        btnLogin.setVisibility(View.VISIBLE);
        tvTitle.setText("Important Notice");

        String message = "This call feature is strictly for emergency use only.\n" +
                "\n" +
                "Please do not use this service for personal or non-emergency purposes.\n" +
                "\n" +
                "For security and compliance reasons, all calls are monitored.\n" +
                "\n" +
                "To place a call, you must first submit a request. The call will be initiated only after approval, and you will receive the call shortly.\n" +
                "\n" +
                "Misuse of this feature may lead to account suspension.";

        tvMessage.setText(message);

        tvMessage.setMovementMethod(new ScrollingMovementMethod());


        if (agent.equalsIgnoreCase(receiver)) {
            btnLogin.setText("Retry");
            tvTitle.setText("Can't Make a Call");
            tvMessage.setText("Can't make a call on this number, Please try any other contact number");
        } else {
            int lines = message.length() / 40;   // approx chars per line
            int baseHeight = 60;                 // min height in dp
            int extraPerLine = 12;               // dp per line

            int finalHeightDp = baseHeight + (lines * extraPerLine);

            setViewHeight(scrollMessage, finalHeightDp);
        }

        btnLogin.setOnClickListener(v -> {

            if (agent.equalsIgnoreCase(receiver)) {
                callRequestDialog.dismiss();
                return;
            }

            btnLogin.setVisibility(View.GONE);
            tvTitle.setText("Initiating Call");
            tvMessage.setText("We are initiating your call request.\n" +
                    "Please wait while the connection is being established.");

            CommonMethods.sendCallAlert(TAG, activity, agent, receiver);
        });

        callRequestDialog.show();
    }

    private static void setViewHeight(View view, int heightInDp) {

        int heightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                heightInDp,
                view.getResources().getDisplayMetrics()
        );

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = heightPx;
        view.setLayoutParams(params);
    }

    public static void onUserLogin(String TAG, String newUserId) {

        // 🔥 MOST IMPORTANT LINE
//        OneSignal.logout();

        // 🔥 small delay so SDK really resets identity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            OneSignal.getUser().getPushSubscription().optIn();

            OneSignal.login(newUserId);

            CommonLogic.showTestLog(TAG,
                    "🟢 CLEAN OneSignal re-login with: " + newUserId);

        }, 2000);
    }

    public static void showSecureView(Activity activity, View secureView) {
        if (secureView == null) {
            secureView = LayoutInflater.from(activity)
                    .inflate(R.layout.layout_secure_preview, null);
            activity.addContentView(secureView,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));
        }

        secureView.setVisibility(View.VISIBLE);
    }

    public static void hideSecureView(View secureView) {
        if (secureView != null) {
            secureView.setVisibility(View.GONE);
        }
    }

}
