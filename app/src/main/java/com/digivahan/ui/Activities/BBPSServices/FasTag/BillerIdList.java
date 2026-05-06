package com.digivahan.ui.Activities.BBPSServices.FasTag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.ashu.ashuutils.APIHelper;
import com.digivahan.R;
import com.digivahan.data.adapters.FasTagBankItemAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.FasTagCardModel;
import com.digivahan.databinding.ActivityFasTagCardListBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagAppDatabase;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagDao;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagEntity;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagMapper;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillerIdList extends BaseActivity {
    String TAG = "FasTagBankListData";

    ActivityFasTagCardListBinding binding;

    PreferencesManager manager;
    AshDialog loadingDialog;

    FasTagBankItemAdapter fasTagBankItemAdapter;

    ArrayList<FasTagCardModel> fasTagCardList = new ArrayList<>();
    ArrayList<FasTagCardModel> filterFasTagCardList = new ArrayList<>();

    FasTagAppDatabase database;
    FasTagDao dao;

    int totalPages = 1, currentPage = 1;

    String vehicleNumber = "", categoryKey = "", billerDetailsUrl = APIData.GET_BILLER_ENQUIRY;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFasTagCardListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);


        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        getOnBackPressedDispatcher().addCallback(BillerIdList.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        try {
            if (getIntent().hasExtra("vehicleNumber")) {
                vehicleNumber = getIntent().getStringExtra("vehicleNumber");
                categoryKey = getIntent().getStringExtra("categoryKey");
                binding.vehicleNumber.setText("Vehicle Number: " + vehicleNumber);

                if (categoryKey.equalsIgnoreCase("C10")) {
                    binding.toolbarLayout.tvTitle.setText("Bank List");
                } else if (categoryKey.equalsIgnoreCase("C31")) {
                    binding.toolbarLayout.tvTitle.setText("Police Department");
                }
            }
        } catch (Exception e) {
            vehicleNumber = "";
        }

        database = FasTagAppDatabase.getInstance(BillerIdList.this);
        dao = database.fasTagDao();

        manager = new PreferencesManager(BillerIdList.this);
        loadingDialog = new AshDialog(BillerIdList.this, "Please wait", "Getting Data...");

        fasTagBankItemAdapter = new FasTagBankItemAdapter(BillerIdList.this, filterFasTagCardList, new FasTagBankItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FasTagCardModel model) {

                if (!model.isAvailable() && !model.getBillerStatus().equalsIgnoreCase("ACTIVE")) {
                    showServiceDialog(BillerIdList.this);
                    return;
                }

                if (!vehicleNumber.isEmpty()) {
                    model.setVehicleNumber(vehicleNumber);

                    checkBillerDetails(model);

                    /*if (categoryKey.equalsIgnoreCase("C31")) {
                        checkBillerDetails(model);
                    } else if (categoryKey.equalsIgnoreCase("C10")) {
                        getBillerDetails(model);
                    }*/
                } else {
                    showAlertDialog(BillerIdList.this);
                }
            }
        });
        binding.rvFasTagCard.setAdapter(fasTagBankItemAdapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filerList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        setBillerIdList();

    }

    private void checkBillerDetails(FasTagCardModel model) {
        loadingDialog.show();
        CommonLogic.showTestLog(TAG, APIData.GET_BILLER_DETAILS + model.getBillerId());
        ApiClient.getApiService(BillerIdList.this)
                .commonGETMethodToHitAllAPIs(APIData.GET_BILLER_DETAILS + model.getBillerId())
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);
                            JSONObject bbApiResponse = responseBody.getJSONObject("data");

                            JSONObject dataObj = bbApiResponse.getJSONObject("data");

                            CommonLogic.showTestLog(TAG, "Full Response: " + bbApiResponse.toString());

                            boolean status = responseBody.has("success") && responseBody.getBoolean("success") && bbApiResponse.has("statuscode") &&
                                    bbApiResponse.getString("statuscode").equalsIgnoreCase("TXN");

                            CommonLogic.showTestLog(TAG, "Status: " + status);

                            if (status) {

                                if (categoryKey.equalsIgnoreCase("C10")) {
                                    JSONArray paymentModesList = dataObj.getJSONArray("paymentModes");

                                    for (int i = 0; i < paymentModesList.length(); i++){
                                        JSONObject object = paymentModesList.getJSONObject(i);

                                        model.setMinAmount(object.getDouble("minAmount"));
                                        model.setMaxAmount(object.getDouble("maxAmount"));

                                        if (object.getString("name").equalsIgnoreCase("Cash")){
                                            break;
                                        }
                                    }
                                }

                                if (dataObj.has("fetchRequirement") && dataObj.getString("fetchRequirement").equalsIgnoreCase("MANDATORY")) {
                                    billerDetailsUrl = APIData.GET_BILLER_ENQUIRY;
                                } else {
                                    billerDetailsUrl = APIData.GET_VALIDATE_BILLER;
                                }
                            }

                            getBillerDetails(model);

                        } catch (Exception e) {
                            e.printStackTrace();
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        CommonLogic.showTestLog(TAG, "checkBillerDetails error: " + t.getMessage());
                        loadingDialog.dismiss();
                    }
                });
    }

    private void getBillerDetails(FasTagCardModel model) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("billerId", model.getBillerId());
        requestBody.addProperty("vehicle_number", model.getVehicleNumber());
        requestBody.addProperty("phone_number", manager.getUser().getPhone_number());
        requestBody.addProperty("trans_id", manager.getUser().getUserId() + String.valueOf(System.currentTimeMillis()));

        CommonLogic.showTestLog(TAG, "getBillerDetails:- " + requestBody.toString());

        ApiClient.getApiService(BillerIdList.this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(billerDetailsUrl, requestBody).
                enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);
                            JSONObject bbApiResponse = responseBody.getJSONObject("data");

                            JSONObject dataObj = bbApiResponse.getJSONObject("data");

                            CommonLogic.showTestLog(TAG, "Full Response: " + bbApiResponse.toString());

                            boolean status = responseBody.has("success") && responseBody.getBoolean("success") && bbApiResponse.has("statuscode") &&
                                    bbApiResponse.getString("statuscode").equalsIgnoreCase("TXN");

                            CommonLogic.showTestLog(TAG, "Status: " + status);

                            if (status) {

                                if (categoryKey.equalsIgnoreCase("C10")) {

                                    if (dataObj.has("enquiryReferenceId")) {
                                        model.setEnquiryReferenceId(dataObj.getString("enquiryReferenceId"));
                                    }

                                    if (dataObj.has("CustomerName")) {
                                        model.setCustomerName(dataObj.getString("CustomerName"));
                                    }

                                    JSONArray additionalDetails = dataObj.optJSONArray("AdditionalDetails");

                                    String walletBalance = "0";

                                    if (additionalDetails != null) {
                                        for (int i = 0; i < additionalDetails.length(); i++) {
                                            JSONObject obj = additionalDetails.optJSONObject(i);

                                            if (obj != null && ("Wallet balance".equalsIgnoreCase(obj.optString("Name")) ||
                                                    "FASTag Balance".equalsIgnoreCase(obj.optString("Name"))) ||
                                                    "Available Balance".equalsIgnoreCase(Objects.requireNonNull(obj).optString("Name")) ||
                                                    obj.optString("Name").toLowerCase().contains("balance")) {
                                                model.setWalletBalance(obj.optString("Value", "0"));
                                                break;
                                            }
                                        }
                                    }

                                    FasTagEntity entity = FasTagMapper.toEntity(model);

                                    // ✅ Check if vehicle already exists
                                    FasTagEntity existing = dao.getByVehicle(entity.vehicleNumber);

                                    if (existing != null) {
                                        // Keep same ID → update existing row
                                        entity.id = existing.id;
                                        dao.update(entity);
                                    } else {
                                        // Insert new
                                        dao.insert(entity);
                                    }

                                    loadingDialog.dismiss();

                                    disableHideContentSecureForNextNavigation();
                                    Intent FasTagWalletPage = new Intent(BillerIdList.this, FasTagWallet.class);
                                    FasTagWalletPage.putExtra("selectedBank", model);
                                    startActivity(FasTagWalletPage);
                                    finish();
                                }else {
                                    showAlertDialog(BillerIdList.this);
                                }
                            } else {
                                showAlertDialog(BillerIdList.this);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlertDialog(BillerIdList.this);
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        loadingDialog.dismiss();
                        showAlertDialog(BillerIdList.this);
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setBillerIdList() {

        CommonLogic.showTestLog(TAG, "API CALL STARTED");

        fasTagCardList.clear();

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("categorykey", categoryKey);
        requestBody.addProperty("pageNumber", 1);
        requestBody.addProperty("recordsPerPage", 100);


        CommonLogic.showTestLog(TAG, "Request Body: " + requestBody.toString());

        ApiClient.getApiService(BillerIdList.this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_BILLER_LIST, requestBody)
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {

                        CommonLogic.showTestLog(TAG, "API RESPONSE RECEIVED");

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);
                            JSONObject bbApiResponse = responseBody.getJSONObject("data");

                            CommonLogic.showTestLog(TAG, "Full Response: " + bbApiResponse.toString());

                            boolean status = responseBody.has("success") && responseBody.getBoolean("success") && bbApiResponse.has("statuscode") &&
                                    bbApiResponse.getString("statuscode").equalsIgnoreCase("TXN");

                            CommonLogic.showTestLog(TAG, "Status: " + status);

                            if (status) {

                                JSONObject dataObj = bbApiResponse.getJSONObject("data");
                                JSONObject metaData = dataObj.getJSONObject("meta");

                                totalPages = metaData.getInt("totalPages");
                                currentPage = metaData.getInt("currentPage");

                                CommonLogic.showTestLog(TAG,
                                        "Pagination -> currentPage: " + currentPage +
                                                ", totalPages: " + totalPages);

                                JSONArray recordsArray = dataObj.getJSONArray("records");

                                CommonLogic.showTestLog(TAG,
                                        "Records Count: " + recordsArray.length());

                                for (int i = 0; i < recordsArray.length(); i++) {

                                    JSONObject obj = recordsArray.getJSONObject(i);

                                    FasTagCardModel model = new FasTagCardModel();

                                    model.setBillerId(obj.optString("billerId"));
                                    model.setBillerName(obj.optString("billerName"));
                                    model.setCategoryKey(obj.optString("categoryKey"));
                                    model.setType(obj.optString("type"));
                                    model.setCategoryName(obj.optString("categoryName"));
                                    model.setCoverageCity(obj.optString("coverageCity"));
                                    model.setCoverageState(obj.optString("coverageState"));
                                    model.setCoveragePincode(obj.optInt("coveragePincode"));
                                    model.setUpdatedDate(obj.optString("updatedDate"));
                                    model.setBillerStatus(obj.optString("billerStatus"));
                                    model.setAvailable(obj.optBoolean("isAvailable"));
                                    model.setIconUrl(obj.optString("iconUrl"));

                                    fasTagCardList.add(model);

                                    // Optional: log first few items only (avoid spam)
                                    if (i < 3) {
                                        CommonLogic.showTestLog(TAG,
                                                "Item " + i + ": " + model.getBillerName());
                                    }
                                }

                                CommonLogic.showTestLog(TAG,
                                        "Final List Size: " + fasTagCardList.size());

                                filterFasTagCardList.clear();
                                filterFasTagCardList.addAll(fasTagCardList);
                                fasTagBankItemAdapter.notifyDataSetChanged();

                            } else {
                                CommonLogic.showTestLog(TAG,
                                        "API FAILED: " + bbApiResponse.optString("status"));
                            }

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "Exception: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {

                        CommonLogic.showTestLog(TAG, "API FAILURE: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filerList(String name) {
        filterFasTagCardList.clear();
        for (FasTagCardModel item : fasTagCardList) {
            if (item.getBillerName().toLowerCase().startsWith(name.toLowerCase())) {
                filterFasTagCardList.add(item);
            }
        }

        fasTagBankItemAdapter.notifyDataSetChanged();
    }


    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    public void showAlertDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_password_changed);
        dialog.setCancelable(false);

        // Transparent background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView btnLogin = dialog.findViewById(R.id.btnLogin);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        ImageView imgStatus = dialog.findViewById(R.id.imgStatus);
        imgStatus.setImageResource(R.drawable.app_logo);

        btnLogin.setText("Ok");

        String title = "FasTag Not Found";
        String message = "Unable to find your FASTag in this bank, Please choose correct bank and try again.";

        if (categoryKey.equalsIgnoreCase("C31")){
            title = "Wrong Department";
            message = "You have select wrong department, Please choose correct department and try again.";
        }

        tvTitle.setText(title);
        tvMessage.setText(message);

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
        });

        loadingDialog.dismiss();
        dialog.show();
    }

    public void showServiceDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_password_changed);
        dialog.setCancelable(false);

        // Transparent background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView btnLogin = dialog.findViewById(R.id.btnLogin);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        ImageView imgStatus = dialog.findViewById(R.id.imgStatus);
        imgStatus.setImageResource(R.drawable.app_logo);

        btnLogin.setText("Ok");

        tvTitle.setText("No Service");
        tvMessage.setText("No service available for this option at this time, Please choose any other option.");

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (loadingDialog != null && loadingDialog.isVisible()) {
            loadingDialog.dismiss();
        }
    }

}
