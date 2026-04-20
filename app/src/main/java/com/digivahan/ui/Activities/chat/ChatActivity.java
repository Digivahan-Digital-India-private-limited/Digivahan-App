package com.digivahan.ui.Activities.chat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.adapters.ChatListAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.ChatItemModel;
import com.digivahan.data.model.CompressFileData;
import com.digivahan.data.model.MembersModel;
import com.digivahan.data.model.SavedImageData;
import com.digivahan.data.model.User;
import com.digivahan.databinding.ActivityChatBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.changPassword.ChangePasswordPage;
import com.digivahan.ui.Activities.qr.ChatNotificationInfoRequestPage;
import com.digivahan.ui.Activities.qr.ConnectEmergencyContactsPage;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.TimeUtils;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
//import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
//import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
//import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
//import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements ChattingSystem.OnMessageReceivedListener {
    String TAG = "ChatActivityData";

    ActivityChatBinding binding;

    ChatListAdapter chatListAdapter;
    ArrayList<ChatItemModel> chatItemList = new ArrayList<>();
    ArrayList<MembersModel> roomMembersList = new ArrayList<>();

    PreferencesManager manager;

    private static final long FIVE_MINUTES = 1 * 60 * 1000; // 5 minutes

    long runningMinutes = 0 , runningSeconds = 0;
    String chatRoomId = "", receiverId = "";
    User vehicleOwnerDetails;
    boolean isCameraSelected = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable chatUpdaterRunnable;

    AshDialog loadingDialog;

    ChattingSystem chattingSystem;
    private CountDownTimer countDownTimer;
    Dialog callRequestDialog;

    int newMessageCount = 0;

    LinearLayoutManager rvChatListLayoutManager;

    // Call this in onCreate() or onResume()
    private void startChatAutoRefresh() {
        chatUpdaterRunnable = new Runnable() {
            @Override
            public void run() {
                // Call your API
                getChatData();

                // Schedule again after 1 second
                handler.postDelayed(this, 2000);
            }
        };

        // Start immediately
        handler.post(chatUpdaterRunnable);
    }

    // Call this in onPause() or onDestroy()
    private void stopChatAutoRefresh() {
        if (handler != null && chatUpdaterRunnable != null) {
            handler.removeCallbacks(chatUpdaterRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        startChatAutoRefresh();

        getChatData();

        if (callRequestDialog != null && callRequestDialog.isShowing()){
            callRequestDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopChatAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (chattingSystem != null) {
            chattingSystem.disconnect();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.notificationBellLayout.setVisibility(View.GONE);
        binding.toolbarLayout.ivBell.setImageResource(R.drawable.menu_dot_icon);

        binding.toolbarLayout.ivProfileLayout.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent profilePage = new Intent(ChatActivity.this, ChatUserProfile.class);
            profilePage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
            profilePage.putExtra("timer", (runningMinutes * 60 * 1000) + (runningSeconds * 1000));
            startActivity(profilePage);
        });

        getOnBackPressedDispatcher().addCallback(ChatActivity.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.viewImage.getVisibility() == View.VISIBLE) {
                    binding.viewImage.setVisibility(View.GONE);
                    binding.viewImageCrossIcon.setVisibility(View.GONE);
                } else {
                    disableHideContentSecureForNextNavigation();
                    finish();
                }
            }
        });

        binding.btnScrollToBottom.setOnClickListener(v -> {
            binding.rvChatList.scrollToPosition(chatListAdapter.getItemCount() - 1);
            newMessageCount = 0;

            binding.btnScrollToBottom.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    PorterDuff.Mode.SRC_IN
            );


            binding.tvNewMessageCount.setVisibility(View.GONE);
            binding.btnScrollToBottomLayout.setVisibility(View.GONE);
        });


        callRequestDialog = new Dialog(ChatActivity.this);
        callRequestDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        callRequestDialog.setContentView(R.layout.dialog_password_changed);
        callRequestDialog.setCancelable(true);

        binding.toolbarLayout.audioCall.setVisibility(View.VISIBLE);
        binding.toolbarLayout.audioCall.setOnClickListener(view -> {
            CommonMethods.showCallRequestDialog(TAG, ChatActivity.this, callRequestDialog, manager.getUser().getPhone_number(), vehicleOwnerDetails.getPhone_number());
        });

