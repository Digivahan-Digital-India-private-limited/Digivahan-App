package com.digivahan.ui.Activities.chat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;

import com.ashu.ashuutils.APIHelper;
import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.User;
import com.digivahan.databinding.ActivityChatUserProfileBinding;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;
//import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
//import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
//import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatUserProfile extends BaseActivity {
    String TAG = "ChatUserProfileData";

    ActivityChatUserProfileBinding binding;

    User vehicleOwnerDetails;

    private long FIVE_MINUTES = 1 * 60 * 1000; // 5 minutes

    long runningMinutes = 0 , runningSeconds = 0;

    private CountDownTimer countDownTimer;

    PreferencesManager manager;

    Dialog callRequestDialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (callRequestDialog != null && callRequestDialog.isShowing()){
            callRequestDialog.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setImageResource(R.drawable.menu_dot_icon);
        binding.toolbarLayout.tvTitle.setText("User Details");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        binding.toolbarLayout.ivBell.setOnClickListener(v -> {
            showAlertPopupMenu(v);
        });

        getOnBackPressedDispatcher().addCallback(ChatUserProfile.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        manager = new PreferencesManager(ChatUserProfile.this);

        try {
            vehicleOwnerDetails = (User) getIntent().getSerializableExtra("vehicleOwnerDetails");
            binding.toolbarLayout.tvTitle.setText(vehicleOwnerDetails.getNick_name() + " Details");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        callRequestDialog = new Dialog(ChatUserProfile.this);
        callRequestDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        callRequestDialog.setContentView(R.layout.dialog_password_changed);
        callRequestDialog.setCancelable(true);

        binding.audioCallLayout.setOnClickListener(view -> {
            CommonMethods.showCallRequestDialog(TAG, ChatUserProfile.this, callRequestDialog, manager.getUser().getPhone_number(), vehicleOwnerDetails.getPhone_number());
        });

//        setCallData();



//        binding.toolbarLayout.audioCall.setResourceID("zego_uikit_call");

        try {
            FIVE_MINUTES = getIntent().getLongExtra("timer", 0);
        } catch (Exception e) {
            FIVE_MINUTES = 0;
        }


        if (FIVE_MINUTES > 0) {
            initUI(true);
            startReverseTimer();
        }

        int tempImage = R.drawable.temp_profile_icon;

        if (manager.getUser().getGender().equalsIgnoreCase("male")){
            tempImage = R.drawable.boy_avatar;
        }else if (manager.getUser().getGender().equalsIgnoreCase("female")){
            tempImage = R.drawable.girl_avatar;
        }

        ImageHelperMethods.loadImage(TAG, ChatUserProfile.this, vehicleOwnerDetails.getPublic_pic(), binding.imgProfile, tempImage);

        binding.userNameTemp.setText((vehicleOwnerDetails.getNick_name().isEmpty() || vehicleOwnerDetails.getNick_name().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getNick_name());
        binding.address.setText((vehicleOwnerDetails.getAddress().isEmpty() || vehicleOwnerDetails.getAddress().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getAddress());
        binding.age.setText((vehicleOwnerDetails.getAge().isEmpty() || vehicleOwnerDetails.getAge().equalsIgnoreCase("N/A")) ? "Undefine" : CommonMethods.getDatePartFromDateString(vehicleOwnerDetails.getAge(), "age") + " year old");
        binding.gender.setText((vehicleOwnerDetails.getGender().isEmpty() || vehicleOwnerDetails.getGender().equalsIgnoreCase("N/A")) ? "Undefine" : vehicleOwnerDetails.getGender());


    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    /*public void setCallData(){
        PreferencesManager manager = new PreferencesManager(ChatUserProfile.this);
        ApiClient.getApiService(ChatUserProfile.this).commonGETMethodToHitAllAPIs(APIData.GET_APP_INFO).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                try {
                    boolean status = responseBody.has("success") && responseBody.getBoolean("success");

                    if (status) {
                        JSONObject appInfoData = responseBody.getJSONObject("data");
                        if (appInfoData.has("zigoApp_data")) {
                            JSONObject zigoApp_data = appInfoData.getJSONObject("zigoApp_data");
                            if (zigoApp_data.has("zigoAppID") && !zigoApp_data.getString("zigoAppID").isEmpty() && !zigoApp_data.getString("zigoAppID").equalsIgnoreCase("empty") &&
                                    zigoApp_data.has("zigoAppSignKey") && !zigoApp_data.getString("zigoAppSignKey").isEmpty() && !zigoApp_data.getString("zigoAppSignKey").equalsIgnoreCase("empty")){
                                long appID = zigoApp_data.getLong("zigoAppID");
                                String appSign = zigoApp_data.getString("zigoAppSignKey");
                                String userID = manager.getUserId() == null ? " " : manager.getUserId(); // yourUserID, userID should only contain numbers, English characters, and '_'.
                                String userName = (manager.getUser() == null || manager.getUser().getNick_name() == null) ? " " : manager.getUser().getNick_name();
                                // yourUserName

                                // 🔹 Log the details for debugging
                                Log.i("ZegoAppInit", "----------------------------------------");
                                Log.i("ZegoAppInit", "ZEGOCLOUD Initialization Details:");
                                Log.i("ZegoAppInit", "Application Context: " + getApplicationContext());
                                Log.i("ZegoAppInit", "App ID: " + appID);
                                Log.i("ZegoAppInit", "App Sign: " + appSign);
                                Log.i("ZegoAppInit", "User ID: " + userID);
                                Log.i("ZegoAppInit", "User Name: " + userName);
                                Log.i("ZegoAppInit", "----------------------------------------");


                                binding.audioCallLayout.setVisibility(View.VISIBLE);

                                binding.audioCallLayout.setOnClickListener(v -> {

                                    makeCall();

                                    ZegoUIKitPrebuiltCallService.sendInvitationWithUIChange(
                                            ChatUserProfile.this,
                                            Collections.singletonList(
                                                    new ZegoUIKitUser(
                                                            vehicleOwnerDetails.getUserId(),
                                                            vehicleOwnerDetails.getNick_name()
                                                    )
                                            ),
                                            ZegoInvitationType.VOICE_CALL,
                                            result -> {
                                                int code = (int) result.get("code");
                                                String message = (String) result.get("message");

                                                if (code != 0) {
                                                    // ❌ DO NOTHING → toast suppressed
                                                    Log.e(TAG, "Call failed: " + code + " " + message);
                                                }
                                            }
                                    );

                                });

                                try {
                                    FIVE_MINUTES = getIntent().getLongExtra("timer", 0);
                                } catch (Exception e) {
                                    FIVE_MINUTES = 0;
                                }


                                if (FIVE_MINUTES > 0) {
                                    initUI(true);
                                    startReverseTimer();
                                }
                            }else {
                                binding.audioCallLayout.setVisibility(View.GONE);
                            }
                        }else {
                            binding.audioCallLayout.setVisibility(View.GONE);
                        }
                    }else {
                        binding.audioCallLayout.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    binding.audioCallLayout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable) {
                binding.audioCallLayout.setVisibility(View.GONE);
            }
        });
    }*/

    public void makeCall(){

        JsonObject jsonObjectCall = new JsonObject();
        jsonObjectCall.addProperty("sender_id", manager.getUserId());
        jsonObjectCall.addProperty("receiver_id", vehicleOwnerDetails.getUserId());

        ApiCall.callApi(TAG, ChatUserProfile.this, APIData.CALL_USER, jsonObjectCall, "post", new ApiCall.ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {

            }

            @Override
            public void onError(String errorMessage) {

            }
        });

    }

    private void initUI(boolean isTimerSet) {
        if (isTimerSet) {
            // 🔴 Hide views initially
            binding.toolbarLayout.ivBell.setVisibility(View.GONE);
            binding.audioCallLayout.setVisibility(View.GONE);
            binding.toolbarLayout.timer.setVisibility(View.VISIBLE);
        }else {
            binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
            binding.audioCallLayout.setVisibility(View.VISIBLE);
            binding.toolbarLayout.timer.setVisibility(View.GONE);
        }
    }

    private void startReverseTimer() {

        countDownTimer = new CountDownTimer(FIVE_MINUTES, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                runningMinutes = (millisUntilFinished / 1000) / 60;
                runningSeconds = (millisUntilFinished / 1000) % 60;

                String timeFormatted = String.format(Locale.getDefault(),
                        "%02d:%02d", runningMinutes, runningSeconds);

                binding.toolbarLayout.timer.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                binding.toolbarLayout.timer.setText("00:00");
                binding.toolbarLayout.timer.setVisibility(View.GONE);

                // 🟢 Show views when time finishes
                binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
                binding.audioCallLayout.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @SuppressLint("RestrictedApi")
    private void showAlertPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(anchorView.getContext(), anchorView, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.menu_alert_popup, popupMenu.getMenu());

        // Force icons and style (optional)
        if (popupMenu.getMenu() instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) popupMenu.getMenu();
            m.setOptionalIconsVisible(true);
        }

        // Handle item clicks
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sms_alert) {
                Toast.makeText(ChatUserProfile.this, "Send SMS Alert clicked", Toast.LENGTH_SHORT).show();
            } /*else if (item.getItemId() == R.id.action_send_alert) {
                Toast.makeText(ChatUserProfile.this, "Send Alert to Digivahan clicked", Toast.LENGTH_SHORT).show();
            }*/
            return false;
        });

        popupMenu.show();
    }
}