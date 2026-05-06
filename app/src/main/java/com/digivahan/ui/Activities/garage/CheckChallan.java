package com.digivahan.ui.Activities.garage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.adapters.CheckChallanItemAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.AppDatabase;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.local.VehicleChallanEntity;
import com.digivahan.data.model.ChallanModel;
import com.digivahan.data.model.OffenceModel;
import com.digivahan.databinding.ActivityCheckChallanBinding;
import com.digivahan.databinding.BottomsheetRefreshTimerBinding;
import com.digivahan.databinding.ProfileIncompleteDialogDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.AppExecutors;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckChallan extends BaseActivity {
    private final String TAG = "CheckChallanData";
    private ActivityCheckChallanBinding binding;

    private String vehicleNumber = "";
    private ArrayList<ChallanModel> filteredChallanList = new ArrayList<>(), allChallanList = new ArrayList<>();

    CheckChallanItemAdapter challanItemAdapter;

    PreferencesManager preferencesManager;

    AshDialog loadingDialog;

    private static final long REFRESH_INTERVAL =
            24 * 60 * 60 * 1000L; // 24 hours

    BottomSheetDialog bottomSheetChallanRefreshDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckChallanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setImageResource(R.drawable.refresh_icon);
        binding.toolbarLayout.ivBell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkVehicleChallanOncePerDay(vehicleNumber);
            }
        });
        binding.toolbarLayout.tvTitle.setText("Challan Info");


        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            back();
        });

        getOnBackPressedDispatcher().addCallback(CheckChallan.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        loadingDialog = new AshDialog(CheckChallan.this, "Please wait", "");
        preferencesManager = new PreferencesManager(CheckChallan.this);

        bottomSheetChallanRefreshDialog = new BottomSheetDialog(this);

        CommonLogic.showTestLog(TAG, "🟢 onCreate() started");

        challanItemAdapter = new CheckChallanItemAdapter(CheckChallan.this, filteredChallanList);
        binding.rvChallanList.setAdapter(challanItemAdapter);

        if (getIntent().hasExtra("vehicleNumber")) {
            vehicleNumber = getIntent().getStringExtra("vehicleNumber");
            CommonLogic.showTestLog(TAG, "📥 Received vehicle number from intent: " + vehicleNumber);

            if (vehicleNumber != null && !vehicleNumber.isEmpty()) {
                CommonLogic.showTestLog(TAG, "🚀 Calling checkVehicleChallan() for: " + vehicleNumber);
                if (getIntent().hasExtra("serviceType") && Objects.requireNonNull(getIntent().getStringExtra("serviceType")).equalsIgnoreCase("check")) {
                    checkVehicleChallan(vehicleNumber);
                } else {
                    loadChallanFromRoom(vehicleNumber);

                }
//                generateTestChallanData();
            } else {
                CommonLogic.showTestLog(TAG, "⚠️ Vehicle number is empty or null!");
            }
        } else {
            CommonLogic.showTestLog(TAG, "⚠️ Intent missing 'vehicleNumber' extra!");
        }

        // 🔹 Add your tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Pending"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Paid"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"));

// 🔹 Set listener for tab clicks
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabText = tab.getText().toString();
                filerChallan(tabText);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Optional: handle if needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                /*String tabText = tab.getText().toString();
                Toast.makeText(CheckChallan.this, "Re-clicked: " + tabText, Toast.LENGTH_SHORT).show();*/
            }
        });
    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    private void checkVehicleChallanOncePerDay(String vehicleNumber) {

        AppExecutors.getInstance().diskIO().execute(() -> {

            VehicleChallanEntity entity = AppDatabase.getInstance(this)
                    .vehicleChallanDao()
                    .getByVehicleNumber(vehicleNumber);

            runOnUiThread(() -> {

                if (entity == null) {
                    // First time → hit API
                    CommonLogic.showTestLog(TAG, "📡 No local data, calling API");
                    checkVehicleChallan(vehicleNumber);
                } else {
                    // Has data → check server time
                    getServerTimeAndHandleRefresh(entity, vehicleNumber);
                }

            });
        });
    }

    private void getServerTimeAndHandleRefresh(VehicleChallanEntity entity,
                                               String vehicleNumber) {

        ApiClient.getApiService(this)
                .commonGETMethodToHitAllAPIs(APIData.GET_APP_INFO)
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {

                        JSONObject body = APIHelper.getResponseData(
                                TAG, response);

                        try {
                            if (!body.optBoolean("success")) {
                                // Fail-safe → allow refresh
                                checkVehicleChallan(vehicleNumber);
                                return;
                            }

                            String serverDate = body.getString("currentDate");
                            String serverTime = body.getString("currentTime");

                            long serverMillis = CommonLogic
                                    .parseServerDateTimeToMillis(serverDate, serverTime);

                            handleRefreshDecision(entity, vehicleNumber, serverMillis);

                        } catch (Exception e) {
                            // Fail-safe

                            String fallbackDate =
                                    CommonMethods.getCurrentDate(CheckChallan.this, "yyyy-MM-dd");

                            String fallbackTime =
                                    CommonMethods.getCurrentDate(CheckChallan.this, "HH:mm:ss");


                            long serverMillis = CommonLogic
                                    .parseServerDateTimeToMillis(fallbackDate, fallbackTime);

                            handleRefreshDecision(entity, vehicleNumber, serverMillis);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call,
                                          @NonNull Throwable t) {
                        // Fail-safe
                        String fallbackDate =
                                CommonMethods.getCurrentDate(CheckChallan.this, "yyyy-MM-dd");

                        String fallbackTime =
                                CommonMethods.getCurrentDate(CheckChallan.this, "HH:mm:ss");


                        long serverMillis = CommonLogic
                                .parseServerDateTimeToMillis(fallbackDate, fallbackTime);

                        handleRefreshDecision(entity, vehicleNumber, serverMillis);
                    }
                });
    }


    private void handleRefreshDecision(VehicleChallanEntity entity,
                                       String vehicleNumber,
                                       long serverMillis) {

        long nextAllowedTime =
                entity.lastHitServerMillis + REFRESH_INTERVAL;

        long remainingTime = nextAllowedTime - serverMillis;

        if (remainingTime > 0) {
            // ❌ Not allowed → show bottom sheet with timer
            showChallanRefreshBottomSheet(
                    remainingTime,
                    false
            );
        } else {
            // ✅ Allowed → call API and show success state
            checkVehicleChallan(vehicleNumber);
            showChallanRefreshBottomSheet(
                    0,
                    true
            );
        }
    }




    public static void showUpdateDataDialog(String TAG, Activity context, String date) {

        CommonLogic.showTestLog(TAG, "🔔 showUpdateDataDialog() CALLED");

        PreferencesManager manager = new PreferencesManager(context);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.CustomDialogTheme);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context)
                .inflate(R.layout.profile_incomplete_dialog_design, null);

        ProfileIncompleteDialogDesignBinding dialogBinding = ProfileIncompleteDialogDesignBinding.bind(view);


        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show(); // ⚠️ MUST call show() first

