package com.digivahan.ui.Activities.emergencyContacts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import com.digivahan.data.model.EmergencyContactModel;
import com.digivahan.databinding.ActivityEditEmergencyContactsBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.ui.Activities.documentVault.DocumentVaultActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEmergencyContacts extends BaseActivity {

    String TAG = "EditEmergencyContactsData";
    ActivityEditEmergencyContactsBinding binding;
    String hit_type = "add";
    PreferencesManager manager;

    boolean isCameraSelected = false;
    File publicImageFile;
    String publicImagePath = "", publicImage64 = "";
    AshDialog loadingDialog;

    CompressFileData selectedImage;

    EmergencyContactModel emergencyContactModelData;

    List<String> relationList = Arrays.asList(
            "Select Relation",
            "Friend",
            "Father",
            "Mother",
            "Son",
            "Daughter",
            "Brother",
            "Sister",
            "Husband",
            "Wife"/*,
            "Grandfather",
            "Grandmother",
            "Grandson",
            "Granddaughter",
            "Uncle",
            "Aunt",
            "Cousin",
            "Nephew",
            "Niece",
            "Father-in-law",
            "Mother-in-law",
            "Brother-in-law",
            "Sister-in-law",
            "Son-in-law",
            "Daughter-in-law",
            "Guardian",
            "Spouse",
            "Partner",
            "Friend",
            "Colleague",
            "Manager",
            "Supervisor",
            "Teacher",
            "Student",
            "Neighbor",
            "Relative",
            "Emergency Contact",
            "Legal Representative",
            "Caretaker",
            "Doctor",
            "Client",
            "Employer",
            "Employee"*/
    );


    ArrayAdapter<String> relationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditEmergencyContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        manager = new PreferencesManager(EditEmergencyContacts.this);

        loadingDialog = new AshDialog(EditEmergencyContacts.this, "Please wait", "");

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.GONE);
        binding.toolbarLayout.tvTitle.setText("Edit Emergency Contact");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        try {
            hit_type = getIntent().getStringExtra("hit_type");
            assert hit_type != null;
            if (hit_type.equalsIgnoreCase("add")) {
                binding.toolbarLayout.tvTitle.setText("Add Emergency Contact");
                binding.btnUpdate.setText("Add");
            } else {
                binding.toolbarLayout.tvTitle.setText("Edit Emergency Contact");
                emergencyContactModelData = (EmergencyContactModel) getIntent().getSerializableExtra("emergencyContactItemData");
            }
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        getOnBackPressedDispatcher().addCallback(EditEmergencyContacts.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });


        CommonLogic.setUserInPutFiledData(binding.firstNameField, R.drawable.profile_icon1, "First Name", "name", false, binding.scrollView, binding.btnUpdate);
        CommonLogic.setUserInPutFiledData(binding.lastNameField, R.drawable.profile_icon1, "Last Name (optional)", "name", false, binding.scrollView, binding.btnUpdate);
