package com.digivahan.other.zegoCloud;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.Constants;
//import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
//import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;

import org.json.JSONObject;

import java.util.ArrayList;

//import im.zego.zegoexpress.ZegoExpressEngine;
//import im.zego.zegoexpress.callback.IZegoEventHandler;
//import im.zego.zegoexpress.constants.ZegoPlayerState;
//import im.zego.zegoexpress.constants.ZegoPublisherState;
//import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
//import im.zego.zegoexpress.constants.ZegoScenario;
//import im.zego.zegoexpress.constants.ZegoUpdateType;
//import im.zego.zegoexpress.entity.ZegoEngineProfile;
//import im.zego.zegoexpress.entity.ZegoRoomConfig;
//import im.zego.zegoexpress.entity.ZegoStream;
//import im.zego.zegoexpress.entity.ZegoUser;

public interface VoiceCallZego {

   /* public static void createEngine(Activity context, String userID, String userName) {
        ZegoEngineProfile profile = new ZegoEngineProfile();

        long appID = Constants.APP_ID;   // yourAppID
        String appSign = Constants.zigoAppSign;  // yourAppSign
        // yourUserName

        // 🔹 Log the details for debugging
        Log.i("ZegoAppInit", "----------------------------------------");
        Log.i("ZegoAppInit", "ZEGOCLOUD Initialization Details:");
        Log.i("ZegoAppInit", "Application Context: " + context.getApplication());
        Log.i("ZegoAppInit", "App ID: " + appID);
        Log.i("ZegoAppInit", "App Sign: " + appSign);
        Log.i("ZegoAppInit", "User ID: " + userID);
        Log.i("ZegoAppInit", "User Name: " + userName);
        Log.i("ZegoAppInit", "----------------------------------------");

        // Get your AppID and AppSign from ZEGOCLOUD Console
        //[My Projects -> AppID] : https://console.zegocloud.com/project
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.DEFAULT; // General scenario.
        profile.application = context.getApplication();
        ZegoExpressEngine.createEngine(profile, null);

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallService.init(context.getApplication(), appID, appSign, userID, userName,callInvitationConfig);

        // 🔥 REQUIRED for background / killed app calls
        ZegoUIKitPrebuiltCallService.enableFCMPush();


    }


    // destroy engine
    public static void destroyEngine() {
        ZegoExpressEngine.destroyEngine(null);
    }

    public static void startListenEvent(String TAG) {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            // Callback for updates on the status of the streams in the room.
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                // When `updateType` is set to `ZegoUpdateType.ADD`, an audio and video
                // stream is added, and you can call the `startPlayingStream` method to
                // play the stream.
                if (updateType == ZegoUpdateType.ADD) {
                    startPlayStream(streamList.get(0).streamID);
                } else {
                    stopPlayStream(streamList.get(0).streamID);
                }
            }

            @Override
            // Callback for updates on the status of other users in the room.
            // Users can only receive callbacks when the isUserStatusNotify property of ZegoRoomConfig is set to `true` when logging in to the room (loginRoom).
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                // You can implement service logic in the callback based on the login
                // and logout status of users.
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoUser user : userList) {
                        String text = user.userID + "logged in to the room.";
//                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        CommonLogic.showTestLog(TAG, text);
                    }
                } else if (updateType == ZegoUpdateType.DELETE) {
                    for (ZegoUser user : userList) {
                        String text = user.userID + "logged out of the room.";
                        CommonLogic.showTestLog(TAG, text);
                    }
                }
            }

            @Override
            // Callback for updates on the current user's room connection status.
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int i, JSONObject jsonObject) {
                super.onRoomStateChanged(roomID, reason, i, jsonObject);
                if (reason == ZegoRoomStateChangedReason.LOGIN_FAILED) {
                    CommonLogic.showTestLog(TAG, "ZegoRoomStateChangedReason.LOGIN_FAILED");
                }
            }

            // Status notification of audio and video stream publishing.
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                if (errorCode != 0) {
                    // Stream publishing exception.
                }
                if (state == ZegoPublisherState.PUBLISHING) {
                    // Publishing streams.
                } else if (state == ZegoPublisherState.NO_PUBLISH) {
                    // Streams not published.
                    CommonLogic.showTestLog(TAG, "ZegoPublisherState.NO_PUBLISH");

                } else if (state == ZegoPublisherState.PUBLISH_REQUESTING) {
                    // Requesting stream publishing.
                }
            }

            // Status notifications of audio and video stream playing.
            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                if (errorCode != 0) {
                    // Stream playing exception.
                    CommonLogic.showTestLog(TAG, "onPlayerStateUpdate, state:" + state + "errorCode:" + errorCode);
                }
                if (state == ZegoPlayerState.PLAYING) {
                    // Playing streams.
                } else if (state == ZegoPlayerState.NO_PLAY) {
                    // Streams not played.
                    CommonLogic.showTestLog(TAG, "ZegoPlayerState.NO_PLAY" + errorCode);

                } else if (state == ZegoPlayerState.PLAY_REQUESTING) {
                    // Requesting stream playing.
                }
            }
        });
    }

    public static void stopListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(null);
    }


    public static void loginRoom(String TAG, String roomID, String userID, String userName) {
        ZegoUser user = new ZegoUser(userID, userName);
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        // The `onRoomUserUpdate` callback can be received only when
        // `ZegoRoomConfig` in which the `isUserStatusNotify` parameter is set to
        // `true` is passed.
        roomConfig.isUserStatusNotify = true;
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, roomConfig, (int error, JSONObject extendedData) -> {
            // Room login result. This callback is sufficient if you only need to
            // check the login result.
            if (error == 0) {
                // Login successful.
                // Start the preview and stream publishing.
                CommonLogic.showTestLog(TAG, "Login successful.");
//                startPreview();
                startPublish(roomID);
            } else {
                // Login failed. For details, see [Error codes\|_blank](/404).
                CommonLogic.showTestLog(TAG, "Login failed. error = " + error);
            }
        });
    }

    public static void logoutRoom() {
        ZegoExpressEngine.getEngine().logoutRoom();
    }


    public static void startPublish(String roomID) {
        // After calling the `loginRoom` method, call this method to publish streams.
        // The StreamID must be unique in the room.
        String streamID = roomID + "_call";
        ZegoExpressEngine.getEngine().enableCamera(false);
        ZegoExpressEngine.getEngine().startPublishingStream(streamID);
    }


    public static void stopPublish() {
        ZegoExpressEngine.getEngine().stopPublishingStream();
    }

    public static void startPlayStream(String streamID){
        ZegoExpressEngine.getEngine().startPlayingStream(streamID);
    }

    public static void stopPlayStream(String streamID){
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
//        findViewById(R.id.remoteUserView).setVisibility(View.GONE);
    }

    public static void askForRecordPermission(Activity activity){
        String[] permissionNeeded = {
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                // 101 is the requestCode. It can be any number greater than 0, and will be passed through to the permission request result callback onRequestPermissionsResult.
                activity.requestPermissions(permissionNeeded, 101);
            }
        }
    }

    public static void makeVoiceCall(String TAG, Activity activity, String userId, String receiverId, String userName){
        loginRoom(TAG, receiverId, userId, userName);
        startListenEvent(TAG);
    }

    public static void stopVoiceCall(){
        stopPublish();
        logoutRoom();
        destroyEngine();
    }*/
}
