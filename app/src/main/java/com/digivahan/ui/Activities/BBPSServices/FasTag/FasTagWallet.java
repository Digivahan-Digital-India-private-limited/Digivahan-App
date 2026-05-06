package com.digivahan.ui.Activities.BBPSServices.FasTag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.ashu.ashuutils.APIHelper;
import com.digivahan.R;
import com.digivahan.data.adapters.FasTagAdditionalDetailsAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.FasTagCardModel;
import com.digivahan.databinding.ActivityFasTagWalletBinding;
import com.digivahan.databinding.ProfileIncompleteDialogDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagAppDatabase;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagDao;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagEntity;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData.FasTagMapper;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.gson.JsonObject;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FasTagWallet extends BaseActivity implements PaymentResultListener {
    String TAG = "FasTagWalletData";

    ActivityFasTagWalletBinding binding;

    FasTagCardModel selectedBank;

    String billerDetailsUrl = APIData.GET_BILLER_ENQUIRY;
    PreferencesManager manager;

    AshDialog loadingDialog;

    int transactionAmount = 0;

    FasTagAppDatabase database;
    FasTagDao dao;
    int minimumRecharge = 100, maximumRecharge = 9000;

    ArrayList<String> allowedKeysForMiniBalance = new ArrayList<>();
    ArrayList<String> allowedKeysForMaxBalance = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFasTagWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("FasTag Wallet");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        getOnBackPressedDispatcher().addCallback(FasTagWallet.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        manager = new PreferencesManager(FasTagWallet.this);

        database = FasTagAppDatabase.getInstance(FasTagWallet.this);
        dao = database.fasTagDao();


        allowedKeysForMiniBalance.clear();
        allowedKeysForMiniBalance.add("Minimum Recharge");
        allowedKeysForMiniBalance.add("MinimumRechargeAmount");
        allowedKeysForMiniBalance.add("Minimum Recharge Amount");
        allowedKeysForMiniBalance.add("Minimum Top-up Amount");
        allowedKeysForMiniBalance.add("Minimum Amount for Top-up"); // optional (future use)

        allowedKeysForMaxBalance.clear();
        allowedKeysForMaxBalance.add("Available Recharge Limit");
        allowedKeysForMaxBalance.add("Maximum Permissible Recharge Amount"); // optional (future use)

        loadingDialog = new AshDialog(FasTagWallet.this, "Please wait", "");

        try {
            if (getIntent().hasExtra("selectedBank")) {
                selectedBank = (FasTagCardModel) getIntent().getSerializableExtra("selectedBank");
                minimumRecharge = (int) Objects.requireNonNull(selectedBank).getMinAmount();
                maximumRecharge = (int) Objects.requireNonNull(selectedBank).getMaxAmount();
            }
        } catch (Exception e) {
            selectedBank = null;
        }

        // ✅ Handle chip clicks (Flexbox version)
        for (int i = 0; i < binding.flexboxAmount.getChildCount(); i++) {
            View view = binding.flexboxAmount.getChildAt(i);

            if (view instanceof Chip) {
                Chip chip = (Chip) view;

                chip.setOnClickListener(v -> {

                    // Unselect all chips
                    for (int j = 0; j < binding.flexboxAmount.getChildCount(); j++) {
                        View innerView = binding.flexboxAmount.getChildAt(j);
                        if (innerView instanceof Chip) {
                            ((Chip) innerView).setChecked(false);
                        }
                    }

                    // Select current chip
                    chip.setChecked(true);

                    // Set amount in EditText
                    String amount = chip.getText().toString().replace("₹", "");
                    binding.etAmount.setText(amount);
                });
            }
        }

        // ✅ Remove chip selection if user types manually
        binding.etAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                for (int i = 0; i < binding.flexboxAmount.getChildCount(); i++) {
                    View view = binding.flexboxAmount.getChildAt(i);
                    if (view instanceof Chip) {
                        ((Chip) view).setChecked(false);
                    }
                }
            }
        });

        // ✅ Proceed button
        binding.btnAddMoney.setOnClickListener(v -> {

            String amountStr = binding.etAmount.getText() != null
                    ? binding.etAmount.getText().toString().trim()
                    : "";

            if (amountStr.isEmpty()) {
                binding.etAmount.setError("Enter amount");
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(amountStr);
            } catch (Exception e) {
                binding.etAmount.setError("Invalid amount");
                return;
            }

            if (amount < minimumRecharge) {
                binding.etAmount.setError("Minimum amount is ₹" + minimumRecharge);
                return;
            }

            if (amount > maximumRecharge) {
                binding.etAmount.setError("Maximum amount is ₹" + maximumRecharge);
                return;
            }

            if (selectedBank.getEnquiryReferenceId() == null || selectedBank.getEnquiryReferenceId().isEmpty()) {
                binding.etAmount.setError("Unable to make payment for this FasTag right now, please try after some time");
                Toast.makeText(this, "Unable to make payment for this FasTag right now, please try after some time", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Call Payment API / Razorpay here
            CommonLogic.showTestLog(TAG, "Proceeding with ₹" + amount);

            makePaymentForFasTag(amount);
        });

        if (selectedBank != null){
            CommonLogic.showTestLog(TAG, "selectedBank:- " + selectedBank.getVehicleNumber());
            // set data
            binding.tvVehicleNumber.setText(selectedBank.getVehicleNumber());
            binding.tvCustomerName.setText(selectedBank.getCustomerName());
            binding.tvBankName.setText(selectedBank.getBillerName());

            CommonLogic.loadSvg(FasTagWallet.this, binding.imgBank, selectedBank.getIconUrl());

            getBillerDetails(selectedBank.getBillerId(), selectedBank.getVehicleNumber());

        }


    }

    /*private void checkBillerDetails(String billerId, boolean checkBalance) {

        CommonLogic.showTestLog(TAG, APIData.GET_BILLER_DETAILS + billerId);
        ApiClient.getApiService(FasTagWallet.this)
                .commonGETMethodToHitAllAPIs(APIData.GET_BILLER_DETAILS + billerId)
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
                                if (dataObj.has("fetchRequirement") && dataObj.getString("fetchRequirement").equalsIgnoreCase("MANDATORY")) {
                                    billerDetailsUrl = APIData.GET_BILLER_ENQUIRY;
                                } else {
                                    billerDetailsUrl = APIData.GET_VALIDATE_BILLER;
                                }
                            }

                            if (checkBalance){
                                getBillerDetails(billerId, selectedBank.getVehicleNumber());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        CommonLogic.showTestLog(TAG, "checkBillerDetails error: " + t.getMessage());
                    }
                });
    }*/

    private void setChipSetValues(int amount){
        if (amount > 100){
            amount = 100;
        }
        binding.chip1.setText(String.valueOf(amount));
        binding.chip2.setText(String.valueOf(amount * 2));
        binding.chip3.setText(String.valueOf(amount * 5));
        binding.chip4.setText(String.valueOf(amount * 10));

        binding.amountNote.setText("* Please enter amount minimum ("
                + amount + ") or above");
    }

    private void getBillerDetails(String billerId, String vehicle_number) {
        loadingDialog.show();
        selectedBank.setVehicleNumber(vehicle_number);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("billerId", billerId);
        requestBody.addProperty("vehicle_number", vehicle_number);
        requestBody.addProperty("phone_number", manager.getUser().getPhone_number());
        requestBody.addProperty("trans_id", manager.getUser().getUserId() + String.valueOf(System.currentTimeMillis()));

        CommonLogic.showTestLog(TAG, "getBillerDetails:- " + requestBody.toString());

        ApiClient.getApiService(FasTagWallet.this)
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

                                if (dataObj.has("enquiryReferenceId")) {
                                    selectedBank.setEnquiryReferenceId(dataObj.getString("enquiryReferenceId"));
                                }

                                if (dataObj.has("BillAmount") && !dataObj.getString("BillAmount").isEmpty() && !dataObj.getString("BillAmount").equalsIgnoreCase("NA")
                                        && !dataObj.getString("enquiryReferenceId").equalsIgnoreCase("N/A")) {
                                    minimumRecharge = (int) Double.parseDouble(dataObj.getString("BillAmount"));
                                }

                                if (dataObj.has("CustomerName")) {
                                    binding.tvCustomerName.setText(dataObj.getString("CustomerName"));
                                }

                                JSONArray additionalDetails = dataObj.optJSONArray("AdditionalDetails");

                                ArrayList<FasTagAdditionalDetails> fasTagAdditionalDetailsList = new ArrayList<>();

                                for (FasTagAdditionalDetails additionalDetail :
                                        APIHelper.convertJsonArrayToList(
                                                Objects.requireNonNull(additionalDetails),
                                                FasTagAdditionalDetails.class)) {

                                    if (additionalDetail.getValue() != null
                                            && !additionalDetail.getValue().isEmpty()
                                            && !additionalDetail.getValue().equalsIgnoreCase("NA")
                                            && !additionalDetail.getValue().equalsIgnoreCase("N/A")) {

                                        // 🔹 Format name here
                                        additionalDetail.setName(formatName(additionalDetail.getName()));

                                        for (String key : allowedKeysForMiniBalance) {
                                            if (key.equalsIgnoreCase(additionalDetail.getName())) {
                                                try {
                                                    minimumRecharge = (int) Double.parseDouble(additionalDetail.getValue());
                                                } catch (Exception e) {
                                                    minimumRecharge = 100;
                                                }
                                                break;
                                            }
                                        }

                                        for (String key : allowedKeysForMaxBalance) {
                                            if (key.equalsIgnoreCase(additionalDetail.getName())) {
                                                try {
                                                    maximumRecharge = (int) Double.parseDouble(additionalDetail.getValue());
                                                } catch (Exception e) {
                                                    maximumRecharge = 9000;
                                                }
                                                break;
                                            }
                                        }

                                        fasTagAdditionalDetailsList.add(additionalDetail);
                                    }
                                }

                                setChipSetValues(minimumRecharge);


                                FasTagAdditionalDetailsAdapter fasTagAdditionalDetailsAdapter = new FasTagAdditionalDetailsAdapter(FasTagWallet.this, fasTagAdditionalDetailsList);
                                binding.rvDataList.setAdapter(fasTagAdditionalDetailsAdapter);

                                /*String walletBalance = "0";

                                if (additionalDetails != null) {
                                    for (int i = 0; i < additionalDetails.length(); i++) {
                                        JSONObject obj = additionalDetails.optJSONObject(i);

                                        if (obj != null && ("Wallet balance".equalsIgnoreCase(obj.optString("Name")) ||
                                        "FASTag Balance".equalsIgnoreCase(obj.optString("Name"))) ||
                                                "Available Balance".equalsIgnoreCase(Objects.requireNonNull(obj).optString("Name")) ||
                                                obj.optString("Name").toLowerCase().contains("balance")) {
                                            walletBalance = obj.optString("Value", "0");
                                            break;
                                        }
                                    }
                                }*/


//                                binding.tvBalance.setText("₹ " + walletBalance);
                                binding.tvVehicleNumber.setText(vehicle_number);

                                binding.tvBankName.setText(selectedBank.getBillerName());

                                CommonLogic.loadSvg(FasTagWallet.this, binding.imgBank, selectedBank.getIconUrl());

                                FasTagEntity entity = FasTagMapper.toEntity(selectedBank);

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
                            }

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "getBillerDetails api error: " + e);
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        CommonLogic.showTestLog(TAG, "getBillerDetails api onFailure: " + t.getMessage());
                        loadingDialog.dismiss();
                    }
                });
    }

    public static String formatName(String input) {
        if (input == null || input.isEmpty()) return input;

        // Add space before uppercase letters (camelCase → camel Case)
        String result = input.replaceAll("([a-z])([A-Z])", "$1 $2");

        // Capitalize first letter of each word
        String[] words = result.split(" ");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return formatted.toString().trim();
    }

    private void makePaymentForFasTag(int orderPrice){

        transactionAmount = orderPrice;

        loadingDialog.show();
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("amount", orderPrice * 100);
        jsonObject.addProperty("user_id", manager.getUserId());
        /*if (Constants.ENABLE_TESTING) {
            jsonObject.addProperty("status", "test");
        }*/
        jsonObject.addProperty("purpose", "Payment to order QR code");

        CommonLogic.showTestLog(TAG, "makePaymentForFasTag:- " + jsonObject.toString());


        ApiClient.getApiService(this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.RAZORPAY_PAYMENT, jsonObject)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        CommonLogic.showTestLog(TAG, "📬 API onResponse() triggered");

                        JSONObject APIResponse = APIHelper.getResponseData(TAG, response);
                        CommonLogic.showTestLog(TAG, "✅ API Raw Response: " + APIResponse.toString());

                        try {
                            if (APIResponse.has("success") && APIResponse.getBoolean("success")) {
                                makePayment(manager.getUser().getFirst_name(), String.valueOf(orderPrice * 100), manager.getUser().getEmail(),
                                        manager.getUser().getPhone_number(), APIResponse.getJSONObject("order").getString("id"),
                                        APIResponse.getString("razorpay_key_id"), "payment for FasTag");
                            }else {
                                showOrderMaintenanceDialog(FasTagWallet.this);
                            }

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "🔥 Exception while parsing response: " + e.getMessage());
                            showOrderMaintenanceDialog(FasTagWallet.this);
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable) {
                        showOrderMaintenanceDialog(FasTagWallet.this);
                        CommonLogic.showTestLog(TAG, "💔 API call failed: " + throwable.getMessage());
                        loadingDialog.dismiss();
                    }
                });
    }

    @SuppressLint("GestureBackNavigation")
    public void showOrderMaintenanceDialog(Activity context) {
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

        ImageView closeBtn = view.findViewById(R.id.closeBtn);
        closeBtn.setVisibility(View.GONE);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Under Maintenance");

        TextView tvSubTitle = view.findViewById(R.id.tvSubTitle);
        tvSubTitle.setText("Currently, FasTag Service are not working.\n" +
                "We’re actively working to bring you a smoother and better experience very soon.\n" +
                "Thank you for your patience ❤\uFE0F");

        Button openLoginPageBtn = view.findViewById(R.id.openProfileBtn);
        openLoginPageBtn.setText("OK");

        openLoginPageBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void makePayment(String name, String amount, String email, String phone, String orderId, String Key, String description) {
        disableHideContentSecureForNextNavigation();
        CommonLogic.showTestLog(TAG, "payment Values: " + "name:- " + name + " amount:- " + amount + " email:- " + email + " phone:- " + phone + " orderId:- " + orderId + "Key:- " + Key);
        final Checkout co = new Checkout();
        co.setKeyID(Key);

        try {
            JSONObject options = new JSONObject();

            options.put("name", name);
            options.put("description", description);
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", "INR");

//            options.put("amount", amount);

            options.put("order_id", orderId);

            JSONObject preFill = new JSONObject();

            preFill.put("email", email);

            preFill.put("contact", phone);

            options.put("prefill", preFill);

            co.open(FasTagWallet.this, options);


        } catch (Exception e) {
            CommonLogic.showTestLog("Error in payment: ", e.getMessage());
            e.printStackTrace();
        }
    }


    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    @Override
    public void onPaymentSuccess(String s) {

        CommonLogic.showTestLog(TAG, "Payment Success");

        makeBBPayment(s);

    }

    private void makeBBPayment(String s) {
        loadingDialog.show();
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("user_id", manager.getUserId());
        jsonObject.addProperty("billerId", selectedBank.getBillerId());
        jsonObject.addProperty("externalRef", manager.getUserId() + String.valueOf(System.currentTimeMillis()) + s);
        jsonObject.addProperty("enquiryReferenceId", selectedBank.getEnquiryReferenceId());
        jsonObject.addProperty("vehicle_number", selectedBank.getVehicleNumber());
        jsonObject.addProperty("mobile", manager.getUser().getPhone_number());
        jsonObject.addProperty("transactionAmount", transactionAmount);

        CommonLogic.showTestLog(TAG, "makeBBPayment params:- " + jsonObject.toString());


        ApiClient.getApiService(this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.PAYMENT_SERVICE, jsonObject)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        CommonLogic.showTestLog(TAG, "📬 API onResponse() triggered");

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);
                            CommonLogic.showTestLog(TAG, "✅ API Raw Response: " + responseBody.toString());
                            JSONObject bbApiResponse = responseBody.getJSONObject("data");

                            CommonLogic.showTestLog(TAG, "Full Response: " + bbApiResponse.toString());

                            boolean status = responseBody.has("success") && responseBody.getBoolean("success") && bbApiResponse.has("statuscode") &&
                                    bbApiResponse.getString("statuscode").equalsIgnoreCase("TXN");

                            CommonLogic.showTestLog(TAG, "Status: " + status);

                            if (status) {
                                showSuccessDialog(FasTagWallet.this);
                            }else {
                                showOrderMaintenanceDialog(FasTagWallet.this);
                            }
                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "🔥 Exception while parsing response: " + e.getMessage());
                            showOrderMaintenanceDialog(FasTagWallet.this);
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable) {
                        showOrderMaintenanceDialog(FasTagWallet.this);
                        CommonLogic.showTestLog(TAG, "💔 API call failed: " + throwable.getMessage());
                        loadingDialog.dismiss();
                    }
                });
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
    }

    public void showSuccessDialog(Activity activity) {
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


        btnLogin.setText("Done");
        tvTitle.setText("Recharge Successful");
        tvMessage.setText("Your FASTag has been successfully recharged. Thank you for choosing our service. The updated balance will reflect in your wallet shortly.");

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
            // 👉 Redirect to login activity here
            getBillerDetails(selectedBank.getBillerId(), selectedBank.getVehicleNumber());
        });
        dialog.show();
    }
}
