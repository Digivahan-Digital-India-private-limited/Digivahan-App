package com.digivahan.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.net.ParseException;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ashu.ashuutils.models.CompressFileData;
import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.PasswordValidationResult;
import com.digivahan.data.model.User;
import com.digivahan.databinding.CustomInputFieldBinding;
import com.ashu.ashuutils.APIHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.digivahan.other.CustomDialog.AshDialog;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.ComponentRegistry;
import coil.ImageLoader;
import coil.decode.SvgDecoder;
import coil.request.ImageRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface CommonLogic {

    public static String TAG = "CommonLogicData";
    public static int IMAGE_CROP_REQUEST = 100, PROFILE_IMAGE_REQUEST = 101, STORAGE_PERMISSION_REQUEST_CODE = 102, CAMARA_PERMISSION_REQUEST_CODE = 103, DOCUMENT_REQUEST_CODE = 104;

    public static void showTestLog(String TAG, String message) {
        if (Constants.ENABLE_TESTING) {
            Log.d(TAG, message);
        }
    }

    public static void showTestToast(Context context, String message) {
        if (Constants.ENABLE_TESTING) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    // Call this from your activity to hide status bar
    @SuppressLint({"WrongConstant", "ObsoleteSdkInt"})
    public static void hideStatusBar(Activity activity) {
        Window window = activity.getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            // Below Android 11 — use legacy flags
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }

    public static void loadSvg(Context context, ImageView imageView, String url) {
        ImageLoader loader = new ImageLoader.Builder(context)
                .components(new ComponentRegistry.Builder()
                        .add(new SvgDecoder.Factory())
                        .build())
                .build();

        ImageRequest request = new ImageRequest.Builder(context)
                .data(url)
                .target(imageView)
                .build();

        loader.enqueue(request);
    }


    // to check interNet connected or not
    static boolean Connected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @SuppressLint("ObsoleteSdkInt")
    static String convertHtmlToString(String htmlText) {

        if (htmlText == null) return "";

        // Fix wrong newline format
//        htmlText = htmlText.replace("/n", "\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Spanned spannedText = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
            return spannedText.toString();
        } else {
            Spanned spannedText = Html.fromHtml(htmlText);
            return spannedText.toString();
        }
    }


    public static Fragment switchFragment(FragmentManager fm, Fragment targetFragment, Fragment activeFragment, int containerId) {
        final String TAG = "FragmentSwitcher";

        if (fm == null || targetFragment == null) {
            Log.e(TAG, "Invalid params: fm=" + fm + " target=" + targetFragment + " active=" + activeFragment);
            return activeFragment; // Return current if params are invalid
        }

        try {
            FragmentTransaction transaction = fm.beginTransaction();

            // Case 1: No active fragment yet (first load)
            if (activeFragment == null) {
                Log.d(TAG, "No active fragment. Adding: " + targetFragment.getClass().getSimpleName());
                transaction.add(containerId, targetFragment).commitAllowingStateLoss();
                return targetFragment;
            }

            // Case 2: Switching between fragments
            if (activeFragment == targetFragment) {
                Log.d(TAG, "Target fragment is already active: " + targetFragment.getClass().getSimpleName());
                return targetFragment; // Already showing
            }

            if (!targetFragment.isAdded()) {
                Log.d(TAG, "Adding fragment: " + targetFragment.getClass().getSimpleName() +
                        " | Hiding: " + activeFragment.getClass().getSimpleName());
                transaction.add(containerId, targetFragment).hide(activeFragment).commitAllowingStateLoss();
            } else {
                Log.d(TAG, "Showing fragment: " + targetFragment.getClass().getSimpleName() +
                        " | Hiding: " + activeFragment.getClass().getSimpleName());
                transaction.hide(activeFragment).show(targetFragment).commitAllowingStateLoss();
            }

            Log.i(TAG, "Switched to fragment: " + targetFragment.getClass().getSimpleName());
            return targetFragment; // New active fragment
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException while switching fragments", e);
            return activeFragment; // Fallback to current fragment
        } catch (Exception e) {
            Log.e(TAG, "Exception while switching fragments", e);
            return activeFragment; // Fallback to current fragment
        }
    }

    public static void setupField(CustomInputFieldBinding rootView, String fieldType, EditText etInput) {

        if (rootView != null) {
            etInput = rootView.etInput;
        }

        etInput.setFilters(new InputFilter[]{});
        etInput.setTransformationMethod(null);
        etInput.setError(null);

        if (rootView != null) {
            rootView.ivEye.setVisibility(View.GONE);
        }

        switch (fieldType.toLowerCase()) {

            // ================= EMAIL =================
            case "email":
                etInput.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                );
                break;

            // ================= PHONE =================
            case "phone":
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    etInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                }

                etInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(10),
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                if (!Character.isDigit(source.charAt(i))) return "";
                            }
                            return null;
                        }
                });
                break;

            // ================= NUMBER =================
            case "number":
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                etInput.setFilters(new InputFilter[]{
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                if (!Character.isDigit(source.charAt(i))) return "";
                            }
                            return null;
                        }
                });
                break;

            case "name":

                // 🔹 Keyboard: First letter uppercase, rest normal text
                etInput.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_FLAG_CAP_WORDS
                );

                // 🔹 Allow only letters and space
                etInput.setFilters(new InputFilter[]{
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                char c = source.charAt(i);
                                if (!Character.isLetter(c) && c != ' ')
                                    return "";
                            }
                            return null;
                        }
                });

                // 🔹 Force: First letter uppercase, rest lowercase
                EditText finalEtInput = etInput;
                etInput.addTextChangedListener(new TextWatcher() {

                    private boolean isEditing;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (isEditing || s.length() == 0) return;

                        isEditing = true;

                        String text = s.toString();
                        String formatted =
                                text.substring(0, 1).toUpperCase() +
                                        text.substring(1).toLowerCase();

                        if (!text.equals(formatted)) {
                            finalEtInput.setText(formatted);
                            finalEtInput.setSelection(formatted.length());
                        }

                        isEditing = false;
                    }
                });

                break;

            // ================= PAN CARD =================
            case "pan":
                etInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                etInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(10),
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                char c = source.charAt(i);
                                // PAN: AAAAA9999A
                                if (!Character.isLetterOrDigit(c)) return "";
                            }
                            return source.toString().toUpperCase();
                        }
                });
                break;

            // ================= PIN CODE =================
            case "pincode":
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                etInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(6),
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                if (!Character.isDigit(source.charAt(i))) return "";
                            }
                            return null;
                        }
                });
                break;

            // ================= AADHAAR =================
            case "aadhar":
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    etInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                }

                etInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(12),
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                if (!Character.isDigit(source.charAt(i))) return "";
                            }
                            return null;
                        }
                });
                break;


            // ================= POLLUTION (PUC) =================
            case "pollution":
                etInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                etInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(16),
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                char c = source.charAt(i);
                                if (!Character.isLetterOrDigit(c)) return "";
                            }
                            return source.toString().toUpperCase();
                        }
                });
                break;

            // ================= INSURANCE POLICY =================
            case "insurance":
                etInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                etInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(20),
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                char c = source.charAt(i);
                                if (!Character.isLetterOrDigit(c)) return "";
                            }
                            return source.toString().toUpperCase();
                        }
                });
                break;

