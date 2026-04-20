package com.digivahan.ui.Fragments.profileMenu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashu.ashuutils.APIHelper;
import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.User;
import com.digivahan.databinding.FragmentProfileMenuBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.ui.Activities.garage.MyGarageActivity;
import com.digivahan.ui.Activities.orderDetails.OrderListPage;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileMenuFragment extends Fragment {
    String TAG = "ProfileMenuFragmentData";
    FragmentProfileMenuBinding binding;

    PreferencesManager manager;

    NavController navController;

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        binding.userName.setText(manager.getUser().getFirst_name() + " " + manager.getUser().getLast_name());
        binding.userNickName.setText(manager.getUser().getNick_name());
        ImageHelperMethods.loadImage(TAG, getContext(), manager.getUser().getProfile_pic(), binding.profileImage, R.drawable.temp_profile_icon);

        setProfileValueStatus();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileMenuBinding.inflate(getLayoutInflater(), container, false);

//        binding.footerVersion.setText("Version " + BuildConfig.VERSION_NAME);


        NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        manager = new PreferencesManager(requireActivity());

        setMenuData();

        binding.liveTrackingBtn.menuItemLayout.setOnClickListener(v -> {
            binding.liveTrackingBtn.switchBtn.setChecked(!binding.liveTrackingBtn.switchBtn.isChecked());
        });

        binding.notificationBtn.menuItemLayout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFragment(
                        R.id.nav_notification,   // target fragment id
                        false,                // showBottomNav
                        false                 // showNotificationIcon
                );
            }
        });

        binding.myGarageBtn.menuItemLayout.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent intent = new Intent(getContext(), MyGarageActivity.class);
            startActivity(intent);
        });

        binding.myOrderBtn.menuItemLayout.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent mainPage = new Intent(getContext(), OrderListPage.class);
            startActivity(mainPage);
        });

        binding.myVirtualQRBtn.menuItemLayout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFragment(
                        R.id.nav_virtualQR,   // target fragment id
                        false,                // showBottomNav
                        true                 // showNotificationIcon
                );
            }
        });

        binding.updateProfileBtn.menuItemLayout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFragment(
                        R.id.nav_profile,   // target fragment id
                        false,                // showBottomNav
                        true                 // showNotificationIcon
                );
            }
        });

        /*binding.aboutUsBtn.menuItemLayout.setOnClickListener(v -> {
            Intent aboutUsPage = new Intent(getContext(), AboutUsPage.class);
            startActivity(aboutUsPage);
        });

        binding.termConditionBtn.menuItemLayout.setOnClickListener(v -> {
            Intent termConditionPage = new Intent(getContext(), TermsConditionPage.class);
            startActivity(termConditionPage);
        });

        binding.privacyPolicyBtn.menuItemLayout.setOnClickListener(v -> {
            Intent privacyPolicyPage = new Intent(getContext(), PrivacyPolicyPage.class);
            startActivity(privacyPolicyPage);
        });*/

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void setProfileValueStatus() {

        CommonLogic.showTestLog(TAG, "setProfileValueStatus() called");

        JsonObject jsonObjectDeliveryAddress = new JsonObject();
        jsonObjectDeliveryAddress.addProperty("user_id", manager.getUserId());
        jsonObjectDeliveryAddress.addProperty("details_type", "basic_details");

        CommonLogic.showTestLog(TAG, "Request Body: " + jsonObjectDeliveryAddress.toString());

        binding.progressText.setText(manager.getUser().getProfile_completion_percent() + "% Complete");

        binding.progressCircle.setProgress(
                manager.getUser().getProfile_completion_percent() != null ?
                        Integer.parseInt(manager.getUser().getProfile_completion_percent()) : 0
        );

        CommonLogic.showTestLog(TAG, "Current Profile Percent: " + manager.getUser().getProfile_completion_percent());

        ApiClient.getApiService(getContext())
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_USER_DETAILS, jsonObjectDeliveryAddress)
                .enqueue(new Callback<JsonObject>() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        CommonLogic.showTestLog(TAG, "API Response Code: " + response.code());
                        CommonLogic.showTestLog(TAG, "API Raw Response: " + response.toString());

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                            CommonLogic.showTestLog(TAG, "Parsed Response: " + responseBody);

                            boolean status = Objects.requireNonNull(responseBody).optBoolean("success", false);
                            String message = responseBody.optString("message", "Server error, Please try after some time.");

                            CommonLogic.showTestLog(TAG, "API Status: " + status);
                            CommonLogic.showTestLog(TAG, "API Message: " + message);

                            if (status) {
                                try {

                                    JSONObject data = responseBody.getJSONObject("data");
                                    CommonLogic.showTestLog(TAG, "Data Object: " + data);

                                    String percent = data.getString("profile_completion_percent");
                                    CommonLogic.showTestLog(TAG, "Profile Completion Percent from API: " + percent);

                                    User userUpdatedData = manager.getUser();
                                    userUpdatedData.setProfile_completion_percent(percent);

                                    manager.saveUser(userUpdatedData);

                                    binding.progressText.setText(manager.getUser().getProfile_completion_percent() + "% Complete");

                                    binding.progressCircle.setProgress(
                                            manager.getUser().getProfile_completion_percent() != null ?
                                                    Integer.parseInt(manager.getUser().getProfile_completion_percent()) : 0
                                    );

                                    CommonLogic.showTestLog(TAG, "UI Updated Successfully");

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

                        CommonLogic.showTestLog(TAG, "API onFailure Triggered");
                        CommonLogic.showTestLog(TAG, "Failure Reason: " + t.getMessage());

                    }
                });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setMenuData() {

        binding.liveTrackingBtn.navIcon.setImageResource(R.drawable.live_location_icon);
        binding.liveTrackingBtn.titleText.setText(getResources().getString(R.string.nav_live_tracking));
        binding.liveTrackingBtn.switchBtn.setVisibility(View.VISIBLE);
        binding.liveTrackingBtn.switchBtn.setFocusable(false);
        binding.liveTrackingBtn.switchBtn.setClickable(false);

        binding.notificationBtn.navIcon.setImageResource(R.drawable.notifications_icon1);
        binding.notificationBtn.titleText.setText(getResources().getString(R.string.nav_notification));
        binding.notificationBtn.switchBtn.setVisibility(View.VISIBLE);
        binding.notificationBtn.switchBtn.setChecked(manager.getBoolean(PreferencesManager.NOTIFICATION_SOUND, false));
        binding.notificationBtn.switchBtn.setOnCheckedChangeListener((compoundButton, b) -> {
            manager.setBoolean(PreferencesManager.NOTIFICATION_SOUND, b);

            JsonObject jsonObjectSendNotification = new JsonObject();
            jsonObjectSendNotification.addProperty("user_id", manager.getUserId());
            jsonObjectSendNotification.addProperty("is_notification_on", b);

            ApiCall.callApi(TAG, getActivity(), APIData.SET_NOTIFICATION_SOUND, jsonObjectSendNotification, "post", new ApiCall.ApiResponseCallback() {
                @Override
                public void onSuccess(JSONObject responseBody, boolean status, String message) {

                }

                @Override
                public void onError(String errorMessage) {

                }
            });
        });

        binding.myGarageBtn.navIcon.setImageResource(R.drawable.my_garage_icon);
        binding.myGarageBtn.titleText.setText(getResources().getString(R.string.nav_my_garage));

        binding.myOrderBtn.navIcon.setImageResource(R.drawable.my_order_icon);
        binding.myOrderBtn.titleText.setText(getResources().getString(R.string.nav_my_order));

        binding.myVirtualQRBtn.navIcon.setImageResource(R.drawable.ic_qr);
        binding.myVirtualQRBtn.titleText.setText(getResources().getString(R.string.nav_virtual_qrs));

        binding.updateProfileBtn.navIcon.setImageResource(R.drawable.profile_icon1);
        binding.updateProfileBtn.titleText.setText(getResources().getString(R.string.nav_update_profile));

        /*binding.aboutUsBtn.navIcon.setImageResource(R.drawable.group_icon);
        binding.aboutUsBtn.titleText.setText(getResources().getString(R.string.nav_about_us));

        binding.termConditionBtn.navIcon.setImageResource(R.drawable.term_condition_icon);
        binding.termConditionBtn.titleText.setText(getResources().getString(R.string.nav_term_condition));

        binding.privacyPolicyBtn.navIcon.setImageResource(R.drawable.policy_icon);
        binding.privacyPolicyBtn.titleText.setText(getResources().getString(R.string.nav_privacy_policy));
        binding.privacyPolicyBtn.divider.setVisibility(View.GONE);*/

    }
}