// ✅ Force width to MATCH_PARENT
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }


// 🔒 HARD BLOCK dismiss
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

// 🔒 Block BACK button
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                CommonLogic.showTestLog(TAG, "⛔ Back button blocked on dialog");
                return true;
            }
            return false;
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        }

        dialogBinding.vehicleNumberField.rootLayout.setVisibility(View.GONE);

        dialogBinding.tvTitle.setText("Data UP To Date");

        dialogBinding.tvSubTitle.setText("Your challan details are already up to date as of "+ date +".\n" +
                "Our system fetches data directly from the RTO database. If any new challan has been issued recently, it may take some time to reflect in official records.\n" +
                "Please try again after 24 hours if you believe any information is missing.");

        ImageView closeBtn = view.findViewById(R.id.closeBtn);
        CommonLogic.showTestLog(TAG, "✅ Dialog is cancelable");
        closeBtn.setVisibility(View.VISIBLE);
        closeBtn.setOnClickListener(v -> {
            CommonLogic.showTestLog(TAG, "❎ Dialog dismissed via close button");
            dialog.dismiss();
        });

        Button openProfileBtn = view.findViewById(R.id.openProfileBtn);
        openProfileBtn.setText("OK");
        openProfileBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.show();
    }


    /**
     * ✅ API call to fetch challan details by vehicle number
     */
    private void checkVehicleChallan(String vehicleNumber) {

        loadingDialog.show();
        CommonLogic.showTestLog(TAG, "📡 checkVehicleChallan() called with vehicleNumber: " + vehicleNumber);

        JsonObject jsonObjectChallan = new JsonObject();
        jsonObjectChallan.addProperty("rcNumber", vehicleNumber);

        ApiClient.getApiServiceWithoutBaseUrl(this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(
                        "https://core.kashidigitalapis.com/v1/challan-plus",
                        jsonObjectChallan
                )
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {

                        JSONObject APIResponse =
                                APIHelper.getResponseData(TAG, response);

                        if (response.code() == 500 || response.code() == 503){
                            CommonMethods.showMessageDialog(CheckChallan.this, "Under Maintenance", "RTO under maintenance, No challan Found, Please try after some time");
                            loadingDialog.dismiss();
                            return;
                        }

                        try {
                            JSONArray challanArray = null;

                            if (APIResponse.has("data")) {
                                challanArray = APIResponse.getJSONArray("data");
                            } else if (APIResponse.has("result")) {
                                challanArray = APIResponse.getJSONArray("result");
                            }

                            if (challanArray == null || challanArray.length() == 0) {
                                Toast.makeText(CheckChallan.this,
                                        "No challan data found.", Toast.LENGTH_SHORT).show();

                                // 🔥 IMPORTANT: Save empty challan list with date & time
                                AppExecutors.getInstance().diskIO().execute(() -> {
                                    saveChallanToRoom(vehicleNumber, new ArrayList<>());
                                });

                                loadingDialog.dismiss();
                                return;
                            }

                            // ✅ Parse JSON
                            Type listType = new TypeToken<List<ChallanModel>>() {
                            }.getType();
                            List<ChallanModel> parsedList =
                                    new Gson().fromJson(challanArray.toString(), listType);

                            // ✅ UI LIST UPDATE (MAIN THREAD)
                            filteredChallanList.clear();
                            allChallanList.clear(); // 🔥 IMPORTANT FIX

                            filteredChallanList.addAll(parsedList);
                            allChallanList.addAll(parsedList);

                            filerChallan("Pending");
                            challanItemAdapter.notifyDataSetChanged();

                            // ✅ SAVE TO ROOM (BACKGROUND THREAD)
                            AppExecutors.getInstance().diskIO().execute(() -> {
                                saveChallanToRoom(vehicleNumber, parsedList);
                            });

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG,
                                    "🔥 Exception while parsing response: " + e.getMessage());
                            Toast.makeText(CheckChallan.this,
                                    "Failed to parse response", Toast.LENGTH_SHORT).show();
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call,
                                          @NonNull Throwable throwable) {

                        Toast.makeText(CheckChallan.this,
                                "Network error: " + throwable.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        loadingDialog.dismiss();
                    }
                });
    }


    private void saveChallanToRoom(String vehicleNumber,
                                   List<ChallanModel> challanList) {

        ApiClient.getApiService(this)
                .commonGETMethodToHitAllAPIs(APIData.GET_APP_INFO)
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {

                        JSONObject body = APIHelper.getResponseData(
                                TAG, response);

                        try {
                            if (!body.optBoolean("success")) {
                                saveWithFallbackTime(vehicleNumber, challanList);
                                return;
                            }

                            String serverDate = body.getString("currentDate");
                            String serverTime = body.getString("currentTime");

                            long serverMillis = CommonLogic
                                    .parseServerDateTimeToMillis(serverDate, serverTime);

                            saveToRoom(vehicleNumber, challanList,
                                    serverDate, serverTime, serverMillis);

                        } catch (Exception e) {
                            saveWithFallbackTime(vehicleNumber, challanList);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call,
                                          @NonNull Throwable t) {
                        saveWithFallbackTime(vehicleNumber, challanList);
                    }
                });
    }


    private void saveToRoom(String vehicleNumber,
                            List<ChallanModel> challanList,
                            String serverDate,
                            String serverTime,
                            long serverMillis) {

        AppExecutors.getInstance().diskIO().execute(() -> {

            VehicleChallanEntity entity = new VehicleChallanEntity();
            entity.vehicleNumber = vehicleNumber;
            entity.challanJson = new Gson().toJson(challanList);

            entity.lastHitServerDate = serverDate;
            entity.lastHitServerTime = serverTime;
            entity.lastHitServerMillis = serverMillis;

            AppDatabase.getInstance(this)
                    .vehicleChallanDao()
                    .insertOrUpdate(entity);

            CommonLogic.showTestLog(TAG,
                    "💾 Challan saved using SERVER time");
        });
    }


    private void saveWithFallbackTime(String vehicleNumber,
                                      List<ChallanModel> challanList) {

        long fallbackMillis = System.currentTimeMillis();

        String fallbackDate =
                CommonMethods.getCurrentDate(this, "yyyy-MM-dd");

        String fallbackTime =
                CommonMethods.getCurrentDate(this, "HH:mm:ss");


        saveToRoom(vehicleNumber, challanList,
                fallbackDate, fallbackTime, fallbackMillis);

        CommonLogic.showTestLog(TAG,
                "⚠️ Server time failed, used device time");
    }



    private void loadChallanFromRoom(String vehicleNumber) {

        AppExecutors.getInstance().diskIO().execute(() -> {

            VehicleChallanEntity entity = AppDatabase.getInstance(this)
                    .vehicleChallanDao()
                    .getByVehicleNumber(vehicleNumber);

            if (entity == null) {
                runOnUiThread(() -> checkVehicleChallan(vehicleNumber));
                return;
            }

            Type listType = new TypeToken<List<ChallanModel>>() {
            }.getType();
            List<ChallanModel> list =
                    new Gson().fromJson(entity.challanJson, listType);

            // 🔁 Switch back to UI thread
            runOnUiThread(() -> {

                filteredChallanList.clear();
                allChallanList.clear();

                filteredChallanList.addAll(list);
                allChallanList.addAll(list);

                /*if (allChallanList.isEmpty()) {
                    checkVehicleChallan(vehicleNumber);
                }*/

                filerChallan("Pending");
                challanItemAdapter.notifyDataSetChanged();

                CommonLogic.showTestLog(
                        TAG,
                        "📦 Loaded challan from DB for " + vehicleNumber
                );
            });
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    public void filerChallan(String status) {
        filteredChallanList.clear();
        for (ChallanModel model : allChallanList) {
            if ((status.equalsIgnoreCase("Paid") && (model.getChallanStatus().equalsIgnoreCase("paid") || model.getChallanStatus().equalsIgnoreCase("cash") || model.getChallanStatus().equalsIgnoreCase("disposed")))
                    || (status.equalsIgnoreCase("Pending") && (model.getChallanStatus().equalsIgnoreCase("pending") || model.getChallanStatus().equalsIgnoreCase("unpaid") || model.getChallanStatus().equalsIgnoreCase("partially paid")))
                    || status.equalsIgnoreCase("All")) {
                filteredChallanList.add(model);
            }
        }
        challanItemAdapter.notifyDataSetChanged();

        if (filteredChallanList.isEmpty()) {
            binding.emptyLayout.setVisibility(View.VISIBLE);
            if (status.equalsIgnoreCase("Paid")) {
                binding.emptyImage.setImageResource(R.drawable.no_paid_challan);
            } else if (status.equalsIgnoreCase("Pending")) {
                binding.emptyImage.setImageResource(R.drawable.no_pendding_challan);
            } else {
                binding.emptyImage.setImageResource(R.drawable.no_challan_found);
            }

            binding.rvChallanList.setVisibility(View.GONE);
        } else {
            binding.emptyLayout.setVisibility(View.GONE);
            binding.rvChallanList.setVisibility(View.VISIBLE);
        }
    }

    private void generateTestChallanData() {
        filteredChallanList.clear();

        // 🔹 1st Challan - Pending
        ChallanModel challan1 = new ChallanModel();
        challan1.setAccusedName("Ramesh Kumar");
        challan1.setAccusedFatherName("Suresh Kumar");
        challan1.setRcNumber("DL8CAW6601");
        challan1.setChallanNumber("HR4323250920234759");
        challan1.setChallanId(1);
        challan1.setChallanDate("2025-09-20");
        challan1.setChallanStatus("Pending");
        challan1.setChallanAmount("500");
        challan1.setRcStateCode("HR");
        challan1.setChallanPaymentSource("NA");
        challan1.setRtoOfficeName("Gurugram RTO");
        challan1.setChallanPaymentDate("NA");
        challan1.setChallanPlace("Sec 29, Gurugram");

        ArrayList<OffenceModel> offences1 = new ArrayList<>();
        offences1.add(new OffenceModel("Drunken Driving", "u/s 185 MVA", "500"));
        offences1.add(new OffenceModel("Without Helmet", "u/s 129/177", "NA"));
        challan1.setOffences(offences1);

        filteredChallanList.add(challan1);

        // 🔹 2nd Challan - Paid
        ChallanModel challan2 = new ChallanModel();
        challan2.setAccusedName("Amit Sharma");
        challan2.setAccusedFatherName("Mahesh Sharma");
        challan2.setRcNumber("DL3CAZ9087");
        challan2.setChallanNumber("DL20241109205600");
        challan2.setChallanId(2);
        challan2.setChallanDate("2024-11-01");
        challan2.setChallanStatus("Cash");
        challan2.setChallanAmount("300");
        challan2.setRcStateCode("DL");
        challan2.setChallanPaymentSource("Card");
        challan2.setRtoOfficeName("Moti Nagar");
        challan2.setChallanPaymentDate("2024-11-02");
        challan2.setChallanPlace("Moti Nagar Chowk");

        ArrayList<OffenceModel> offences2 = new ArrayList<>();
        offences2.add(new OffenceModel("Jumping Red Light", "184 MVA", "300"));
        challan2.setOffences(offences2);

        filteredChallanList.add(challan2);

        // 🔹 3rd Challan - Pending
        ChallanModel challan3 = new ChallanModel();
        challan3.setAccusedName("Ravi Verma");
        challan3.setAccusedFatherName("Om Prakash");
        challan3.setRcNumber("UP14BX2234");
        challan3.setChallanNumber("UP142024030201");
        challan3.setChallanId(3);
        challan3.setChallanDate("2024-03-02");
        challan3.setChallanStatus("Pending");
        challan3.setChallanAmount("1000");
        challan3.setRcStateCode("UP");
        challan3.setChallanPaymentSource("NA");
        challan3.setRtoOfficeName("Ghaziabad RTO");
        challan3.setChallanPaymentDate("NA");
        challan3.setChallanPlace("NH-24 Bypass");

        ArrayList<OffenceModel> offences3 = new ArrayList<>();
        offences3.add(new OffenceModel("Over Speeding", "S112 RW S183(1)(i)", "1000"));
        challan3.setOffences(offences3);

        filteredChallanList.add(challan3);

        // 🔹 4th Challan - Paid
        ChallanModel challan4 = new ChallanModel();
        challan4.setAccusedName("Sunil Mehta");
        challan4.setAccusedFatherName("R. Mehta");
        challan4.setRcNumber("MH12XY7777");
        challan4.setChallanNumber("MH122023121009");
        challan4.setChallanId(4);
        challan4.setChallanDate("2023-12-10");
        challan4.setChallanStatus("Cash");
        challan4.setChallanAmount("600");
        challan4.setRcStateCode("MH");
        challan4.setChallanPaymentSource("UPI");
        challan4.setRtoOfficeName("Pune RTO");
        challan4.setChallanPaymentDate("2023-12-10");
        challan4.setChallanPlace("JM Road");

        ArrayList<OffenceModel> offences4 = new ArrayList<>();
        offences4.add(new OffenceModel("Driving Without License", "u/s 3/181", "600"));
        challan4.setOffences(offences4);

        filteredChallanList.add(challan4);

        ChallanModel challan5 = new ChallanModel();
        challan5.setAccusedName("Sunil Mehta");
        challan5.setAccusedFatherName("R. Mehta");
        challan5.setRcNumber("MH12XY7777");
        challan5.setChallanNumber("MH122023121009");
        challan5.setChallanId(4);
        challan5.setChallanDate("2023-12-10");
        challan5.setChallanStatus("Cash"); // ✅ Paid challan
        challan5.setChallanAmount("600");
        challan5.setRcStateCode("MH");
        challan5.setChallanPaymentSource("UPI");
        challan5.setRtoOfficeName("Pune RTO");
        challan5.setChallanPaymentDate("2023-12-10");
        challan5.setChallanPlace("JM Road");

// ✅ Add multiple offences
        ArrayList<OffenceModel> offences5 = new ArrayList<>();
        offences5.add(new OffenceModel(
                "Driving Without License",
                "u/s 3/181",
                "300"
        ));
        offences5.add(new OffenceModel(
                "Over Speeding in City Limits",
                "S112 RW S183(1)(i)",
                "300"
        ));
        challan5.setOffences(offences5);

        filteredChallanList.add(challan5);

        ChallanModel challan6 = new ChallanModel();
        challan6.setAccusedName("Aarav Singh");
        challan6.setAccusedFatherName("Rajesh Singh");
        challan6.setRcNumber("DL9CAB1234");
        challan6.setChallanNumber("DL9202411050830");
        challan6.setChallanId(5);
        challan6.setChallanDate("2024-11-05");
        challan6.setChallanStatus("Pending"); // 🔸 Still pending
        challan6.setChallanAmount("1200");
        challan6.setRcStateCode("DL");
        challan6.setChallanPaymentSource("NA");
        challan6.setRtoOfficeName("Delhi South RTO");
        challan6.setChallanPaymentDate("NA");
        challan6.setChallanPlace("AIIMS Flyover");

// 🔹 Add multiple offences
        ArrayList<OffenceModel> offences6 = new ArrayList<>();
        offences6.add(new OffenceModel(
                "Using Mobile Phone While Driving",
                "Sec 184 MVA",
                "600"
        ));
        offences6.add(new OffenceModel(
                "Failure to Stop at Red Light",
                "Sec 119 U/s 177",
                "600"
        ));
        challan6.setOffences(offences6);

        filteredChallanList.add(challan6);

        allChallanList.addAll(filteredChallanList);

        filerChallan("Pending");


        // ✅ Log final list
        CommonLogic.showTestLog("ChallanTestData", "🧾 Total challans created: " + filteredChallanList.size());
    }

    private long getRemainingRefreshTime(long lastHitMillis,
                                         long currentServerMillis) {

        long nextAllowedTime = lastHitMillis + REFRESH_INTERVAL;
        return nextAllowedTime - currentServerMillis;
    }

    private void handleChallanRefresh(String vehicleNumber,
                                      long currentServerMillis) {

        AppExecutors.getInstance().diskIO().execute(() -> {

            VehicleChallanEntity entity =
                    AppDatabase.getInstance(this)
                            .vehicleChallanDao()
                            .getByVehicleNumber(vehicleNumber);

            runOnUiThread(() -> {

                if (entity == null) {
                    // First time → call API
                    checkVehicleChallan(vehicleNumber);
                    return;
                }

                long remainingTime = getRemainingRefreshTime(
                        entity.lastHitServerMillis,
                        currentServerMillis
                );

                showChallanRefreshBottomSheet(
                        remainingTime,
                        remainingTime <= 0
                );
            });
        });
    }

    private void showChallanRefreshBottomSheet(long remainingMillis,
                                               boolean canRefresh) {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottomsheet_refresh_timer, null);

        BottomsheetRefreshTimerBinding bottomsheetRefreshTimerBinding = BottomsheetRefreshTimerBinding.bind(view);

        bottomSheetChallanRefreshDialog.setContentView(view);

        ShimmerFrameLayout shimmer = view.findViewById(R.id.shimmerTimer);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        TextView tvDays = view.findViewById(R.id.tvDays);
        TextView tvHours = view.findViewById(R.id.tvHours);
        TextView tvMinutes = view.findViewById(R.id.tvMinutes);
        TextView tvSeconds = view.findViewById(R.id.tvSeconds);

        bottomsheetRefreshTimerBinding.ivClose.setOnClickListener(view1 -> bottomSheetChallanRefreshDialog.dismiss());
        bottomsheetRefreshTimerBinding.btnGotIt.setOnClickListener(view1 -> bottomSheetChallanRefreshDialog.dismiss());

        shimmer.startShimmer();

        // Fake loading like real apps
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            shimmer.stopShimmer();
            shimmer.setShimmer(null);

            if (canRefresh) {
                tvMessage.setText("Challan data updated successfully");
                tvDays.setVisibility(View.GONE);
                tvHours.setVisibility(View.GONE);
                tvMinutes.setVisibility(View.GONE);
                tvSeconds.setVisibility(View.GONE);
            } else {
                startReverseTimer(
                        remainingMillis,
                        tvDays, tvHours, tvMinutes, tvSeconds
                );
            }

        }, 1000);

        if (bottomSheetChallanRefreshDialog != null && !bottomSheetChallanRefreshDialog.isShowing()) {
            bottomSheetChallanRefreshDialog.show();
        }
    }

    private void startReverseTimer(long millis,
                                   TextView d, TextView h,
                                   TextView m, TextView s) {

        new CountDownTimer(millis, 1000) {

            public void onTick(long ms) {
                long days = TimeUnit.MILLISECONDS.toDays(ms);
                long hours = TimeUnit.MILLISECONDS.toHours(ms) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;

                d.setText(String.format("%02d", days));
                h.setText(String.format("%02d", hours));
                m.setText(String.format("%02d", minutes));
                s.setText(String.format("%02d", seconds));
            }

            public void onFinish() {
                d.setText("00");
                h.setText("00");
                m.setText("00");
                s.setText("00");
            }
        }.start();
    }




}
