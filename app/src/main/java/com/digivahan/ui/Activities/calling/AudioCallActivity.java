package com.digivahan.ui.Activities.calling;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.databinding.ActivityAudioCallBinding;
import com.digivahan.utils.Constants;
//import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
//import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;

public class AudioCallActivity extends AppCompatActivity {

    String TAG = "AudioCallActivityData";
    ActivityAudioCallBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*PreferencesManager preferencesManager = new PreferencesManager(AudioCallActivity.this);

        long appID = Constants.APP_ID;   // yourAppID
        String appSign = Constants.zigoAppSign;  // yourAppSign
        String userID = preferencesManager.getUser().getPhone_number();
        String userName = preferencesManager.getUser().getFirst_name() + " " + preferencesManager.getUser().getLast_name();
        String callID = "audio_call_123"; // Unique room ID for audio call

        // 1️⃣ Create config for one-on-one audio call
        ZegoUIKitPrebuiltCallConfig callConfig = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall();

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
                .replace(binding.callContainer.getId(), fragment)
                .commit();

         */
    }
}