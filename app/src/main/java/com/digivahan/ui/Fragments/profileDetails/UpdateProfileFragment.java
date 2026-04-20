package com.digivahan.ui.Fragments.profileDetails;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.EmergencyContactModel;
import com.digivahan.data.model.User;
import com.digivahan.databinding.FragmentUpdateProfileBinding;
import com.digivahan.databinding.ItemProfileRowBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.changPassword.ChangePasswordPage;
import com.digivahan.ui.Activities.deliveryAddress.SetDefaultAddressPage;
import com.digivahan.ui.Activities.emergencyContacts.EmergencyContacts;
import com.digivahan.ui.Activities.profile.UpdateBasicDetails;
import com.digivahan.ui.Activities.deliveryAddress.UpdateDeliveryAddress;
import com.digivahan.ui.Activities.profile.UpdatePublicDetails;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.AppKit;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileFragment extends Fragment {
    String TAG = "UpdateProfileFragmentData";
    FragmentUpdateProfileBinding binding;

    PreferencesManager preferencesManager;


    @Override
    public void onResume() {
        super.onResume();
        setProfileValueStatus();

        checkEmergencyContactList();

        binding.userName.setText(preferencesManager.getUser().getFirst_name() + " " + preferencesManager.getUser().getLast_name());
        ImageHelperMethods.loadImage(TAG, getContext(), preferencesManager.getUser().getProfile_pic(), binding.profileImage, R.drawable.temp_profile_icon);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUpdateProfileBinding.inflate(getLayoutInflater(), container, false);

        preferencesManager = new PreferencesManager(requireContext());


        binding.basicDetailBtn.mainLayout.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent basicProfile = new Intent(getContext(), UpdateBasicDetails.class);
            startActivity(basicProfile);
        });

        binding.publicDetailsBtn.mainLayout.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent basicProfile = new Intent(getContext(), UpdatePublicDetails.class);
            startActivity(basicProfile);
        });

        binding.tvAddress.mainLayout.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent basicProfile = new Intent(getContext(), UpdateDeliveryAddress.class);
            startActivity(basicProfile);
        });

        binding.emergencyContact.mainLayout.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent basicProfile = new Intent(getContext(), EmergencyContacts.class);
            startActivity(basicProfile);
        });

        binding.setDefaultOption.mainLayout.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent basicProfile = new Intent(getContext(), SetDefaultAddressPage.class);
            startActivity(basicProfile);
        });

        binding.changePasswordBtn.mainLayout.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent basicProfile = new Intent(getContext(), ChangePasswordPage.class);
            startActivity(basicProfile);
        });

        binding.footerVersion.setText("Version " + AppKit.getAppVersion(getContext(), "1"));

        return binding.getRoot();
    }

    public void checkEmergencyContactList(){
        setButtonData(binding.emergencyContact, R.drawable.emergency_contact_icon, "Emergency Contacts", "1", "0", false, false);

        JsonObject jsonObjectDeliveryAddress = new JsonObject();
        jsonObjectDeliveryAddress.addProperty("user_id", preferencesManager.getUserId());
        jsonObjectDeliveryAddress.addProperty("details_type", "emergency_contacts");

        // --- Step 2: Make API call ---
        ApiClient.getApiService(getContext()).commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_USER_DETAILS, jsonObjectDeliveryAddress).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                    CommonLogic.showTestLog(TAG, responseBody.toString());

                    boolean status = responseBody.has("success") && responseBody.getBoolean("success");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "";