// ================= RC NUMBER =================
            case "rc":
                etInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                etInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(15),
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                char c = source.charAt(i);
                                if (!Character.isLetterOrDigit(c)) return "";
                            }
                            return source.toString().toUpperCase();
                        }
                });
                break;


// ================= DRIVING LICENCE =================
            case "driving_licence":
                etInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    etInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                }

                etInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(16),
                        (source, start, end, dest, dstart, dend) -> {
                            for (int i = start; i < end; i++) {
                                char c = source.charAt(i);
                                if (!Character.isLetterOrDigit(c)) return "";
                            }
                            return source.toString().toUpperCase();
                        }
                });
                break;

            // ================= PASSWORD =================
            case "password":
                etInput.setInputType(InputType.TYPE_CLASS_TEXT);

                /*etInput.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                );*/
                etInput.setTransformationMethod(PasswordTransformationMethod.getInstance());

                if (rootView != null) {
                    rootView.ivEye.setVisibility(View.VISIBLE);
                    rootView.ivEye.setImageResource(R.drawable.ic_eye_closed);

                    rootView.ivEye.setOnClickListener(v -> {
                        if (rootView.etInput.getTransformationMethod()
                                instanceof PasswordTransformationMethod) {

                            rootView.etInput.setTransformationMethod(
                                    HideReturnsTransformationMethod.getInstance()
                            );
                            rootView.ivEye.setImageResource(R.drawable.ic_eye_open);
                        } else {
                            rootView.etInput.setTransformationMethod(
                                    PasswordTransformationMethod.getInstance()
                            );
                            rootView.ivEye.setImageResource(R.drawable.ic_eye_closed);
                        }
                        rootView.etInput.setSelection(rootView.etInput.getText().length());
                    });
                }
                break;

            default:
                etInput.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }
    }

    public static boolean validateField(
            EditText etInput,
            String fieldType
    ) {

        String value = etInput.getText().toString().trim();

        // Clear old error
        etInput.setError(null);

        if (value.isEmpty()) {
            etInput.setError("Field can't be empty");
            return false;
        }

        switch (fieldType.toLowerCase()) {

            // ================= EMAIL =================
            case "email":
                if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                    etInput.setError("Enter a valid email address");
                    return false;
                }
                return true;

            // ================= PHONE =================
            case "phone":
                if (value.length() != 10) {
                    etInput.setError("Enter a valid 10-digit mobile number");
                    return false;
                }
                return true;

            // ================= NAME =================
            case "name":
                if (value.length() < 2) {
                    etInput.setError("Name must be at least 2 characters");
                    return false;
                }
                return true;

            // ================= PAN CARD =================
            /*case "pan":
                if (!value.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
                    etInput.setError("Enter valid PAN (e.g. ABCDE1234F)");
                    return false;
                }
                return true;*/

            // ================= PIN CODE =================
            case "pincode":
                if (!value.matches("\\d{6}")) {
                    etInput.setError("Enter valid 6-digit PIN code");
                    return false;
                }
                return true;

            // ================= AADHAAR =================
            case "aadhar":
                if (!value.matches("\\d{12}")) {
                    etInput.setError("Enter valid 12-digit Aadhaar number");
                    return false;
                }
                return true;

            // ================= POLLUTION (PUC) =================
            case "pollution":
                if (value.length() < 8) {
                    etInput.setError("Enter valid Pollution certificate number");
                    return false;
                }
                return true;

            // ================= INSURANCE =================
            case "insurance":
                if (value.length() < 8) {
                    etInput.setError("Enter valid Insurance policy number");
                    return false;
                }
                return true;

            // ================= RC NUMBER =================
            /*case "rc":
                // Example: DL01AB1234
                if (!value.matches("[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}")) {
                    etInput.setError("Enter valid RC number");
                    return false;
                }
                return true;*/

            // ================= DRIVING LICENCE =================
           /* case "driving_licence":
                // Example: DL-0420110149646
                if (!value.matches("[A-Z]{2}[0-9]{13}")) {
                    etInput.setError("Enter valid Driving Licence number");
                    return false;
                }
                return true;*/

            // ================= PASSWORD =================
            case "password":
                PasswordValidationResult result =
                        CommonLogic.validatePassword(value, 3);

                if (!result.isValid) {
                    etInput.setError(result.message);
                    return false;
                }
                return true;

            default:
                if (value.isEmpty()) {
                    etInput.setError("This field is required");
                    return false;
                }
                return true;
        }
    }


    public static boolean isValidPhone(String phone) {

        if (phone == null) return false;

        phone = phone.trim();

        // 1️⃣ Must be exactly 10 digits
        if (!phone.matches("^[0-9]{10}$")) {
            return false;
        }

        // 2️⃣ All digits same (0000000000, 1111111111)
        char firstChar = phone.charAt(0);
        boolean allSame = true;
        for (int i = 1; i < phone.length(); i++) {
            if (phone.charAt(i) != firstChar) {
                allSame = false;
                break;
            }
        }
        if (allSame) return false;

        // 3️⃣ Sequential numbers (1234567890 or 0987654321)
        String asc = "0123456789";
        String desc = "9876543210";
        if (asc.contains(phone) || desc.contains(phone)) {
            return false;
        }

        // 4️⃣ Indian mobile numbers usually start from 6–9
        char firstDigit = phone.charAt(0);
        if (firstDigit < '6') {
            return false;
        }

        return true;
    }


    public static boolean isValidNumber(String value) {
        return value != null && value.matches("^[0-9]+$");
    }

    public static boolean isValidName(String name) {
        if (name == null) return false;

        name = name.trim();

        return name.length() >= 3
                && name.matches("^[A-Za-z ]+$");
    }



    public static Uri setImageUri(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "image" + System.currentTimeMillis() + ".png");
        Uri photoURI = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                file
        );

        new PreferencesManager(context).setString(PreferencesManager.IMAGE_PATH, file.getAbsolutePath());
        return photoURI;
    }



    @SuppressLint("ClickableViewAccessibility")
    public static void setScrolling(EditText editText, ScrollView scrollView, View scrollWidget) {
        editText.setOnTouchListener((v, event) -> {
            scrollView.postDelayed(() -> {
                scrollView.smoothScrollTo(0, (int) scrollWidget.getY());
            }, 200);
            return false; // return false so default behavior (keyboard showing) still happens
        });
    }

    public static void focusToWidget(ScrollView scrollView, View scrollWidget) {

        scrollView.post(() ->
                scrollView.smoothScrollTo(
                        0,
                        scrollWidget.getTop()
                )
        );
    }

    public static void setUserInPutFiledData(CustomInputFieldBinding filedData, int resourceFile, String hint, String filedType, boolean setAutoScrolling, ScrollView scrollView, View scrollWidget) {
        filedData.etInput.setHint(hint);
        filedData.ivIcon.setImageResource(resourceFile);
        CommonLogic.setupField(filedData, filedType, null);
        if (setAutoScrolling) {
            CommonLogic.setScrolling(filedData.etInput, scrollView, scrollWidget);
        }
    }


    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public static void addAndRemoveDeviceData(String TAG, Activity activity, String user_id, String hit_type,
                                              String player_id) {

        // --- Step 1: Prepare request JSON ---
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("hit_by", hit_type);
        requestBody.addProperty("user_id", user_id);
        // UUID (unique device identifier)
        String uuid = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        requestBody.addProperty("uuid", uuid);

        if (hit_type.equalsIgnoreCase("add")) {
            // Device details
            requestBody.addProperty("device_name", Build.MANUFACTURER); // e.g. Oppo
            requestBody.addProperty("device_version", "Android " + Build.VERSION.RELEASE); // e.g. Android 14
            requestBody.addProperty("device_model", Build.MODEL); // e.g. OPPO F1 S

            // App version
            try {
                String versionName = activity.getPackageManager()
                        .getPackageInfo(activity.getPackageName(), 0).versionName;
                requestBody.addProperty("app_version", versionName);
            } catch (Exception e) {
                requestBody.addProperty("app_version", "unknown");
            }

            // Player ID (if you use OneSignal or Firebase)
            requestBody.addProperty("player_id", player_id != null ? player_id : "");

        }

        CommonLogic.showTestLog(TAG, "checkAppVersion params:- " + requestBody);

        // --- Step 2: Make API call ---
        ApiClient.getApiService(activity).addAndRemoveDeviceData(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                    boolean status = responseBody.getBoolean("status");

                    CommonLogic.showTestLog("VersionAPPData", String.valueOf(responseBody.getString("message")));


                } catch (Exception e) {
                    CommonLogic.showTestLog(TAG, "getOneSignalId: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                CommonLogic.showTestLog(TAG, "onFailure:- " + t.getMessage());

            }
        });


    }


    public static void checkAppVersion(String TAG, Activity activity) {

        PreferencesManager preferencesManager = new PreferencesManager(activity);

        String appPackageName = activity.getPackageName();

        ApiCall.callApi(TAG, activity, APIData.GET_ANDROID_APP_INFO, null, "get", new ApiCall.ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                try {
                    if (status) {
                        if (responseBody.has("data") && responseBody.getJSONObject("data").has("version") &&
                                !responseBody.getJSONObject("data").getString("version").isEmpty()) {

                            Log.d("VersionAPPData", String.valueOf(responseBody.getJSONObject("data").getString("version")));


                            String versionNumber = responseBody.getJSONObject("data").getString("version");

                                /*String appLink = msg.optString("app_update_link");
                                String app_update_desc = msg.optString("app_update_desc");*/

                            // Get the current version of the app
                            String currentVersion = getAppVersion(activity);

                            // Compare versions
                            if (!versionNumber.equalsIgnoreCase("0") && !versionNumber.equalsIgnoreCase(currentVersion)) {
                                // If the versions don't match, redirect to the Play Store
                                Log.e("TAG", "showSecurityDialog: dialog show");
                                final Dialog dialog = new Dialog(activity);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView(R.layout.block_dialog);

                                Window window = dialog.getWindow();
                                if (window != null) {

                                    window.setLayout(
                                            WindowManager.LayoutParams.MATCH_PARENT,
                                            WindowManager.LayoutParams.WRAP_CONTENT
                                    );

                                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                    // ✅ Add 10dp horizontal padding
                                    int paddingPx = (int) TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP,
                                            10,
                                            dialog.getContext().getResources().getDisplayMetrics()
                                    );

                                    window.getDecorView().setPadding(
                                            paddingPx,
                                            0,
                                            paddingPx,
                                            0
                                    );
                                }

                                dialog.setCancelable(false);


                                Button button = dialog.findViewById(R.id.exit_btn);
                                button.setVisibility(View.VISIBLE);
                                button.setText("Update");
                                TextView titleView = dialog.findViewById(R.id.title_text);
                                TextView descView = dialog.findViewById(R.id.desc_text);
                                titleView.setText("Need Update");
                                descView.setText("Please Update your app");
                                button.setOnClickListener(v -> {
                                    preferencesManager.setBoolean(PreferencesManager.KEY_IS_LOGGED_IN, false);
                                    redirectToPlayStore(activity, "https://play.google.com/store/apps/details?id=" + appPackageName);
                                });
                                dialog.show();
                            }

                        }
                    }
                } catch (JSONException e) {
//                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String errorMessage) {

            }
        });


    }

    // Method to get the current version of the app
    public static String getAppVersion(Activity activity) {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return packageInfo.versionName;
//            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    public static void redirectToPlayStore(Activity activity, String appLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appLink));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "No app found to open the link", Toast.LENGTH_SHORT).show();
        }
    }


    public static User parseUserFromJson(String TAG, JSONObject userJson) {
        User user = new User();

        try {

            // Extract basic_details
            JSONObject basicDetails = userJson.getJSONObject("basic_details");
            JSONObject publicDetails = userJson.getJSONObject("public_details");

            if (basicDetails != null) {
                user.setFirst_name(basicDetails.has("first_name") ? basicDetails.getString("first_name") : "");
                user.setLast_name(basicDetails.has("last_name") ? basicDetails.getString("last_name") : "");
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
                user.setProfile_id(basicDetails.has("profile_id") ? basicDetails.getString("profile_id") : "");
            }


            if (publicDetails != null) {
                user.setNick_name(publicDetails.has("nick_name") ? publicDetails.getString("nick_name") : "");
                user.setAddress(publicDetails.has("address") ? publicDetails.getString("address") : "");
                user.setAge(publicDetails.has("age") ? publicDetails.getString("age") : "");
                user.setGender(publicDetails.has("gender") ? publicDetails.getString("gender") : "");
                user.setPublic_pic(publicDetails.has("public_pic") ? publicDetails.getString("public_pic") : "");
                user.setPublic_id(basicDetails.has("public_id") ? basicDetails.getString("public_id") : "");
            }

            // You can extract public_details, address_book, garage, etc. if needed
            // For now, only the basic details are saved

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        return user;
    }


    public interface FileCallback {
        void onFileReady(CompressFileData selectedImageData);
    }

    /*public static void handleImagePick(Intent data, boolean isCamera, boolean isCropped,
                                       String cameraPath,
                                       Activity activity,
                                       ImageView imageView,
                                       ProgressDialog progressDialog,
                                       FileCallback callback) {

        String TAG = "ImagePickProcess";
        Log.i(TAG, "🟢 handleImagePick called - isCamera: " + isCamera + ", cameraPath: " + cameraPath);

        if (progressDialog != null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Loading image...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        ProgressDialog finalProgressDialog = progressDialog;

        executor.execute(() -> {
            String path = "";

            try {
                if (isCropped) {
                    Uri croppedImageUri = UCrop.getOutput(data);  // Get Cropped Image URI
                    if (croppedImageUri != null) {

// 🟢 Case 1: Single image (some devices)
                        if (data.getData() != null) {
                            croppedImageUri = data.getData();
                            Log.i(TAG, "🖼️ Picked image URI (getData): " + croppedImageUri);
                        }

// 🟢 Case 2: Single / Multiple images (Xiaomi / Android 13)
                        else if (data.getClipData() != null && data.getClipData().getItemCount() > 0) {
                            croppedImageUri = data.getClipData().getItemAt(0).getUri();
                            Log.i(TAG, "🖼️ Picked image URI (ClipData): " + croppedImageUri);
                        }

                        if (croppedImageUri == null) {
                            throw new IllegalStateException("Gallery returned null URI");
                        }

// ✅ COPY URI → FILE (do NOT resolve real path)
                        File imageFile = copyUriToCacheFile(activity, croppedImageUri);
                        if (imageFile == null || !imageFile.exists()) {
                            throw new IOException("Failed to copy image from URI");
                        }

                        path = imageFile.getAbsolutePath();
                    }
                } else {
                    if (isCamera) {
                        path = cameraPath;
                        Log.i(TAG, "📸 Using camera image path: " + path);
                    } else {
                        Uri selectedUri = null;

// 🟢 Case 1: Single image (some devices)
                        if (data.getData() != null) {
                            selectedUri = data.getData();
                            Log.i(TAG, "🖼️ Picked image URI (getData): " + selectedUri);
                        }

// 🟢 Case 2: Single / Multiple images (Xiaomi / Android 13)
                        else if (data.getClipData() != null && data.getClipData().getItemCount() > 0) {
                            selectedUri = data.getClipData().getItemAt(0).getUri();
                            Log.i(TAG, "🖼️ Picked image URI (ClipData): " + selectedUri);
                        }

                        if (selectedUri == null) {
                            throw new IllegalStateException("Gallery returned null URI");
                        }

// ✅ COPY URI → FILE (do NOT resolve real path)
                        File imageFile = copyUriToCacheFile(activity, selectedUri);
                        if (imageFile == null || !imageFile.exists()) {
                            throw new IOException("Failed to copy image from URI");
                        }

                        path = imageFile.getAbsolutePath();
                        Log.i(TAG, "📂 Copied image path: " + path);

                    }
                }

                if (path == null || path.isEmpty()) {
                    Log.e(TAG, "❌ Path is null or empty!");
                    handler.post(() -> {
                        if (finalProgressDialog != null && finalProgressDialog.isShowing())
                            finalProgressDialog.dismiss();
                        Toast.makeText(activity, "Unable to load this image, Please another image", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                File imageFile = new File(path);
                if (!imageFile.exists()) {
                    Log.e(TAG, "❌ File does not exist at: " + path);
                } else {
                    Log.i(TAG, "✅ File exists at: " + path);
                }

                CompressFileData selectedImageData = CommonLogic.compressImage(activity, imageFile, 1024);
                selectedImageData.setFilePath(path);

                Log.i(TAG, "🧩 Compression complete: " +
                        (selectedImageData.getFileFormat() != null ?
                                selectedImageData.getFileFormat().getAbsolutePath() : "null"));

                handler.post(() -> {
                    if (imageView != null) {
                        // Set image on UI
                        Glide.with(activity)
                                .load(imageFile)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                                @NonNull Target<Drawable> target, boolean isFirstResource) {
                                        Log.e(TAG, "❌ Glide failed to load image: " + e);
                                        if (finalProgressDialog != null && finalProgressDialog.isShowing())
                                            finalProgressDialog.dismiss();
                                        Toast.makeText(activity, "Unable to load this image, Please another image", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model,
                                                                   Target<Drawable> target,
                                                                   @NonNull DataSource dataSource, boolean isFirstResource) {
                                        Log.i(TAG, "✅ Image successfully loaded into ImageView.");
                                        if (finalProgressDialog != null && finalProgressDialog.isShowing())
                                            finalProgressDialog.dismiss();
                                        return false;
                                    }
                                })
                                .into(imageView);
                    }

                    // Callback
                    if (callback != null) {
                        Log.i(TAG, "📤 Returning file to callback: " + selectedImageData.getFilePath());
                        callback.onFileReady(selectedImageData);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "🔥 Exception in handleImagePick: " + e.getMessage(), e);
                handler.post(() -> {
                    if (finalProgressDialog != null && finalProgressDialog.isShowing())
                        finalProgressDialog.dismiss();
                    Toast.makeText(activity, "Unable to load this image, Please another image", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }*/

    public static Uri getUriFromBitmap(Bitmap bitmap, Context context) {
        try {
            // Create a temporary file in the cache directory
            File imagesDir = new File(context.getCacheDir(), "images");
            if (!imagesDir.exists()) imagesDir.mkdirs();

            File file = new File(imagesDir, "camera_image.jpg");

            FileOutputStream outputStream = new FileOutputStream(file);

            // Compress and save the bitmap to the file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Return a content URI using FileProvider
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File copyUriToCacheFile(Context context, Uri uri) throws IOException {

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        File cacheFile = new File(
                context.getCacheDir(),
                "IMG_" + System.currentTimeMillis() + ".jpg"
        );

        OutputStream outputStream = new FileOutputStream(cacheFile);

        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return cacheFile;
    }


    public static void handleDocumentPick(Intent data, Activity activity, FileCallback callback) {
        String TAG = "DocumentPickProcess";
        Log.i(TAG, "🟢 handleDocumentPick called.");

        if (data == null) {
            Log.e(TAG, "❌ No data received from document picker.");
            Toast.makeText(activity, "No document selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                List<File> pickedFiles = new ArrayList<>();

                if (data.getClipData() != null) {
                    // 📂 Multiple files selected
                    int count = data.getClipData().getItemCount();
                    Log.i(TAG, "📚 Multiple documents selected: " + count);
                    for (int i = 0; i < count; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        String filePath = getRealPathFromURI(activity, fileUri);
                        if (filePath != null) {
                            File file = new File(filePath);
                            if (file.exists()) {
                                pickedFiles.add(file);
                                Log.i(TAG, "✅ File added: " + filePath);
                            } else {
                                Log.w(TAG, "⚠️ File not found at: " + filePath);
                            }
                        }
                    }
                } else if (data.getData() != null) {
                    // 📄 Single file selected
                    Uri fileUri = data.getData();
                    String filePath = getRealPathFromURI(activity, fileUri);
                    Log.i(TAG, "📄 Single document selected: " + filePath);
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            pickedFiles.add(file);
                        } else {
                            Log.w(TAG, "⚠️ File not found at: " + filePath);
                        }
                    }
                }

                if (pickedFiles.isEmpty()) {
                    handler.post(() -> Toast.makeText(activity, "No valid file found.", Toast.LENGTH_SHORT).show());
                    return;
                }

                // ✅ Return the first file for now (can be extended for multiple)
                File selectedFile = pickedFiles.get(0);
                CompressFileData fileData = new CompressFileData();
                fileData.setFileFormat(selectedFile);
                fileData.setFilePath(selectedFile.getAbsolutePath());
                fileData.setString64BaseFormat(null); // optional base64 not needed for doc

                handler.post(() -> {
                    Log.i(TAG, "📤 Returning selected document: " + selectedFile.getAbsolutePath());
                    if (callback != null) callback.onFileReady(fileData);
                });

            } catch (Exception e) {
                Log.e(TAG, "🔥 Exception in handleDocumentPick: " + e.getMessage(), e);
                handler.post(() -> Toast.makeText(activity, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }


    @SuppressLint("Range")
    public static String getRealPathFromURI(Context context, Uri uri) {
        String TAG = "RealPathResolver";
        Log.i(TAG, "🔍 Resolving URI: " + uri);

        if (uri == null) return null;

        try {
            // Case 1: File URI (direct access)
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                Log.i(TAG, "📂 Detected file:// URI");
                return new File(uri.getPath()).getAbsolutePath();
            }

            // Case 2: Content URI (modern SAF Uris)
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String displayName = null;

                try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }

                if (displayName == null) {
                    displayName = "document_" + System.currentTimeMillis();
                }

                // Create temp file inside app cache
                File cacheDir = new File(context.getCacheDir(), "picked_docs");
                if (!cacheDir.exists()) cacheDir.mkdirs();

                File file = new File(cacheDir, displayName);

                try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                     OutputStream outputStream = new FileOutputStream(file)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        // ✅ Correct parameter order
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }

                Log.i(TAG, "✅ File copied to cache: " + file.getAbsolutePath());
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.e(TAG, "🔥 Error resolving URI: " + e.getMessage(), e);
        }

        Log.w(TAG, "⚠️ Could not resolve path for URI: " + uri);
        return null;
    }


    public static CompressFileData compressImage(Activity activity, File originalFile, int targetSizeKB) {
        CompressFileData data = new CompressFileData();
        String TAG = "ImagePickProcess";

        try {
            Log.i(TAG, "🧮 Starting compression for: " + originalFile.getAbsolutePath());
            if (!originalFile.exists()) {
                Log.e(TAG, "❌ Original file does not exist!");
                return data;
            }

            // Decode image with scaling options to avoid OOM for large files
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);
            Log.i(TAG, "📏 Original Image Dimensions: " + options.outWidth + "x" + options.outHeight);

            options.inSampleSize = calculateInSampleSize(options);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            if (bitmap == null) {
                Log.e(TAG, "❌ Failed to decode bitmap from file!");
                return data;
            }

            // Handle rotation
            ExifInterface exif = new ExifInterface(originalFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationAngle = switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> 90;
                case ExifInterface.ORIENTATION_ROTATE_180 -> 180;
                case ExifInterface.ORIENTATION_ROTATE_270 -> 270;
                default -> 0;
            };

            if (rotationAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                Log.i(TAG, "🔄 Rotated image by " + rotationAngle + " degrees");
            }

            // Prepare for compression
            int quality = 90;
            int targetSizeBytes = targetSizeKB * 1024;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            File appDir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), Constants.appImageFolder);
            if (!appDir.exists()) appDir.mkdirs();

            File compressedFile = new File(appDir,
                    "compressed_" + System.currentTimeMillis() + "_" + originalFile.getName());


            // Compress loop
            do {
                byteArrayOutputStream.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                Log.i(TAG, "📉 Compressing... quality=" + quality + ", size=" + byteArrayOutputStream.size() / 1024 + " KB");
                quality -= 5;
            } while (byteArrayOutputStream.size() > targetSizeBytes && quality > 10);

            // Write file
            try (FileOutputStream fos = new FileOutputStream(compressedFile)) {
                fos.write(byteArrayOutputStream.toByteArray());
            }

            Log.i(TAG, "✅ Compression finished, saved at: " + compressedFile.getAbsolutePath());

            // Save data
            String base64String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            data.setBitmapFormat(bitmap);
            data.setFileFormat(compressedFile);
            data.setString64BaseFormat(base64String);

            bitmap.recycle();

        } catch (Exception e) {
            Log.e(TAG, "🔥 Compression failed: " + e.getMessage(), e);
            data.setBitmapFormat(null);
            data.setFileFormat(null);
            data.setString64BaseFormat(null);
        }

        return data;
    }


    private static int calculateInSampleSize(BitmapFactory.Options options) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > 1000 || width > 1000) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= 1000 && (halfWidth / inSampleSize) >= 1000) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    public static String getFileNameFromPath(Context context, String pathOrUrl) {
        String TAG = "FileNameResolver";
        if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) {
            Log.w(TAG, "⚠️ Provided pathOrUrl is null or empty.");
            return "unknown_file";
        }

        try {
            Uri uri = Uri.parse(pathOrUrl);

            // Case 1️⃣: If it's a content:// URI (from MediaStore or FileProvider)
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            String fileName = cursor.getString(nameIndex);
                            Log.i(TAG, "📄 File name from content URI: " + fileName);
                            return fileName;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "🔥 Error reading content URI: " + e.getMessage());
                } finally {
                    if (cursor != null) cursor.close();
                }
            }

            // Case 2️⃣: If it's a file:// URI or a normal file path
            if (pathOrUrl.startsWith("file://") || new File(pathOrUrl).exists()) {
                File file = new File(pathOrUrl.replace("file://", ""));
                String fileName = file.getName();
                Log.i(TAG, "📁 File name from path: " + fileName);
                return fileName;
            }

            // Case 3️⃣: If it's a URL (e.g., https://example.com/file.pdf)
            if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
                String fileName = pathOrUrl.substring(pathOrUrl.lastIndexOf('/') + 1);
                try {
                    fileName = URLDecoder.decode(fileName, "UTF-8"); // handle %20 etc.
                } catch (Exception ignore) {
                }
                Log.i(TAG, "🌐 File name from URL: " + fileName);
                return fileName;
            }

            // Case 4️⃣: Fallback (extract after last slash)
            String fallback = pathOrUrl.substring(pathOrUrl.lastIndexOf('/') + 1);
            Log.w(TAG, "⚠️ Fallback file name used: " + fallback);
            return fallback;

        } catch (Exception e) {
            Log.e(TAG, "🔥 Exception in getFileNameFromPath: " + e.getMessage(), e);
            return "unknown_file";
        }
    }


    public static boolean isLocationEnabled(Context context) {

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }

        CommonLogic.showTestLog(
                TAG,
                "GPS Enabled: " + gpsEnabled + ", Network Enabled: " + networkEnabled
        );

        return gpsEnabled || networkEnabled;
    }

    public interface LocationResultListener {
        void onLocationReceived(Location location);

        void onLocationError(String error);
    }


    public static void getCurrentLocation(
            String TAG,
            Activity activity,
            FusedLocationProviderClient fusedLocationClient,
            LocationResultListener listener
    ) {

        /*if (isLocationRequestRunning) {
            CommonLogic.showTestLog(TAG, "⏳ Location request already running, skipping");
            return;
        }

        isLocationRequestRunning = true;*/

        CommonLogic.showTestLog(TAG, "getCurrentLocation() called");

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

//            isLocationRequestRunning = false;
            listener.onLocationError("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, location -> {

                    if (location != null) {

//                        isLocationRequestRunning = false;
                        listener.onLocationReceived(location);

                    } else {

                        CommonLogic.showTestLog(TAG, "LastLocation NULL → requesting fresh location");
                        requestNewLocation(TAG, activity, fusedLocationClient, listener);
                    }
                })
                .addOnFailureListener(e -> {
//                    isLocationRequestRunning = false;
                    listener.onLocationError(e.getMessage());
                });
    }


    private static void requestNewLocation(
            String TAG,
            Activity activity,
            FusedLocationProviderClient fusedLocationClient,
            LocationResultListener listener
    ) {

        CommonLogic.showTestLog(TAG, "requestNewLocation() called");

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500)
                .setNumUpdates(1);

        LocationSettingsRequest settingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                        .setAlwaysShow(true)
                        .build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);

        CommonLogic.showTestLog(TAG, "Checking location settings...");

        settingsClient.checkLocationSettings(settingsRequest)
                .addOnSuccessListener(response -> {

                    CommonLogic.showTestLog(TAG, "Location settings OK → requesting updates");

                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        CommonLogic.showTestLog(TAG, "Permission missing at request time");
                        listener.onLocationError("Permission missing");
                        return;
                    }

                    fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult result) {

                                    Location location = result.getLastLocation();

                                    CommonLogic.showTestLog(TAG, "onLocationResult() callback triggered");

                                    if (location != null) {
                                        CommonLogic.showTestLog(TAG,
                                                "Fresh Location → " +
                                                        location.getLatitude() + ", " +
                                                        location.getLongitude());

                                        listener.onLocationReceived(location);
                                    } else {
                                        listener.onLocationError("Location result null");
                                    }

                                    fusedLocationClient.removeLocationUpdates(this);
                                }
                            },
                            Looper.getMainLooper()
                    );
                })
                .addOnFailureListener(e -> {

                    CommonLogic.showTestLog(TAG,
                            "Location settings FAILED → " + e.getClass().getSimpleName());

                    // 🚨 THIS IS THE MISSING PART
                    if (e instanceof ResolvableApiException) {
                        try {
                            CommonLogic.showTestLog(TAG, "Requesting user to enable GPS");
                            ((ResolvableApiException) e)
                                    .startResolutionForResult(activity, 1001);
                        } catch (IntentSender.SendIntentException ex) {
                            listener.onLocationError("Unable to open GPS settings");
                        }
                    } else {
                        listener.onLocationError("Enable GPS for location");
                    }
                });
    }


    public static void openGoogleMaps(Context context, double latitude, double longitude) {
        try {
            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            context.startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(context, "Google Maps is not installed", Toast.LENGTH_SHORT).show();
        }
    }


    public static PasswordValidationResult validatePassword(String password, int strengthLevel) {

        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, "Password cannot be empty");
        }

        String message = "";

        switch (strengthLevel) {

            // 🔹 Level 1: Any string
            case 1:
                return new PasswordValidationResult(true, "Valid password");

            // 🔹 Level 2: Minimum length
            case 2:
                if (password.length() < 6)
                    return new PasswordValidationResult(false, "Password must be at least 6 characters");
                return new PasswordValidationResult(true, "Valid password");

            // 🔹 Level 3: Length + letters + numbers
            case 3:
                if (!password.matches(".*[A-Za-z].*")) {
                    message += "Password must contain letters\n";
                }

                if (!password.matches(".*\\d.*")) {
                    message += "Password must contain at least one number\n";
                }

                if (password.length() < 8) {
                    message += "Password must be at least 8 characters";
                }

                if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*") || password.length() < 8)
                    return new PasswordValidationResult(false, message);

                return new PasswordValidationResult(true, "Valid password");

            // 🔹 Level 4: Strong password
            case 4:
                if (!password.matches(".*[A-Z].*")) {
                    message = "Password must contain an uppercase letter\n";
                }

                if (!password.matches(".*[a-z].*")) {
                    message = "Password must contain a lowercase letter\n";
                }

                if (!password.matches(".*\\d.*")) {
                    message = "Password must contain a number\n";
                }

                if (!password.matches(".*[!@#$%^&*()_+=\\-].*")) {
                    message = "Password must contain a special character\n";
                }

                if (password.length() < 8) {
                    message = "Password must be at least 8 characters";
                }

                if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[a-z].*") ||
                        !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*()_+=\\-].*") || password.length() < 8) {
                    return new PasswordValidationResult(false, message);
                }

                return new PasswordValidationResult(true, "Strong password");

            // 🔹 Level 5: Very strong password
            case 5:
                if (password.contains(" ")) {
                    message = "Password must not contain spaces\n";
                }

                if (!password.matches(".*[A-Z].*")) {
                    message = "Password must contain an uppercase letter\n";
                }

                if (!password.matches(".*[a-z].*")) {
                    message = "Password must contain a lowercase letter\n";
                }

                if (!password.matches(".*\\d.*")) {
                    message = "Password must contain a number\n";
                }

                if (!password.matches(".*[!@#$%^&*()_+=\\-].*")) {
                    message = "Password must contain a special character\n";
                }

                if (password.length() < 10) {
                    message = "Password must be at least 10 characters";
                }

                if (password.contains(" ") || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") ||
                        !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*()_+=\\-].*") || password.length() < 10) {
                    return new PasswordValidationResult(false, message);
                }

                return new PasswordValidationResult(true, "Very strong password");

            default:
                return new PasswordValidationResult(false, "Invalid password strength level");
        }
    }


    public static boolean isValidAge(
            @NonNull String currentDateStr,
            @NonNull String selectedDobStr,
            int minimumAge
    ) {
        if (currentDateStr == null || selectedDobStr == null) return false;

        String DATE_FORMAT = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        sdf.setLenient(false);

        try {
            Date currentDate = sdf.parse(currentDateStr);
            Date dob = sdf.parse(selectedDobStr);

            if (currentDate == null || dob == null) return false;

            // ❌ Future DOB not allowed
            if (dob.after(currentDate)) return false;

            Calendar today = Calendar.getInstance();
            today.setTime(currentDate);

            Calendar birth = Calendar.getInstance();
            birth.setTime(dob);

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            // Birthday not yet occurred this year
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= minimumAge;

        } catch (ParseException e) {
            return false;
        } catch (java.text.ParseException e) {
//            throw new RuntimeException(e);
            return false;
        }


    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) return;

        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static long dateToTimestamp(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            sdf.setLenient(false);

            Date date = sdf.parse(dateStr);
            return date != null ? date.getTime() : -1;

        } catch (java.text.ParseException e) {
            return -1;
        }
    }

    public static String timestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }


    public static String getVehicleAge(String currentDateStr, String registrationDateStr) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            sdf.setLenient(false);

            Date currentDate = sdf.parse(currentDateStr);
            Date registrationDate = sdf.parse(registrationDateStr);

            if (currentDate == null || registrationDate == null) {
                return "";
            }

            // Convert to LocalDate (API-safe)
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(registrationDate);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(currentDate);

            int years = 0;
            int months = 0;

            // Calculate years
            while (true) {
                Calendar temp = (Calendar) startCal.clone();
                temp.add(Calendar.YEAR, 1);
                if (temp.after(endCal)) break;
                startCal.add(Calendar.YEAR, 1);
                years++;
            }

            // Calculate months
            while (true) {
                Calendar temp = (Calendar) startCal.clone();
                temp.add(Calendar.MONTH, 1);
                if (temp.after(endCal)) break;
                startCal.add(Calendar.MONTH, 1);
                months++;
            }

            // Remaining days
            long diffMillis = endCal.getTimeInMillis() - startCal.getTimeInMillis();
            int days = (int) (diffMillis / (1000 * 60 * 60 * 24));

            StringBuilder ageBuilder = new StringBuilder();

            if (years > 0) {
                ageBuilder.append(years)
                        .append(" year")
                        .append(years > 1 ? "s" : "");
            }

            if (months > 0) {
                if (ageBuilder.length() > 0) ageBuilder.append(", ");
                ageBuilder.append(months)
                        .append(" month")
                        .append(months > 1 ? "s" : "");
            }

            if (days > 0 || ageBuilder.length() == 0) {
                if (ageBuilder.length() > 0) ageBuilder.append(", ");
                ageBuilder.append(days)
                        .append(" day")
                        .append(days > 1 ? "s" : "");
            }

            return ageBuilder.toString();

        } catch (Exception e) {
            return "";
        }
    }

    public static String getTimeGreeting() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 0–23

        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening";
        } else {
//            return "Good Night";
            return "Good Evening";
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public static String convertUtcToDeviceTime(String TAG, String utcTime) {

        try {
            Log.d(TAG, "Input UTC Time: " + utcTime);
            Log.d(TAG, "Device SDK: " + Build.VERSION.SDK_INT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // ✅ API 26+
                ZoneId deviceZone = ZoneId.systemDefault();
                Log.d(TAG, "Device TimeZone (API 26+): " + deviceZone.getId());

                Instant instant = Instant.parse(utcTime);
                Log.d(TAG, "Parsed Instant (UTC): " + instant.toString());

                ZonedDateTime localTime = instant.atZone(deviceZone);
                Log.d(TAG, "Local ZonedDateTime: " + localTime.toString());

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

                String result = localTime.format(formatter);
                Log.d(TAG, "Formatted Local Time: " + result);

                return result;

            } else {

                // ✅ Below API 26
                TimeZone deviceTimeZone = TimeZone.getDefault();
                Log.d(TAG, "Device TimeZone (Legacy): " + deviceTimeZone.getID());

                SimpleDateFormat utcFormat =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = utcFormat.parse(utcTime);
                Log.d(TAG, "Parsed Date (UTC): " + date);

                SimpleDateFormat localFormat =
                        new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US);
                localFormat.setTimeZone(deviceTimeZone);

                String result = localFormat.format(date);
                Log.d(TAG, "Formatted Local Time: " + result);

                return result;
            }

        } catch (Exception e) {
            Log.e("TimeConversion", "Error converting time", e);
            return "";
        }
    }

    public static long parseServerDateTimeToMillis(String date, String time) {
        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            Date parsed = sdf.parse(date + " " + time);
            return parsed != null ? parsed.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }


    public static void shareAppWithImage(Context context, AshDialog loadingDialog) {
        final String TAG = "ShareAppDebug";

        // 1️⃣ Show dialog on UI thread
        if (loadingDialog != null && !loadingDialog.isVisible()) {
            loadingDialog.show();
            CommonLogic.showTestLog(TAG, "Loading dialog shown");
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                CommonLogic.showTestLog(TAG, "Background work started");

                // 2️⃣ Create cache directory
                File cachePath = new File(context.getCacheDir(), "images");
                cachePath.mkdirs();

                File file = new File(cachePath, "share_image.png");

                // 3️⃣ Convert drawable → bitmap (BACKGROUND THREAD)
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.sharing_app_img);
                if (drawable == null) {
                    throw new RuntimeException("Drawable not found");
                }

                int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 1080;
                int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 1080;

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                FileOutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                stream.close();

                Uri contentUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        file
                );

                CommonLogic.showTestLog(TAG, "Image prepared successfully");

                // 4️⃣ Switch back to UI thread
                mainHandler.post(() -> {
                    try {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

                        String message = CommonMethods.getAppSharingMessage(context);

                        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this app");
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        context.startActivity(
                                Intent.createChooser(shareIntent, "Share App Using")
                        );

                        CommonLogic.showTestLog(TAG, "Share chooser launched");

                    } catch (Exception e) {
                        CommonLogic.showTestLog(TAG, "UI error: " + e.getMessage());
                    } finally {
                        if (loadingDialog != null && loadingDialog.isVisible()) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, "Loading dialog dismissed");
                        }
                    }
                });

            } catch (Exception e) {
                CommonLogic.showTestLog(TAG, "Background error: " + e.getMessage());

                mainHandler.post(() -> {
                    if (loadingDialog != null && loadingDialog.isVisible()) {
                        loadingDialog.dismiss();
                    }
                });
            }
        });
    }


    public interface CropImageCallback {
        void onCropOptionCanceled();
    }

    public static void showCropOptionDialog(Activity context, Uri imageUri, int imageRequest, String imageName, CropImageCallback callback) {
        // Create a dialog instance
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.crop_image_dialog_design);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize dialog elements
        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        TextView btnNo = dialog.findViewById(R.id.btnNo);
        TextView btnYes = dialog.findViewById(R.id.btnYes);

