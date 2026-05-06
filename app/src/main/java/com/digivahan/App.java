package com.digivahan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.ashu.ashuutils.APIHelper;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.utils.AppExecutors;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class App extends Application {

    String TAG = "AppLevelData";

    private static App instance;
    private AppExecutors appExecutors;

    PreferencesManager manager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize executors
        appExecutors = AppExecutors.getInstance();
        manager = new PreferencesManager(this);

//        createNotificationChannels();

        setUpOneSignal(getString(R.string.one_signal_id));
        CommonMethods.getAppInfo(getAppContext());

        // 👇 Add this
        new Handler().postDelayed(() -> {
            logAllNotificationChannels();
        }, 5000);

        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {

                    @Override
                    public void onActivityCreated(Activity activity,
                                                  Bundle savedInstanceState) {
                        // 🔒 Block screenshots & screen recording
                        /*activity.getWindow().setFlags(
                                WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE
                        );*/
                    }

                    @Override public void onActivityStarted(Activity activity) {}
                    @Override public void onActivityResumed(Activity activity) {}
                    @Override public void onActivityPaused(Activity activity) {}
                    @Override public void onActivityStopped(Activity activity) {}
                    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
                    @Override public void onActivityDestroyed(Activity activity) {}
                }
        );

//        setCallUserData();

        setBaseUrl();

       /* Application application = (Application) getApplicationContext(); // Android's application context
        long appID = Constants.APP_ID;   // yourAppID
        String appSign = Constants.zigoAppSign;  // yourAppSign
        String userID = manager.getUserId() == null ? " " : manager.getUserId(); // yourUserID, userID should only contain numbers, English characters, and '_'.
        String userName = (manager.getUser() == null || manager.getUser().getNick_name() == null) ? " " : manager.getUser().getNick_name();
        // yourUserName

        // 🔹 Log the details for debugging
        Log.i("ZegoAppInit", "----------------------------------------");
        Log.i("ZegoAppInit", "ZEGOCLOUD Initialization Details:");
        Log.i("ZegoAppInit", "Application Context: " + application);
        Log.i("ZegoAppInit", "App ID: " + appID);
        Log.i("ZegoAppInit", "App Sign: " + appSign);
        Log.i("ZegoAppInit", "User ID: " + userID);
        Log.i("ZegoAppInit", "User Name: " + userName);
        Log.i("ZegoAppInit", "----------------------------------------");

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallService.init(application, appID, appSign, userID, userName,callInvitationConfig);*/

        // 🔥 REQUIRED for background / killed app calls
