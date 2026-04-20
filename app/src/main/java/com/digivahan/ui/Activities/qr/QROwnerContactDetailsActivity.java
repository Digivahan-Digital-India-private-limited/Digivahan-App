package com.digivahan.ui.Activities.qr;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.digivahan.R;
import com.digivahan.data.adapters.NotificationRequestItemAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.NotificationRequestCardItemModel;
import com.digivahan.data.model.User;
import com.digivahan.databinding.ActivityQrownerContectDetailsBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.documentVault.DocumentVaultActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QROwnerContactDetailsActivity extends BaseActivity {
    String TAG = "QROwnerContactDetailsActivityData";

    private NotificationRequestCardItemModel selectedReason;
    private boolean isCooldown = false;
    private CountDownTimer cooldownTimer;

    private static final long COOLDOWN_DURATION = 30000; // 30 seconds

    private NotificationRequestItemAdapter adapter;
    private List<NotificationRequestCardItemModel> cardList;

    ActivityQrownerContectDetailsBinding binding;

    AshDialog loadingDialog;

    PreferencesManager manager;

    int clickedPosition = -1, notificationLimit = 1;

    String vehicleOwnerId = "empty", chatRoomId = "empty";
    User vehicleOwnerDetails;
    String vehicleId = "", notification_type = "vehicle", notificationIssueType = "No parking", notificationIssueMessage = "Your car is parking in no parking zon.";


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cooldownTimer != null) {
            cooldownTimer.cancel();
            cooldownTimer = null;
        }

        if (loadingDialog != null && loadingDialog.isVisible()) {
            loadingDialog.dismiss();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrownerContectDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        loadingDialog = new AshDialog(QROwnerContactDetailsActivity.this, "Please wait", "");

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Scan QR Code");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });

        manager = new PreferencesManager(QROwnerContactDetailsActivity.this);

        try {

            CommonLogic.showTestLog(TAG, "🔹 QROwnerContactDetailsActivity → Intent received");

            if (getIntent().hasExtra("vehicleId")) {
                vehicleId = getIntent().getStringExtra("vehicleId");
                CommonLogic.showTestLog(TAG, "vehicleId: " + vehicleId);
            } else {
                CommonLogic.showTestLog(TAG, "vehicleId: NOT FOUND");
            }

            if (getIntent().hasExtra("vehicleOwnerDetails")) {
                vehicleOwnerDetails = (User) getIntent().getSerializableExtra("vehicleOwnerDetails");

                CommonLogic.showTestLog(TAG, "vehicleOwnerDetails received: " + (vehicleOwnerDetails != null));

                if (vehicleOwnerDetails != null) {

                    CommonLogic.showTestLog(TAG, "Owner Nick Name: " + vehicleOwnerDetails.getNick_name());
                    CommonLogic.showTestLog(TAG, "Owner Public Pic: " + vehicleOwnerDetails.getPublic_pic());

                    binding.toolbarLayout.tvTitle.setText(vehicleOwnerDetails.getNick_name());

                    ImageHelperMethods.loadImage(
                            TAG,
                            QROwnerContactDetailsActivity.this,
                            vehicleOwnerDetails.getPublic_pic(),
                            binding.toolbarLayout.ivProfile,
                            R.drawable.temp_profile_icon
                    );

                    if (getIntent().hasExtra("chatRoomId")) {
                        chatRoomId = getIntent().getStringExtra("chatRoomId");
                        CommonLogic.showTestLog(TAG, "chatRoomId from Intent: " + chatRoomId);
                    } else {
                        CommonLogic.showTestLog(TAG, "chatRoomId not found, calling getChatRoomId()");
                        getChatRoomId();
                    }
                }

            } else {
                CommonLogic.showTestLog(TAG, "vehicleOwnerDetails: NOT FOUND");
            }

            if (getIntent().hasExtra("receiverId")) {
                vehicleOwnerId = getIntent().getStringExtra("receiverId");
                CommonLogic.showTestLog(TAG, "receiverId: " + vehicleOwnerId);

                if (vehicleOwnerDetails == null) {
                    CommonLogic.showTestLog(TAG, "vehicleOwnerDetails is NULL, fetching user details");
                    getUserDetails(vehicleOwnerId);
                } else {
                    vehicleOwnerDetails.setUserId(vehicleOwnerId);
                    vehicleOwnerDetails.setVehicleId(vehicleId);

                    CommonLogic.showTestLog(TAG, "vehicleOwnerDetails updated with receiverId & vehicleId");
                }
            } else {
                CommonLogic.showTestLog(TAG, "receiverId: NOT FOUND");
            }

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, "Exception: " + e.getMessage());
        }



        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                new AlertDialog.Builder(QROwnerContactDetailsActivity.this)
                        .setTitle("Cancel Process")
                        .setMessage("Are you sure you want to cancel?")
                        .setCancelable(false)
                        .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


