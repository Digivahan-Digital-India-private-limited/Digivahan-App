package com.digivahan.ui.Fragments.dashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.bumptech.glide.Glide;
import com.digivahan.R;
import com.digivahan.data.adapters.NewsStoriesItemAdapter;
import com.digivahan.data.adapters.TipsItemAdapter;
import com.digivahan.data.adapters.TrendingCarsItemAdapter;
import com.digivahan.data.adapters.TrendingVSCarsItemAdapter;
import com.digivahan.data.adapters.VehicleCardAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.FuelItemModel;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.NewsStoriesItemModel;
import com.digivahan.data.model.TipsItemModel;
import com.digivahan.data.model.TrendingCarsModel;
import com.digivahan.data.model.TrendingVSCarsModel;
import com.digivahan.databinding.FragmentDashBoardBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.other.DateRatioUtil;

import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.DrawerController;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.ui.Activities.garage.MyGarageActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.TimeUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DashBoardFragment extends Fragment {

    String TAG = "DashBoardFragmentData";
    FragmentDashBoardBinding binding;
    ArrayList<String> stateList = new ArrayList<>();
    ArrayList<FuelItemModel> fuelItemDataList = new ArrayList<>();
    ArrayList<TrendingCarsModel> trendingCards = new ArrayList<>();
    ArrayList<TrendingVSCarsModel> trendingVSCards = new ArrayList<>();
    ArrayList<TipsItemModel> tipsItemList = new ArrayList<>();
    ArrayList<NewsStoriesItemModel> newsStoriesItemList = new ArrayList<>();

    TrendingCarsItemAdapter trendingCarsItemAdapter;
    TrendingVSCarsItemAdapter trendingVSCarsItemAdapter;
    TipsItemAdapter tipsItemAdapter;
    NewsStoriesItemAdapter newsStoriesItemAdapter;
    VehicleCardAdapter vehicleCardAdapter;

    Handler handler = new Handler();
    Runnable runnable;
    ArrayList<GarageItemModel> vehicleListData = new ArrayList<>();

    PreferencesManager preferencesManager;
    LinearLayoutManager layoutManager;

    int autoCardMoveTimer = 5000;

    AshDialog loadingDialog;

    private Handler tipsItemHandler, newsStoriesItemHandler;
    private Runnable tipsItemUpdate, newsStoriesItemUpdate;

    private static final int LOCATION_PERMISSION_REQUEST = 201;

    private FusedLocationProviderClient fusedLocationClient;

    DrawerController drawerController;


    Call<JsonObject> fuelPriceCall, garageCall, trendingCarsCall, compareVehiclesCall, tipsTricksCall, newsCall;

    ArrayList<GarageItemModel> cachedList;

    private PagerSnapHelper snapHelper;
    private RecyclerView.OnScrollListener garageScrollListener;
    private int lastPosition = -1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof DrawerController) {
            drawerController = (DrawerController) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        CommonLogic.showTestLog(TAG, "onResume call");

        CommonMethods.startAutoScroll(tipsItemHandler, tipsItemUpdate, 3000);
        CommonMethods.startAutoScroll(newsStoriesItemHandler, newsStoriesItemUpdate, 3000);

        cachedList = CommonMethods.loadGarageCache(getActivity());

        if (cachedList != null && !cachedList.isEmpty()){
            updateGarageUI(cachedList);
        }
        else{
            CommonLogic.showTestLog(TAG, "Vehicle list is empty. Showing Add Garage UI.");
            vehicleListData.clear();
            vehicleCardAdapter.notifyDataSetChanged();
            binding.carRecyclerView.setVisibility(View.GONE);
            binding.vehicleDocDatesLayout.setVisibility(View.GONE);
            binding.addGarageItemLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CommonLogic.showTestLog(TAG, "onViewCreated call");
//        loadingDialog.show();

        vehicleCardAdapter = new VehicleCardAdapter(getContext(), vehicleListData);
        binding.carRecyclerView.setAdapter(vehicleCardAdapter);

// ✅ Attach SnapHelper ONLY ONCE
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.carRecyclerView);

// ✅ Create ScrollListener ONLY ONCE
        garageScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                CommonLogic.showTestLog(TAG, "Scroll state changed = " + newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (vehicleListData == null || vehicleListData.isEmpty()) return;

                    View centerView = snapHelper.findSnapView(layoutManager);

                    if (centerView != null) {

                        int position = layoutManager.getPosition(centerView);
                        int actualIndex = position % vehicleListData.size();

                        CommonLogic.showTestLog(TAG,
                                "Center position = " + position +
                                        ", actualIndex = " + actualIndex);

                        updateDots(actualIndex % 3);

                        if (position != lastPosition) {

                            lastPosition = position;

                            CommonLogic.showTestLog(TAG,
                                    "Updating vehicle card data for index = " + actualIndex);

                            updateVehicleCardData(vehicleListData.get(actualIndex));
                        }
                    }
                }
            }
        };

// attach listener
        binding.carRecyclerView.addOnScrollListener(garageScrollListener);

        cachedList = CommonMethods.loadGarageCache(getActivity());

        if (cachedList == null || cachedList.isEmpty()){
            getGarageVehicleList();
        }

        getFuelPrice();
        getTrendingCars();
        getCompareVehicleSet();
        getTipsTricks();
        getNews();
    }


    @Override
    public void onPause() {
        super.onPause();
        CommonMethods.stopAutoScroll(tipsItemHandler, tipsItemUpdate);
        CommonMethods.stopAutoScroll(newsStoriesItemHandler, newsStoriesItemUpdate);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDashBoardBinding.inflate(getLayoutInflater(), container, false);

        loadingDialog = new AshDialog(getContext(), "Please wait", "");

        preferencesManager = new PreferencesManager(requireContext());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        binding.vehicleDocDatesLayout.setVisibility(View.GONE);

        binding.userName.setText(preferencesManager.getUser().getFirst_name() + " " + preferencesManager.getUser().getLast_name());

        ImageHelperMethods.loadImage(TAG, getContext(), preferencesManager.getUser().getProfile_pic(), binding.ivProfile, R.drawable.temp_profile_icon);

        binding.greetingText.setText(CommonLogic.getTimeGreeting());

        CommonMethods.setNotificationCount(getActivity(), binding.notificationCount, binding.ivBell, preferencesManager.getUserId());

        binding.ivBell.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFragment(
                        R.id.nav_notification,   // target fragment id
                        false,                // showBottomNav
                        false                 // showNotificationIcon
                );
            }
        });

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.carRecyclerView.setLayoutManager(layoutManager);

// Allow adjacent cards to be visible
        binding.carRecyclerView.setClipToPadding(false);
        binding.carRecyclerView.setClipChildren(false);
        binding.carRecyclerView.setPadding(40, 0, 40, 0);
        binding.carRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

