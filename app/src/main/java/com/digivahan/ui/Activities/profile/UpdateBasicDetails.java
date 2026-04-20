package com.digivahan.ui.Activities.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.digivahan.databinding.ActivityUpdateBasicDetailsBinding;
import com.digivahan.other.CustomDialog.AshDialog;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateBasicDetails extends BaseActivity {

    String TAG = "UpdateBasicDetailsData";
    ActivityUpdateBasicDetailsBinding binding;
    PreferencesManager manager;

    boolean isCameraSelected = false;

    CompressFileData selectedImage;
    AshDialog loadingDialog;

    List<String> occupationList = Arrays.asList(
            "Select Occupation",
            "IT / Software",
            "Business / Entrepreneur",
            "Finance / Banking",
            "Education",
            "Medical / Healthcare",
            "Government / Public Service",
            "Sales / Marketing",
            "Creative / Media",
            "Transport / Logistics",
            "Skilled / Technical",
            "Manufacturing / Industrial",
            "Retail / Shopkeeper",
            "Freelance / Self-Employed",
            "Agriculture / Farming",
            "Homemaker",
            "Student",
            "Intern / Trainee",
            "Unemployed / Job Seeker",
            "Retired",
            "Other"
    );

    ArrayAdapter<String> occupationAdapter;


    @Override
    protected void onResume() {
        super.onResume();
        setData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateBasicDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Profile update");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                new AlertDialog.Builder(UpdateBasicDetails.this)
                        .setTitle("Details not saved!")
                        .setMessage("Are you sure you want to cancel?")
                        .setCancelable(false)
                        .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            disableHideContentSecureForNextNavigation();
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


        manager = new PreferencesManager(this);

        loadingDialog = new AshDialog(UpdateBasicDetails.this, "Please wait", "");

        CommonLogic.setUserInPutFiledData(binding.firstNameField, R.drawable.profile_icon1, "Enter your First Name", "name", false, binding.scrollView, binding.btnUpdate);
        CommonLogic.setUserInPutFiledData(binding.lastNameField, R.drawable.profile_icon1, "Enter your Last Name", "name", false, binding.scrollView, binding.btnUpdate);
        CommonLogic.setUserInPutFiledData(binding.emailField, R.drawable.email_icon1, "Enter your Email", "email", false, binding.scrollView, binding.btnUpdate);
        CommonLogic.setUserInPutFiledData(binding.phoneField, R.drawable.call_icon2, "Enter your Number", "number", true, binding.scrollView, binding.btnUpdate);

        occupationAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        occupationList
                );

        occupationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        binding.spinnerOccupation.setAdapter(occupationAdapter);

        /*binding.spinnerOccupation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    // Default selection
                    return;
                }

                String selectedOccupation = parent.getItemAtPosition(position).toString();
                Log.d("Occupation", "Selected: " + selectedOccupation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/


        binding.phoneField.etInput.setFocusable(false);
        binding.phoneField.etInput.setClickable(false);


        binding.pickImage.setOnClickListener(v -> {
            CommonLogic.showTestLog(TAG, "ImageClicked");
            CommonLogic.showPickImageDialog(UpdateBasicDetails.this,
                    CommonLogic.IMAGE_CROP_REQUEST, true, new CommonLogic.CameraSelectionCallback() {
                        @Override
                        public void onCameraSelected(boolean isCamera) {
                            isCameraSelected = isCamera;
                        }
                    });
        });

        binding.btnUpdate.setOnClickListener(v -> {
            String firstName = binding.firstNameField.etInput.getText().toString().trim();
            String lastName = binding.lastNameField.etInput.getText().toString().trim();
            String email = binding.emailField.etInput.getText().toString().trim();
            String phone = binding.phoneField.etInput.getText().toString().trim();
            String occupation = binding.spinnerOccupation.getSelectedItem().toString();


            // Basic validation
            if (firstName.isEmpty()) {
                binding.firstNameField.etInput.setError("Please enter your first name");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonLogic.isValidName(firstName)) {
                binding.firstNameField.etInput.setError(getString(R.string.name_length_error));
                Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
                return;
            } else if (lastName.isEmpty()) {
                binding.lastNameField.etInput.setError("Please enter your last name");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonLogic.isValidName(lastName)) {
                binding.lastNameField.etInput.setError(getString(R.string.name_length_error));
                Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
                return;
            } else if (email.isEmpty()) {
                binding.emailField.etInput.setError("Please enter your email");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonLogic.isValidEmail(email)) {
                binding.emailField.etInput.setError("Invalid email");
                Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                return;
            } else if (phone.isEmpty()) {
                binding.phoneField.etInput.setError("Please enter your phone number");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonLogic.isValidPhone(phone)) {
                binding.phoneField.etInput.setError("Invalid phone number");
                Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                return;
            } else if (occupation.isEmpty() || occupation.equalsIgnoreCase("Select Occupation")) {
                Toast.makeText(this, "Please select occupation", Toast.LENGTH_SHORT).show();
                return;
            }


            // to log data.
            JsonObject jsonObjectProfileData = new JsonObject();
            jsonObjectProfileData.addProperty("user_id", manager.getUserId());
            jsonObjectProfileData.addProperty("first_name", firstName);
            jsonObjectProfileData.addProperty("last_name", lastName);
            jsonObjectProfileData.addProperty("occupation", occupation);

            CommonLogic.showTestLog(TAG, "Params: " + jsonObjectProfileData.toString());

            loadingDialog.show();

            // 👉 Empty part when no image is selected
            RequestBody empty = RequestBody.create(MediaType.parse("text/plain"), "");
            MultipartBody.Part profileRequest = MultipartBody.Part.createFormData("profile_pic", "", empty);

            if (selectedImage != null) {
                // Detect file extension & MIME type
                String extension = "";
                int dotIndex = selectedImage.getFilePath().lastIndexOf(".");
                if (dotIndex > 0)
                    extension = selectedImage.getFilePath().substring(dotIndex + 1).toLowerCase();

                String mimeType = extension.equals("png") ? "image/png"
                        : (extension.equals("jpg") || extension.equals("jpeg")) ? "image/jpeg"
                        : (extension.equals("pdf")) ? "application/pdf"
                        : "application/octet-stream";

                Log.i(TAG, "🧩 File Extension: " + extension + " | MIME Type: " + mimeType);

                // Prepare file request
                RequestBody profileRequestFile = RequestBody.create(selectedImage.getFileFormat(), MediaType.parse(mimeType));
                profileRequest =
                        MultipartBody.Part.createFormData("profile_pic", selectedImage.getFileFormat().getName(), profileRequestFile);

                Log.i(TAG, "✅ Multipart file prepared successfully: " + selectedImage.getFileFormat().getName());
                Log.d(TAG, "🔍 Upload Details -> FileName: " + selectedImage.getFileFormat().getName() + ", Size: " + selectedImage.getFileFormat().length() / 1024 + " KB");

            }

            // 🧾 Prepare text fields
            RequestBody userIdRequest = RequestBody.create(MediaType.parse("user_id"), manager.getUserId());
            RequestBody firstNameRequest = RequestBody.create(MediaType.parse("first_name"), firstName);
            RequestBody lastNameRequest = RequestBody.create(MediaType.parse("last_name"), lastName);
            RequestBody occupationRequest = RequestBody.create(MediaType.parse("occupation"), occupation);

            // --- Step 2: Make API call ---
            ApiClient.getApiService(UpdateBasicDetails.this).setProfileBasicDetails(userIdRequest,
                    profileRequest,
                    firstNameRequest, lastNameRequest, occupationRequest).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    try {
                        JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                        boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                        String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";

                        Toast.makeText(UpdateBasicDetails.this, message, Toast.LENGTH_SHORT).show();

                        if (status) {
                            try {
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
        try {
            binding.firstNameField.etInput.setText(manager.getUser().getFirst_name());
            binding.lastNameField.etInput.setText(manager.getUser().getLast_name());
            binding.emailField.etInput.setText(manager.getUser().getEmail());
            binding.phoneField.etInput.setText(manager.getUser().getPhone_number());
            binding.spinnerOccupation.setSelection(occupationAdapter.getPosition(manager.getUser().getOccupation()));

            Glide.with(UpdateBasicDetails.this)
                    .load(manager.getUser().getProfile_pic()) // Load from File object or path
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.temp_profile_icon)  // shown while loading
                            .error(R.drawable.temp_profile_icon)              // shown if loading fails
                            .centerCrop()                              // or .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.ALL))  // cache for better performance
                    .into(binding.profileImage);

            int completedFields = 0;

            if (!manager.getUser().getFirst_name().isEmpty()) completedFields += 1;
            if (!manager.getUser().getLast_name().isEmpty()) completedFields += 1;
            if (!manager.getUser().getEmail().isEmpty()) completedFields += 1;
            if (!manager.getUser().getPhone_number().isEmpty()) completedFields += 1;
            if (!manager.getUser().getOccupation().isEmpty()) completedFields += 1;
            if (!manager.getUser().getProfile_pic().isEmpty()) completedFields += 1;

// ✅ Convert to float to avoid integer division
            int progress = (int) (((float) completedFields / 6) * 100);

// ✅ Set progress
            binding.progressCircle.setProgress(progress);


        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }
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


                CommonLogic.showCropOptionDialog(UpdateBasicDetails.this, CommonLogic.getUriFromBitmap(Objects.requireNonNull(croppedImage), UpdateBasicDetails.this),
                        CommonLogic.PROFILE_IMAGE_REQUEST
                        , "profile", new CommonLogic.CropImageCallback() {
                            @Override
                            public void onCropOptionCanceled() {
                                CommonLogic.handleImagePick(data, isCameraSelected,  false, manager.getString(PreferencesManager.IMAGE_PATH, ""),
                                        UpdateBasicDetails.this, binding.profileImage, null, new CommonLogic.FileCallback() {
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
                        UpdateBasicDetails.this, binding.profileImage, null, new CommonLogic.FileCallback() {
                            @Override
                            public void onFileReady(CompressFileData selectedImageData) {
                                selectedImage = selectedImageData;
                                CommonLogic.showTestLog(TAG, "selectedImageData: File- " + selectedImageData.getFileFormat() + " path: " + selectedImageData.getFilePath());
                            }
                        });
            } else if (requestCode == CommonLogic.CAMARA_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
                isCameraSelected = true;
                CommonLogic.takePictureFromCamera(UpdateBasicDetails.this, CommonLogic.PROFILE_IMAGE_REQUEST, false);
            } else if (requestCode == CommonLogic.STORAGE_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
                isCameraSelected = false;
                CommonLogic.choosePictureFromGallery(UpdateBasicDetails.this, CommonLogic.PROFILE_IMAGE_REQUEST);
            }

        }catch (IOException e) {
//            throw new RuntimeException(e);
            CommonLogic.showTestLog(TAG, "Image issue: " + e.getMessage());
        }
    }

}