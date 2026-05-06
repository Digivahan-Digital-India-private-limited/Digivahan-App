package com.digivahan.ui.Activities.qr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashu.ashuutils.ImagePickerAppConstants;
import com.ashu.ashuutils.fileUtils.FileUtils;
import com.ashu.ashuutils.fileUtils.image.ImagePicker;
import com.ashu.ashuutils.fileUtils.image.ImagePickerWithoutPermission;
import com.ashu.ashuutils.fileUtils.image.ImageProcessingUtils;
import com.ashu.ashuutils.models.CompressFileData;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.digivahan.R;
import com.digivahan.data.adapters.SelectedImageListAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.SavedImageData;
import com.digivahan.data.model.User;
import com.digivahan.databinding.ActivityNotificationInfoRequestPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.ui.Activities.chat.CommonChattingMethods;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ChatNotificationInfoRequestPage extends BaseActivity implements OnMapReadyCallback {
    String TAG = "ChatNotificationInfoRequestPageData";
    ActivityNotificationInfoRequestPageBinding binding;

    User vehicleOwnerDetails;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 201;
    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyAoLQrc4Wc4bnY2LrZM2ru72bhegDt6Cdc";

    boolean isCameraSelected = false;
    ArrayList<SavedImageData> selectedImageList = new ArrayList<>();
    ArrayList<File> selectedImageFileList = new ArrayList<>();

    SelectedImageListAdapter selectedImageListAdapter;

    PreferencesManager manager;

    AshDialog loadingDialog;


    Location userLocation;

    private boolean shouldRetryLocation = false;

    String chatRoomId = "empty", notification_type = "vehicle", issue_type = "No parking", notificationTitle = "Your car is parking in no parking zon.";



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationInfoRequestPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        ImagePickerWithoutPermission.init(this);

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Scan QR Code");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        loadingDialog = new AshDialog(ChatNotificationInfoRequestPage.this, "Please wait", "");

        try {
            vehicleOwnerDetails = (User) getIntent().getSerializableExtra("vehicleOwnerDetails");
            chatRoomId = getIntent().getStringExtra("chatRoomId");
            notification_type = getIntent().getStringExtra("notification_type");
            issue_type = getIntent().getStringExtra("notificationIssueType");
            notificationTitle = getIntent().getStringExtra("notificationTittle");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        manager = new PreferencesManager(ChatNotificationInfoRequestPage.this);

        selectedImageListAdapter = new SelectedImageListAdapter(ChatNotificationInfoRequestPage.this, selectedImageList, loadingDialog);
        binding.rvSelectedImages.setAdapter(selectedImageListAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!CommonLogic.isLocationEnabled(ChatNotificationInfoRequestPage.this)) {
            CommonLogic.showTestLog(TAG, "Location services OFF, asking user to enable");

            Toast.makeText(this,
                    "Please enable location services",
                    Toast.LENGTH_SHORT).show();

            shouldRetryLocation = true;
            disableHideContentSecureForNextNavigation();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        } else {
            getCurrentLocation();
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        binding.mapView.onCreate(mapViewBundle);
        binding.mapView.getMapAsync(this);

        if (vehicleOwnerDetails != null) {
            binding.userNameTemp.setText((vehicleOwnerDetails.getNick_name().isEmpty() || vehicleOwnerDetails.getNick_name().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getNick_name());
            binding.address.setText((vehicleOwnerDetails.getAddress().isEmpty() || vehicleOwnerDetails.getAddress().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getAddress());
            binding.age.setText((vehicleOwnerDetails.getAge().isEmpty() || vehicleOwnerDetails.getAge().equalsIgnoreCase("N/A")) ? "Undefine" : CommonMethods.getDatePartFromDateString(vehicleOwnerDetails.getAge(), "age") + " year old");
            binding.gender.setText((vehicleOwnerDetails.getGender().isEmpty() || vehicleOwnerDetails.getGender().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getGender());

            int tempImage = R.drawable.temp_profile_icon;

            if (manager.getUser().getGender().equalsIgnoreCase("male")){
                tempImage = R.drawable.boy_avatar;
            }else if (manager.getUser().getGender().equalsIgnoreCase("female")){
                tempImage = R.drawable.girl_avatar;
            }

            ImageHelperMethods.loadImage(TAG, ChatNotificationInfoRequestPage.this, vehicleOwnerDetails.getPublic_pic(),
                    binding.imgProfile, tempImage);
        }

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                new AlertDialog.Builder(ChatNotificationInfoRequestPage.this)
                        .setTitle("Cancel Process")
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


        binding.requestBtn.setOnClickListener(v -> {

            if (binding.message.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please add issue", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!CommonLogic.isLocationEnabled(ChatNotificationInfoRequestPage.this)) {
                CommonLogic.showTestLog(TAG, "Location services OFF, asking user to enable");

                Toast.makeText(this,
                        "Please enable location services",
                        Toast.LENGTH_SHORT).show();

                disableHideContentSecureForNextNavigation();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }

            if (userLocation == null || userLocation.getLatitude() == 0 || userLocation.getLongitude() == 0){
                getCurrentLocation();
                return;
            }

            if (selectedImageList.size() < 2) {
                Toast.makeText(this, "Please add at least 2 images", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageList.size() > 4) {
                Toast.makeText(this, "You can add up to 4 images", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject jsonObjectSendNotification = new JsonObject();
            jsonObjectSendNotification.addProperty("sender_id", manager.getUserId());
//            jsonObjectSendNotification.addProperty("sender_name", manager.getUser().getNick_name());
            jsonObjectSendNotification.addProperty("receiver_id", vehicleOwnerDetails.getUserId());
            jsonObjectSendNotification.addProperty("notification_type", "chat");
            jsonObjectSendNotification.addProperty("issue_type", issue_type);
            if (!chatRoomId.equalsIgnoreCase("empty")) {
                jsonObjectSendNotification.addProperty("chat_room_id", chatRoomId);
            }
            jsonObjectSendNotification.addProperty("notification_title", notificationTitle);
            jsonObjectSendNotification.addProperty("message", binding.message.getText().toString().trim());
            jsonObjectSendNotification.addProperty("vehicle_id", vehicleOwnerDetails.getVehicleId());
            jsonObjectSendNotification.addProperty("seen_status", false);

            JsonArray attachmentsArray = new JsonArray();
            for (SavedImageData s : selectedImageList) {
                attachmentsArray.add(s.getImage_url());
            }

            jsonObjectSendNotification.add("incident_proof", attachmentsArray);

            if (userLocation != null) {
                jsonObjectSendNotification.addProperty("latitude", userLocation.getLatitude());
                jsonObjectSendNotification.addProperty("longitude", userLocation.getLongitude());
            }


            CommonLogic.showTestLog(TAG, "sendNotification param: " + jsonObjectSendNotification.toString());

            loadingDialog.show();

            ApiCall.callApi(TAG,
                    ChatNotificationInfoRequestPage.this,
                    APIData.SEND_NOTIFICATION,
                    jsonObjectSendNotification, "post",
                    new ApiCall.ApiResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject responseBody, boolean status, String message) {
                            CommonLogic.showTestLog(TAG, "SEND_NOTIFICATION: " + responseBody.toString());
                            if (status) {

                                CommonChattingMethods.sendMessageAPI(TAG, ChatNotificationInfoRequestPage.this, chatRoomId,
                                        binding.message.getText().toString().trim(), selectedImageList, String.valueOf(userLocation.getLatitude())
                                        , String.valueOf(userLocation.getLongitude()), manager, loadingDialog);

                                if (issue_type.equalsIgnoreCase("accident_alert")){
                                    disableHideContentSecureForNextNavigation();
//                                    CommonMethods.sendWhatsAppAlert(TAG, ChatNotificationInfoRequestPage.this, manager.getUser().getPhone_number(), vehicleOwnerDetails.getPhone_number());
                                    Intent emergencyPage = new Intent(ChatNotificationInfoRequestPage.this, ConnectEmergencyContactsPage.class);
                                    emergencyPage.putExtra("chatRoomId", chatRoomId);
                                    emergencyPage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
                                    startActivity(emergencyPage);
                                    finish();
                                }else {
                                    disableHideContentSecureForNextNavigation();
                                    Intent chatPage = new Intent(ChatNotificationInfoRequestPage.this, ChatActivity.class);
                                    chatPage.putExtra("timer", true);
                                    chatPage.putExtra("chatRoomId", chatRoomId);
                                    chatPage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
                                    startActivity(chatPage);
                                    loadingDialog.dismiss();
                                    finish();
                                }
                            } else {
                                loadingDialog.dismiss();
                                Toast.makeText(ChatNotificationInfoRequestPage.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(QROwnerContactDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        });

        binding.pickImage.setOnClickListener(v -> {
            CommonLogic.showTestLog(TAG, "ImageClicked");
            ImagePicker.showPickImageDialog(TAG, ChatNotificationInfoRequestPage.this, ImagePickerAppConstants.IMAGE_REQUEST, 0, new FileUtils.ResultCallback() {
                @Override
                public void onCameraSelected(boolean isCamera) {
                    isCameraSelected = isCamera;
                }

                @Override
                public void onGallerySelected() {
                    ImagePickerWithoutPermission.pickImage(TAG, uri -> {
                        ImageProcessingUtils.handleGalleryFromUri(
                                TAG,
                                ChatNotificationInfoRequestPage.this,
                                uri,
                                null,
                                false,
                                null,
                                selectedImageData -> {
                                    uploadImage(selectedImageData);
                                    CommonLogic.showTestLog(TAG, "selectedImageData: File- " + selectedImageData.getFileFormat() + " path: " + selectedImageData.getFilePath());
                                }
                        );
                    });
                }
            });
        });

    }


    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        googleMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = location;
                CommonLogic.showTestLog(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f));
                googleMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("You are here"));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();

        CommonLogic.showTestLog(TAG, "onResume() called");

        getCurrentLocation();

    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    protected void onPause() {
        binding.mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        binding.mapView.onSaveInstanceState(mapViewBundle);
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loadingDialog.show();

        loadingDialog.show();

        if (requestCode == ImagePickerAppConstants.IMAGE_REQUEST && resultCode == RESULT_OK) {
            ImageProcessingUtils.handleCameraImage(TAG, ChatNotificationInfoRequestPage.this,
                    FileUtils.getImagePath(ChatNotificationInfoRequestPage.this),
                    null, false,null, new FileUtils.FileCallback() {
                        @Override
                        public void onFileReady(CompressFileData selectedImageData) {

                            uploadImage(selectedImageData);

                        }
                    });

        }
    }

    private void uploadImage(CompressFileData selectedImageData) {
        CommonMethods.uploadSingleImage(ChatNotificationInfoRequestPage.this,
                selectedImageData.getFileFormat(), selectedImageData.getFilePath(), Constants.vehicleAccidents,
                new CommonMethods.ImageUploadCallback() {
                    @Override
                    public void onUploadSuccess(SavedImageData uploadedImage) {
//                                            Toast.makeText(EditEmergencyContacts.this, "Upload successful!", Toast.LENGTH_SHORT).show();
                        uploadedImage.setImageFile(selectedImageData.getFileFormat());
                        selectedImageList.add(uploadedImage);
                        selectedImageListAdapter.notifyDataSetChanged();
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onUploadError(String errorMessage) {
                        Toast.makeText(ChatNotificationInfoRequestPage.this, errorMessage, Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onUploadJSON(JSONObject errorMessage) {
                    }
                }
        );
    }


    private void getCurrentLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ChatNotificationInfoRequestPage.this);

        CommonLogic.showTestLog(TAG, "getCurrentLocation() called");

        CommonLogic.getCurrentLocation(TAG,
                ChatNotificationInfoRequestPage.this,
                fusedLocationClient, new CommonLogic.LocationResultListener() {
                    @Override
                    public void onLocationReceived(Location location) {
                        userLocation = location;
                    }

                    @Override
                    public void onLocationError(String error) {
                        CommonLogic.showTestLog(TAG, "location error: " + error);
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        CommonLogic.showTestLog(TAG, "onRequestPermissionsResult() called");

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                CommonLogic.showTestLog(TAG, "Location permission GRANTED by user");
                getCurrentLocation();

            } else {

                CommonLogic.showTestLog(TAG, "Location permission DENIED by user");

                Toast.makeText(this,
                        "Location permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}