//        setInAppCallData();

        binding.toolbarLayout.ivBell.setOnClickListener(v -> {
            showAlertPopupMenu(v);
        });

        loadingDialog = new AshDialog(ChatActivity.this, "Please wait", "");

        try {
            if (getIntent().hasExtra("chatRoomId")) {
                chatRoomId = getIntent().getStringExtra("chatRoomId");
                initializeChattingSystem(chatRoomId);
            }else {
                getChatRoomId();
            }

            if (getIntent().hasExtra("vehicleOwnerDetails")) {
                vehicleOwnerDetails = (User) getIntent().getSerializableExtra("vehicleOwnerDetails");
                if (vehicleOwnerDetails != null) {
                    binding.toolbarLayout.tvTitle.setText(vehicleOwnerDetails.getNick_name());

                    CommonLogic.showTestLog(TAG, "Target User ID: " + vehicleOwnerDetails.getUserId());
                    CommonLogic.showTestLog(TAG, "Target User Name: " + vehicleOwnerDetails.getNick_name());

                    int tempImage = R.drawable.temp_profile_icon;

                    if (manager.getUser().getGender().equalsIgnoreCase("male")){
                        tempImage = R.drawable.boy_avatar;
                    }else if (manager.getUser().getGender().equalsIgnoreCase("female")){
                        tempImage = R.drawable.girl_avatar;
                    }
                    ImageHelperMethods.loadImage(TAG, ChatActivity.this, vehicleOwnerDetails.getPublic_pic(), binding.toolbarLayout.ivProfile, tempImage);
                }
            }

            if (getIntent().hasExtra("receiverId")) {
                receiverId = getIntent().getStringExtra("receiverId");
                if (vehicleOwnerDetails == null){
                    getUserDetails(receiverId);
                }else {
                    vehicleOwnerDetails.setUserId(receiverId);
                }
            }

            // for testing



        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }


        manager = new PreferencesManager(ChatActivity.this);

        rvChatListLayoutManager = new LinearLayoutManager(this);
        rvChatListLayoutManager.setStackFromEnd(true);
        binding.rvChatList.setLayoutManager(rvChatListLayoutManager);

        chatListAdapter = new ChatListAdapter(ChatActivity.this, chatItemList, roomMembersList, binding.viewImage, binding.viewImageCrossIcon);
        binding.rvChatList.setAdapter(chatListAdapter);

// Ensures it scrolls to the very bottom once layout is ready

        /*binding.rvChatList.post(() -> {
            binding.rvChatList.scrollToPosition(chatListAdapter.getItemCount() - 1);
        });*/

        binding.rvChatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if (rvChatListLayoutManager == null) return;

                int lastVisible = rvChatListLayoutManager.findLastCompletelyVisibleItemPosition();
                int total = chatListAdapter.getItemCount();

                boolean isAtBottom = lastVisible >= total - 1;

                if (isAtBottom) {
                    newMessageCount = 0;
                    binding.btnScrollToBottom.setColorFilter(
                            ContextCompat.getColor(ChatActivity.this, R.color.white),
                            PorterDuff.Mode.SRC_IN
                    );
                    binding.tvNewMessageCount.setVisibility(View.GONE);
                    binding.btnScrollToBottomLayout.setVisibility(View.GONE);
                }else {
                    binding.btnScrollToBottomLayout.setVisibility(View.VISIBLE);
                }
            }
        });


        binding.viewImageCrossIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.viewImage.setVisibility(View.GONE);
                binding.viewImageCrossIcon.setVisibility(View.GONE);
            }
        });


