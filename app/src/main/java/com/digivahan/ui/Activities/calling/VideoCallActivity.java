package com.digivahan.ui.Activities.calling;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.databinding.ActivityVideoCallBinding;
import com.digivahan.utils.Constants;
//import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
//import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;

public class VideoCallActivity extends AppCompatActivity {
    String TAG = "VideoCallActivityData";

    ActivityVideoCallBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PreferencesManager preferencesManager = new PreferencesManager(VideoCallActivity.this);


        long appID = Constants.APP_ID;   // yourAppID
        String appSign = Constants.zigoAppSign;  // yourAppSign
        String userID = preferencesManager.getUser().getPhone_number();
        String userName = preferencesManager.getUser().getFirst_name() + " " + preferencesManager.getUser().getLast_name();
        String callID = "test_call_123"; // Unique room ID

        /*// 1️⃣ Create config (one-on-one or group)
        ZegoUIKitPrebuiltCallConfig callConfig = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();

        // 2️⃣ Create Fragment instance
        ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                appID,
                appSign,
                userID,
                userName,
                callID,
                callConfig
        );

        // 3️⃣ Show the call fragment inside your Activity
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.callContainer, fragment)
                .commit();*/
    }
}