//        CommonLogic.setUserInPutFiledData(binding.relationField, R.drawable.relation_icon, "Enter your Relation", "", true, binding.scrollView, binding.btnUpdate);
        CommonLogic.setUserInPutFiledData(binding.phoneField, R.drawable.call_icon2, "Phone Number", "phone", true, binding.scrollView, binding.btnUpdate);

        relationAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        relationList
                );

        relationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        binding.spinnerRelation.setAdapter(relationAdapter);


        binding.btnChangeImage.setOnClickListener(v -> {
            CommonLogic.showTestLog(TAG, "ImageClicked");
            CommonLogic.showPickImageDialog(EditEmergencyContacts.this,
                    CommonLogic.PROFILE_IMAGE_REQUEST, false, new CommonLogic.CameraSelectionCallback() {
                        @Override
                        public void onCameraSelected(boolean isCamera) {
                            isCameraSelected = isCamera;
                        }
                    });
        });


        binding.btnUpdate.setOnClickListener(v -> {

            String firstName = Objects.requireNonNull(binding.firstNameField.etInput.getText()).toString().trim();
            String lastName = Objects.requireNonNull(binding.lastNameField.etInput.getText()).toString().trim();
            String relation = binding.spinnerRelation.getSelectedItem().toString();
            String contactNo = Objects.requireNonNull(binding.phoneField.etInput.getText()).toString().trim();


            // Basic validation
            if (firstName.isEmpty()) {
                binding.firstNameField.etInput.setError("Please enter first name");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonLogic.isValidName(firstName)) {
                binding.firstNameField.etInput.setError(getString(R.string.name_length_error));
                Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
                return;
            } /*else if (!CommonLogic.isValidName(lastName)) {
                binding.lastNameField.etInput.setError(getString(R.string.name_length_error));
                Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
                return;
            }*/else if (contactNo.isEmpty()) {
                binding.phoneField.etInput.setError("Please enter contact number");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonLogic.isValidPhone(contactNo)) {
                binding.phoneField.etInput.setError("Invalid phone number");
                Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                return;
            } else if (contactNo.equalsIgnoreCase(manager.getUser().getPhone_number())) {
                binding.phoneField.etInput.setError("Can't use your own number");
                Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                return;
            } else if (relation == null
                    || relation.trim().isEmpty()
                    || relation.equalsIgnoreCase("Select Relation")) {

                Toast.makeText(this, "Please select Relation", Toast.LENGTH_SHORT).show();

                // 👉 Spinner focus + open dropdown
                binding.spinnerRelation.requestFocus();
                binding.spinnerRelation.performClick();

                return;
            }


            // to check data...
            JsonObject jsonObjectProfileData = new JsonObject();
            if (!hit_type.equalsIgnoreCase("add")) {
                jsonObjectProfileData.addProperty("contact_id", emergencyContactModelData.get_id());
                jsonObjectProfileData.addProperty("public_id", emergencyContactModelData.getPublic_id());
            }
            jsonObjectProfileData.addProperty("user_id", manager.getUserId());
            jsonObjectProfileData.addProperty("hit_type", hit_type);
            jsonObjectProfileData.addProperty("first_name", firstName);
            jsonObjectProfileData.addProperty("last_name", lastName);
            jsonObjectProfileData.addProperty("relation", relation);
            jsonObjectProfileData.addProperty("phone_number", contactNo);

            if (selectedImage != null) {
                jsonObjectProfileData.addProperty("profile_pic", selectedImage.getFilePath());
            }

            CommonLogic.showTestLog(TAG, jsonObjectProfileData.toString());


            RequestBody profileRequestFile = RequestBody.create(MediaType.parse("text/plain"), "");
            MultipartBody.Part publicProfileRequest = MultipartBody.Part.createFormData("profile_pic", "", profileRequestFile);
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
                profileRequestFile = RequestBody.create(selectedImage.getFileFormat(), MediaType.parse(mimeType));
                publicProfileRequest =
                        MultipartBody.Part.createFormData("profile_pic", selectedImage.getFileFormat().getName(), profileRequestFile);

                Log.i(TAG, "✅ Multipart file prepared successfully: " + selectedImage.getFileFormat().getName());
                Log.d(TAG, "🔍 Upload Details -> FileName: " + selectedImage.getFileFormat().getName() + ", Size: " + selectedImage.getFileFormat().length() / 1024 + " KB");

            }

            // 🧾 Prepare text fields
            RequestBody userIdRequest = RequestBody.create(MediaType.parse("text/plain"), manager.getUserId());
            RequestBody firstNameRequest = RequestBody.create(MediaType.parse("text/plain"), firstName);
            RequestBody lastNameRequest = RequestBody.create(MediaType.parse("text/plain"), lastName);
            RequestBody relationRequest = RequestBody.create(MediaType.parse("text/plain"), relation);
            RequestBody contactNoRequest = RequestBody.create(MediaType.parse("text/plain"), contactNo);


            loadingDialog.show();

            Call<JsonObject> addEditEmergencyContact = ApiClient.getApiService(EditEmergencyContacts.this).addEmergencyContact
                    (userIdRequest, publicProfileRequest, firstNameRequest, lastNameRequest, relationRequest, contactNoRequest);

            if (!hit_type.equalsIgnoreCase("add")) {
                addEditEmergencyContact = ApiClient.getApiService(EditEmergencyContacts.this).editEmergencyContact(userIdRequest, publicProfileRequest,
                        firstNameRequest, lastNameRequest, relationRequest, contactNoRequest, RequestBody.create(MediaType.parse("text/plain"), emergencyContactModelData.get_id())
                        , RequestBody.create(MediaType.parse("text/plain"), emergencyContactModelData.getPublic_id()));
            }

            // --- Step 2: Make API call ---
            addEditEmergencyContact.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                    // Always dismiss loader
                    loadingDialog.dismiss();

                    try {
                        JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                        CommonLogic.showTestLog(TAG, responseBody.toString());

                        boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                        String message = responseBody.has("message") ? responseBody.getString("message") : "";

                        Toast.makeText(EditEmergencyContacts.this, message, Toast.LENGTH_SHORT).show();

                        if (status) {
                            back();
                            /*try {
                                JsonObject userJson = responseBody.getAsJsonObject("user");

                                if (userJson != null) {
                                    // --- Step 3: Map to User Entity ---
                                    User userEntity = parseUserFromJson(userJson);

                                    prefs.saveUser(userEntity);
                                }

                                showSuccessDialog();
                            } catch (Exception e) {
                                CommonLogic.showTestLog(TAG, e.getMessage());
                            }*/
                        }
                    } catch (Exception e) {
                        CommonLogic.showTestLog(TAG, e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    loadingDialog.dismiss();
                    CommonLogic.showTestLog(TAG, "onFailure:- Updation failed. Please try again. " + t.getMessage());
                }
            });
        });

        if (!hit_type.equalsIgnoreCase("add") && emergencyContactModelData != null) {
            ImageHelperMethods.loadImage(TAG, EditEmergencyContacts.this, emergencyContactModelData.getProfile_pic(), binding.imgProfile, R.drawable.temp_profile_icon);
            binding.firstNameField.etInput.setText(emergencyContactModelData.getFirst_name());
            binding.lastNameField.etInput.setText(emergencyContactModelData.getLast_name());
            binding.spinnerRelation.setSelection(relationAdapter.getPosition(emergencyContactModelData.getRelation()));
            binding.phoneField.etInput.setText(emergencyContactModelData.getPhone_number());
        }
    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CommonLogic.PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            CommonLogic.handleImagePick(data, isCameraSelected, false, manager.getString(PreferencesManager.IMAGE_PATH, ""),
                    EditEmergencyContacts.this, binding.imgProfile, null, new CommonLogic.FileCallback() {
                        @Override
                        public void onFileReady(CompressFileData selectedImageData) {
                            selectedImage = selectedImageData;
                        }
                    });

            CommonLogic.showTestLog(TAG, "profileImagePath:- " + publicImagePath);
        }else if (requestCode == CommonLogic.CAMARA_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            isCameraSelected = true;
            CommonLogic.takePictureFromCamera(EditEmergencyContacts.this, CommonLogic.PROFILE_IMAGE_REQUEST, false);
        } else if (requestCode == CommonLogic.STORAGE_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            isCameraSelected = false;
            CommonLogic.choosePictureFromGallery(EditEmergencyContacts.this, CommonLogic.PROFILE_IMAGE_REQUEST);
        }
    }
}