//        getChatData();

        binding.sendBtn.setOnClickListener(view -> {
            String message = binding.messageFiled.getText().toString().trim();

            if (message.isEmpty()) {
                return;
            }

            String localId = "local_" + System.currentTimeMillis();

            ChatItemModel model = new ChatItemModel();

            model.setLocalId(localId);
            model.setSender_id(manager.getUserId());
            model.setMessage(message);
            model.setSending(true);

            chatItemList.add(model);

            chatListAdapter.notifyItemInserted(chatItemList.size() - 1);

            binding.rvChatList.scrollToPosition(chatItemList.size() - 1);

            if (chattingSystem != null && chattingSystem.isConnected()) {
                chattingSystem.sendMessage(message);
            }



            JsonObject jsonObjectMessage = new JsonObject();
            jsonObjectMessage.addProperty("chat_room_id", chatRoomId);
            jsonObjectMessage.addProperty("sender_id", manager.getUserId());
            jsonObjectMessage.addProperty("message", message);

            CommonLogic.showTestLog(TAG, jsonObjectMessage.toString());

            binding.messageFiled.setText("");

            /*CommonChattingMethods.sendMessageAPI(TAG, ChatActivity.this, chatRoomId,
                    message, null, ""
                    , "",  manager, loadingDialog);*/

        });

        binding.filesBtn.setOnClickListener(v -> {
            CommonLogic.showTestLog(TAG, "ImageClicked");
            CommonLogic.showPickImageDialog(ChatActivity.this,
                    CommonLogic.PROFILE_IMAGE_REQUEST, false, new CommonLogic.CameraSelectionCallback() {
                        @Override
                        public void onCameraSelected(boolean isCamera) {
                            isCameraSelected = isCamera;
                        }
                    });
        });

        binding.copyBtn.setOnClickListener(v -> {
            String textToCopy = binding.messageFiled.getText().toString().trim();

            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", textToCopy);
                clipboard.setPrimaryClip(clip);
            }
        });


    }

    /*public void setInAppCallData(){
        PreferencesManager manager = new PreferencesManager(ChatActivity.this);
        ApiClient.getApiService(ChatActivity.this).commonGETMethodToHitAllAPIs(APIData.GET_APP_INFO).enqueue(new Callback<JsonObject>() {
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

                                ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

                                ZegoUIKitPrebuiltCallService.init((Application) getApplicationContext(), appID, appSign, userID, userName, callInvitationConfig);

                                binding.toolbarLayout.audioCall.setVisibility(View.VISIBLE);
                                binding.toolbarLayout.audioCall.setOnClickListener(v -> {

                                    makeCall();

                                    ZegoUIKitPrebuiltCallService.sendInvitationWithUIChange(
                                            ChatActivity.this,
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

                                if (getIntent().hasExtra("timer") && getIntent().getBooleanExtra("timer", false)) {
                                    initUI(true);
                                    startReverseTimer();
                                }
                            }else {
                                binding.toolbarLayout.audioCall.setVisibility(View.GONE);
                            }
                        }else {
                            binding.toolbarLayout.audioCall.setVisibility(View.GONE);
                        }
                    }else {
                        binding.toolbarLayout.audioCall.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    binding.toolbarLayout.audioCall.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable) {
                binding.toolbarLayout.audioCall.setVisibility(View.GONE);
            }
        });
    }*/

    public void makeCall(){

        JsonObject jsonObjectCall = new JsonObject();
        jsonObjectCall.addProperty("sender_id", manager.getUserId());
        jsonObjectCall.addProperty("receiver_id", vehicleOwnerDetails.getUserId());

        ApiCall.callApi(TAG, ChatActivity.this, APIData.CALL_USER, jsonObjectCall, "post", new ApiCall.ApiResponseCallback() {
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
            binding.toolbarLayout.audioCall.setVisibility(View.GONE);
//            binding.toolbarLayout.ivBell.setVisibility(View.GONE);
            binding.toolbarLayout.timer.setVisibility(View.VISIBLE);
        }else {
            binding.toolbarLayout.audioCall.setVisibility(View.VISIBLE);
//            binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
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
                binding.toolbarLayout.audioCall.setVisibility(View.VISIBLE);
//                binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void initializeChattingSystem(String roomId) {

        chattingSystem = new ChattingSystem(
                ChatActivity.this,
                roomId,
                (messageJson, isSelf) -> runOnUiThread(() -> {

                    ChatItemModel model = parseSocketMessage(messageJson);

                    CommonLogic.showTestLog(TAG, "message received:- " + messageJson);

                    if (isSelf) {

                        for (ChatItemModel item : chatItemList) {

                            if (item.isSending()) {

                                item.setSending(false);
                                item.setId(model.getId());

                                chatListAdapter.notifyDataSetChanged();

                                return;
                            }
                        }
                    }

                    if (!containsMessage(model.getId())) {

                        chatItemList.add(model);

                        chatListAdapter.notifyItemInserted(chatItemList.size() - 1);

                        boolean userAtBottom =
                                rvChatListLayoutManager.findLastVisibleItemPosition()
                                        >= chatListAdapter.getItemCount() - 2;

                        if (userAtBottom) {
                            binding.rvChatList.scrollToPosition(chatItemList.size() - 1);
                        } else {
                            newMessageCount++;
                        }
                    }

                }),
                new ChattingSystem.SocketConnectionListener() {

                    @Override
                    public void onConnected() {

                        runOnUiThread(() ->
                                CommonLogic.showTestLog(TAG, "🟢 Chat is LIVE"));

                    }

                    @Override
                    public void onDisconnected() {

                        runOnUiThread(() ->
                                CommonLogic.showTestLog(TAG, "🔴 Chat disconnected"));

                    }

                    @Override
                    public void onError(String reason) {

                        runOnUiThread(() ->
                                CommonLogic.showTestLog(TAG, "❌ Socket error: " + reason));

                    }
                }
        );
    }

    private ChatItemModel parseSocketMessage(JSONObject json) {

        ChatItemModel model = new ChatItemModel();

        try {

            model.setId(json.optString("_id"));
            model.setSenderId(json.optString("sender_id"));
            model.setMessage(json.optString("message"));

            model.setMessageTimestamp(json.optString("createdAt"));

            // Images
            JSONArray imagesArray = json.optJSONArray("images");

            if (imagesArray != null) {

                List<String> images = new ArrayList<>();

                for (int i = 0; i < imagesArray.length(); i++) {
                    images.add(imagesArray.getString(i));
                }

                model.setImages(images);
            }

            // Location
            JSONObject location = json.optJSONObject("location");

            if (location != null) {
                model.setLatitude(location.optString("latitude"));
                model.setLongitude(location.optString("longitude"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }


    private void getChatRoomId() {
        JsonObject jsonObjectChatRoom = new JsonObject();
        jsonObjectChatRoom.addProperty("user1", manager.getUserId());
        jsonObjectChatRoom.addProperty("user2", vehicleOwnerDetails.getUserId());

        ApiCall.callApi(TAG,
                ChatActivity.this,
                APIData.CREATE_CHAT_ROOM,
                jsonObjectChatRoom, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                chatRoomId = responseBody.getJSONObject("data").getString("_id");
                                initializeChattingSystem(chatRoomId);
                            } else {
                                loadingDialog.dismiss();
                                CommonLogic.showTestLog(TAG, "Failed: " + message);
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
                Toast.makeText(ChatActivity.this, "Send SMS Alert clicked", Toast.LENGTH_SHORT).show();
            }
            /*else if (item.getItemId() == R.id.action_ivr_call) {
                *//*Intent userRequestPage = new Intent(ChatActivity.this, AudioCallActivity.class);
                startActivity(userRequestPage);*//*

                Dialog progress = new Dialog(ChatActivity.this);
                progress.setContentView(R.layout.custom_call_dialog_layout);

                Window window = progress.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
                }

                TextView messageText = progress.findViewById(R.id.messageText);

                ZegoSendCallInvitationButton audioCall = progress.findViewById(R.id.audioCall);
                audioCall.setIsVideoCall(false);
//                audioCall.setResourceID("zego_uikit_call");
                audioCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(receiverId, receiverId)));

                progress.show();

            }*/
            /*else if (item.getItemId() == R.id.action_send_alert) {
                Toast.makeText(ChatActivity.this, "Send Alert to Digivahan clicked", Toast.LENGTH_SHORT).show();
            }*/
            return false;
        });

        popupMenu.show();
    }


    private void getChatData() {
        ApiCall.callApi(TAG,
                ChatActivity.this,
                APIData.GET_CHAT_MESSAGE_LIST + chatRoomId,
                null, "get",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        CommonLogic.showTestLog(TAG, "getChatData response: " + responseBody);
                        try {
                            if (status) {
                                int oldSize = chatItemList.size();
                                ArrayList<ChatItemModel> newMessages = APIHelper.convertJsonArrayToList(
                                        responseBody.getJSONArray("data").getJSONObject(0).getJSONArray("chats"),
                                        ChatItemModel.class
                                );

                                if (roomMembersList.isEmpty()){
                                    roomMembersList.addAll(APIHelper.convertJsonArrayToList(
                                            responseBody.getJSONArray("members"),
                                            MembersModel.class
                                    ));
                                }

                                if (chatItemList.isEmpty() && !newMessages.isEmpty()){
                                    chatItemList.addAll(newMessages);
                                    chatListAdapter.notifyDataSetChanged();
                                }
                                else if (!newMessages.isEmpty() && oldSize != newMessages.size()) {
//                                    filterChatByCurrentDate(newMessages, oldSize, newMessages.size());
                                    filterChatByCurrentDate(newMessages);

                                    /*if (oldSize < newMessages.size()) {
                                        // ✅ Notify adapter only for new range
                                        chatListAdapter.notifyItemRangeInserted(oldSize, newMessages.size());
                                    } else {
                                        chatListAdapter.notifyDataSetChanged();
                                    }

                                    binding.rvChatList.post(() -> {
                                        if (chatListAdapter.getItemCount() > 0) {
                                            binding.rvChatList.scrollToPosition(chatListAdapter.getItemCount() - 1);
                                        }
                                    });*/
                                }

                            } else {
                                CommonLogic.showTestLog(TAG, "Failed: " + message);
                            }
                        } catch (JSONException e) {
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }

                    }

                    @Override
                    public void onError(String errorMessage) {
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(MyGarageActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void filterChatByCurrentDate(ArrayList<ChatItemModel> chatItemModels) {

        CommonLogic.showTestLog(TAG, "==============================");
        CommonLogic.showTestLog(TAG, "filterChatByCurrentDate() called");

        if (chatItemModels == null || chatItemModels.isEmpty()) {
            CommonLogic.showTestLog(TAG, "API list is null or empty → returning");
            return;
        }

        CommonLogic.showTestLog(TAG, "API total messages: " + chatItemModels.size());
        CommonLogic.showTestLog(TAG, "Current local list size: " + chatItemList.size());

        ArrayList<ChatItemModel> filteredList = new ArrayList<>();

        String currentDate = CommonMethods.getCurrentDate(
                ChatActivity.this,
                "dd MMM yyyy"
        );

        CommonLogic.showTestLog(TAG, "Current Device Date: " + currentDate);

        for (ChatItemModel chatItem : chatItemModels) {

            String messageDate = TimeUtils.convertDateFormat(
                    chatItem.getMessageTimestamp(),
                    "dd MMM yyyy"
            );

            CommonLogic.showTestLog(TAG,
                    "Checking message ID: " + chatItem.getId()
                            + " | Message Date: " + messageDate);

            if (messageDate.equalsIgnoreCase(currentDate)) {
                filteredList.add(chatItem);
            }
        }

        CommonLogic.showTestLog(TAG, "Filtered list size (today only): " + filteredList.size());

        // 🔵 FIRST LOAD
        if (chatItemList.isEmpty()) {

            CommonLogic.showTestLog(TAG, "First load detected → loading full filtered list");

            chatItemList.addAll(filteredList);
            chatListAdapter.notifyDataSetChanged();

            if (!chatItemList.isEmpty()) {
                binding.rvChatList.scrollToPosition(chatItemList.size() - 1);
            }

            CommonLogic.showTestLog(TAG, "Initial load completed. New size: " + chatItemList.size());
            CommonLogic.showTestLog(TAG, "==============================");
            return;
        }

        // 🔵 GET LAST EXISTING MESSAGE ID
        String lastExistingId = chatItemList
                .get(chatItemList.size() - 1)
                .getId();

        CommonLogic.showTestLog(TAG, "Last existing message ID: " + lastExistingId);

        boolean userAtBottom =
                rvChatListLayoutManager.findLastVisibleItemPosition()
                        >= chatListAdapter.getItemCount() - 1;

        CommonLogic.showTestLog(TAG, "User at bottom: " + userAtBottom);

        int newItemsAdded = 0;

        for (ChatItemModel item : filteredList) {

            CommonLogic.showTestLog(TAG, "Evaluating message ID: " + item.getId());

            // Append only NEW messages
            if (!item.getId().equals(lastExistingId)
                    && !containsMessage(item.getId())) {

                CommonLogic.showTestLog(TAG,
                        "New message detected → Appending ID: " + item.getId());

                chatItemList.add(item);
                chatListAdapter.notifyItemInserted(chatItemList.size() - 1);

                newItemsAdded++;

                if (userAtBottom) {
                    binding.rvChatList.scrollToPosition(chatItemList.size() - 1);
                    CommonLogic.showTestLog(TAG, "Auto scrolled to bottom");
                } else {
                    newMessageCount++;
                    binding.btnScrollToBottomLayout.setVisibility(View.VISIBLE);
                    binding.tvNewMessageCount.setText(String.valueOf(newMessageCount));
                    CommonLogic.showTestLog(TAG,
                            "User not at bottom → newMessageCount: " + newMessageCount);
                }

            } else {
                CommonLogic.showTestLog(TAG,
                        "Duplicate or already existing message ID: " + item.getId());
            }
        }

        CommonLogic.showTestLog(TAG, "Total new items added in this cycle: " + newItemsAdded);
        CommonLogic.showTestLog(TAG, "Final local list size: " + chatItemList.size());
        CommonLogic.showTestLog(TAG, "==============================");
    }

    private boolean containsMessage(String id) {
        for (ChatItemModel model : chatItemList) {
            if (id != null && id.equals(model.getId())) {
                return true;
            }
        }
        return false;
    }

    /*private void filterChatByCurrentDate(ArrayList<ChatItemModel> chatItemModels,
                                         int oldSize,
                                         int newSize) {

        CommonLogic.showTestLog(TAG, "------------------------------");
        CommonLogic.showTestLog(TAG, "Old Size: " + oldSize);
        CommonLogic.showTestLog(TAG, "New Size (API): " + newSize);

        // ✅ CHECK BEFORE MODIFYING LIST
        boolean userAtBottom =
                rvChatListLayoutManager.findLastVisibleItemPosition()
                        >= chatListAdapter.getItemCount() - 1;

        CommonLogic.showTestLog(TAG, "User At Bottom BEFORE update: " + userAtBottom);

        ArrayList<ChatItemModel> filteredList = new ArrayList<>();

        *//*chatItemList.clear();
        chatItemList.addAll(chatItemModels);*//*

        for (ChatItemModel chatItem : chatItemModels) {

            String messageDate = TimeUtils.convertDateFormat(
                    chatItem.getMessageTimestamp(),
                    "dd MMM yyyy"
            );

            String currentDate = CommonMethods.getCurrentDate(
                    ChatActivity.this,
                    "dd MMM yyyy"
            );

            if (messageDate.equalsIgnoreCase(currentDate)) {
                filteredList.add(chatItem);
            }
        }

//        chatItemList.clear();
        chatItemList.addAll(filteredList);

        if (oldSize < newSize) {

//            chatListAdapter.notifyItemInserted(filteredList.size() - 1);

            if (!chatItemList.isEmpty()) {
                String lastId = chatItemList.get(chatItemList.size() - 1).getId();

                for (ChatItemModel item : filteredList) {
                    if (item.getId().compareTo(lastId) > 0) {
                        chatItemList.add(item);
                        chatListAdapter.notifyItemInserted(chatItemList.size() - 1);
                    }
                }
            }

            if (userAtBottom) {

                CommonLogic.showTestLog(TAG, "Auto scrolling to bottom");

                binding.rvChatList.post(() ->
                        binding.rvChatList.scrollToPosition(chatListAdapter.getItemCount() - 1)
                );

            } else {

                newMessageCount++;

                CommonLogic.showTestLog(TAG,
                        "User not at bottom. New Message Count: " + newMessageCount);

                binding.btnScrollToBottom.setColorFilter(
                        ContextCompat.getColor(this, R.color.colorPrimary),
                        PorterDuff.Mode.SRC_IN
                );

                binding.tvNewMessageCount.setText(String.valueOf(newMessageCount));
                binding.tvNewMessageCount.setVisibility(View.VISIBLE);
                binding.btnScrollToBottomLayout.setVisibility(View.VISIBLE);
            }

        } else {

            chatListAdapter.notifyDataSetChanged();
        }

        CommonLogic.showTestLog(TAG, "------------------------------");
    }*/



    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loadingDialog.show();

        if (requestCode == CommonLogic.PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            CommonLogic.handleImagePick(data, isCameraSelected, false, manager.getString(PreferencesManager.IMAGE_PATH, ""),
                    ChatActivity.this, null, null, new CommonLogic.FileCallback() {
                        @Override
                        public void onFileReady(CompressFileData selectedImageData) {

                            CommonMethods.uploadSingleImage(
                                    ChatActivity.this,
                                    selectedImageData.getFileFormat(), selectedImageData.getFilePath(), Constants.chatImages,
                                    new CommonMethods.ImageUploadCallback() {
                                        @Override
                                        public void onUploadSuccess(SavedImageData uploadedImage) {

                                            JsonObject jsonObjectMessage = new JsonObject();
                                            jsonObjectMessage.addProperty("chatId", chatRoomId);
                                            jsonObjectMessage.addProperty("senderId", manager.getUserId());
                                            jsonObjectMessage.addProperty("receiverId", vehicleOwnerDetails.getUserId());
                                            jsonObjectMessage.addProperty("message", "empty");

                                            // ✅ Create a proper JSON array for attachments
                                            JsonArray attachmentsArray = new JsonArray();
                                            attachmentsArray.add(uploadedImage.getImage_url());

                                            // ✅ Add array to main JSON
                                            jsonObjectMessage.add("attachments", attachmentsArray);

                                            CommonLogic.showTestLog(TAG, jsonObjectMessage.toString());

                                            ArrayList<SavedImageData> selectedImageList = new ArrayList<>();
                                            selectedImageList.add(uploadedImage);

                                            CommonChattingMethods.sendMessageAPI(TAG, ChatActivity.this, chatRoomId,
                                                    "empty", selectedImageList, ""
                                                    , "", manager, loadingDialog);

                                        }

                                        @Override
                                        public void onUploadError(String errorMessage) {
                                            loadingDialog.dismiss();
                                            CommonLogic.showTestLog(TAG, errorMessage);
                                        }

                                        @Override
                                        public void onUploadJSON(JSONObject errorMessage) {
                                        }
                                    }
                            );
                        }
                    });

        } else if (requestCode == CommonLogic.CAMARA_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            isCameraSelected = true;
            CommonLogic.takePictureFromCamera(ChatActivity.this, CommonLogic.PROFILE_IMAGE_REQUEST, false);
        } else if (requestCode == CommonLogic.STORAGE_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            isCameraSelected = false;
            CommonLogic.choosePictureFromGallery(ChatActivity.this, CommonLogic.PROFILE_IMAGE_REQUEST);
        }
        else {
            loadingDialog.dismiss();
        }
    }

    private void getUserDetails(String userId){
        JsonObject jsonObjectUserDetails = new JsonObject();
        jsonObjectUserDetails.addProperty("user_id", userId);
        jsonObjectUserDetails.addProperty("details_type", "all");

        loadingDialog.show();
        ApiCall.callApi(TAG,
                ChatActivity.this,
                APIData.GET_USER_DETAILS,
                jsonObjectUserDetails, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {

                                vehicleOwnerDetails = CommonLogic.parseUserFromJson(TAG, responseBody.getJSONObject("data"));

                                vehicleOwnerDetails.setUserId(userId);
                                CommonLogic.showTestLog(TAG, "Target User ID: " + vehicleOwnerDetails.getUserId());
                                CommonLogic.showTestLog(TAG, "Target User Name: " + vehicleOwnerDetails.getNick_name());


                                binding.toolbarLayout.tvTitle.setText(vehicleOwnerDetails.getNick_name());

                                int tempImage = R.drawable.temp_profile_icon;

                                if (manager.getUser().getGender().equalsIgnoreCase("male")){
                                    tempImage = R.drawable.boy_avatar;
                                }else if (manager.getUser().getGender().equalsIgnoreCase("female")){
                                    tempImage = R.drawable.girl_avatar;
                                }

                                ImageHelperMethods.loadImage(TAG, ChatActivity.this, vehicleOwnerDetails.getPublic_pic(), binding.toolbarLayout.ivProfile, tempImage);

                            } else {
                                CommonLogic.showTestLog(TAG, "Failed: " + message);
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

    @Override
    public void onMessageReceived(JSONObject messageResponse, boolean isSelf) {

    }
}