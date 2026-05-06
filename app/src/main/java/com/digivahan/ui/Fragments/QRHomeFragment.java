package com.digivahan.ui.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.digivahan.R;
import com.digivahan.data.adapters.BenefitsOfQRCodeAdapter;
import com.digivahan.data.adapters.HowToUseQRAdapter;
import com.digivahan.data.adapters.NearByServiceAdapter;
import com.digivahan.data.adapters.NewServicesAdapter;
import com.digivahan.data.adapters.VehicleServiceItemAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.BenefitsOfQRCodeModel;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.HowToUseQRItemModel;
import com.digivahan.data.model.NearByServiceItem;
import com.digivahan.data.model.NewServicesItemModel;
import com.digivahan.data.model.VehicleServiceItemModel;
import com.digivahan.databinding.FragmentQRHomeBinding;
import com.digivahan.ui.Activities.BBPSServices.FasTag.SelectVehicleBBPS;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.ui.Activities.garage.MyGarageActivity;
import com.digivahan.ui.Activities.garage.VehicleInformation;
import com.digivahan.ui.Activities.orderDetails.OrderQRPage;
import com.digivahan.ui.Activities.qr.ScanQRCode;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.android.material.slider.Slider;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRHomeFragment extends Fragment {
    String TAG = "QRHomeFragmentData";
    FragmentQRHomeBinding binding;

    int autoCardMoveTimer = 5000;
    private int currentPage = 0, size = 0;
    private Handler howToUseQRHandler, newServiceAutoScrollHandler;
    private Runnable howToUseQRUpdate, newServiceAutoScrollRunnable;

    private ArrayList<Slider> sliderArrayList;

    NavController navController;

    ArrayList<NearByServiceItem> nearByServiceItemList = new ArrayList<>();
    ArrayList<HowToUseQRItemModel> howToUseQRItemModels = new ArrayList<>();
    ArrayList<BenefitsOfQRCodeModel> benefitsOfQRCodeItemList = new ArrayList<>();

    BenefitsOfQRCodeAdapter benefitsOfQRCodeAdapter;
    NearByServiceAdapter nearByServiceAdapter;
    VehicleServiceItemAdapter vehicleServiceItemAdapter;

    Handler handler = new Handler();
    Runnable runnable;
    LinearLayoutManager layoutManager;

    PreferencesManager manager;

    @Override
    public void onResume() {
        super.onResume();
        CommonMethods.startAutoScroll(howToUseQRHandler, howToUseQRUpdate, 3000);
        CommonMethods.startAutoScroll(newServiceAutoScrollHandler, newServiceAutoScrollRunnable, 4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        CommonMethods.stopAutoScroll(howToUseQRHandler, howToUseQRUpdate);
        CommonMethods.stopAutoScroll(newServiceAutoScrollHandler, newServiceAutoScrollRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  FragmentQRHomeBinding.inflate(getLayoutInflater(), container, false);

        howToUseQRItemModels.clear();
//        howToUseQRItemModels.add(R.drawable.how_to_use_image1);
        /*howToUseQRItemModels.add(new HowToUseQRItemModel(R.drawable.how_to_use_image1, "Scan QR Code"));
        howToUseQRItemModels.add(new HowToUseQRItemModel(R.drawable.how_to_use_image2, "Send alert to QR Owner"));
        howToUseQRItemModels.add(new HowToUseQRItemModel(R.drawable.how_to_use_image3, "Send alert to QR Owner"));
        howToUseQRItemModels.add(new HowToUseQRItemModel(R.drawable.how_to_use_image4, "Message to QR Owner"));
        howToUseQRItemModels.add(new HowToUseQRItemModel(R.drawable.how_to_use_image5, "Call QR Owner"));
        howToUseQRItemModels.add(new HowToUseQRItemModel(R.drawable.how_to_use_image6, "QR Owner will pick the call"));*/

        manager = new PreferencesManager(requireActivity());

        HowToUseQRAdapter tipsItemAdapter = new HowToUseQRAdapter(getContext(), howToUseQRItemModels);


        layoutManager = new LinearLayoutManager(
                getContext(),
                RecyclerView.HORIZONTAL,
                false
        );

        /*binding.rvHowToUse.setLayoutManager(layoutManager);
        binding.rvHowToUse.setAdapter(tipsItemAdapter);

// ✅ Attach SnapHelper ONCE
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.rvHowToUse);

// ✅ Auto scroll
        CommonMethods.setInfiniteAutoScroll(
                getContext(),
                binding.rvHowToUse,
                layoutManager,
                howToUseQRItemModels.size(),
                3000,
                howToUseQRHandler,
                howToUseQRUpdate, snapHelper
        );*/


        nearByServiceAdapter = new NearByServiceAdapter(getContext(), nearByServiceItemList);
        binding.nearByServices.setAdapter(nearByServiceAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        binding.nearByServices.setLayoutManager(gridLayoutManager);

        getNearByServiceList();


        NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }


        benefitsOfQRCodeAdapter = new BenefitsOfQRCodeAdapter(getContext(), benefitsOfQRCodeItemList);
        binding.rvBenefitsOfQRCode.setAdapter(benefitsOfQRCodeAdapter);

        getBenefitVideos();

        getNewServices();


        binding.qrContainer.setOnClickListener(v -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent userRequestPage = new Intent(getContext(), ScanQRCode.class);
            startActivity(userRequestPage);
        });


        ArrayList<VehicleServiceItemModel> vehicleServiceItemList = getVehicleServiceItemModels();

        vehicleServiceItemAdapter = new VehicleServiceItemAdapter(getContext(), vehicleServiceItemList);
        binding.vehicleServices.setAdapter(vehicleServiceItemAdapter);
        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(getContext(), 4);
        binding.vehicleServices.setLayoutManager(gridLayoutManager1);


        return binding.getRoot();
    }

    @NonNull
    private static ArrayList<VehicleServiceItemModel> getVehicleServiceItemModels() {
        ArrayList<VehicleServiceItemModel> vehicleServiceItemList = new ArrayList<>();

        VehicleServiceItemModel model1 = new VehicleServiceItemModel("Scan QR", "scan_qr", R.drawable.scan_qr_code_icon);
        vehicleServiceItemList.add(model1);

        VehicleServiceItemModel model2 = new VehicleServiceItemModel("Check Vehicle", "check_vehicle", R.drawable.check_vehicle_icon);
        vehicleServiceItemList.add(model2);

        VehicleServiceItemModel model3 = new VehicleServiceItemModel("Check Challan", "check_challan", R.drawable.check_challan_icon);
        vehicleServiceItemList.add(model3);

        VehicleServiceItemModel model4 = new VehicleServiceItemModel("FASTag", "FASTag", R.drawable.fastag_icon);
        vehicleServiceItemList.add(model4);

        /*VehicleServiceItemModel model9 = new VehicleServiceItemModel("Challan Pay", "challan_pay", R.drawable.check_challan_icon);
        vehicleServiceItemList.add(model9);*/

        VehicleServiceItemModel model5 = new VehicleServiceItemModel("Activate QR", "activate_qr", R.drawable.activate_qr_code_icon);
        vehicleServiceItemList.add(model5);

        VehicleServiceItemModel model6 = new VehicleServiceItemModel("My Garage", "my_garage", R.drawable.my_garage_icon);
        vehicleServiceItemList.add(model6);

        VehicleServiceItemModel model7 = new VehicleServiceItemModel("Download QR Code", "download_qr_code", R.drawable.download_qr_icon);
        vehicleServiceItemList.add(model7);

        VehicleServiceItemModel model8 = new VehicleServiceItemModel("Order QR Code", "order_qr_code", R.drawable.order_physical_qr_code_icon);
        vehicleServiceItemList.add(model8);
        return vehicleServiceItemList;
    }


    private void getNearByServiceList() {

        // --- Step 2: Make API call ---

        ApiClient.getApiService(getContext()).commonGETMethodToHitAllAPIs(APIData.GET_NEAR_BY_SERVICES).enqueue(new Callback<JsonObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response);


                    boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";


                    if (status) {
                        try {
                            nearByServiceItemList.clear();

                            for (NearByServiceItem nearByServiceItem : APIHelper.convertJsonArrayToList(responseBody.getJSONArray("data"), NearByServiceItem.class)){
                                if (nearByServiceItem.getStatus().equalsIgnoreCase("true")){
                                nearByServiceItemList.add(nearByServiceItem);
                                }
                            }

                            nearByServiceAdapter.notifyDataSetChanged();


                            if (!nearByServiceItemList.isEmpty()) {
                                binding.nearByServiceLayout.setVisibility(View.VISIBLE);
                            } else {
                                binding.nearByServiceLayout.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            binding.nearByServiceLayout.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    binding.nearByServiceLayout.setVisibility(View.GONE);
                    CommonLogic.showTestLog(TAG, e.getMessage());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                // ❌ Network or unexpected failure
                JsonObject errorObj = new JsonObject();
                errorObj.addProperty("status", false);
                errorObj.addProperty("message", t.getMessage());
                binding.nearByServiceLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setupDots(int itemCount) {
        binding.dotContainer.removeAllViews();

        int dotCount = Math.min(3, itemCount);

        for (int i = 0; i < dotCount; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(16, 16);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_unselected);
            binding.dotContainer.addView(dot);
        }
    }

    private void updateDots(int selectedIndex) {
        int childCount = binding.dotContainer.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View dot = binding.dotContainer.getChildAt(i);
            if (i == selectedIndex){
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(20, 20);
                params.setMargins(8, 0, 8, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_selected);
            }else {
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(16, 16);
                params.setMargins(8, 0, 8, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_unselected);
            }
        }
    }

    private void getNewServices() {

        ArrayList<NewServicesItemModel> serviceItemList = new ArrayList<>();
        serviceItemList.add(new NewServicesItemModel(R.drawable.activate_qr_code_icon, "Activate QR Code", "Scan Digivahan QR code to activate it.",
                "Scan Now", "scanAssign"));

        serviceItemList.add(new NewServicesItemModel(R.drawable.garage_empty_image1, "Add Vehicle", "Add your vehicle to the garage",
                "Add Vehicle", "garage"));

        serviceItemList.add(new NewServicesItemModel(R.drawable.qr_code_img, "Scan QR Code", "Scan Digivahan QR code to contact the vehicle owner.",
                "Scan Now", "scanConnect"));

        /*serviceItemList.add(new NewServicesItemModel(R.drawable.ic_vehicle_4w, "Check Challan", "Check Vehicle Challan information",
                "Check Challan", "challanInfo"));*/

        setupDots(serviceItemList.size());

        binding.newServices.setClipToPadding(false);
        binding.newServices.setClipChildren(false);
        binding.newServices.setPadding(40, 0, 40, 0);
        binding.newServices.setOverScrollMode(View.OVER_SCROLL_NEVER);

// Add spacing between items
        int spacing = 20;
        binding.newServices.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = spacing;
                outRect.left = spacing;
            }
        });


        NewServicesAdapter servicesAdapter = new NewServicesAdapter(getActivity(), serviceItemList);
        binding.newServices.setAdapter(servicesAdapter);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.newServices);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        binding.newServices.setLayoutManager(layoutManager);
        /*CommonMethods.setInfiniteAutoScroll(
                getContext(),
                binding.newServices,
                layoutManager,
                serviceItemList.size(),
                4000, // auto scroll every 3 seconds
                newServiceAutoScrollHandler,
                newServiceAutoScrollRunnable, snapHelper

        );*/

        binding.scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v,
                                       int scrollX,
                                       int scrollY,
                                       int oldScrollX,
                                       int oldScrollY) {

                if (scrollY > oldScrollY) {
                    hideQrText();   // scrolling down
                } else if (scrollY < oldScrollY) {
                    showQrText();   // scrolling up
                }
            }
        });


        binding.newServices.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);

                    if (centerView != null) {
                        int position =
                                layoutManager.getPosition(centerView);

                        int actualIndex =
                                position % serviceItemList.size();

                        updateDots(actualIndex % 3);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        /*// Slow auto-scroll
        runnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = layoutManager.findFirstVisibleItemPosition() + 1;

                SlowLinearSmoothScroller scroller = new SlowLinearSmoothScroller(getContext());
                scroller.setTargetPosition(nextItem);
                layoutManager.startSmoothScroll(scroller);

                handler.postDelayed(this, autoCardMoveTimer); // scroll every 3 seconds
            }
        };
        handler.postDelayed(runnable, autoCardMoveTimer);*/
    }

    private void hideQrText() {
        if (binding.qrText.getVisibility() == View.VISIBLE) {
            binding.qrText.animate()
                    .alpha(0f)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction(() ->
                            binding.qrText.setVisibility(View.GONE)
                    )
                    .start();
        }
    }

    private void showQrText() {
        if (binding.qrText.getVisibility() != View.VISIBLE) {
            binding.qrText.setVisibility(View.VISIBLE);
            binding.qrText.setAlpha(0f);
            binding.qrText.setScaleX(0f);
            binding.qrText.setScaleY(0f);

            binding.qrText.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
        }
    }


    private void orderQr(String orderType){
        ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
        Intent orderDetailsPage = new Intent(getContext(), OrderQRPage.class);
        orderDetailsPage.putExtra("orderType", orderType);
        startActivity(orderDetailsPage);
    }

    private void getBenefitVideos() {

        // --- Step 2: Make API call ---
        ApiClient.getApiService(getContext()).commonGETMethodToHitAllAPIs(APIData.GET_BENEFIT_VIDEO).enqueue(new Callback<JsonObject>() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response);


                    boolean status = responseBody.has("success") && responseBody.getBoolean("success");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";


                    if (status) {
                        try {

                            benefitsOfQRCodeItemList.clear();
                            benefitsOfQRCodeItemList.addAll(APIHelper.convertJsonArrayToList(responseBody.getJSONArray("data"), BenefitsOfQRCodeModel.class));

                            benefitsOfQRCodeAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!benefitsOfQRCodeItemList.isEmpty()){
                            binding.benefitsQRLayout.setVisibility(View.VISIBLE);
                        }else {binding.benefitsQRLayout.setVisibility(View.GONE);}
                    }
                } catch (JSONException e) {
                    CommonLogic.showTestLog(TAG, e.getMessage());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                // ❌ Network or unexpected failure
                JsonObject errorObj = new JsonObject();
                errorObj.addProperty("status", false);
                errorObj.addProperty("message", t.getMessage());
            }
        });
    }
}