// Add spacing between items
        int spacing = 20;
        binding.carRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = spacing;
                outRect.left = spacing;
            }
        });

        binding.ivProfile.setOnClickListener(v -> {
            if (drawerController != null) {
                drawerController.openDrawer();
            }
        });


        vehicleCardAdapter = new VehicleCardAdapter(getContext(), vehicleListData);
        binding.carRecyclerView.setAdapter(vehicleCardAdapter);



        /*binding.carCardBtn.setOnClickListener(v -> {
            Intent myGarage = new Intent(getContext(), MyGarageActivity.class);
            startActivity(myGarage);
        });*/


        trendingCarsItemAdapter = new TrendingCarsItemAdapter(getContext(), trendingCards);
        binding.rvTrendingCars.setAdapter(trendingCarsItemAdapter);

        trendingVSCarsItemAdapter = new TrendingVSCarsItemAdapter(getContext(), trendingVSCards);
        binding.rvVSTrendingCars.setAdapter(trendingVSCarsItemAdapter);

        tipsItemAdapter = new TipsItemAdapter(getContext(), tipsItemList);
        binding.rvTips.setAdapter(tipsItemAdapter);


        newsStoriesItemAdapter = new NewsStoriesItemAdapter(getContext(), newsStoriesItemList);
        binding.rvNewsStories.setAdapter(newsStoriesItemAdapter);


        List<Float> values = new ArrayList<>();
        values.add(20f); // expired
        values.add(60f); // valid
        values.add(15f); // pending
        values.add(5f);  // something else

        List<Integer> colors = new ArrayList<>();
        colors.add(0xFFFF6B6B); // red
        colors.add(0xFF2FB132); // green
        colors.add(0xFFEEDC82); // yellow
        colors.add(0xFF9AE6FF); // light blue

        binding.donutChart.setData(values, colors);
        binding.donutChart.setStrokeWidth(50f); // optional thickness


        // Example binding
        binding.addVehicle.tvTitle.setText("Add Vehicle");
        binding.addVehicle.tvDescription.setText("Add your vehicle to the garage");
        binding.addVehicle.serviceBtn.setText("Add Vehicle");

        Glide.with(requireContext())
                .load(R.drawable.garage_empty_image1)
                .override(800, 800)   // 🔥 LIMIT SIZE
                .centerInside()
                .into(binding.addVehicle.serviceIcon);

        binding.addVehicle.serviceBtn.setOnClickListener(view -> {
            ((BaseActivity) requireActivity()).disableHideContentSecureForNextNavigation();
            Intent myGarage = new Intent(getContext(), MyGarageActivity.class);
            startActivity(myGarage);
        });


        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        CommonLogic.showTestLog(TAG, "getCurrentLocation() called");

        if (!hasLocationPermission()) {
            CommonLogic.showTestLog(TAG, "Location permission NOT granted. Requesting permission.");
            showLocationDisclosure();
            return;
        }

        CommonLogic.showTestLog(TAG, "Location permission already granted");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {
                        CommonLogic.showTestLog(TAG,
                                "Last location received → Lat: "
                                        + location.getLatitude()
                                        + ", Lng: "
                                        + location.getLongitude());

                        handleLocation(location);
                    } else {
                        CommonLogic.showTestLog(TAG,
                                "Last location is NULL. Requesting fresh location.");

                        requestFreshLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    CommonLogic.showTestLog(TAG,
                            "Error while getting last location: " + e.getMessage());
                });
    }

    private void showLocationDisclosure() {
        new AlertDialog.Builder(getContext())
                .setTitle("Location Permission Required")
                .setMessage("DigiVahan collects location data to:\n\n" +
                        "• Find petrol prices\n" +
                        "• Show nearby services\n\n" +
                        "Location may be collected even when app is in background or running.")
                .setCancelable(false)
                .setPositiveButton("Allow", (dialog, which) -> {
                    requestActualPermission();
                })
                .setNegativeButton("Deny", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Location is required for core features", Toast.LENGTH_LONG).show();
                })
                .show();
    }


    @SuppressLint("MissingPermission")
    private void requestFreshLocation() {

        CommonLogic.showTestLog(TAG, "requestFreshLocation() called");

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setNumUpdates(1);

        CommonLogic.showTestLog(TAG,
                "Fresh location request created (HIGH_ACCURACY, 1 update)");

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {

                        CommonLogic.showTestLog(TAG,
                                "Fresh location callback received");

                        fusedLocationClient.removeLocationUpdates(this);
                        CommonLogic.showTestLog(TAG,
                                "Location updates removed");

                        Location location = locationResult.getLastLocation();

                        if (location != null) {
                            CommonLogic.showTestLog(TAG,
                                    "Fresh location → Lat: "
                                            + location.getLatitude()
                                            + ", Lng: "
                                            + location.getLongitude());

                            handleLocation(location);
                        } else {
                            CommonLogic.showTestLog(TAG,
                                    "Fresh location result is NULL");
                        }
                    }
                },
                Looper.getMainLooper()
        );
    }


    private void handleLocation(Location location) {

        CommonLogic.showTestLog(TAG,
                "handleLocation() called with Lat: "
                        + location.getLatitude()
                        + ", Lng: "
                        + location.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            CommonLogic.showTestLog(TAG, "Geocoder initialized");

            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {

                Address address = addresses.get(0);

                String state = address.getAdminArea();
                String city = address.getLocality();

                CommonLogic.showTestLog(TAG, "Geocoder success");
                CommonLogic.showTestLog(TAG, "State: " + state);
                CommonLogic.showTestLog(TAG, "City: " + city);

                autoSelectState(state);

            } else {
                CommonLogic.showTestLog(TAG,
                        "Geocoder returned empty address list");
            }

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG,
                    "Geocoder exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void autoSelectState(String userState) {
        int index = getStateIndex(stateList, userState);
        binding.spinnerState.setSelection(index);
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }


    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestActualPermission() {

        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST
        );
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

            if (i == selectedIndex) {
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(20, 20);
                params.setMargins(8, 0, 8, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_selected);
            } else {
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(16, 16);
                params.setMargins(8, 0, 8, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_unselected);
            }
        }
    }


    private void getGarageVehicleList() {

        CommonLogic.showTestLog(TAG, "getGarageVehicleList call");

        if (!isSafeToUpdateUI()) return;

        garageCall = CommonMethods.getGarageVehicleList(TAG,
                getActivity(),
                preferencesManager.getUserId(),
                new CommonMethods.GarageListCallback() {

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(ArrayList<GarageItemModel> garageList) {

                        CommonLogic.showTestLog(TAG, "getGarageVehicleList garageList size: " + garageList.size());

                        loadingDialog.dismiss();

                        updateGarageUI(garageList);

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        loadingDialog.dismiss();
                        if (!isSafeToUpdateUI()) return;
//                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateGarageUI(ArrayList<GarageItemModel> garageList) {

        CommonLogic.showTestLog(TAG, "updateGarageUI called. List size = " +
                (garageList != null ? garageList.size() : "null"));

        if (!isSafeToUpdateUI()) {
            CommonLogic.showTestLog(TAG, "UI not safe to update.");
            return;
        }

        vehicleListData.clear();

        if (garageList != null)
            vehicleListData.addAll(garageList);

        CommonLogic.showTestLog(TAG,
                "vehicleListData updated. Size = " + vehicleListData.size());

        vehicleCardAdapter.notifyDataSetChanged();

        setupDots(vehicleListData.size());

        if (!vehicleListData.isEmpty()) {

            CommonLogic.showTestLog(TAG, "Vehicle list not empty.");

            updateVehicleCardData(vehicleListData.get(0));

            // Infinite scroll start position
            int middle = Integer.MAX_VALUE / 2;
            int startPosition = middle - (middle % vehicleListData.size());

            CommonLogic.showTestLog(TAG,
                    "Scrolling to startPosition = " + startPosition);

            layoutManager.scrollToPosition(startPosition);

            binding.carRecyclerView.setVisibility(View.VISIBLE);
            binding.vehicleDocDatesLayout.setVisibility(View.VISIBLE);
            binding.addGarageItemLayout.setVisibility(View.GONE);

            CommonLogic.showTestLog(TAG, "Garage UI visible.");

        } else {

            CommonLogic.showTestLog(TAG, "Garage list empty.");

            binding.carRecyclerView.setVisibility(View.GONE);
            binding.vehicleDocDatesLayout.setVisibility(View.GONE);
            binding.addGarageItemLayout.setVisibility(View.VISIBLE);
        }
    }

    /*@SuppressLint("NotifyDataSetChanged")
    private void updateGarageUI(ArrayList<GarageItemModel> garageList) {

        CommonLogic.showTestLog(TAG, "updateGarageUI called. List size = " +
                (garageList != null ? garageList.size() : "null"));

        if (!isSafeToUpdateUI()) {
            CommonLogic.showTestLog(TAG, "UI not safe to update. Returning.");
            return;
        }

        vehicleListData.clear();
        vehicleListData.addAll(Objects.requireNonNull(garageList));

        CommonLogic.showTestLog(TAG, "vehicleListData updated. New size = " + vehicleListData.size());

        setupDots(vehicleListData.size());

        vehicleCardAdapter.notifyDataSetChanged();

        if (!vehicleListData.isEmpty()) {

            CommonLogic.showTestLog(TAG, "Vehicle list is not empty. Updating first vehicle card.");

            if (!isSafeToUpdateUI()) {
                CommonLogic.showTestLog(TAG, "UI not safe to update (second check). Returning.");
                return;
            }

            updateVehicleCardData(vehicleListData.get(0));

            // Start at middle for infinite effect
            int middle = Integer.MAX_VALUE / 2;
            int startPosition = middle - (middle % vehicleListData.size());

            CommonLogic.showTestLog(TAG, "Scrolling to startPosition = " + startPosition);

            layoutManager.scrollToPosition(startPosition);

            // Snap effect for centered cards
            SnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(binding.carRecyclerView);

            CommonLogic.showTestLog(TAG, "SnapHelper attached.");

            final int[] lastPosition = {-1};

            binding.carRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    CommonLogic.showTestLog(TAG, "Scroll state changed = " + newState);

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                        View centerView = snapHelper.findSnapView(layoutManager);

                        if (centerView != null) {

                            int position = layoutManager.getPosition(centerView);
                            int actualIndex = position % vehicleListData.size();

                            CommonLogic.showTestLog(TAG,
                                    "Center position = " + position +
                                            ", actualIndex = " + actualIndex);

                            updateDots(actualIndex % 3);

                            if (position != RecyclerView.NO_POSITION && position != lastPosition[0]) {

                                lastPosition[0] = position;

                                CommonLogic.showTestLog(TAG,
                                        "Updating vehicle card data for index = " + actualIndex);

                                updateVehicleCardData(
                                        vehicleListData.get(actualIndex)
                                );
                            }
                        } else {
                            CommonLogic.showTestLog(TAG, "Center view is null.");
                        }
                    }
                }
            });

            binding.carRecyclerView.setVisibility(View.VISIBLE);
            binding.vehicleDocDatesLayout.setVisibility(View.VISIBLE);
            binding.addGarageItemLayout.setVisibility(View.GONE);

            CommonLogic.showTestLog(TAG, "Garage UI visible.");

        }
        else {

            CommonLogic.showTestLog(TAG, "Vehicle list is empty. Showing Add Garage UI.");

            binding.carRecyclerView.setVisibility(View.GONE);
            binding.vehicleDocDatesLayout.setVisibility(View.GONE);
            binding.addGarageItemLayout.setVisibility(View.VISIBLE);
        }
    }*/

    public void updateVehicleCardData(GarageItemModel model) {

        binding.insuranceExpiryDate.setText(TimeUtils.convertDateFormat(model.getInsurance_expiry(), "dd MMM yyyy"));
        binding.insuranceRenewedDate.setText(TimeUtils.convertDateFormat(model.getInsurance_renewed_date(), "dd MMM yyyy"));
        binding.insurerName.setText(model.getInsurer_name());
        binding.policyType.setText(model.getInsurance_type());

        int insuranceDaysLeft = (int) TimeUtils.getDaysDifference(model.getInsurance_expiry(),
                CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"));


        if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), model.getInsurance_expiry())) {
            insuranceDaysLeft = insuranceDaysLeft != 0 ? insuranceDaysLeft * -1 : 0;
        }
        if (insuranceDaysLeft >= 0) {
            binding.insuranceDaysLeft.setText(insuranceDaysLeft + " Days Left");
        } else {
            binding.insuranceDaysLeft.setText((insuranceDaysLeft * -1) + " Days Ago");
        }

        CommonLogic.showTestLog(TAG, "insuranceProgressGraph calculated percentage: " + calculateRemainingPercentage(insuranceDaysLeft));

        binding.insuranceProgressGraph.setProgress(calculateRemainingPercentage(insuranceDaysLeft));

        if (insuranceDaysLeft >= 90
//                && insuranceDaysLeft > 150
        ) {
            binding.insuranceProgressGraph.setProgressColor(
                    ContextCompat.getColor(requireContext(), R.color.graphColor)
            );

        } else if (insuranceDaysLeft >= 30) {
            binding.insuranceProgressGraph.setProgressColor(
                    ContextCompat.getColor(requireContext(), R.color.graphColor1)
            );
        } else if (insuranceDaysLeft >= 10) {
            binding.insuranceProgressGraph.setProgressColor(
                    ContextCompat.getColor(requireContext(), R.color.graphColor2)
            );
        } else {
            binding.insuranceProgressGraph.setProgressColor(
                    ContextCompat.getColor(requireContext(), R.color.graphColor3)
            );
        }

//                                                        binding.insuranceProgressGraph.setProgress();

        binding.PUCValidUpTo.setText(TimeUtils.convertDateFormat(model.getPollution_expiry(), "dd MMM yyyy"));
        binding.PUCRenewedDate.setText(TimeUtils.convertDateFormat(model.getPollution_renew_date(), "dd MMM yyyy"));

        int pucDaysLeft = (int) TimeUtils.getDaysDifference(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), model.getPollution_expiry());

        if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), model.getPollution_expiry())) {
            pucDaysLeft = pucDaysLeft != 0 ? pucDaysLeft * -1 : 0;
        }
        if (pucDaysLeft >= 0) {
            binding.pucDaysLeft.setText(pucDaysLeft + " \nDays Left");
        } else {
            binding.pucDaysLeft.setText((pucDaysLeft * -1) + " \nDays Ago");
        }

        CommonLogic.showTestLog(TAG, "PUCProgressGraph calculated percentage: " + calculateRemainingPercentage(pucDaysLeft));

        binding.PUCProgressGraph.setProgress(calculateRemainingPercentage(pucDaysLeft));

        if (pucDaysLeft >= 90
//                && pucDaysLeft > 150
        ) {
            binding.PUCProgressGraph.setProgressColor(
                    ContextCompat.getColor(requireContext(), R.color.graphColor)
            );

        } else if (pucDaysLeft >= 30) {
            binding.PUCProgressGraph.setProgressColor(
                    ContextCompat.getColor(requireContext(), R.color.graphColor1)
            );
        } else if (pucDaysLeft >= 10) {
            binding.PUCProgressGraph.setProgressColor(
                    ContextCompat.getColor(requireContext(), R.color.graphColor2)
            );
        } else {
            binding.PUCProgressGraph.setProgressColor(
                    ContextCompat.getColor(requireContext(), R.color.graphColor3)
            );
        }

        binding.vehicleRegistered.setText(TimeUtils.convertDateFormat(model.getRegistration_date(), "dd MMM yyyy"));
        binding.fitnessUpto.setText(TimeUtils.convertDateFormat(model.getFitness_upto(), "dd MMM yyyy"));

        if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), TimeUtils.convertDateFormat(model.getFitness_upto(), "dd MMM yyyy"))) {
            binding.fitnessStatus.setText("Expired");
            binding.fitnessStatusIndicator.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_red));

            List<Float> values = new ArrayList<>();
            values.add(100f); // expired