//        ZegoUIKitPrebuiltCallService.enableFCMPush();

    }


    public void setBaseUrl(){
        Application application = (Application) getApplicationContext();
        PreferencesManager manager = new PreferencesManager(application);
        ApiClient.getApiServiceWithoutBaseUrl(application).commonGETMethodToHitAllAPIs(APIData.GET_BASE_URL).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                try {
                    boolean status = responseBody.has("status") && responseBody.getBoolean("status");

                    if (status) {
                        manager.setString(PreferencesManager.BASE_URL, responseBody.getString("base_url"));
                    }
                } catch (JSONException ignored) {

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable) {

            }
        });
    }

    /*public void setCallUserData(){
        Application application = (Application) getApplicationContext();
        PreferencesManager manager = new PreferencesManager(application);
        ApiClient.getApiService(application).commonGETMethodToHitAllAPIs(APIData.GET_APP_INFO).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                try {
                    boolean status = responseBody.has("success") && responseBody.getBoolean("success");

                    if (status) {
                        JSONObject appInfoData = responseBody.getJSONObject("data");
                        if (appInfoData.has("zigoApp_data")) {
                            JSONObject zigoApp_data = appInfoData.getJSONObject("zigoApp_data");
                            if (zigoApp_data.has("zigoAppID") && !zigoApp_data.getString("zigoAppID").isEmpty() && !zigoApp_data.getString("zigoAppID").equalsIgnoreCase("empty") &&
                                    zigoApp_data.has("zigoAppSignKey") && !zigoApp_data.getString("zigoAppSignKey").isEmpty() && !zigoApp_data.getString("zi" +
                                    "goAppSignKey").equalsIgnoreCase("empty")){
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

                                ZegoUIKitPrebuiltCallService.init((Application) getApplicationContext(), appID, appSign, userID, userName,callInvitationConfig);


                            }
                        }
                    }
                } catch (JSONException ignored) {

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable) {

            }
        });
    }*/

    public static App getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public AppExecutors getAppExecutors() {
        return appExecutors;
    }




    /*private void setUpOneSignal(String oneSignalID) {

        CommonLogic.showTestLog(TAG, "🚀 setUpOneSignal() called");
        CommonLogic.showTestLog(TAG, "🆔 OneSignal App ID: " + oneSignalID);

        *//* ---------------- ENABLE DEBUG LOGS ---------------- *//*
        OneSignal.Debug.setLogLevel(OneSignal.LogLevel.VERBOSE);
        CommonLogic.showTestLog(TAG, "🐞 OneSignal debug log level set to VERBOSE");

        *//* ---------------- INITIALIZE SDK ---------------- *//*
        OneSignal.initWithContext(this, oneSignalID);
        CommonLogic.showTestLog(TAG, "✅ OneSignal initialized successfully");

        *//* ---------------- FOREGROUND NOTIFICATION HANDLER ---------------- *//*
        OneSignal.Notifications.addForegroundLifecycleListener(event -> {

            String title = event.getNotification().getTitle();
            String body = event.getNotification().getBody();
            String notificationId = event.getNotification().getNotificationId();

            JSONObject additionalData = event.getNotification().getAdditionalData();

            CommonLogic.showTestLog(TAG, "🔔 Foreground Notification RECEIVED");
            CommonLogic.showTestLog(TAG, "   • ID: " + notificationId);
            CommonLogic.showTestLog(TAG, "   • Title: " + title);
            CommonLogic.showTestLog(TAG, "   • Body: " + body);

            if (additionalData != null) {
                CommonLogic.showTestLog(
                        TAG,
                        "   • Additional Data: " + additionalData.toString()
                );
            } else {
                CommonLogic.showTestLog(TAG, "   • Additional Data: NULL");
            }

            // Allow notification to be shown
            event.complete(event.getNotification());
        });

        CommonLogic.showTestLog(TAG, "📌 Foreground notification listener registered");

        *//* ---------------- IN-APP MESSAGE LIFECYCLE ---------------- *//*
        OneSignal.InAppMessages.addLifecycleListener(
                new OneSignal.InAppMessageLifecycleListener() {

                    @Override
                    public void onWillDisplay(OneSignal.InAppMessage message) {
                        CommonLogic.showTestLog(TAG, "📩 IAM WILL DISPLAY");
                        CommonLogic.showTestLog(TAG, "   • IAM ID: " + message.getMessageId());
                    }

                    @Override
                    public void onDidDisplay(OneSignal.InAppMessage message) {
                        CommonLogic.showTestLog(TAG, "📩 IAM DID DISPLAY");
                        CommonLogic.showTestLog(TAG, "   • IAM ID: " + message.getMessageId());
                    }

                    @Override
                    public void onWillDismiss(OneSignal.InAppMessage message) {
                        CommonLogic.showTestLog(TAG, "📩 IAM WILL DISMISS");
                        CommonLogic.showTestLog(TAG, "   • IAM ID: " + message.getMessageId());
                    }

                    @Override
                    public void onDidDismiss(OneSignal.InAppMessage message) {
                        CommonLogic.showTestLog(TAG, "📩 IAM DID DISMISS");
                        CommonLogic.showTestLog(TAG, "   • IAM ID: " + message.getMessageId());
                    }
                }
        );

        CommonLogic.showTestLog(TAG, "📌 In-App Message lifecycle listener registered");
    }*/

    private void setUpOneSignal(String oneSignalID) {

        CommonLogic.showTestLog(TAG, "🚀 setUpOneSignal() called");
        CommonLogic.showTestLog(TAG, "🆔 OneSignal App ID: " + oneSignalID);

        OneSignal.getDebug().setLogLevel(
                com.onesignal.debug.LogLevel.VERBOSE
        );

        OneSignal.initWithContext(this, oneSignalID);
        CommonLogic.showTestLog(TAG, "✅ OneSignal initialized");

        // ⏱ DELAYED OPT-IN (MOST IMPORTANT FOR XIAOMI)
        new android.os.Handler().postDelayed(() -> {

            CommonLogic.showTestLog(TAG, "⏱ Delayed optIn trigger");
            OneSignal.getUser().getPushSubscription().optIn();

        }, 4000);

        // ✅ SUBSCRIPTION OBSERVER
        OneSignal.getUser().getPushSubscription().addObserver(state -> {

            boolean optedIn = state.getCurrent().getOptedIn();
            String token = state.getCurrent().getToken();
            String id = state.getCurrent().getId();

            CommonLogic.showTestLog(TAG, "📡 isSubscribed: " + optedIn);
            CommonLogic.showTestLog(TAG, "📨 pushToken: " + token);
            CommonLogic.showTestLog(TAG, "🆔 subscriptionId: " + id);

            // 🚨 THIS IS KEY
            if (token == null) {
                CommonLogic.showTestLog(TAG, "⚠ TOKEN STILL NULL → waiting...");
                return;
            }

            String userId = manager.getUserId();

            if (userId != null && !userId.isEmpty()) {

                OneSignal.login(userId);

                CommonLogic.showTestLog(TAG,
                        "🔁 Linked OneSignal to user: " + userId
                );
            }

        });

        // ONLY LOGIN AFTER INIT (not before token)
        if (manager.getUserId() != null && !manager.getUserId().isEmpty()) {

            new android.os.Handler().postDelayed(() -> {

                OneSignal.login(manager.getUserId());

                CommonLogic.showTestLog(TAG,
                        "🔗 Safe delayed login with userId: " + manager.getUserId()
                );

            }, 5000);
        }

        setNotificationClickEvent();
        registerForegroundNotificationListener();

        CommonLogic.showTestLog(TAG, "📌 OneSignal listeners registered");
    }

    @SuppressLint("ObsoleteSdkInt")
    public void logAllNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager == null) return;

            List<NotificationChannel> channels = manager.getNotificationChannels();

            for (NotificationChannel channel : channels) {

                Log.d(TAG, "CHANNEL_DEBUG -------------------------");
                Log.d(TAG, "CHANNEL_DEBUG ID: " + channel.getId());
                Log.d(TAG, "CHANNEL_DEBUG Name: " + channel.getName());
                Log.d(TAG, "CHANNEL_DEBUG Importance: " + channel.getImportance());
                Log.d(TAG, "CHANNEL_DEBUG Sound: " + channel.getSound());
                Log.d(TAG, "CHANNEL_DEBUG Vibration: " + channel.shouldVibrate());
            }
        }
    }






    private void registerForegroundNotificationListener() {

        OneSignal.getNotifications().addForegroundLifecycleListener(event -> {

            JSONObject data = event.getNotification().getAdditionalData();

            String channelId = event.getNotification().getAndroidNotificationId() + "";
            CommonLogic.showTestLog(TAG, "📣 Android Notification ID: " + channelId);

            CommonLogic.showTestLog(TAG, "📦 FULL NOTIFICATION: " + event.getNotification().toString());

            CommonLogic.showTestLog(TAG, "AdditionalData: " +data);

            String notificationType = data != null
                    ? data.optString("notification_type", "")
                    : "";

            CommonLogic.showTestLog(TAG, "🔔 NOTIFICATION ARRIVED (FOREGROUND)");
            CommonLogic.showTestLog(TAG, "➡ Type: " + notificationType);

            boolean isEnabled = NotificationManagerCompat
                    .from(this)
                    .areNotificationsEnabled();

            CommonLogic.showTestLog(TAG, "🔐 Notification Permission: " + isEnabled);

            // ✅ THIS IS THE MAIN FIX
            event.getNotification().display();


            // 🔊 Play sound immediately (foreground only)
//            playNotificationSoundByType(notificationType);

            // ✅ NO event.complete() in OneSignal v5
        });
    }



    private void setNotificationClickEvent() {

        OneSignal.getNotifications().addClickListener(event -> {

            String title = event.getNotification().getTitle();
            String body = event.getNotification().getBody();
            JSONObject data = event.getNotification().getAdditionalData();

            CommonLogic.showTestLog(TAG, "👉 NOTIFICATION CLICKED");
            CommonLogic.showTestLog(TAG, "📌 Title: " + title);
            CommonLogic.showTestLog(TAG, "📝 Body: " + body);

            if (data == null) {
                CommonLogic.showTestLog(TAG, "❌ AdditionalData is NULL");
                return;
            }

            CommonLogic.showTestLog(TAG, "📦 Click Data: " + data.toString());

            String notification_type = data.optString("notification_type", "");
            String chatRoomId = data.optString("chat_room_id", "");
            String senderId = data.optString("sender_id", "");
            String orderId = data.optString("order_id", "");
            String vehicleId = data.optString("vehicle_id", "");

            CommonLogic.showTestLog(TAG, "➡ Type: " + notification_type);

            String channelId = event.getNotification().toString();

            CommonLogic.showTestLog(
                    TAG,
                    "📣 Notification Channel ID: " + channelId
            );

            handleNotificationNavigation(notification_type, chatRoomId, senderId, orderId, vehicleId);

            /*Intent mainPage = new Intent(getAppContext(), MainActivity.class);

            mainPage.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(mainPage);*/
        });

    }

    private void handleNotificationNavigation(
            String notification_type,
            String chatRoomId,
            String senderId,
            String orderId,
            String vehicleId
    ) {

        manager.setBoolean(PreferencesManager.NOTIFICATION_CLICKED, true);
        manager.setString(PreferencesManager.NOTIFICATION_TYPE_TEMP, notification_type);
        manager.setString(PreferencesManager.NOTIFICATION_CHAT_ROOM_ID_TEMP, chatRoomId);
        manager.setString(PreferencesManager.NOTIFICATION_SENDER_ID_TEMP, senderId);
        manager.setString(PreferencesManager.NOTIFICATION_VEHICLE_ID_TEMP, vehicleId);

    }



    private MediaPlayer mediaPlayer;

    private void playNotificationSoundByType(String type) {

        int soundResId;

        switch (type.toLowerCase()) {

            case "chat":
                soundResId = R.raw.sound;
                break;

            case "vehicle":
                soundResId = R.raw.sound;
                break;

            case "order":
                soundResId = R.raw.sound;
                break;

            case "doc_access":
                soundResId = R.raw.sound;
                break;

            default:
                soundResId = R.raw.sound; // fallback
                break;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(this, soundResId);

            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> mp.release());
                mediaPlayer.start();
            }

            CommonLogic.showTestLog(TAG, "🔊 Playing sound for type: " + type);

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, "❌ Sound play failed: " + e.getMessage());
        }
    }


    private static final String VEHICLE_CHANNEL_ID = "vehicle_channel";

    @SuppressLint("ObsoleteSdkInt")
    private void createNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager.getNotificationChannel(VEHICLE_CHANNEL_ID) == null) {

                Uri soundUri = Uri.parse(
                        ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                                getPackageName() + "/" + R.raw.sound
                );

                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                NotificationChannel channel = new NotificationChannel(
                        VEHICLE_CHANNEL_ID,
                        "Vehicle Alerts",
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.setSound(soundUri, attrs);
                channel.enableVibration(true);

                manager.createNotificationChannel(channel);
            }
        }
    }





    /*private void createNotificationChannels() {

        CommonLogic.showTestLog(TAG, "🔔 createNotificationChannels() called");

        CommonLogic.showTestLog(
                TAG,
                "✅ Android O+ detected, creating notification channels"
        );

        createChannel(
                "chat_channel",
                "Chat Notifications",
                R.raw.vehicle_sound
        );

        createChannel(
                "vehicle_channel",
                "Parking Alerts",
                R.raw.vehicle_sound
        );

        createChannel(
                "order_channel",
                "Order Notifications",
                R.raw.vehicle_sound
        );

    }

    private void createChannel(String channelId, String channelName, int soundResId) {

        NotificationManager manager =
                (NotificationManager) getSystemService(NotificationManager.class);

        if (manager == null) return;

        // 🔒 Already exists → DO NOTHING
        if (manager.getNotificationChannel(channelId) != null) {
            CommonLogic.showTestLog(TAG, "ℹ Channel already exists: " + channelId);
            return;
        }

        Uri soundUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        getPackageName() + "/" + soundResId
        );

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setSound(soundUri, attributes);

        manager.createNotificationChannel(channel);

        CommonLogic.showTestLog(TAG, "✅ Channel CREATED: " + channelId);
    }*/


}
