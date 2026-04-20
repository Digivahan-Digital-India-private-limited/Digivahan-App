package com.digivahan.ui.Activities.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.CompressFileData;
import com.digivahan.data.model.User;
import com.digivahan.databinding.ActivityUpdatePublicDetailsBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePublicDetails extends BaseActivity {

    String TAG = "UpdatePublicDetailsData";
    ActivityUpdatePublicDetailsBinding binding;

    PreferencesManager manager;

    boolean isCameraSelected = false;

    CompressFileData selectedImage;

    AshDialog loadingDialog;

    private String selectedGender = "male";

    @SuppressLint({"SetTextI18n", "LogNotTimber"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdatePublicDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.notificationBellLayout.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Profile update");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                new AlertDialog.Builder(UpdatePublicDetails.this)
                        .setTitle("Details not saved!")
                        .setMessage("Are you sure you want to cancel?")
                        .setCancelable(false)
                        .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                            disableHideContentSecureForNextNavigation();
                            dialog.dismiss();
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        manager = new PreferencesManager(this);

        loadingDialog = new AshDialog(UpdatePublicDetails.this, "Please wait", "");

        setData();

        CommonLogic.setUserInPutFiledData(binding.nickNameField, R.drawable.profile_icon1, "Enter your Nick Name", "name", false, binding.scrollView, binding.btnUpdate);
        CommonLogic.setUserInPutFiledData(binding.addressField, R.drawable.live_location_icon1, "Enter your Address", "", false, binding.scrollView, binding.btnUpdate);
//        CommonLogic.setUserInPutFiledData(binding.ageField, R.drawable.calendar_icon, "Enter your Age", "number", true, binding.scrollView, binding.btnUpdate);

        setSelectedGender(selectedGender);


        binding.pickImage.setOnClickListener(v -> {
            CommonLogic.showTestLog(TAG, "ImageClicked");
            CommonLogic.showPickImageDialog(UpdatePublicDetails.this,
                    CommonLogic.IMAGE_CROP_REQUEST, true, new CommonLogic.CameraSelectionCallback() {
                        @Override
                        public void onCameraSelected(boolean isCamera) {
                            isCameraSelected = isCamera;
                        }
                    });
        });

        binding.ageTextLayout.setOnClickListener(v -> {
            binding.tvAge.setError(null);

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        // Format: DD-MM-YYYY
                        String selectedDate = selectedDay + "-"
                                + (selectedMonth + 1) + "-"
                                + selectedYear;

                        binding.tvAge.setText(selectedDate);
                    },
                    year,
                    month,
                    day
            );

            // 🔒 Allow selection only up to (today - 10 years)
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, -10);

            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

            datePickerDialog.show();
        });



        binding.btnUpdate.setOnClickListener(v -> {
            String nickName = binding.nickNameField.etInput.getText().toString().trim();
            String address = binding.addressField.etInput.getText().toString().trim();
            String age = binding.tvAge.getText().toString().trim();


            // Basic validation
            if (nickName.isEmpty()) {
                binding.nickNameField.etInput.setError("Please enter your nick name");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonLogic.isValidName(nickName)) {
                binding.nickNameField.etInput.setError(getString(R.string.name_length_error));
                Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
                return;
            } else if (address.isEmpty()) {
                binding.addressField.etInput.setError("Please enter your address");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (age.isEmpty()) {
                binding.tvAge.setError("Please enter your age");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            }else if (!CommonLogic.isValidAge(CommonMethods.getCurrentDate(UpdatePublicDetails.this,"dd-MM-yyyy"), age, 9)) {
//                binding.tvAge.setError("Please enter your correct age, should be 10 or above");
                Toast.makeText(this, "Please enter your correct age, should be 10 or above", Toast.LENGTH_SHORT).show();
                return;
            }  /*else if (selectedGender.isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            }*/


            // to log data
            JsonObject jsonObjectProfileData = new JsonObject();
            jsonObjectProfileData.addProperty("user_id", manager.getUserId());
            jsonObjectProfileData.addProperty("nick_name", nickName);
            jsonObjectProfileData.addProperty("address", address);
            jsonObjectProfileData.addProperty("age", age);
            jsonObjectProfileData.addProperty("gender", selectedGender);

            CommonLogic.showTestLog(TAG, jsonObjectProfileData.toString());

            loadingDialog.show();

            RequestBody profileRequestFile = RequestBody.create(MediaType.parse("text/plain"), "");
            MultipartBody.Part publicProfileRequest = MultipartBody.Part.createFormData("public_pic", "", profileRequestFile);
            if(selectedImage != null) {
                // Detect file extension & MIME type
                String extension = "";
                int dotIndex = selectedImage.getFilePath().lastIndexOf(".");
                if (dotIndex > 0) extension = selectedImage.getFilePath().substring(dotIndex + 1).toLowerCase();

                String mimeType = extension.equals("png") ? "image/png"
                        : (extension.equals("jpg") || extension.equals("jpeg")) ? "image/jpeg"
                        : (extension.equals("pdf")) ? "application/pdf"
                        : "application/octet-stream";

                Log.i(TAG, "🧩 File Extension: " + extension + " | MIME Type: " + mimeType);

                // Prepare file request
                profileRequestFile = RequestBody.create(selectedImage.getFileFormat(), MediaType.parse(mimeType));
                publicProfileRequest =
                        MultipartBody.Part.createFormData("public_pic", selectedImage.getFileFormat().getName(), profileRequestFile);

                Log.i(TAG, "✅ Multipart file prepared successfully: " + selectedImage.getFileFormat().getName());
                Log.d(TAG, "🔍 Upload Details -> FileName: " + selectedImage.getFileFormat().getName() +
                        ", Size: " + selectedImage.getFileFormat().length() / 1024 + " KB");

            }

            // 🧾 Prepare text fields
            RequestBody userIdRequest = RequestBody.create(MediaType.parse("text/plain"), manager.getUserId());
            RequestBody nickNameRequest = RequestBody.create(MediaType.parse("text/plain"), nickName);
            RequestBody addressRequest = RequestBody.create(MediaType.parse("text/plain"), address);
            RequestBody ageRequest = RequestBody.create(MediaType.parse("text/plain"), age);
            RequestBody genderRequest = RequestBody.create(MediaType.parse("text/plain"), selectedGender);

            // --- Step 2: Make API call ---
            ApiClient.getApiService(UpdatePublicDetails.this).setProfilePublicDetails(userIdRequest, publicProfileRequest, nickNameRequest,
                    addressRequest, ageRequest, genderRequest).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    try {
                        JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                        CommonLogic.showTestLog(TAG, responseBody.toString());

                        boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                        String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

                        Toast.makeText(UpdatePublicDetails.this, message, Toast.LENGTH_SHORT).show();

                        if (status) {
                            try {
                                // --- Step 3: Map to User Entity ---
                                User userEntity = CommonLogic.parseUserFromJson(TAG, responseBody);

                                manager.saveUser(userEntity);

                                finish();

                            } catch (Exception e) {
                                CommonLogic.showTestLog(TAG, e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        CommonLogic.showTestLog(TAG, e.getMessage());
                    }

                    loadingDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    loadingDialog.dismiss();
                    CommonLogic.showTestLog(TAG, "onFailure:- Failed to update. Please try again.");
                }
            });
        });

    }

    private void setData() {
        binding.nickNameField.etInput.setText(manager.getUser().getNick_name());
        binding.addressField.etInput.setText(manager.getUser().getAddress());
        binding.tvAge.setText(manager.getUser().getAge());
        setSelectedGender(manager.getUser().getGender());

        int tempImage = R.drawable.temp_profile_icon;

        if (manager.getUser().getGender().equalsIgnoreCase("male")){
            tempImage = R.drawable.boy_avatar;
        }else if (manager.getUser().getGender().equalsIgnoreCase("female")){
            tempImage = R.drawable.girl_avatar;
        }

        Glide.with(UpdatePublicDetails.this)
                .load(manager.getUser().getPublic_pic()) // Load from File object or path
                .apply(new RequestOptions()
                        .placeholder(tempImage)  // shown while loading
                        .error(tempImage)              // shown if loading fails
                        .centerCrop()                              // or .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))  // cache for better performance
                .into(binding.profileImage);

        int completedFields = 0;

        if (!manager.getUser().getNick_name().isEmpty()) completedFields += 1;
        if (!manager.getUser().getAddress().isEmpty()) completedFields += 1;
        if (!manager.getUser().getAge().isEmpty()) completedFields += 1;
        if (!manager.getUser().getGender().isEmpty()) completedFields += 1;
        if (!manager.getUser().getPublic_pic().isEmpty()) completedFields += 1;

// ✅ Convert to float to avoid integer division
        int progress = (int) (((float) completedFields / 5) * 100);

// ✅ Set progress
        binding.progressCircle.setProgress(progress);
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == CommonLogic.IMAGE_CROP_REQUEST && resultCode == RESULT_OK) {
                Bitmap croppedImage = null;

                if (isCameraSelected) {
                    // ✅ CAMERA: load bitmap from saved file path
                    String path = manager.getString(PreferencesManager.IMAGE_PATH, "");

                    CommonLogic.showTestLog(TAG, "📸 Camera image path: " + path);

                    if (path == null || path.isEmpty()) {
                        CommonLogic.showTestLog(TAG, "❌ Camera image path is empty");
                        return;
                    }

                    File imageFile = new File(path);
                    if (!imageFile.exists()) {
                        CommonLogic.showTestLog(TAG, "❌ Camera image file does not exist");
                        return;
                    }

                    croppedImage = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

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

                    CommonLogic.showTestLog(TAG, "🖼️ Gallery URI: " + selectedUri);

                    croppedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedUri);
                }

                if (croppedImage == null) {
                    CommonLogic.showTestLog(TAG, "❌ Bitmap is null");
                    return;
                }


                CommonLogic.showCropOptionDialog(UpdatePublicDetails.this, CommonLogic.getUriFromBitmap(Objects.requireNonNull(croppedImage), UpdatePublicDetails.this),
                        CommonLogic.PROFILE_IMAGE_REQUEST
                        , "profile", new CommonLogic.CropImageCallback() {
                            @Override
                            public void onCropOptionCanceled() {
                                CommonLogic.handleImagePick(data, isCameraSelected, false, manager.getString(PreferencesManager.IMAGE_PATH, ""),
                                        UpdatePublicDetails.this, binding.profileImage, null, new CommonLogic.FileCallback() {
                                            @Override
                                            public void onFileReady(CompressFileData selectedImageData) {
                                                selectedImage = selectedImageData;
                                                CommonLogic.showTestLog(TAG, "selectedImageData: File- " + selectedImageData.getFileFormat() + " path: " + selectedImageData.getFilePath());
                                            }
                                        });
                            }
                        });

            } else if (requestCode == CommonLogic.PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK) {


                CommonLogic.handleImagePick(data, isCameraSelected, true, manager.getString(PreferencesManager.IMAGE_PATH, ""),
                        UpdatePublicDetails.this, binding.profileImage, null, new CommonLogic.FileCallback() {
                            @Override
                            public void onFileReady(CompressFileData selectedImageData) {
                                selectedImage = selectedImageData;
                                CommonLogic.showTestLog(TAG, "selectedImageData: File- " + selectedImageData.getFileFormat() + " path: " + selectedImageData.getFilePath());
                            }
                        });
            } else if (requestCode == CommonLogic.CAMARA_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
                isCameraSelected = true;
                CommonLogic.takePictureFromCamera(UpdatePublicDetails.this, CommonLogic.PROFILE_IMAGE_REQUEST, false);
            } else if (requestCode == CommonLogic.STORAGE_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
                isCameraSelected = false;
                CommonLogic.choosePictureFromGallery(UpdatePublicDetails.this, CommonLogic.PROFILE_IMAGE_REQUEST);
            }
        }catch (IOException e) {
//            throw new RuntimeException(e);
            CommonLogic.showTestLog(TAG, "Image issue: " + e.getMessage());
        }
    }

    private void setSelectedGender(String savedGender) {

        selectedGender = savedGender;

        for (int i = 0; i < binding.flexGender.getChildCount(); i++) {

            TextView genderView = (TextView) binding.flexGender.getChildAt(i);

            String genderText = genderView.getText().toString();

            if (genderText.equalsIgnoreCase(savedGender)) {
                // ✅ Select matched item
                genderView.setBackgroundResource(R.drawable.bg_card_selected);
//                genderView.setTextColor(Color.WHITE);
            } else {
                // ❌ Reset others
                genderView.setBackgroundResource(R.drawable.bg_card_unselected);
//                genderView.setTextColor(Color.BLACK);
            }

            // Keep click handling intact
            genderView.setOnClickListener(v -> {
                setSelectedGender(genderView.getText().toString().toLowerCase());
            });
        }
    }

}