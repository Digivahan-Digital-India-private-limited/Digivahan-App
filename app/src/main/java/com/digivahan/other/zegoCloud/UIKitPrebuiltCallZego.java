package com.digivahan.other.zegoCloud;

import android.app.Activity;

import com.digivahan.App;
import com.digivahan.utils.Constants;
//import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
//import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
//import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
//import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;


public interface UIKitPrebuiltCallZego {


    /*public static void loginUser(String userId, String userName) {

        ZegoUIKitPrebuiltCallInvitationConfig config =
                new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallService.init(
                App.getInstance(),
                Constants.APP_ID,
                Constants.zigoAppSign,
                userId,
                userName,
                config
        );

        // Enable FCM for background calls
        ZegoUIKitPrebuiltCallService.enableFCMPush();
    }


    public static void makeVoiceCall(Activity activity, String receiverUserId) {

        ZegoUIKitUser user = new ZegoUIKitUser(receiverUserId);

        ZegoUIKitPrebuiltCallService.sendInvitationWithUIChange(
                activity,
                Collections.singletonList(user),
                ZegoInvitationType.VOICE_CALL,
                null
        );
    }

    public static void endCall() {
        ZegoUIKitPrebuiltCallService.endCall();
    }*/


}