//        if (!vehicleOwner.equalsIgnoreCase(manager.getString(PreferencesManager.LAST_SCANNED_USER, ""))){
        if (vehicleOwnerId.equalsIgnoreCase(manager.getString(PreferencesManager.LAST_SCANNED_USER, ""))){
            manager.setLong(PreferencesManager.NOTIFICATION_SEND_COUNT, 0);
        }


        // Select Reason
        binding.chatBtn.setOnClickListener(v -> {
            openChatRequestPage();
        });

        // Initialize data
        cardList = new ArrayList<>();
        cardList.add(new NotificationRequestCardItemModel(R.drawable.parking_icon, "No Parking Sign Emergency", "1"));
        cardList.add(new NotificationRequestCardItemModel(R.drawable.congested_parking_icon, "Congested Parking", "2"));
//        cardList.add(new NotificationRequestCardItemModel(R.drawable.road_block_alert_icon, "Road Block Alert", "3"));
//        cardList.add(new NotificationRequestCardItemModel(R.drawable.blocked_vehicle_alert_icon, "Blocked Vehicle Alert", "4"));
        cardList.add(new NotificationRequestCardItemModel(R.drawable.car_lights_windows_left_open_icon, "Car Lights/Windows Left Open", "5"));
        cardList.add(new NotificationRequestCardItemModel(R.drawable.car_horn_alarm_going_on_icon, "Car Horn or Alarm Going On", "6"));
        cardList.add(new NotificationRequestCardItemModel(R.drawable.unknown_issue_alert_icon, "Unknown Issue Alert", "7"));
        cardList.add(new NotificationRequestCardItemModel(R.drawable.accident_alert_icon, "Accident Alert", "8"));
        cardList.add(new NotificationRequestCardItemModel(R.drawable.document_alert_icon, "Request for Document Access", "9"));

        // Setup RecyclerView with Grid Layout
        binding.rvNotificationRequestList.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new NotificationRequestItemAdapter(this, cardList, clickedPosition, (item, clickedPosition) -> {
            // This is called when an item is clicked
            /*Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedTitle", item.getTitle());
            resultIntent.putExtra("selectedIcon", item.getIconRes());
            setResult(RESULT_OK, resultIntent);
            finish();*/ // Return to the previous screen

            selectedReason = item;

        });
        binding.rvNotificationRequestList.setAdapter(adapter);

        // Send Notification with cooldown
        binding.btnSendNotification.setOnClickListener(v -> {

            if (isCooldown) {
                Toast.makeText(this, "Please wait before sending another request.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedReason == null) {
                Toast.makeText(this, "Please select a reason first!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!CommonLogic.isLocationEnabled(QROwnerContactDetailsActivity.this)) {
                CommonLogic.showTestLog(TAG, "Location services OFF, asking user to enable");

                Toast.makeText(this,
                        "Please enable location services",
                        Toast.LENGTH_SHORT).show();
                disableHideContentSecureForNextNavigation();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }



            /*if (manager.getLong(PreferencesManager.NOTIFICATION_SEND_COUNT, 0) >= notificationLimit){
                openChatRequestPage();
                return;
            }*/

            if (isFinishing() || isDestroyed()) {
                return;
            }
            loadingDialog.show();

            if (selectedReason.getRequestId().equalsIgnoreCase("1")){
                notificationIssueType = "no_parking";
                notificationIssueMessage = "Your car is parking in no parking zon.";
            } else if (selectedReason.getRequestId().equalsIgnoreCase("2")){
                notificationIssueType = "congested_parking";
                notificationIssueMessage = "your vehicle has been identified as causing parking congestion in the area.\n" +
                        "Kindly move your vehicle to a suitable parking space to avoid inconvenience to others.";
            } else if (selectedReason.getRequestId().equalsIgnoreCase("3")){
                notificationIssueType = "road_block_alert";
                notificationIssueMessage = "your vehicle has been identified as obstructing traffic flow and causing a road block.\n" +
                        "Please move your vehicle immediately to clear the way and ensure smooth movement for others.";
            } else if (selectedReason.getRequestId().equalsIgnoreCase("4")){
                notificationIssueType = "blocked_vehicle_alert";
                notificationIssueMessage = "your vehicle is blocking another parked vehicle.\n" +
                        "Please move your vehicle promptly to allow the other driver to exit smoothly.";
            } else if (selectedReason.getRequestId().equalsIgnoreCase("5")){
                notificationIssueType = "car_lights_windows_left_open";
                notificationIssueMessage = "⚠\uFE0F Your car lights or windows are open. Please check your vehicle immediately for safety.";
            } else if (selectedReason.getRequestId().equalsIgnoreCase("6")){
                notificationIssueType = "car_horn_alarm_going_on";
                notificationIssueMessage = "Your car alarm or horn is going off. Please check your vehicle immediately.";
            } else if (selectedReason.getRequestId().equalsIgnoreCase("7")){
                notificationIssueType = "unknown_issue_alert";
                notificationIssueMessage = "\uD83D\uDE97 An unknown issue has been detected with your vehicle. Please inspect it for safety.";
            } else if (selectedReason.getRequestId().equalsIgnoreCase("8")){
                disableHideContentSecureForNextNavigation();
                Intent requestInfoPage = new Intent(QROwnerContactDetailsActivity.this, ChatNotificationInfoRequestPage.class);
                notificationIssueType = "accident_alert";
                requestInfoPage.putExtra("notificationTittle", selectedReason.getTitle());
                requestInfoPage.putExtra("notificationIssueMessage", notificationIssueMessage);
                requestInfoPage.putExtra("notificationIssueType", notificationIssueType);
                requestInfoPage.putExtra("notification_type", notification_type);
                requestInfoPage.putExtra("chatRoomId", chatRoomId);
                vehicleOwnerDetails.setUserId(vehicleOwnerId);
                vehicleOwnerDetails.setVehicleId(vehicleId);
                requestInfoPage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
                startActivity(requestInfoPage);
                finish();
                return;
            } else if (selectedReason.getRequestId().equalsIgnoreCase("9")){
                notificationIssueType = "doc_access";
                notification_type = "doc_access";
                notificationIssueType = "Request for Document Access";
                notificationIssueMessage = "⚠\uFE0F You’ve received a request for document access. Please review and approve if appropriate.";
            }

            if(chatRoomId.isEmpty() || chatRoomId.equalsIgnoreCase("empty")){
                createChatRoom(vehicleOwnerId, notification_type, notificationIssueType, selectedReason.getTitle(), notificationIssueMessage,
                        vehicleId, selectedReason.getRequestId().equalsIgnoreCase("9"));
            }
            else {
                sendNotification(vehicleOwnerId, notification_type, notificationIssueType, selectedReason.getTitle(), notificationIssueMessage,
                        vehicleId, selectedReason.getRequestId().equalsIgnoreCase("9"));
            }

        });

    }

    private void openChatRequestPage() {
        if (chatRoomId.isEmpty() || chatRoomId.equalsIgnoreCase("empty")){
            JsonObject jsonObjectChatRoom = new JsonObject();

            jsonObjectChatRoom.addProperty("type", "direct");

// Members array
            JsonArray membersArray = new JsonArray();
            membersArray.add(vehicleOwnerId);

            jsonObjectChatRoom.add("members", membersArray);

// Created by
            jsonObjectChatRoom.addProperty("createdBy", manager.getUserId());

            ApiCall.callApi(TAG,
                    QROwnerContactDetailsActivity.this,
                    APIData.CREATE_CHAT_ROOM,
                    jsonObjectChatRoom, "post",
                    new ApiCall.ApiResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject responseBody, boolean status, String message) {
                            try {
                                if (status) {
                                    chatRoomId = responseBody.getJSONObject("room").getString("_id");
                                    disableHideContentSecureForNextNavigation();
                                    Intent chatPage = new Intent(QROwnerContactDetailsActivity.this, ChatNotificationInfoRequestPage.class);
                                    chatPage.putExtra("notificationTittle", selectedReason.getTitle());
                                    chatPage.putExtra("notificationIssueMessage", notificationIssueMessage);
                                    chatPage.putExtra("notificationIssueType", notificationIssueType);
                                    chatPage.putExtra("notification_type", notification_type);
                                    chatPage.putExtra("chatRoomId", chatRoomId);
                                    chatPage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
                                    startActivity(chatPage);
                                    finish();

                                } else {
                                    Toast.makeText(QROwnerContactDetailsActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                                }
                                loadingDialog.dismiss();
                            } catch (JSONException e) {
                                loadingDialog.dismiss();
                                CommonLogic.showTestLog(TAG, e.getMessage());
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
        }
        else {
            disableHideContentSecureForNextNavigation();
            Intent chatPage = new Intent(QROwnerContactDetailsActivity.this, ChatNotificationInfoRequestPage.class);
            chatPage.putExtra("notificationTittle", selectedReason.getTitle());
            chatPage.putExtra("notificationIssueMessage", notificationIssueMessage);
            chatPage.putExtra("notificationIssueType", notificationIssueType);
            chatPage.putExtra("notification_type", notification_type);
            chatPage.putExtra("chatRoomId", chatRoomId);
            chatPage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
            startActivity(chatPage);
            finish();
        }
    }

    /*private void makeVaultDocAccessRequest() {
        JsonObject jsonObjectSendNotification = new JsonObject();
        jsonObjectSendNotification.addProperty("user_id", manager.getUserId());
        jsonObjectSendNotification.addProperty("vehicle_number", vehicleId);
        jsonObjectSendNotification.addProperty("requester_id", generateRequestCode());

        ApiCall.callApi(TAG,
                QROwnerContactDetailsActivity.this,
                APIData.VAULT_ACCESS,
                jsonObjectSendNotification, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        if (status) {
                            loadingDialog.dismiss();
                            Intent documentPage = new Intent(QROwnerContactDetailsActivity.this, DocumentVaultActivity.class);
                            vehicleOwnerDetails.setUserId(vehicleOwnerId);
                            vehicleOwnerDetails.setVehicleId(vehicleId);
                            documentPage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
                            startActivity(documentPage);
                            finish();
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(QROwnerContactDetailsActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
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
    }*/

    public static String generateRequestCode() {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000); // ensures 6 digits (100000–999999)
        return "REQ" + randomNumber;
    }


    private void startCooldown() {
        PreferencesManager prefs = new PreferencesManager(QROwnerContactDetailsActivity.this);

        // Increase call count
        long currentCount = prefs.getLong(PreferencesManager.NOTIFICATION_SEND_COUNT, 0) + 1;
        prefs.setLong(PreferencesManager.NOTIFICATION_SEND_COUNT, currentCount);

        isCooldown = true;
        binding.btnSendNotification.setEnabled(false);

        cooldownTimer = new CountDownTimer(COOLDOWN_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                loadingDialog.dismiss();
                binding.btnSendNotification.setText("Wait " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                isCooldown = false;

                openChatRequestPage();

                binding.btnSendNotification.setEnabled(true);
                binding.btnSendNotification.setText("Send Notification");

                if (currentCount >= 1) {
                    binding.btnSendNotification.setText("Send Notification Again!");
                }

                if (currentCount >= notificationLimit) {
                    binding.btnSendNotification.setText("Chat with Owner");
                }

                // Check if count has reached 3
                if (currentCount >= notificationLimit - 1) {
                    binding.chatBtnLayout.setVisibility(View.VISIBLE);
                }

                // Reset cooldown end time
                prefs.removeValue(PreferencesManager.KEY_COOLDOWN_END_TIME);
            }
        }.start();
    }

    private void getUserDetails(String vehicleOwnerId){
        JsonObject jsonObjectUserDetails = new JsonObject();
        jsonObjectUserDetails.addProperty("user_id", vehicleOwnerId);
        jsonObjectUserDetails.addProperty("details_type", "all");

        loadingDialog.show();
        ApiCall.callApi(TAG,
                QROwnerContactDetailsActivity.this,
                APIData.GET_USER_DETAILS,
                jsonObjectUserDetails, "post",
                new ApiCall.ApiResponseCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {

                                vehicleOwnerDetails = CommonLogic.parseUserFromJson(TAG, responseBody.getJSONObject("data"));



                                vehicleOwnerDetails.setUserId(vehicleOwnerId);
                                vehicleOwnerDetails.setVehicleId(vehicleId);

                                // in first item it is not setting data so we have done this.

                                binding.age1.setText((vehicleOwnerDetails.getNick_name().isEmpty() || vehicleOwnerDetails.getNick_name().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getNick_name());
                                binding.address.setText((vehicleOwnerDetails.getAddress().isEmpty() || vehicleOwnerDetails.getAddress().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getAddress());
                                binding.age.setText((vehicleOwnerDetails.getAge().isEmpty() || vehicleOwnerDetails.getAge().equalsIgnoreCase("N/A")) ? "Undefine" : CommonMethods.getDatePartFromDateString(vehicleOwnerDetails.getAge(), "age") + " year old");
                                binding.gender.setText((vehicleOwnerDetails.getGender().isEmpty() || vehicleOwnerDetails.getGender().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getGender());


                                int tempImage = R.drawable.temp_profile_icon;

                                if (manager.getUser().getGender().equalsIgnoreCase("male")){
                                    tempImage = R.drawable.boy_avatar;
                                }else if (manager.getUser().getGender().equalsIgnoreCase("female")){
                                    tempImage = R.drawable.girl_avatar;
                                }

                                if (vehicleOwnerDetails.getPublic_pic() != null && !vehicleOwnerDetails.getPublic_pic().isEmpty()){
                                    ImageHelperMethods.loadImage(TAG, QROwnerContactDetailsActivity.this, vehicleOwnerDetails.getPublic_pic(),
                                            binding.imgProfile, tempImage);
                                }else {
                                    binding.imgProfile.setImageResource(tempImage);
                                }

                                if (getIntent().hasExtra("chatRoomId")) {
                                    chatRoomId = getIntent().getStringExtra("chatRoomId");
                                }else {
                                    getChatRoomId();
                                }

                                // You can access the data if needed
                                // JSONObject data = responseBody.optJSONObject("data");
                            } else {
                                Toast.makeText(QROwnerContactDetailsActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(QROwnerContactDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    private void setTextOrNA(TextView tv, String value) {
        if (value != null && !value.trim().isEmpty()) {
            tv.setText(value);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setText("N/A");
            tv.setVisibility(View.VISIBLE);
        }
    }


    private void getChatRoomId() {
        JsonObject jsonObjectChatRoom = new JsonObject();

        jsonObjectChatRoom.addProperty("type", "direct");

// Members array
        JsonArray membersArray = new JsonArray();
        membersArray.add(vehicleOwnerId);

        jsonObjectChatRoom.add("members", membersArray);

// Created by
        jsonObjectChatRoom.addProperty("createdBy", manager.getUserId());

        ApiCall.callApi(TAG,
                QROwnerContactDetailsActivity.this,
                APIData.CREATE_CHAT_ROOM,
                jsonObjectChatRoom, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                chatRoomId = responseBody.getJSONObject("room").getString("_id");
                            } else {
                                loadingDialog.dismiss();
                                Toast.makeText(QROwnerContactDetailsActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, e.getMessage());
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
    }

    private void sendNotification(String receiverId, String notificationType, String issueType, String notificationTitle, String notificationMessage, String vehicleId, boolean isVaultAccess){
        JsonObject jsonObjectSendNotification = new JsonObject();
        jsonObjectSendNotification.addProperty("sender_id", manager.getUserId());
//        jsonObjectSendNotification.addProperty("sender_name", manager.getUser().getNick_name());
        jsonObjectSendNotification.addProperty("receiver_id", receiverId);
        jsonObjectSendNotification.addProperty("notification_type", notificationType);
        jsonObjectSendNotification.addProperty("issue_type", issueType);
//        jsonObjectSendNotification.addProperty("notification_title", notificationTitle);
        jsonObjectSendNotification.addProperty("notification_title", notificationTitle);
        jsonObjectSendNotification.addProperty("message", notificationMessage);
        jsonObjectSendNotification.addProperty("vehicle_id", vehicleId);

        CommonLogic.showTestLog(TAG, "sendNotification param: " + jsonObjectSendNotification.toString());

//        sendMessage(notificationMessage, issueType);

        ApiCall.callApi(TAG,
                QROwnerContactDetailsActivity.this,
                APIData.SEND_NOTIFICATION,
                jsonObjectSendNotification, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        if (status) {
                            if (isVaultAccess){
                                disableHideContentSecureForNextNavigation();
                                Intent documentPage = new Intent(QROwnerContactDetailsActivity.this, DocumentVaultActivity.class);
                                documentPage.putExtra("docAccessType", "verify");
                                documentPage.putExtra("vehicleId", vehicleId);
                                documentPage.putExtra("vehicleOwnerId", vehicleOwnerId);
                                startActivity(documentPage);
                                finish();
                            }else {
                                manager.setString(PreferencesManager.LAST_SCANNED_USER, vehicleOwnerId);
                                adapter.setFixedNotification();
                                startCooldown();
                                Toast.makeText(QROwnerContactDetailsActivity.this, "Notification Sent Successfully", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(QROwnerContactDetailsActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
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
    }

    private void createChatRoom(String receiverId, String notificationType, String issueType, String notificationTitle, String notificationMessage, String vehicleId, boolean isVaultAccess){
        JsonObject jsonObjectChatRoom = new JsonObject();

        jsonObjectChatRoom.addProperty("type", "direct");

// Members array
        JsonArray membersArray = new JsonArray();
        membersArray.add(receiverId);

        jsonObjectChatRoom.add("members", membersArray);

// Created by
        jsonObjectChatRoom.addProperty("createdBy", manager.getUserId());


        ApiCall.callApi(TAG,
                QROwnerContactDetailsActivity.this,
                APIData.CREATE_CHAT_ROOM,
                jsonObjectChatRoom, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                chatRoomId = responseBody.getJSONObject("room").getString("_id");
                                sendNotification(vehicleOwnerId, notificationType, issueType, notificationTitle, notificationMessage,
                                        vehicleId, isVaultAccess);
                            } else {
                                Toast.makeText(QROwnerContactDetailsActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }

                    }

                    @Override
                    public void onError(String errorMessage) {
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(QROwnerContactDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void sendMessage(String message, String issueType){
        JsonObject jsonObjectMessage = new JsonObject();
        jsonObjectMessage.addProperty("chatId", chatRoomId);
        jsonObjectMessage.addProperty("senderId", manager.getUserId());
        jsonObjectMessage.addProperty("receiverId", vehicleOwnerId);
        jsonObjectMessage.addProperty("message", message);

        // ✅ Create a proper JSON array for attachments
        JsonArray attachmentsArray = new JsonArray();
        attachmentsArray.add(issueType);

        // ✅ Add array to main JSON
        jsonObjectMessage.add("attachments", attachmentsArray);

        CommonLogic.showTestLog(TAG, "sendMessage params: " + jsonObjectMessage.toString());

        CommonLogic.showTestLog(TAG, jsonObjectMessage.toString());

        ApiCall.callApi(TAG,
                QROwnerContactDetailsActivity.this,
                APIData.SEND_CHAT_MESSAGE,
                jsonObjectMessage, "Post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        CommonLogic.showTestLog(TAG, "sendMessage response: " + responseBody.toString());
                    }

                    @Override
                    public void onError(String errorMessage) {
                        CommonLogic.showTestLog(TAG, errorMessage);
                    }
                }
        );
    }
}