//                                values.add(60f); // valid
//                                values.add(15f); // pending
//                                values.add(5f);  // something else

            List<Integer> colors = new ArrayList<>();
            colors.add(0xFFEF3B3B); // red
//                                colors.add(0xFF2FB132); // green
//                                colors.add(0xFFEEDC82); // yellow
//                                colors.add(0xFF9AE6FF); // light blue

            binding.donutChart.setData(values, colors);
            binding.donutChart.setStrokeWidth(50f); // optional thickness
        } else {
            binding.fitnessStatus.setText("Valid");
            binding.fitnessStatusIndicator.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPrimary));

            DateRatioUtil.TimeRatio ratio =
                    DateRatioUtil.calculateTimeRatio(TimeUtils.convertDateFormat(model.getRegistration_date(), "dd MMM yyyy"),
                            TimeUtils.convertDateFormat(model.getFitness_upto(), "dd MMM yyyy"));

            int passed = ratio.passed;       // e.g. 40
            int remaining = ratio.remaining; // e.g. 60

            List<Float> values = new ArrayList<>();
            values.add((float) passed); // expired
            values.add((float) remaining); // valid

            List<Integer> colors = new ArrayList<>();
            colors.add(0xFFFF6B6B); // red
            colors.add(0xFF2FB132); // green

            binding.donutChart.setData(values, colors);
            binding.donutChart.setStrokeWidth(50f); // optional thickness
        }

        if (model.getPermitNumber() != null && !model.getPermitNumber().isEmpty() && !model.getPermitNumber().equalsIgnoreCase("null")
                && !model.getPermitNumber().equalsIgnoreCase("NA") && !model.getPermitNumber().equalsIgnoreCase("N/A")) {
            binding.permitCard.setVisibility(View.VISIBLE);
            binding.permitNumber.setText(model.getPermitNumber());
            binding.permitType.setText(model.getPermitType());
            binding.permitValidUpto.setText(TimeUtils.convertDateFormat(model.getPermitValidUpto(), "dd MMM yyyy"));

            String currentDate =
                    CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy");

// Convert API date FIRST
            String permitValidUptoFormatted =
                    TimeUtils.convertDateFormat(
                            model.getPermitValidUpto(),
                            "dd MMM yyyy"
                    );

            CommonLogic.showTestLog(TAG, "Current Date (normalized): " + currentDate);
            CommonLogic.showTestLog(TAG, "Permit Valid Upto (normalized): " + permitValidUptoFormatted);

            binding.permitValidUpto.setText(permitValidUptoFormatted);

// Now calculations are SAFE
            int permitDaysLeft = (int) TimeUtils.getDaysDifference(
                    currentDate,
                    permitValidUptoFormatted
            );

            CommonLogic.showTestLog(TAG, "Days Difference (after normalization): " + permitDaysLeft);

            boolean isExpired =
                    TimeUtils.isDateExpired(currentDate, permitValidUptoFormatted);

            CommonLogic.showTestLog(TAG, "Is Permit Expired (after normalization): " + isExpired);

            if (isExpired) {
                permitDaysLeft = permitDaysLeft != 0 ? permitDaysLeft * -1 : 0;
            }

            if (permitDaysLeft >= 0) {
                binding.permitLeftDays.setText(permitDaysLeft + " \nDays Left");
            } else {
                binding.permitLeftDays.setText((permitDaysLeft * -1) + " \nDays Ago");
            }

            CommonLogic.showTestLog(TAG, "permitProgressBar calculated percentage: " + calculateRemainingPercentage(permitDaysLeft));

            binding.permitProgressBar.setProgress(calculateRemainingPercentage(permitDaysLeft));

            if (permitDaysLeft >= 90
//                && permitDaysLeft > 150
            ) {
                binding.permitProgressBar.setProgressColor(
                        ContextCompat.getColor(requireContext(), R.color.graphColor)
                );

            } else if (permitDaysLeft >= 30) {
                binding.permitProgressBar.setProgressColor(
                        ContextCompat.getColor(requireContext(), R.color.graphColor1)
                );
            } else if (permitDaysLeft >= 10) {
                binding.permitProgressBar.setProgressColor(
                        ContextCompat.getColor(requireContext(), R.color.graphColor2)
                );
            } else {
                binding.permitProgressBar.setProgressColor(
                        ContextCompat.getColor(requireContext(), R.color.graphColor3)
                );
            }

        } else {
            binding.permitCard.setVisibility(View.GONE);
        }

        if (model.getNationalPermitNumber() != null && !model.getNationalPermitNumber().isEmpty() && !model.getNationalPermitNumber().equalsIgnoreCase("null")
                && !model.getNationalPermitNumber().equalsIgnoreCase("NA") && !model.getNationalPermitNumber().equalsIgnoreCase("N/A")) {
            binding.nationalPermitCard.setVisibility(View.VISIBLE);
            binding.nationalPermitNumber.setText(model.getNationalPermitNumber());
            binding.nationalPermitIssuedBy.setText(model.getNationalPermitIssuedBy());
            binding.nationalPermitValidUpto.setText(TimeUtils.convertDateFormat(model.getNationalPermitValidUpto(), "dd MMM yyyy"));

            int nationalPermitDaysLeft = (int) TimeUtils.getDaysDifference(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), model.getNationalPermitValidUpto());

            if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), model.getNationalPermitValidUpto())) {
                nationalPermitDaysLeft = nationalPermitDaysLeft != 0 ? nationalPermitDaysLeft * -1 : 0;
            }
            if (nationalPermitDaysLeft >= 0) {
                binding.nationalPermitLeftDays.setText(nationalPermitDaysLeft + " \nDays Left");
            } else {
                binding.nationalPermitLeftDays.setText((nationalPermitDaysLeft * -1) + " \nDays Ago");
            }

            CommonLogic.showTestLog(TAG, "nationalPermitProgressBar calculated percentage: " + calculateRemainingPercentage(nationalPermitDaysLeft));

            binding.nationalPermitProgressBar.setProgress(calculateRemainingPercentage(nationalPermitDaysLeft));

            if (nationalPermitDaysLeft >= 90
//                && nationalPermitDaysLeft > 150
            ) {
                binding.nationalPermitProgressBar.setProgressColor(
                        ContextCompat.getColor(requireContext(), R.color.graphColor)
                );

            } else if (nationalPermitDaysLeft >= 30) {
                binding.nationalPermitProgressBar.setProgressColor(
                        ContextCompat.getColor(requireContext(), R.color.graphColor1)
                );
            } else if (nationalPermitDaysLeft >= 10) {
                binding.nationalPermitProgressBar.setProgressColor(
                        ContextCompat.getColor(requireContext(), R.color.graphColor2)
                );
            } else {
                binding.nationalPermitProgressBar.setProgressColor(
                        ContextCompat.getColor(requireContext(), R.color.graphColor3)
                );
            }

        } else {
            binding.nationalPermitCard.setVisibility(View.GONE);
        }
    }


    public int calculateRemainingPercentage(int daysPassed) {

        final String TAG = "RemainingPercentage";
        final int DAYS_IN_MONTH = 30; // constant reference for each month (30 days assumed)

        CommonLogic.showTestLog(TAG, "➡️ Method called with daysPassed = " + daysPassed);

        // 🔹 Sanity check — ensure daysPassed is not negative
        if (daysPassed < 0) {
            CommonLogic.showTestLog(TAG, "⚠️ daysPassed was negative. Resetting to 0");
            daysPassed = 0;
        }

        float percentage = 0f;

        // 🔸 Zone 1 → Beyond 10 months (> 300 days)
        if (daysPassed > 300) {
            percentage = 1f;
            CommonLogic.showTestLog(TAG, "🟥 Zone: >300 days | Percentage set to " + percentage);

            // 🔸 Zone 2 → Between 5–10 months (150–300 days)
        } else if (daysPassed >= 150 && daysPassed <= 300) {
            percentage = ((300f - daysPassed) / (300f - 150f)) * 100f;
            CommonLogic.showTestLog(TAG, "🟧 Zone: 150–300 days | Calculated percentage = " + percentage);

            // 🔸 Zone 3 → Between 3–5 months (90–150 days)
        } else if (daysPassed >= 90 && daysPassed < 150) {
            percentage = ((150f - daysPassed) / (150f - 90f)) * 100f;
            CommonLogic.showTestLog(TAG, "🟨 Zone: 90–150 days | Calculated percentage = " + percentage);

            // 🔸 Zone 4 → Between 1–3 months (30–90 days)
        } else if (daysPassed >= 30 && daysPassed < 90) {
            percentage = ((90f - daysPassed) / (90f - 30f)) * 100f;
            CommonLogic.showTestLog(TAG, "🟩 Zone: 30–90 days | Calculated percentage = " + percentage);

            // 🔸 Zone 5 → Less than 1 month (0–30 days)
        } else if (daysPassed >= 0 && daysPassed < 30) {
            percentage = ((30f - daysPassed) / 30f) * 100f;
            CommonLogic.showTestLog(TAG, "🟦 Zone: 0–30 days | Calculated percentage = " + percentage);

            // 🔸 Not started yet (fallback)
        } else {
            percentage = 100f;
            CommonLogic.showTestLog(TAG, "⬜ Zone: Not started | Percentage set to 100");
        }

        // 🔹 Clamp result safely between 0% and 100%
        int finalPercentage = Math.max(0, Math.min(100, Math.round(percentage)));

        CommonLogic.showTestLog(TAG, "✅ Final clamped percentage = " + finalPercentage);
        CommonLogic.showTestLog(TAG, "⬅️ Method execution completed");

        return finalPercentage;
    }


    private void getFuelPrice() {

        // --- Step 2: Make API call ---

        fuelPriceCall = ApiClient.getApiService(getContext()).commonGETMethodToHitAllAPIs(APIData.GET_FUEL_PRICE);

        fuelPriceCall.enqueue(new Callback<JsonObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (!isSafeToUpdateUI()) return;

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);


                    boolean status = responseBody.has("success") && responseBody.getBoolean("success");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";


                    if (status) {
                        try {

                            if (!isSafeToUpdateUI()) return;

                            fuelItemDataList.clear();
                            stateList.clear();
                            fuelItemDataList.addAll(APIHelper.convertJsonArrayToList(responseBody.getJSONObject("data").getJSONArray("states"), FuelItemModel.class));
                            for (FuelItemModel model : fuelItemDataList) {
                                String stateName = model.getState().replace("_", " "); // replace underscore with space
                                stateList.add(stateName);
                            }


                            // List of states
                            ArrayAdapter<String> stateListAdapter =
                                    new ArrayAdapter<String>(
                                            requireContext(),
                                            R.layout.spinner_selected_item,
                                            stateList
                                    ) {
                                        @Override
                                        public View getView(int position, View convertView, ViewGroup parent) {
                                            View view = super.getView(position, convertView, parent);
                                            TextView tv = (TextView) view;
                                            tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                                            return view;
                                        }
                                    };

                            stateListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinnerState.setAdapter(stateListAdapter);

                            getCurrentLocation();


// Handle selection
                            binding.spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (view instanceof TextView) {
                                        ((TextView) view).setTextColor(
                                                ContextCompat.getColor(requireContext(), android.R.color.black)
                                        );
                                    }


                                    String selectedState = stateList.get(position);
//                                    Toast.makeText(getContext(), "Selected: " + selectedState, Toast.LENGTH_SHORT).show();

                                    if (!fuelItemDataList.get(position).getPetrol().equalsIgnoreCase("NA") &&
                                            !fuelItemDataList.get(position).getPetrol().equalsIgnoreCase("N/A") &&
                                            !fuelItemDataList.get(position).getPetrol().equalsIgnoreCase("0") &&
                                            !fuelItemDataList.get(position).getPetrol().isEmpty()) {
                                        binding.tvPetrolPrice.setText("Petrol:\n" + getString(R.string.currency_sign) + fuelItemDataList.get(position).getPetrol());
                                        binding.tvPetrolPrice.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.tvPetrolPrice.setVisibility(View.GONE);
                                    }

                                    if (!fuelItemDataList.get(position).getDiesel().equalsIgnoreCase("NA") &&
                                            !fuelItemDataList.get(position).getDiesel().equalsIgnoreCase("N/A") &&
                                            !fuelItemDataList.get(position).getDiesel().equalsIgnoreCase("0") &&
                                            !fuelItemDataList.get(position).getDiesel().isEmpty()) {
                                        binding.tvDieselPrice.setText("Diesel:\n" + getString(R.string.currency_sign) + fuelItemDataList.get(position).getDiesel());
                                        binding.tvDieselPrice.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.tvDieselPrice.setVisibility(View.GONE);
                                    }

                                    if (!fuelItemDataList.get(position).getCng().equalsIgnoreCase("NA") &&
                                            !fuelItemDataList.get(position).getCng().equalsIgnoreCase("N/A") &&
                                            !fuelItemDataList.get(position).getCng().equalsIgnoreCase("0") &&
                                            !fuelItemDataList.get(position).getCng().isEmpty()) {
                                        binding.tvCngPrice.setText("CNG:\n" + getString(R.string.currency_sign) + fuelItemDataList.get(position).getCng());
                                        binding.tvCngPrice.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.tvCngPrice.setVisibility(View.GONE);
                                    }


                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });

                            if (!stateList.isEmpty()) {
                                binding.fuelLayout.setVisibility(View.VISIBLE);
                            } else {
                                binding.fuelLayout.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    private int getStateIndex(List<String> stateList, String userState) {

        CommonLogic.showTestLog(TAG, "User State (raw): " + userState);

        if (userState == null || userState.trim().isEmpty()) {
            CommonLogic.showTestLog(TAG, "User state is null or empty → selecting index 0");
            return 0;
        }

        String normalizedUserState = userState.replace(" ", "").trim();

        for (int i = 0; i < stateList.size(); i++) {

            String listState = stateList.get(i);
            String normalizedListState = listState.replace(" ", "").trim();

            CommonLogic.showTestLog(
                    TAG,
                    "Comparing → API State: " + normalizedListState
                            + " | User State: " + normalizedUserState
            );

            if (normalizedListState.equalsIgnoreCase(normalizedUserState)) {
                CommonLogic.showTestLog(TAG, "✅ Match found at index: " + i);
                return i;
            }
        }

        CommonLogic.showTestLog(TAG, "❌ No matching state found → selecting index 0");
        return 0; // fallback
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getTrendingCars() {

        CommonLogic.showTestLog(TAG, "getTrendingCars call");

        trendingCarsItemAdapter.notifyDataSetChanged();
        trendingCards.clear();

        // --- Step 2: Make API call ---

        trendingCarsCall = ApiCall.callApi(TAG, getActivity(), APIData.GET_TRENDING_CARS, null, "get", new ApiCall.ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                if (!isSafeToUpdateUI()) return;

                try {
                    if (status) {
                        JSONArray carsArray = responseBody.getJSONArray("data");

                        for (int i = 0; i < carsArray.length(); i++) {
                            JSONObject carObj = carsArray.getJSONObject(i);
                            JSONObject carDetailsObj = carsArray.getJSONObject(i).getJSONObject("car_details");
                            TrendingCarsModel car = new TrendingCarsModel();

                            // Basic info
                            car.id = carObj.optString("_id");
                            car.brandName = carObj.optString("brand_name");
                            car.modelName = carObj.optString("model_name");
                            car.createdAt = carObj.optString("createdAt");

                            car.type = carDetailsObj.optString("type");
                            car.price = carDetailsObj.optDouble("price");
                            car.priceDisplay = carDetailsObj.optString("price_display");
                            car.mileage = carDetailsObj.optString("mileage");
                            car.topSpeed = carDetailsObj.optString("top_speed");
                            car.imageUrl = carDetailsObj.optString("image_url");

                            // Specifications
                            JSONObject specsObj = carDetailsObj.optJSONObject("specifications");
                            if (specsObj != null) {
                                TrendingCarsModel.Specifications specs = new TrendingCarsModel.Specifications();
                                specs.engine_capacity = specsObj.optString("engine_capacity");
                                specs.transmission = specsObj.optString("transmission");
                                specs.fuel_tank_capacity = specsObj.optString("fuel_tank_capacity");
                                specs.seat_height = specsObj.optString("seat_height");
                                specs.kerb_weight = specsObj.optString("kerb_weight");
                                car.specifications = specs;
                            }

                            // Detailed specifications
                            JSONObject detailsObj = carDetailsObj.optJSONObject("detailed_specifications");
                            if (detailsObj != null) {
                                TrendingCarsModel.DetailedSpecifications details = new TrendingCarsModel.DetailedSpecifications();
                                details.max_power = detailsObj.optString("max_power");
                                details.max_torque = detailsObj.optString("max_torque");
                                details.riding_mode = detailsObj.optString("riding_mode");
                                details.gear_shifting_pattern = detailsObj.optString("gear_shifting_pattern");
                                car.detailedSpecifications = details;
                            }

                            // Dimensions
                            JSONObject dimObj = carDetailsObj.optJSONObject("dimensions");
                            if (dimObj != null) {
                                TrendingCarsModel.Dimensions dim = new TrendingCarsModel.Dimensions();
                                dim.bootspace = dimObj.optString("bootspace");
                                dim.ground_clearance = dimObj.optString("ground_clearance");
                                dim.length = dimObj.optString("length");
                                dim.width = dimObj.optString("width");
                                dim.height = dimObj.optString("height");
                                car.dimensions = dim;
                            }

                            // Features
                            JSONObject featuresObj = carDetailsObj.optJSONObject("features");
                            if (featuresObj != null) {
                                TrendingCarsModel.Features features = new TrendingCarsModel.Features();
                                features.air_conditioner = featuresObj.optBoolean("air_conditioner");
                                features.central_locking = featuresObj.optString("central_locking");
                                features.power_windows = featuresObj.optString("power_windows");
                                features.headrest = featuresObj.optString("headrest");
                                features.parking_assist = featuresObj.optString("parking_assist");
                                features.cruise_control = featuresObj.optBoolean("cruise_control");
                                features.music_system_count = featuresObj.optInt("music_system_count");
                                features.apple_carplay = featuresObj.optString("apple_carplay");
                                features.android_auto = featuresObj.optString("android_auto");
                                features.abs = featuresObj.optBoolean("abs");
                                features.sunroof = featuresObj.optBoolean("sunroof");
                                features.third_row_ac = featuresObj.optBoolean("third_row_ac");

                                JSONArray airbagsArray = featuresObj.optJSONArray("airbags");
                                if (airbagsArray != null) {
                                    List<String> airbagsList = new ArrayList<>();
                                    for (int j = 0; j < airbagsArray.length(); j++) {
                                        airbagsList.add(airbagsArray.getString(j));
                                    }
                                    features.airbags = airbagsList;
                                }

                                car.features = features;
                            }

                            // ✅ Add to list
                            trendingCards.add(car);
                        }

                        CommonLogic.showTestLog(TAG, "Total Cars: " + trendingCards.size());

                        trendingCarsItemAdapter.notifyDataSetChanged();

                        if (!trendingCards.isEmpty()) {
                            binding.trendingCarsLayout.setVisibility(View.VISIBLE);
                        } else {
                            binding.trendingCarsLayout.setVisibility(View.GONE);
                        }
                    } else {
                        binding.trendingCarsLayout.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    binding.trendingCarsLayout.setVisibility(View.GONE);
                    CommonLogic.showTestLog(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isSafeToUpdateUI()) return;
                JsonObject errorObj = new JsonObject();
                errorObj.addProperty("status", false);
                errorObj.addProperty("message", errorMessage);
                binding.trendingCarsLayout.setVisibility(View.GONE);
            }
        });
    }

    private void getCompareVehicleSet() {

        // --- Step 2: Make API call ---
        CommonLogic.showTestLog(TAG, "Calling Compare Vehicle API...");

        compareVehiclesCall = ApiCall.callApi(TAG, getActivity(), APIData.GET_COMPARE_VEHICLE_DATA_SET, null, "get", new ApiCall.ApiResponseCallback() {

            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {

                CommonLogic.showTestLog(TAG, "API Success Status: " + status);
                CommonLogic.showTestLog(TAG, "API Message: " + message);
                CommonLogic.showTestLog(TAG, "Full Response: " + responseBody);

                try {

                    if (status) {

                        trendingVSCards.clear();
                        CommonLogic.showTestLog(TAG, "Cleared old list");

                        JSONArray carsArray = responseBody.getJSONArray("data");
                        CommonLogic.showTestLog(TAG, "Total Data Array Size: " + carsArray.length());

                        for (int i = 0; i < carsArray.length(); i++) {

                            JSONObject compareDataObj = carsArray.getJSONObject(i);
                            CommonLogic.showTestLog(TAG, "Processing Compare Index: " + i);
                            CommonLogic.showTestLog(TAG, "Compare Object: " + compareDataObj);

                            TrendingVSCarsModel trendingVSCarsModel = new TrendingVSCarsModel();

                            // car 1 data
                            JSONObject car1DataObj = compareDataObj
                                    .getJSONObject("car_1");

                            CommonLogic.showTestLog(TAG, "Car 1 Data: " + car1DataObj);

                            TrendingCarsModel car1Data = new TrendingCarsModel();
                            car1Data.car_id = car1DataObj.optString("_id");
                            car1Data.brandName = car1DataObj.optString("brand_name");
                            car1Data.modelName = car1DataObj.optString("model_name");
                            car1Data.priceDisplay = car1DataObj.getJSONObject("car_details").optString("price_display");
                            car1Data.imageUrl = car1DataObj.getJSONObject("car_details").optString("image_url");

                            // car 2 data
                            JSONObject car2DataObj = compareDataObj
                                    .getJSONObject("car_2");

                            CommonLogic.showTestLog(TAG, "Car 2 Data: " + car2DataObj);

                            TrendingCarsModel car2Data = new TrendingCarsModel();
                            car2Data.car_id = car2DataObj.optString("_id");
                            car2Data.brandName = car2DataObj.optString("brand_name");
                            car2Data.modelName = car2DataObj.optString("model_name");
                            car2Data.priceDisplay = car2DataObj.getJSONObject("car_details").optString("price_display");
                            car2Data.imageUrl = car2DataObj.getJSONObject("car_details").optString("image_url");

                            trendingVSCarsModel.comparisonId = compareDataObj.optString("_id");
                            trendingVSCarsModel.car1Data = car1Data;
                            trendingVSCarsModel.car2Data = car2Data;
                            trendingVSCarsModel.createdAt = compareDataObj.optString("createdAt");

                            trendingVSCards.add(trendingVSCarsModel);

                            CommonLogic.showTestLog(TAG,
                                    "Added Comparison ID: " + trendingVSCarsModel.comparisonId);
                        }

                        CommonLogic.showTestLog(TAG,
                                "Final Compare List Size: " + trendingVSCards.size());

                        trendingVSCarsItemAdapter.notifyDataSetChanged();

                        if (!trendingVSCards.isEmpty()) {
                            CommonLogic.showTestLog(TAG, "Showing Comparison Layout");
                            binding.comparisonCarsLayout.setVisibility(View.VISIBLE);
                        } else {
                            CommonLogic.showTestLog(TAG, "Hiding Comparison Layout");
                            binding.comparisonCarsLayout.setVisibility(View.GONE);
                        }

                    } else {
                        CommonLogic.showTestLog(TAG, "API Status False");
                    }

                } catch (JSONException e) {
                    CommonLogic.showTestLog(TAG,
                            "Compare Cars JSON Error: " + e.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                CommonLogic.showTestLog(TAG,
                        "Compare Cars API Error: " + errorMessage);
            }
        });


                /*ApiClient.getApiService(getContext()).getCompareVehicleDataSet(jsonObjectCompareVehicle).enqueue(new Callback<JsonObject>() {
                    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                            CommonLogic.showTestLog(TAG, "getCompareVehicleDataSet response: " + responseBody);

                            boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                            String message = responseBody.has("message") ? responseBody.getString("message") : "Server error, Please try after some time.";


                            if (status) {
                                trendingCards.clear();
                                JSONArray carsArray = responseBody.getJSONObject("data").getJSONArray("comparisons");

                                for (int i = 0; i < carsArray.length(); i++) {
                                    JSONObject compareDataObj = carsArray.getJSONObject(i);
                                    TrendingVSCarsModel trendingVSCarsModel = new TrendingVSCarsModel();

                                    // car 1 data
                                    JSONObject car1DataObj = carsArray.getJSONObject(i).getJSONObject("car_1_data");
                                    TrendingCarsModel car1Data = new TrendingCarsModel();

                                    // Basic info
                                    car1Data.id = car1DataObj.optString("_id");
                                    car1Data.brandName = car1DataObj.optString("brand_name");
                                    car1Data.modelName = car1DataObj.optString("model_name");
                                    car1Data.type = car1DataObj.optString("type");
                                    car1Data.price = car1DataObj.optDouble("price");
                                    car1Data.priceDisplay = car1DataObj.optString("price_display");
                                    car1Data.mileage = car1DataObj.optString("mileage");
                                    car1Data.topSpeed = car1DataObj.optString("top_speed");
                                    car1Data.imageUrl = car1DataObj.optString("image_url");

                                    // Specifications
                                    JSONObject specsObj = car1DataObj.optJSONObject("specifications");
                                    if (specsObj != null) {
                                        TrendingCarsModel.Specifications specs = new TrendingCarsModel.Specifications();
                                        specs.engine_capacity = specsObj.optString("engine_capacity");
                                        specs.transmission = specsObj.optString("transmission");
                                        specs.fuel_tank_capacity = specsObj.optString("fuel_tank_capacity");
                                        specs.seat_height = specsObj.optString("seat_height");
                                        specs.kerb_weight = specsObj.optString("kerb_weight");
                                        car1Data.specifications = specs;
                                    }

                                    // Detailed specifications
                                    JSONObject detailsObj = car1DataObj.optJSONObject("detailed_specifications");
                                    if (detailsObj != null) {
                                        TrendingCarsModel.DetailedSpecifications details = new TrendingCarsModel.DetailedSpecifications();
                                        details.max_power = detailsObj.optString("max_power");
                                        details.max_torque = detailsObj.optString("max_torque");
                                        details.riding_mode = detailsObj.optString("riding_mode");
                                        details.gear_shifting_pattern = detailsObj.optString("gear_shifting_pattern");
                                        car1Data.detailedSpecifications = details;
                                    }

                                    // Dimensions
                                    JSONObject dimObj = car1DataObj.optJSONObject("dimensions");
                                    if (dimObj != null) {
                                        TrendingCarsModel.Dimensions dim = new TrendingCarsModel.Dimensions();
                                        dim.bootspace = dimObj.optString("bootspace");
                                        dim.ground_clearance = dimObj.optString("ground_clearance");
                                        dim.length = dimObj.optString("length");
                                        dim.width = dimObj.optString("width");
                                        dim.height = dimObj.optString("height");
                                        car1Data.dimensions = dim;
                                    }

                                    // Features
                                    JSONObject featuresObj = car1DataObj.optJSONObject("features");
                                    if (featuresObj != null) {
                                        TrendingCarsModel.Features features = new TrendingCarsModel.Features();
                                        features.air_conditioner = featuresObj.optBoolean("air_conditioner");
                                        features.central_locking = featuresObj.optString("central_locking");
                                        features.power_windows = featuresObj.optString("power_windows");
                                        features.headrest = featuresObj.optString("headrest");
                                        features.parking_assist = featuresObj.optString("parking_assist");
                                        features.cruise_control = featuresObj.optBoolean("cruise_control");
                                        features.music_system_count = featuresObj.optInt("music_system_count");
                                        features.apple_carplay = featuresObj.optString("apple_carplay");
                                        features.android_auto = featuresObj.optString("android_auto");
                                        features.abs = featuresObj.optBoolean("abs");
                                        features.sunroof = featuresObj.optBoolean("sunroof");
                                        features.third_row_ac = featuresObj.optBoolean("third_row_ac");

                                        JSONArray airbagsArray = featuresObj.optJSONArray("airbags");
                                        if (airbagsArray != null) {
                                            List<String> airbagsList = new ArrayList<>();
                                            for (int j = 0; j < airbagsArray.length(); j++) {
                                                airbagsList.add(airbagsArray.getString(j));
                                            }
                                            features.airbags = airbagsList;
                                        }

                                        car1Data.features = features;
                                    }

                                    // car 2 data
                                    JSONObject car2DataObj = carsArray.getJSONObject(i).getJSONObject("car_2_data");
                                    TrendingCarsModel car2Data = new TrendingCarsModel();

                                    // Basic info
                                    car2Data.id = car2DataObj.optString("_id");
                                    car2Data.brandName = car2DataObj.optString("brand_name");
                                    car2Data.modelName = car2DataObj.optString("model_name");
                                    car2Data.type = car2DataObj.optString("type");
                                    car2Data.price = car2DataObj.optDouble("price");
                                    car2Data.priceDisplay = car2DataObj.optString("price_display");
                                    car2Data.mileage = car2DataObj.optString("mileage");
                                    car2Data.topSpeed = car2DataObj.optString("top_speed");
                                    car2Data.imageUrl = car2DataObj.optString("image_url");

                                    // Specifications
                                    JSONObject specsObj2 = car2DataObj.optJSONObject("specifications");
                                    if (specsObj2 != null) {
                                        TrendingCarsModel.Specifications specs = new TrendingCarsModel.Specifications();
                                        specs.engine_capacity = specsObj2.optString("engine_capacity");
                                        specs.transmission = specsObj2.optString("transmission");
                                        specs.fuel_tank_capacity = specsObj2.optString("fuel_tank_capacity");
                                        specs.seat_height = specsObj2.optString("seat_height");
                                        specs.kerb_weight = specsObj2.optString("kerb_weight");
                                        car2Data.specifications = specs;
                                    }

                                    // Detailed specifications
                                    JSONObject detailsObj2 = car2DataObj.optJSONObject("detailed_specifications");
                                    if (detailsObj2 != null) {
                                        TrendingCarsModel.DetailedSpecifications details = new TrendingCarsModel.DetailedSpecifications();
                                        details.max_power = detailsObj2.optString("max_power");
                                        details.max_torque = detailsObj2.optString("max_torque");
                                        details.riding_mode = detailsObj2.optString("riding_mode");
                                        details.gear_shifting_pattern = detailsObj2.optString("gear_shifting_pattern");
                                        car2Data.detailedSpecifications = details;
                                    }

                                    // Dimensions
                                    JSONObject dimObj2 = car2DataObj.optJSONObject("dimensions");
                                    if (dimObj2 != null) {
                                        TrendingCarsModel.Dimensions dim = new TrendingCarsModel.Dimensions();
                                        dim.bootspace = dimObj2.optString("bootspace");
                                        dim.ground_clearance = dimObj2.optString("ground_clearance");
                                        dim.length = dimObj2.optString("length");
                                        dim.width = dimObj2.optString("width");
                                        dim.height = dimObj2.optString("height");
                                        car2Data.dimensions = dim;
                                    }

                                    // Features
                                    JSONObject featuresObj2 = car2DataObj.optJSONObject("features");
                                    if (featuresObj2 != null) {
                                        TrendingCarsModel.Features features = new TrendingCarsModel.Features();
                                        features.air_conditioner = featuresObj2.optBoolean("air_conditioner");
                                        features.central_locking = featuresObj2.optString("central_locking");
                                        features.power_windows = featuresObj2.optString("power_windows");
                                        features.headrest = featuresObj2.optString("headrest");
                                        features.parking_assist = featuresObj2.optString("parking_assist");
                                        features.cruise_control = featuresObj2.optBoolean("cruise_control");
                                        features.music_system_count = featuresObj2.optInt("music_system_count");
                                        features.apple_carplay = featuresObj2.optString("apple_carplay");
                                        features.android_auto = featuresObj2.optString("android_auto");
                                        features.abs = featuresObj2.optBoolean("abs");
                                        features.sunroof = featuresObj2.optBoolean("sunroof");
                                        features.third_row_ac = featuresObj2.optBoolean("third_row_ac");

                                        JSONArray airbagsArray = featuresObj2.optJSONArray("airbags");
                                        if (airbagsArray != null) {
                                            List<String> airbagsList = new ArrayList<>();
                                            for (int j = 0; j < airbagsArray.length(); j++) {
                                                airbagsList.add(airbagsArray.getString(j));
                                            }
                                            features.airbags = airbagsList;
                                        }

                                        car2Data.features = features;
                                    }

                                    trendingVSCarsModel.id = compareDataObj.optString("_id");
                                    trendingVSCarsModel.comparisonId = compareDataObj.optString("comparison_id");
                                    trendingVSCarsModel.car1Id = compareDataObj.optString("car_1_id");
                                    trendingVSCarsModel.car2Id = compareDataObj.optString("car_2_id");
                                    trendingVSCarsModel.car1Data = car1Data;
                                    trendingVSCarsModel.car2Data = car2Data;

                                    // ✅ Add to list
                                    trendingVSCards.add(trendingVSCarsModel);
                                }

                                trendingVSCarsItemAdapter.notifyDataSetChanged();

                                CommonLogic.showTestLog(TAG, "Total Cars: " + trendingCards.size());

                                if (!trendingVSCards.isEmpty()) {
                                    binding.comparisonCarsLayout.setVisibility(View.VISIBLE);
                                } else {
                                    binding.comparisonCarsLayout.setVisibility(View.GONE);
                                }
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
                });*/
    }

    private void getTipsTricks() {

        // --- Step 2: Make API call ---

        tipsTricksCall = ApiCall.callApi(TAG, getActivity(), APIData.GET_TIPS_TRICKS, null, "get", new ApiCall.ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                if (!isSafeToUpdateUI()) return;

                if (status) {
                    try {
                        tipsItemList.clear();
                        tipsItemList.addAll(APIHelper.convertJsonArrayToList(responseBody.getJSONArray("data"), TipsItemModel.class));

                        tipsItemAdapter.notifyDataSetChanged();

                        if (!tipsItemList.isEmpty()) {
                            binding.tipsLayout.setVisibility(View.VISIBLE);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
                            CommonMethods.setInfiniteAutoScroll(
                                    getContext(),
                                    binding.rvTips,
                                    layoutManager,
                                    tipsItemList.size(),
                                    3000, // auto scroll every 3 seconds
                                    tipsItemHandler,
                                    tipsItemUpdate, null

                            );
                        } else {
                            binding.tipsLayout.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        binding.tipsLayout.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                } else {
                    binding.tipsLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isSafeToUpdateUI()) return;
                binding.tipsLayout.setVisibility(View.GONE);
            }
        });
    }

    private void getNews() {
        // --- Step 2: Make API call ---
        newsCall = ApiCall.callApi(TAG, getActivity(), APIData.GET_NEW_LIST, null, "get", new ApiCall.ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                if (!isSafeToUpdateUI()) return;
                if (status) {
                    try {
                        newsStoriesItemList.clear();
                        newsStoriesItemList.addAll(APIHelper.convertJsonArrayToList(responseBody.getJSONArray("data"), NewsStoriesItemModel.class));

                        newsStoriesItemAdapter.notifyDataSetChanged();

                        if (!newsStoriesItemList.isEmpty()) {
                            binding.newsLayout.setVisibility(View.VISIBLE);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
                            CommonMethods.setInfiniteAutoScroll(
                                    getContext(),
                                    binding.rvNewsStories,
                                    layoutManager,
                                    newsStoriesItemList.size(),
                                    3000, // auto scroll every 3 seconds
                                    newsStoriesItemHandler,
                                    newsStoriesItemUpdate, null

                            );

                        } else {
                            binding.newsLayout.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        binding.newsLayout.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                } else {
                    binding.newsLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isSafeToUpdateUI()) return;
                binding.newsLayout.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // ✅ Stop auto scroll handler
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }

        // ✅ Remove RecyclerView ScrollListener
        if (binding != null && garageScrollListener != null) {
            binding.carRecyclerView.removeOnScrollListener(garageScrollListener);
        }

        // ✅ Cancel API calls safely
        if (garageCall != null && !garageCall.isCanceled()) {
            garageCall.cancel();
        }

        if (fuelPriceCall != null && !fuelPriceCall.isCanceled()) {
            fuelPriceCall.cancel();
        }

        if (trendingCarsCall != null && !trendingCarsCall.isCanceled()) {
            trendingCarsCall.cancel();
        }

        if (compareVehiclesCall != null && !compareVehiclesCall.isCanceled()) {
            compareVehiclesCall.cancel();
        }

        if (tipsTricksCall != null && !tipsTricksCall.isCanceled()) {
            tipsTricksCall.cancel();
        }

        if (newsCall != null && !newsCall.isCanceled()) {
            newsCall.cancel();
        }

        // ✅ Finally clear binding
        binding = null;
    }


    private boolean isSafeToUpdateUI() {
        return isAdded() && binding != null;
    }


}