//                    Toast.makeText(EmergencyContacts.this, message, Toast.LENGTH_SHORT).show();

                    if (status) {
                        try {
                            setButtonData(binding.emergencyContact, R.drawable.emergency_contact_icon, "Emergency Contacts", "1", "0", !APIHelper.convertJsonArrayToList(responseBody.getJSONArray("data"), EmergencyContactModel.class).isEmpty(), true);

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

            }
        });
    }

    private void setProfileValueStatus() {

        int basicDetailsFiledCompleted = 0;
        basicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getProfile_pic());
        basicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getFirst_name());
        basicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getLast_name());
        basicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getEmail());
        basicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getPhone_number());
        basicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getOccupation());



        setButtonData(binding.basicDetailBtn, R.drawable.basic_detail_icon, "Basic Details", "6", String.valueOf(basicDetailsFiledCompleted), basicDetailsFiledCompleted == 6, true);

        int publicDetailsFiledCompleted = 0;
        publicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getPublic_pic());
        publicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getNick_name());
        publicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getAddress());
        publicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getAge());
        publicDetailsFiledCompleted += checkValue(preferencesManager.getUser().getGender());
        setButtonData(binding.publicDetailsBtn, R.drawable.public_detail_icon, "Public Details", "5", String.valueOf(publicDetailsFiledCompleted), basicDetailsFiledCompleted == 5, true);


        setButtonData(binding.tvAddress, R.drawable.address_icon, "Address Book", "5", "3", false, false);
        setButtonData(binding.setDefaultOption, R.drawable.address_icon, "Default Address", "5", "3", false, false);
        setButtonData(binding.changePasswordBtn, R.drawable.password_icon2, "Change Password", "5", "3", false, false);

        JsonObject jsonObjectDeliveryAddress = new JsonObject();
        jsonObjectDeliveryAddress.addProperty("user_id", preferencesManager.getUserId());
        jsonObjectDeliveryAddress.addProperty("details_type", "basic_details");

        binding.progressText.setText(preferencesManager.getUser().getProfile_completion_percent() + "% Complete");
        binding.progressCircle.setProgress(preferencesManager.getUser().getProfile_completion_percent() != null ? Integer.parseInt(preferencesManager.getUser().getProfile_completion_percent()) : 0);

        ApiClient.getApiService(getContext())
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_USER_DETAILS, jsonObjectDeliveryAddress)
                .enqueue(new Callback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                            boolean status = Objects.requireNonNull(responseBody).optBoolean("success", false);
                            String message = responseBody.optString("message", "Server error, Please try after some time.");

                            if (status) {
                                try {

                                    User userUpdatedData = preferencesManager.getUser();
                                    userUpdatedData.setProfile_completion_percent(responseBody.getJSONObject("data").getString("profile_completion_percent"));

                                    preferencesManager.saveUser(userUpdatedData);

                                    binding.progressText.setText(preferencesManager.getUser().getProfile_completion_percent() + "% Complete");
                                    binding.progressCircle.setProgress(preferencesManager.getUser().getProfile_completion_percent() != null ? Integer.parseInt(preferencesManager.getUser().getProfile_completion_percent()) : 0);

                                } catch (Exception e) {
                                    CommonLogic.showTestLog(TAG, "Data parse error: " + e.getMessage());
                                }
                            } else {
                                CommonLogic.showTestLog(TAG, "API failed: " + message);
                            }

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "Response error: " + e.getMessage());
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        CommonLogic.showTestLog(TAG, "onFailure: Updation failed. Please try again.");
                    }
                });
    }

    private void setButtonData(ItemProfileRowBinding itemProfileRowBinding, int resourceId, String title,
                               String totalValues, String completedValues, boolean isSetCompleted,
                               boolean showFiledDetails) {
        itemProfileRowBinding.ivIcon.setImageResource(resourceId);
        itemProfileRowBinding.tvLabel.setText(title);

        if (isSetCompleted || totalValues.equalsIgnoreCase(completedValues)){
            itemProfileRowBinding.tvCount.setVisibility(View.GONE);
            itemProfileRowBinding.ivCompetedIcon.setVisibility(View.VISIBLE);
        }
        else {
            itemProfileRowBinding.tvCount.setText(completedValues + "/" +totalValues);
            itemProfileRowBinding.tvCount.setVisibility(View.VISIBLE);
            itemProfileRowBinding.ivCompetedIcon.setVisibility(View.GONE);
        }

        if (!showFiledDetails){
            itemProfileRowBinding.tvCount.setVisibility(View.GONE);
            itemProfileRowBinding.ivCompetedIcon.setVisibility(View.GONE);
        }
    }


    public int checkValue(String value){
        int result = 0;

        if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")){
            result = 1;
        }

        return result;
    }


}