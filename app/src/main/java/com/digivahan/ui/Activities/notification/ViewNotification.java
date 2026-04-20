package com.digivahan.ui.Activities.notification;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;

import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.User;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.digivahan.R;
import com.digivahan.data.adapters.ChatImageAdapter;
import com.digivahan.data.model.NotificationItemModel;
import com.digivahan.databinding.ActivityViewNotificationBinding;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.ui.Activities.profile.UpdatePublicDetails;
import com.digivahan.ui.Activities.qr.ConnectEmergencyContactsPage;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;

public class ViewNotification extends BaseActivity {

    String TAG = "ViewNotificationData";
    ActivityViewNotificationBinding binding;
    NotificationItemModel notificationData;

    Dialog callRequestDialog;

    PreferencesManager manager;

    String chatRoomId = "", receiverId = "";

    User vehicleOwnerDetails;

    @Override
    protected void onResume() {
        super.onResume();

        /*if (callRequestDialog != null && callRequestDialog.isShowing()){
            callRequestDialog.dismiss();
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );


        manager = new PreferencesManager(ViewNotification.this);

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.GONE);
        binding.toolbarLayout.tvTitle.setText("Notification");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        try {

            if (getIntent().hasExtra("chatRoomId")) {
                chatRoomId = getIntent().getStringExtra("chatRoomId");
//                initializeChattingSystem(chatRoomId);
            }

            if (getIntent().hasExtra("vehicleOwnerDetails")) {
                vehicleOwnerDetails = (User) getIntent().getSerializableExtra("vehicleOwnerDetails");
            }

            if (getIntent().hasExtra("receiverId")) {
                receiverId = getIntent().getStringExtra("receiverId");
            }

            notificationData = (NotificationItemModel) getIntent().getSerializableExtra("notificationData");
            if (notificationData == null){
                binding.emptyText.setVisibility(View.VISIBLE);
                return;
            }

            if (notificationData.getIncident_proof() != null && !notificationData.getIncident_proof().isEmpty()){
                // Set up a GridLayoutManager with 2 columns
                GridLayoutManager layoutManager = new GridLayoutManager(ViewNotification.this, 2);
                binding.rvAccidentAlertImage.setLayoutManager(layoutManager);

                // Set up the adapter
                ChatImageAdapter adapter = new ChatImageAdapter(ViewNotification.this, notificationData.getIncident_proof(), binding.viewImage, binding.viewImageCrossIcon);
                binding.rvAccidentAlertImage.setAdapter(adapter);
            }else {
                binding.rvAccidentAlertImage.setVisibility(View.GONE);
            }

            binding.emptyText.setVisibility(View.GONE);
            setNotificationData(notificationData);
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
            binding.emptyText.setVisibility(View.VISIBLE);
        }

        binding.viewImageCrossIcon.setOnClickListener(view -> {
            binding.viewImage.setVisibility(View.GONE);
            binding.viewImageCrossIcon.setVisibility(View.GONE);
        });

        binding.sendWhatsAppBtn.setOnClickListener(view -> {
            disableHideContentSecureForNextNavigation();
            Intent chatPage = new Intent(ViewNotification.this, ChatActivity.class);
            chatPage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
            chatPage.putExtra("chatRoomId", chatRoomId);
            startActivity(chatPage);
            finish();
        });


    }

    private void back() {
        if (binding.viewImage.getVisibility() == View.VISIBLE) {
            binding.viewImage.setVisibility(View.GONE);
            binding.viewImageCrossIcon.setVisibility(View.GONE);
        }else {
            disableHideContentSecureForNextNavigation();
            finish();
        }
    }

    private void setNotificationData(NotificationItemModel notificationData) {
        int notificationIcon = R.drawable.parking_icon;

        if (notificationData.getIssue_type().equalsIgnoreCase("no_parking")){
            notificationIcon = R.drawable.parking_icon;
        } else if (notificationData.getIssue_type().equalsIgnoreCase("congested_parking")){
            notificationIcon = R.drawable.congested_parking_icon;
        } else if (notificationData.getIssue_type().equalsIgnoreCase("road_block_alert")){
            notificationIcon = R.drawable.road_block_alert_icon;
        } else if (notificationData.getIssue_type().equalsIgnoreCase("blocked_vehicle_alert")){
            notificationIcon = R.drawable.blocked_vehicle_alert_icon;
        } else if (notificationData.getIssue_type().equalsIgnoreCase("car_lights_windows_left_open")){
            notificationIcon = R.drawable.car_lights_windows_left_open_icon;
        } else if (notificationData.getIssue_type().equalsIgnoreCase("car_horn_alarm_going_on")){
            notificationIcon = R.drawable.car_horn_alarm_going_on_icon;
        } else if (notificationData.getIssue_type().equalsIgnoreCase("unknown_issue_alert")){
            notificationIcon = R.drawable.unknown_issue_alert_icon;
        } else if (notificationData.getIssue_type().equalsIgnoreCase("accident_alert")){
            notificationIcon = R.drawable.accident_alert_icon;

            /*callRequestDialog = new Dialog(ViewNotification.this);
            callRequestDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            callRequestDialog.setContentView(R.layout.dialog_password_changed);
            callRequestDialog.setCancelable(true);

            binding.toolbarLayout.audioCall.setVisibility(View.VISIBLE);
            binding.toolbarLayout.audioCall.setOnClickListener(view -> {
                CommonMethods.showCallRequestDialog(TAG, ViewNotification.this, callRequestDialog, manager.getUser().getPhone_number(), notificationData.getPhone_number());
            });*/
        }

        binding.issueIcon.setImageResource(notificationIcon);

        binding.titleText.setText(notificationData.getNotification_title());
        binding.descriptionText.setText(notificationData.getMessage());

        if (notificationData.getNotification_type().equalsIgnoreCase("vehicle")){
            binding.vehicleMessage.setText("There is a problem with your vehicle " + notificationData.getVehicle_id());
        } else {
            binding.vehicleMessage.setVisibility(View.GONE);
        }

        if (notificationData.getLatitude() != null && !notificationData.getLatitude().isEmpty() && !notificationData.getLatitude().equalsIgnoreCase("0")
        && notificationData.getLongitude() != null && !notificationData.getLongitude().isEmpty() && !notificationData.getLongitude().equalsIgnoreCase("0")){
            binding.locationCard.setVisibility(View.VISIBLE);
        }

        binding.locationCard.setOnClickListener(view -> {
            CommonLogic.openGoogleMaps(ViewNotification.this, Double.parseDouble(notificationData.getLatitude()), Double.parseDouble(notificationData.getLongitude()));
        });
    }
}