//        dialogImage.setImageResource(R.drawable.crop_image_icon);

        // Set message
        dialogMessage.setText("Would you like to crop image for better visibility?");

        // true condition if user not have dl
        // Handle button clicks
        btnNo.setOnClickListener(v -> {
            dialog.dismiss();
            callback.onCropOptionCanceled();
        });
        btnYes.setOnClickListener(v -> {
            // Perform action on OK
            dialog.dismiss();
            CommonLogic.startCrop(context, imageUri, imageRequest, imageName);
        });

        // Show the dialog
        dialog.show();
    }


    public static void startCrop(Activity context, Uri sourceUri, int requestCode, String imageName) {
        Log.d(TAG, "startCrop Run");
        // Create a destination file in cache storage
        File file = new File(context.getCacheDir(), imageName + System.currentTimeMillis() + ".jpg");

        // Use correct FileProvider authority
        Uri destinationUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

        /*// UCrop options
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.withAspectRatio(1, 1);
        options.withMaxResultSize(256, 256);

        // **FIX: Use UCrop's built-in start() method**
        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(context, requestCode); */


        UCrop.Options options = new UCrop.Options();

// Compression
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);

// Circle overlay
        options.setCircleDimmedLayer(true);

// No square
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);

// Lock ratio
        options.withAspectRatio(1, 1);

// Toolbar confirm
        options.setToolbarTitle("Crop");
        options.setToolbarColor(ContextCompat.getColor(context, R.color.white));
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.white));

// UCrop rules
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(false);

// Scale only
        options.setAllowedGestures(
                UCropActivity.SCALE,
                UCropActivity.NONE,
                UCropActivity.NONE
        );

        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(context, requestCode);

    }


    public static void openNearbyService(Context context, String serviceType) {
        try {
            // Encode service type for URL safety
            String query = Uri.encode(serviceType);

            // Google Maps search URL
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + query + "+near+me");

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                // Open in browser if Maps not installed
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/" + query + "+near+me");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                context.startActivity(browserIntent);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show();
        }
    }


}
