package com.digivahan.ui.Activities.qr;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;

import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.R;
import com.digivahan.data.adapters.ConnectEmergencyContactListAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.EmergencyContactModel;
import com.digivahan.data.model.User;
import com.digivahan.databinding.ActivityConnectEmergencyContactsPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectEmergencyContactsPage extends BaseActivity {

    String TAG = "ConnectEmergencyContactsPageData";

    ActivityConnectEmergencyContactsPageBinding binding;

    PreferencesManager manager;
    AshDialog loadingDialog;

    ArrayList<EmergencyContactModel> contactList = new ArrayList<>();
    ConnectEmergencyContactListAdapter emergencyContactListItemAdapter;
    User vehicleOwnerDetails;

    Dialog callRequestDialog;

    String chatRoomId = "";


    @Override
    protected void onResume() {
        super.onResume();
        getEmergencyContactList();

        if (callRequestDialog != null && callRequestDialog.isShowing()){
            callRequestDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectEmergencyContactsPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Connect Emergency Contacts");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        try {

            if (getIntent().hasExtra("chatRoomId")) {
                chatRoomId = getIntent().getStringExtra("chatRoomId");
//                initializeChattingSystem(chatRoomId);
            }

            vehicleOwnerDetails = (User) getIntent().getSerializableExtra("vehicleOwnerDetails");
            CommonLogic.showTestLog(TAG, "vehicleOwnerDetails saved");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        manager = new PreferencesManager(ConnectEmergencyContactsPage.this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });

        callRequestDialog = new Dialog(ConnectEmergencyContactsPage.this);
        callRequestDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        callRequestDialog.setContentView(R.layout.dialog_password_changed);
        callRequestDialog.setCancelable(true);

        loadingDialog = new AshDialog(ConnectEmergencyContactsPage.this, "Please wait", "");

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

            ImageHelperMethods.loadImage(TAG, ConnectEmergencyContactsPage .this, vehicleOwnerDetails.getPublic_pic(),
                    binding.imgProfile, tempImage);
        }

        emergencyContactListItemAdapter = new ConnectEmergencyContactListAdapter(ConnectEmergencyContactsPage.this, contactList, loadingDialog, callRequestDialog);
        binding.rvEmergencyContact.setAdapter(emergencyContactListItemAdapter);

        binding.ivrCallLayout.setOnClickListener(view -> {
            CommonMethods.handleIvrCall(TAG, ConnectEmergencyContactsPage.this, callRequestDialog, manager.getUser().getPhone_number(), vehicleOwnerDetails.getPhone_number());
        });

        binding.sendSMSBtn.setOnClickListener(view -> {
            binding.sendSMSBtn.setClickable(false);
            loadingDialog.show();
            CommonMethods.sendSMSAlert(TAG, ConnectEmergencyContactsPage.this, vehicleOwnerDetails.getUserId(), "accident_alert",
                    new CommonMethods.SMSAlertCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(getApplicationContext(),
                                    "SMS Sent Successfully",
                                    Toast.LENGTH_SHORT).show();

                            binding.sendSMSBtn.setVisibility(View.GONE);
                            binding.sendSMSBtn.setClickable(true);
                            loadingDialog.dismiss();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(getApplicationContext(),
                                    "Unable to send SMS",
                                    Toast.LENGTH_SHORT).show();
                            binding.sendSMSBtn.setClickable(true);
                            loadingDialog.dismiss();
                        }
                    });

        });

        binding.sendWhatsAppBtn.setOnClickListener(view -> {
            disableHideContentSecureForNextNavigation();
            Intent chatPage = new Intent(ConnectEmergencyContactsPage.this, ChatActivity.class);
            chatPage.putExtra("timer", false);
            chatPage.putExtra("chatRoomId", chatRoomId);
            chatPage.putExtra("vehicleOwnerDetails", vehicleOwnerDetails);
            startActivity(chatPage);
            loadingDialog.dismiss();
        });

        /*binding.sendWhatsAppBtn.setOnClickListener(view -> {
            CommonMethods.sendWhatsAppAlert(TAG, ConnectEmergencyContactsPage.this, manager.getUser().getPhone_number(), vehicleOwnerDetails.getPhone_number());
        });*/

    }

    public void getEmergencyContactList(){
        JsonObject jsonObjectDeliveryAddress = new JsonObject();
        jsonObjectDeliveryAddress.addProperty("user_id", vehicleOwnerDetails.getUserId());
        jsonObjectDeliveryAddress.addProperty("details_type", "emergency_contacts");

        CommonLogic.showTestLog(TAG, "getEmergencyContactList params: " + jsonObjectDeliveryAddress.toString());

        loadingDialog.show();

        // --- Step 2: Make API call ---
        ApiClient.getApiService(ConnectEmergencyContactsPage.this).commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_USER_DETAILS, jsonObjectDeliveryAddress).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                // Always dismiss loader
                loadingDialog.dismiss();

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                    CommonLogic.showTestLog(TAG, "getEmergencyContactList response: " + responseBody.toString());

                    boolean status = responseBody.has("success") && responseBody.getBoolean("success");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "";

//                    Toast.makeText(EmergencyContacts.this, message, Toast.LENGTH_SHORT).show();

                    if (status) {
                        try {
                            contactList.clear();
                            CommonLogic.showTestLog(TAG, responseBody.getJSONArray("data").toString());
                            contactList.addAll(APIHelper.convertJsonArrayToList(responseBody.getJSONArray("data"), EmergencyContactModel.class));
                            CommonLogic.showTestLog(TAG, "list size " +String.valueOf(contactList.size()));

                            if (contactList.isEmpty()){
                                binding.emptyLayout.setVisibility(View.VISIBLE);
                                binding.rvEmergencyContact.setVisibility(View.GONE);
                            }else {
                                binding.emptyLayout.setVisibility(View.GONE);
                                binding.rvEmergencyContact.setVisibility(View.VISIBLE);
                            }

                            emergencyContactListItemAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    CommonLogic.showTestLog(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                CommonLogic.showTestLog(TAG, "onFailure:- Updation failed. Please try again.");
            }
        });
    }
}