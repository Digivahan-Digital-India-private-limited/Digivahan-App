package com.digivahan.ui.Activities.orderDetails;

import static com.digivahan.utils.VehicleType.TWO_WHEELER;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.AddressBookModel;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.QRDataModel;
import com.digivahan.databinding.ActivityOrderDetailsPageBinding;
import com.digivahan.databinding.ProfileIncompleteDialogDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.deliveryAddress.AddEditDeliveryAddress;
import com.digivahan.ui.Activities.qr.VirtualQR;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderQRPage extends BaseActivity implements PaymentResultListener {

    String TAG = "OrderQRPageData";
    ActivityOrderDetailsPageBinding binding;
    BottomSheetDialog bottomSheetDialog;
    View sheetView;
    LinearLayout container;
    private int selectedPosition = -1, orderPrice = 0; // No item selected initially

    ArrayList<AddressBookModel> addressList = new ArrayList<>();
    ArrayList<AddressBookModel> addressFilteredList = new ArrayList<>();

    AshDialog loadingDialog;

    PreferencesManager preferencesManager;

    GarageItemModel vehicleDetails;
    QRDataModel qrItem;

    String orderType = "", qr_id = "", qrFor = "", courier_company_id = "",courier_name = "", active_partner = "shiprocket";

    int selectedDeliveryType = R.id.regularDeliveryBtn;

    @Override
    protected void onResume() {
        super.onResume();
        setDeliveryAddress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        preferencesManager = new PreferencesManager(OrderQRPage.this);

        loadingDialog = new AshDialog(OrderQRPage.this, "Please wait", "");

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.notificationBellLayout.setVisibility(View.GONE);
        binding.toolbarLayout.tvTitle.setText("Review your order");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        try {
            if (getIntent().hasExtra("orderType")){
                orderType = Objects.requireNonNull(getIntent().getStringExtra("orderType"));
            }

            if (getIntent().hasExtra("qrFor")) {
                qrFor = getIntent().getStringExtra("qrFor");
            }

            if (getIntent().hasExtra("orderType") && orderType != null && orderType.equalsIgnoreCase("vehicle")) {
                binding.nameFiledLayout.setVisibility(View.GONE);
                if (getIntent().hasExtra("vehicleDetails")) {
                    vehicleDetails = (GarageItemModel) getIntent().getSerializableExtra("vehicleDetails");
                    binding.vehicleName.setText(vehicleDetails.getVehicle_name() + ", " + vehicleDetails.getVehicle_number());
                } else {
                    getVehicleData(getIntent().getStringExtra("vehicleId"));
                }
            }
            else {
                binding.nameFiledLayout.setVisibility(View.VISIBLE);
                CommonLogic.setUserInPutFiledData(binding.nameField, R.drawable.profile_icon, "Enter your " + orderType + " or product name",
                        "", true, binding.scrollView, binding.continueBtn);

                binding.vehicleName.setText("Order QR for " + orderType);

                if (getIntent().hasExtra("qr_id")) {
                    qr_id = getIntent().getStringExtra("qr_id");
                }

            }
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, String.valueOf(e.getMessage()));
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });

        String fullText = "(₹299)";

// Create spannable
        SpannableString spannable = new SpannableString(fullText);

// Apply strike only to ₹299 part → index 1 to 5
        spannable.setSpan(
                new StrikethroughSpan(),
                1, 5,   // positions of ₹299
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        binding.tvOldPrice.setText(spannable);


        binding.tvInfo.setOnClickListener(v -> {

            View popupView = LayoutInflater.from(OrderQRPage.this)
                    .inflate(R.layout.popup_suggestion, null);

            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            popupWindow.setOutsideTouchable(true);
            popupWindow.setElevation(10f);

            // Show above the icon
            popupWindow.showAsDropDown(v, -150, -v.getHeight() * 2);
        });

        binding.deliveryRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            selectedDeliveryType = radioGroup.getCheckedRadioButtonId();
            setEstimatedTime(addressFilteredList.get(selectedPosition).getPincode());
        });


        binding.changeAddressBtn.setOnClickListener(v -> {
            showAddressBottomSheet(this, 0);
        });

        binding.continueBtn.setOnClickListener(v -> {
            if (selectedPosition < 0) {
                Toast.makeText(this, "Please select delivery address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (orderPrice <= 0) {
                Toast.makeText(this, "Use another delivery address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (active_partner.equalsIgnoreCase("shiprocket") && (courier_company_id == null || courier_company_id.equalsIgnoreCase("null") || courier_company_id.isEmpty())) {
//                Toast.makeText(this, "Delivery not available in this area", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Please check your delivery address", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject jsonObjectCheckQr = new JsonObject();
            jsonObjectCheckQr.addProperty("user_id", preferencesManager.getUserId());
            if (orderType.equalsIgnoreCase("vehicle")) {
                jsonObjectCheckQr.addProperty("vehicle_id", vehicleDetails.getVehicle_id());
            }else {
                jsonObjectCheckQr.addProperty("qr_id", qr_id);
            }

            if (binding.nameFiledLayout.getVisibility() == View.VISIBLE){
                qrFor = binding.nameField.etInput.getText().toString();

                if (qrFor.isEmpty()){
                    Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                    binding.nameField.etInput.setError(getString(R.string.empty_field_string));
                    return;
                }else if (qrFor.length() < 3){
                    Toast.makeText(this, "At least 3 characters required", Toast.LENGTH_SHORT).show();
                    binding.nameField.etInput.setError("At least 3 characters required");
                    return;

                }
            }

            getPaymentData();
//            createOrder("paymentId");



        });


        // Setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Small", "Medium", "Large"}
        );
        binding.orderSizeSinner.setAdapter(adapter);

// Quantity logic
        binding.btnMinus.setOnClickListener(v -> {
            int qty = Integer.parseInt(binding.tvQuantity.getText().toString());
            if (qty > 1) {
                binding.tvQuantity.setText(String.valueOf(qty - 1));
//                orderPrice -= 250;
//                binding.orderQRPrice.setText(getString(R.string.currency_sign) + orderPrice);
            }
        });

        binding.btnPlus.setOnClickListener(v -> {
            int qty = Integer.parseInt(binding.tvQuantity.getText().toString());
            if (qty < 2) {
                binding.tvQuantity.setText(String.valueOf(qty + 1));
//                orderPrice += 250;
//                binding.orderQRPrice.setText(getString(R.string.currency_sign) + orderPrice);
            }else {
                Toast.makeText(this, "You can't add more than 2 items", Toast.LENGTH_SHORT).show();
            }
        });

        CommonLogic.showTestLog(TAG, "vehicle class:- " + vehicleDetails.getVehicle_class() + " " + vehicleDetails.getVehicle_name() + " " + vehicleDetails.getMakers_model());

        if (vehicleDetails != null && CommonMethods.getVehicleType(vehicleDetails.getVehicle_class(), vehicleDetails.getCategory()).equals(TWO_WHEELER)){
            binding.tvQuantity.setText(String.valueOf(1));
        }else {
            binding.tvQuantity.setText(String.valueOf(2));
        }

//        setProductPrice();

    }


    private void checkQR(JsonObject jsonObjectCheckQr) {

        loadingDialog.show();

        ApiCall.callApi(TAG,
                OrderQRPage.this,
                APIData.CHECK_QR_CODE,
                jsonObjectCheckQr, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        CommonLogic.showTestLog(TAG, "showAddVehicleBottomSheet: " + responseBody.toString());
                        try {
                            if (status) {
                                qrItem = APIHelper.convertJsonToModel(responseBody.getJSONObject("data"), QRDataModel.class);
                                CommonLogic.showTestLog(TAG, "QR Data: " + qrItem.get_id());
                            }

                            getPaymentData();
                        } catch (Exception e) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, "QR Data error: " + e);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(MyGarageActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void createQRCode(String paymentId) {
        JsonObject jsonObjectCreateQR = new JsonObject();
        jsonObjectCreateQR.addProperty("unit", 1);


// ✅ Log for debugging
        CommonLogic.showTestLog(TAG, "createQRCode Final JSON to send: " + jsonObjectCreateQR.toString());

        loadingDialog.show();

        ApiCall.callApi(TAG,
                OrderQRPage.this,
                APIData.CREATE_QR_CODE,
                jsonObjectCreateQR, "post",
                new ApiCall.ApiResponseCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, responseBody.toString());


                                if (responseBody.has("data") && responseBody.getJSONArray("data").length() > 0) {
                                    qrItem = APIHelper.convertJsonToModel(responseBody.getJSONArray("data").getJSONObject(0), QRDataModel.class);
                                    CommonLogic.showTestLog(TAG, "QR Data: " + qrItem.toString());

                                    if (qrItem != null) {

                                        assignQR(paymentId);

                                    } else {
                                        Toast.makeText(OrderQRPage.this, "Can't create your Order right now.", Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                } else {
                                    Toast.makeText(OrderQRPage.this, "Can't create your Order right now.", Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();
                                    showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                                }

                            } else {
                                showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                                loadingDialog.dismiss();
                                Toast.makeText(OrderQRPage.this, "Can't create your Order right now.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, e.getMessage());
                            showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                        }

                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void assignQR(String paymentId) {
        if (qrItem != null) {
            JsonObject jsonObjectCreateQR = new JsonObject();

            jsonObjectCreateQR.addProperty("qr_id", qrItem.getQr_id());
            jsonObjectCreateQR.addProperty("assign_to", preferencesManager.getUserId());
            jsonObjectCreateQR.addProperty("assigned_by", "user");
            jsonObjectCreateQR.addProperty("product_type", orderType);
            if (orderType.equalsIgnoreCase("vehicle")) {
                jsonObjectCreateQR.addProperty("vehicle_id", vehicleDetails.getVehicle_id());
            }


// ✅ Log for debugging
            CommonLogic.showTestLog(TAG, "createQRCode Final JSON to send: " + jsonObjectCreateQR.toString());

            loadingDialog.show();

            ApiCall.callApi(TAG,
                    OrderQRPage.this,
                    APIData.ASSIGN_QR_CODE,
                    jsonObjectCreateQR, "post",
                    new ApiCall.ApiResponseCallback() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(JSONObject responseBody, boolean status, String message) {
                            try {
                                if (status) {
                                    CommonLogic.showTestLog(TAG, responseBody.toString());

                                    createOrder(paymentId);

                                } else {
                                    loadingDialog.dismiss();
                                    CommonLogic.showTestLog(TAG, "Failed: " + message);
                                    showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);

                                }
                            } catch (Exception e) {
                                loadingDialog.dismiss();
                                CommonLogic.showTestLog(TAG, e.getMessage());
                                showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                            }

                        }

                        @Override
                        public void onError(String errorMessage) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                        }
                    }
            );
        } else {
            Toast.makeText(this, "Unable to create an order", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterList(String query) {
        addressFilteredList.clear();
        if (query.isEmpty()) {
            addressFilteredList.addAll(addressList);
        } else {
            for (AddressBookModel item : addressList) {
                if (item.getName().startsWith(query) || item.getContact_no().startsWith(query)) {
                    addressFilteredList.add(item);
                }
            }
        }

        if (!bottomSheetDialog.isShowing()) {
            showAddressBottomSheet(this, 0);
        }
    }

    private void getVehicleData(String vehicleId) {

        CommonLogic.showTestLog(TAG, "🚗 getVehicleData() called");
        CommonLogic.showTestLog(TAG, "🔎 Input vehicleId: " + vehicleId);

        if (vehicleId != null && !vehicleId.isEmpty()) {

            loadingDialog.show();
            CommonLogic.showTestLog(TAG, "⏳ Loading dialog shown");

            CommonMethods.getGarageVehicleList(
                    TAG,
                    OrderQRPage.this,
                    preferencesManager.getUserId(),
                    new CommonMethods.GarageListCallback() {

                        @Override
                        public void onSuccess(ArrayList<GarageItemModel> garageList) {

                            CommonLogic.showTestLog(
                                    TAG,
                                    "✅ onSuccess() → Garage list received. Size: "
                                            + (garageList != null ? garageList.size() : 0)
                            );

                            if (garageList == null || garageList.isEmpty()) {
                                CommonLogic.showTestLog(TAG, "⚠️ Garage list is empty");
                                loadingDialog.dismiss();
                                return;
                            }

                            boolean isVehicleFound = false;

                            for (GarageItemModel model : garageList) {

                                CommonLogic.showTestLog(
                                        TAG,
                                        "🔍 Checking vehicleId: " + model.getVehicle_id()
                                );

                                if (vehicleId.equalsIgnoreCase(model.getVehicle_id())) {

                                    vehicleDetails = model;
                                    isVehicleFound = true;

                                    CommonLogic.showTestLog(
                                            TAG,
                                            "🎯 Vehicle MATCH FOUND → "
                                                    + "Name: " + model.getVehicle_name()
                                                    + ", Number: " + model.getVehicle_number()
                                    );

                                    binding.vehicleName.setText(
                                            vehicleDetails.getVehicle_name()
                                                    + ", "
                                                    + vehicleDetails.getVehicle_number()
                                    );

                                    if (vehicleDetails != null && vehicleDetails.getVehicle_class().toLowerCase().contains("2wn")){
                                        binding.tvQuantity.setText(String.valueOf(1));
                                    }else {
                                        binding.tvQuantity.setText(String.valueOf(2));
                                    }

                                    break;
                                }
                            }

                            if (!isVehicleFound) {
                                CommonLogic.showTestLog(
                                        TAG,
                                        "❌ No vehicle found for vehicleId: " + vehicleId
                                );
                            }

                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, "✔ Loading dialog dismissed");
                        }

                        @Override
                        public void onFailure(String errorMessage) {

                            CommonLogic.showTestLog(
                                    TAG,
                                    "🔥 onFailure() → " + errorMessage
                            );

                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, "✔ Loading dialog dismissed");
                        }
                    }
            );

        } else {
            CommonLogic.showTestLog(TAG, "🚫 Invalid or empty vehicleId");
        }
    }



    private void setDeliveryAddress() {
        JsonObject jsonObjectDeliveryAddress = new JsonObject();
        jsonObjectDeliveryAddress.addProperty("user_id", preferencesManager.getUserId());
        jsonObjectDeliveryAddress.addProperty("details_type", "address_book");

        loadingDialog.show();
        ApiClient.getApiService(OrderQRPage.this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_USER_DETAILS, jsonObjectDeliveryAddress)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                            boolean status = Objects.requireNonNull(responseBody).optBoolean("success", false);
                            String message = responseBody.optString("message", "Server error, Please try after some time.");

                            if (status) {
                                try {
                                    JSONArray addressArray = responseBody.getJSONArray("data");

                                    // Clear existing list before adding new data
                                    addressFilteredList.clear();
                                    addressList.clear();

                                    for (int i = 0; i < addressArray.length(); i++) {
                                        JSONObject obj = addressArray.getJSONObject(i);

                                        AddressBookModel model = APIHelper.convertJsonToModel(obj, AddressBookModel.class);

                                        if (model.isDefault_status()) {
                                            selectedPosition = i;
                                            binding.nameNumber.setText(model.getName() + " " + model.getContact_no());
                                            binding.deliveryAddress.setText(model.getHouse_no_building() + ", " + model.getRoad_or_area() + ", " + model.getCity() + ", " + model.getState() + ", " + model.getPincode());

                                            ArrayList<AddressBookModel> tempList = new ArrayList<>();
                                            tempList.add(model);
                                            tempList.addAll(addressFilteredList);

                                            addressFilteredList.clear();
                                            addressFilteredList.addAll(tempList);

                                            addressList.clear();
                                            addressList.addAll(tempList);

                                            setEstimatedTime(model.getPincode());
                                        } else {
                                            addressFilteredList.add(model);
                                            addressList.add(model);
                                        }
                                    }

                                    // ✅ Log or verify
                                    CommonLogic.showTestLog(TAG, "Address List Size: " + addressFilteredList.size());

                                    // You can update your UI here if needed
                                    // e.g., addressAdapter.notifyDataSetChanged();

                                } catch (Exception e) {
                                    CommonLogic.showTestLog(TAG, "Data parse error: " + e.getMessage());
                                }
                            } else {
                                CommonLogic.showTestLog(TAG, "API failed: " + message);
                            }

                            loadingDialog.dismiss();

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "Response error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        CommonLogic.showTestLog(TAG, "onFailure: Updation failed. Please try again.");
                        loadingDialog.dismiss();
                    }
                });
    }

    private void setEstimatedTime(String pincode) {
        JsonObject jsonObjectNotificationList = new JsonObject();

        jsonObjectNotificationList.addProperty("delivery_postcode", pincode);
        jsonObjectNotificationList.addProperty("compareOn", "price");

        if (selectedDeliveryType == R.id.fastDeliveryBtn) {
            jsonObjectNotificationList.addProperty("compareOn", "time");
        }




// ✅ Log for debugging
        CommonLogic.showTestLog(TAG, "📦 Final JSON to send: " + jsonObjectNotificationList.toString());

        loadingDialog.show();

        ApiCall.callApi(TAG,
                OrderQRPage.this,
                APIData.GET_ORDER_ESTIMATED_TIME,
                jsonObjectNotificationList, "post",
                new ApiCall.ApiResponseCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, responseBody.toString());

                                String days = "";
                                String date = "";

                                if (responseBody.has("active_partner")) {
                                    active_partner = responseBody.getString("active_partner");
                                }

                                if (responseBody.has("data") && responseBody.getJSONObject("data").has("freight_charge")) {
                                    orderPrice = responseBody.getJSONObject("data").getInt("freight_charge") + 10;
                                    binding.deliveryCharge.setText("₹" + orderPrice);
                                    binding.totalOrderPrice.setText("₹" + orderPrice);
                                }else {
                                    Toast.makeText(OrderQRPage.this, "Delivery not available in this area", Toast.LENGTH_SHORT).show();
                                }

                                if (responseBody.has("data") && responseBody.getJSONObject("data").has("estimated_delivery_days")) {
                                    days = "In " + responseBody.getJSONObject("data").getString("estimated_delivery_days") + " Days, ";
                                }

                                if (responseBody.has("data") && responseBody.getJSONObject("data").has("etd")) {
                                    date = "Estimated Delivery by " + responseBody.getJSONObject("data").getString("etd") + ".";
                                }

                                if (responseBody.has("data") && responseBody.getJSONObject("data").has("courier_company_id")) {
//                                    courier_company_id = "In " + responseBody.getJSONObject("data").getString("courier_company_id") + " Days, ";
                                    courier_company_id = responseBody.getJSONObject("data").getString("courier_company_id");
                                }

                                if (responseBody.has("data") && responseBody.getJSONObject("data").has("courier_name")) {
//                                    courier_company_id = "In " + responseBody.getJSONObject("data").getString("courier_company_id") + " Days, ";
                                    courier_name = responseBody.getJSONObject("data").getString("courier_name");
                                }

                                binding.estimatedTime.setText(days + date);

                            }else {
                                showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                            }
                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "setEstimatedTime Error: " +  e.getMessage());
                            Toast.makeText(OrderQRPage.this, "Delivery not available in this area", Toast.LENGTH_SHORT).show();
                            showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                        }

                        loadingDialog.dismiss();

                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, "setEstimatedTime onError response: " + errorMessage);
                        showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                        Toast.makeText(OrderQRPage.this, "setEstimatedTimeDelivery not available in this area", Toast.LENGTH_SHORT).show();
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setProductPrice() {
        JsonObject jsonObjectNotificationList = new JsonObject();
        jsonObjectNotificationList.addProperty("category", "physical_qr");

        // --- Step 2: Make API call ---
        ApiClient.getApiService(OrderQRPage.this).getOrderPrice(jsonObjectNotificationList).enqueue(new Callback<JsonObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject orderPriceData = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                        boolean status = orderPriceData.has("status") && orderPriceData.getBoolean("status");
                        JSONObject priceData = orderPriceData.getJSONObject("data");

                        if (status) {
                            try {
                                int price = 0, mrpPrice = 0;
                                if (priceData.has("price") && !priceData.getString("price").isEmpty()) {
                                    price = priceData.getInt("price");
                                }

                                if (priceData.has("MRP") && !priceData.getString("MRP").isEmpty()) {
                                    mrpPrice = priceData.getInt("MRP");
                                }

                                binding.orderQRPrice.setText(getString(R.string.currency_sign) + price);

                                // --- Check if MRP > Price ---
                                if (mrpPrice > price) {
                                    // Show MRP
                                    binding.orderQRMRP.setVisibility(View.VISIBLE);
                                    binding.orderQRMRP.setText(getString(R.string.currency_sign) + mrpPrice);

                                    // --- Calculate Discount Percentage ---
                                    int discountPercentage = (int) (((mrpPrice - price) / (float) mrpPrice) * 100);

                                    // Set Discount Text
                                    binding.orderQRdiscountPrice.setVisibility(View.VISIBLE);
                                    binding.orderQRdiscountPrice.setText(discountPercentage + "% OFF");
                                } else {
                                    // Hide MRP and Discount if there is no discount
                                    binding.orderQRMRP.setVisibility(View.GONE);
                                    binding.orderQRdiscountPrice.setVisibility(View.GONE);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
                        // ❌ Non-200 response
                        JsonObject errorObj = new JsonObject();
                        errorObj.addProperty("status", false);
                        errorObj.addProperty("message", "Registration failed. Please try again.");
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


    @SuppressLint("SetTextI18n")
    public void showAddressBottomSheet(Context context, int position) {

        bottomSheetDialog = new BottomSheetDialog(OrderQRPage.this, R.style.BottomSheetDialogTheme);
        sheetView = LayoutInflater.from(OrderQRPage.this).inflate(R.layout.bottom_sheet_address, null);

        // 👇 Add this line before showing dialog
        bottomSheetDialog.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        bottomSheetDialog.setContentView(sheetView);

        // Close button
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView ivClose = sheetView.findViewById(R.id.ivClose);
        ivClose.setVisibility(View.VISIBLE);
        ivClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // dynamically add address items
        container = sheetView.findViewById(R.id.addressContainer);

        TextView tvAddNew = sheetView.findViewById(R.id.tvAddNew);

        tvAddNew.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent editDeliveryAddress = new Intent(OrderQRPage.this, AddEditDeliveryAddress.class);
            startActivity(editDeliveryAddress);
            bottomSheetDialog.dismiss();
        });

        TextView etSearch = sheetView.findViewById(R.id.etSearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used, but required to override
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Called as the user types
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                // Example: Filter list or trigger search
                filterList(query);
            }
        });

        container.removeAllViews();
        // Create 3 sample address items
        for (int i = 0; i < addressFilteredList.size(); i++) {
            View item = LayoutInflater.from(context).inflate(R.layout.item_address, container, false);

            TextView tvName = item.findViewById(R.id.tvName);
            TextView tvAddress = item.findViewById(R.id.tvAddress);
            TextView tvPhone = item.findViewById(R.id.tvPhone);
            Button btnDeliver = item.findViewById(R.id.btnDeliver);
            ConstraintLayout editBtnLayout = item.findViewById(R.id.editBtnLayout);
            editBtnLayout.setVisibility(View.GONE);
            LinearLayout buttonLayout = item.findViewById(R.id.buttonLayout);
            LinearLayout itemLayout = item.findViewById(R.id.itemLayout);
            RadioButton rbSelect = item.findViewById(R.id.rbSelect);

            // Set sample data
            tvName.setText(addressFilteredList.get(i).getName());
            tvAddress.setText(addressFilteredList.get(i).getHouse_no_building() + " " + addressFilteredList.get(i).getRoad_or_area() + " " +
                    addressFilteredList.get(i).getCity() + " " + addressFilteredList.get(i).getState()
                    + " " + addressFilteredList.get(i).getPincode());
            tvPhone.setText("+91 " + addressFilteredList.get(i).getContact_no());


            // Set initial visibility and checked state
            if (position == i) {
                buttonLayout.setVisibility(View.VISIBLE);
                rbSelect.setChecked(true);
            } else {
                buttonLayout.setVisibility(View.GONE);
                rbSelect.setChecked(false);
            }

            // When clicking anywhere on the item
            int finalI1 = i;
            itemLayout.setOnClickListener(v -> {
                updateSelection(container, finalI1);
            });

            // When clicking only on the RadioButton
            int finalI2 = i;
            rbSelect.setOnClickListener(v -> {
                updateSelection(container, finalI2);
            });

            /*editBtn.setOnClickListener(v -> {
                Intent editDeliveryAddress = new Intent(OrderDetailsPage.this, AddEditDeliveryAddress.class);
                startActivity(editDeliveryAddress);
                bottomSheetDialog.dismiss();
            });*/

            int finalI = i;
            btnDeliver.setOnClickListener(v -> {
                selectedPosition = finalI;
                binding.nameNumber.setText(addressFilteredList.get(selectedPosition).getName() + " " + addressFilteredList.get(selectedPosition).getContact_no());
                binding.deliveryAddress.setText(addressFilteredList.get(selectedPosition).getHouse_no_building() + ", " + addressFilteredList.get(selectedPosition).getRoad_or_area() + ", " +
                        addressFilteredList.get(selectedPosition).getCity() + ", " +
                        addressFilteredList.get(selectedPosition).getState() + ", " + addressFilteredList.get(selectedPosition).getPincode());
                setEstimatedTime(addressFilteredList.get(selectedPosition).getPincode());
                bottomSheetDialog.dismiss();
            });

            container.addView(item);
        }

        bottomSheetDialog.show();
    }

    private void updateSelection(LinearLayout container, int clickedItem) {
        int count = container.getChildCount();

        for (int j = 0; j < count; j++) {
            View child = container.getChildAt(j);

            LinearLayout buttonLayout = child.findViewById(R.id.buttonLayout);
            RadioButton rbSelect = child.findViewById(R.id.rbSelect);

            if (j == clickedItem) {
                buttonLayout.setVisibility(View.VISIBLE);
                rbSelect.setChecked(true);
            } else {
                buttonLayout.setVisibility(View.GONE);
                rbSelect.setChecked(false);
            }
        }
    }


    private void showOrderSuccessDialog() {
        Dialog dialog = new Dialog(this);
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

        btnLogin.setText("Track");
        tvTitle.setText("Order Successful");
        tvMessage.setText("Your QR order is successfully placed.");

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
            // 👉 Redirect to login activity here
            disableHideContentSecureForNextNavigation();
            Intent intent = new Intent(this, OrderListPage.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }


    private void getPaymentData() {
        loadingDialog.show();
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("amount", orderPrice * 100);
        jsonObject.addProperty("user_id", preferencesManager.getUserId());
        /*if (Constants.ENABLE_TESTING) {
            jsonObject.addProperty("status", "test");
        }*/
        jsonObject.addProperty("purpose", "Payment to order QR code");

        CommonLogic.showTestLog(TAG, jsonObject.toString());


        ApiClient.getApiService(this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.RAZORPAY_PAYMENT, jsonObject)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        CommonLogic.showTestLog(TAG, "📬 API onResponse() triggered");

                        JSONObject APIResponse = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                        CommonLogic.showTestLog(TAG, "✅ API Raw Response: " + APIResponse.toString());

                        try {
                            if (APIResponse.has("success") && APIResponse.getBoolean("success")) {
                                makePayment(preferencesManager.getUser().getFirst_name(), String.valueOf(orderPrice * 100), preferencesManager.getUser().getEmail(),
                                        preferencesManager.getUser().getPhone_number(), APIResponse.getJSONObject("order").getString("id"),
                                        APIResponse.getString("razorpay_key_id"), "payment for QR");
                            }else {
                                showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                            }

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "🔥 Exception while parsing response: " + e.getMessage());
                            showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable throwable) {
                        showOrderMaintenanceDialog(OrderQRPage.this, vehicleDetails);
                        CommonLogic.showTestLog(TAG, "💔 API call failed: " + throwable.getMessage());
                        loadingDialog.dismiss();
                    }
                });
    }


    private void makePayment(String name, String amount, String email, String phone, String orderId, String Key, String description) {

        // ✅ CRITICAL FIX (force normal screen)
        Window window = getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // Optional but recommended
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        }

        disableHideContentSecureForNextNavigation(); // safe

        final Checkout co = new Checkout();
        co.setKeyID(Key);

        try {
            JSONObject options = new JSONObject();

            options.put("name", name);
            options.put("description", description);
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", "INR");
            options.put("order_id", orderId);

            JSONObject preFill = new JSONObject();
            preFill.put("email", email);
            preFill.put("contact", phone);

            options.put("prefill", preFill);

            co.open(OrderQRPage.this, options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPaymentSuccess(String paymentId) {

        CommonLogic.showTestLog(TAG, "Payment Success");

        createOrder(paymentId);

        /*if (qrItem != null){
            createOrder(paymentId);
        }else {
            createQRCode(paymentId);
        }*/


    }

    private void createOrder(String paymentId) {
        try {
            CommonLogic.showTestLog(TAG, "create Order");

            loadingDialog.show();

            int sub_total = orderPrice, order_value = orderPrice, declared_value = orderPrice;

            /*JsonObject jsonObjectOrderDetails = new JsonObject();

            jsonObjectOrderDetails.addProperty("user_id", preferencesManager.getUserId());
            jsonObjectOrderDetails.addProperty("order_id", paymentId);
            jsonObjectOrderDetails.addProperty("courier_company_id", courier_company_id);

            jsonObjectOrderDetails.addProperty("sub_total", sub_total);
            jsonObjectOrderDetails.addProperty("order_value", order_value);

            jsonObjectOrderDetails.addProperty("is_prepaid", 1);
            jsonObjectOrderDetails.addProperty("is_return", 0);

            jsonObjectOrderDetails.addProperty("declared_value", declared_value);

            // ✅ pickup_location (full address)
            String pickupLocation = "N/A";
            if (selectedPosition >= 0 && selectedPosition < addressFilteredList.size()) {
                pickupLocation = addressFilteredList.get(selectedPosition).getHouse_no_building() + " "
                        + addressFilteredList.get(selectedPosition).getRoad_or_area() + " "
                        + addressFilteredList.get(selectedPosition).getCity() + " "
                        + addressFilteredList.get(selectedPosition).getState() + " "
                        + addressFilteredList.get(selectedPosition).getPincode();

                jsonObjectOrderDetails.addProperty("pickup_location", pickupLocation.trim());
            } else {
                CommonLogic.showTestLog("OrderDetails", "Invalid selectedPosition: " + selectedPosition);
            }


            // ✅ Shipping (same as billing)
            jsonObjectOrderDetails.addProperty("shipping_is_billing", 1);

            jsonObjectOrderDetails.addProperty("shipping_customer_name", addressFilteredList.get(selectedPosition).getName());
            jsonObjectOrderDetails.addProperty("shipping_last_name", " _ ");
            jsonObjectOrderDetails.addProperty("shipping_phone", preferencesManager.getUser().getPhone_number());
            jsonObjectOrderDetails.addProperty("shipping_address", pickupLocation);
            jsonObjectOrderDetails.addProperty("shipping_address_2", "");
            jsonObjectOrderDetails.addProperty("shipping_city", addressFilteredList.get(selectedPosition).getCity());
            jsonObjectOrderDetails.addProperty("shipping_state", addressFilteredList.get(selectedPosition).getState());
            jsonObjectOrderDetails.addProperty("shipping_country", "India");
            jsonObjectOrderDetails.addProperty("shipping_pincode", addressFilteredList.get(selectedPosition).getPincode());
            jsonObjectOrderDetails.addProperty("shipping_email", preferencesManager.getUser().getEmail());


            // ✅ Billing Details
            jsonObjectOrderDetails.addProperty("billing_customer_name", addressFilteredList.get(selectedPosition).getName());
            jsonObjectOrderDetails.addProperty("billing_last_name", " _ ");
            jsonObjectOrderDetails.addProperty("billing_phone", preferencesManager.getUser().getPhone_number());
            jsonObjectOrderDetails.addProperty("billing_address", pickupLocation);
            jsonObjectOrderDetails.addProperty("billing_address_2", "");
            jsonObjectOrderDetails.addProperty("billing_city", addressFilteredList.get(selectedPosition).getCity());
            jsonObjectOrderDetails.addProperty("billing_state", addressFilteredList.get(selectedPosition).getState());
            jsonObjectOrderDetails.addProperty("billing_country", "India");
            jsonObjectOrderDetails.addProperty("billing_pincode", addressFilteredList.get(selectedPosition).getPincode());
//            jsonObjectOrderDetails.addProperty("billing_email", preferencesManager.getUser().getEmail());


            jsonObjectOrderDetails.addProperty("length", 20);
            jsonObjectOrderDetails.addProperty("breadth", 15);
            jsonObjectOrderDetails.addProperty("height", 10);
            jsonObjectOrderDetails.addProperty("weight", 0.05);

            if (orderType.equalsIgnoreCase("vehicle")) {
                jsonObjectOrderDetails.addProperty("vehicle_id", vehicleDetails.getVehicle_id());
                jsonObjectOrderDetails.addProperty("makers_model", vehicleDetails.getMakers_model());
                jsonObjectOrderDetails.addProperty("makers_name", vehicleDetails.getMakers_name());
                if (vehicleDetails.getVehicle_class().toLowerCase().contains("2wn")){
                    jsonObjectOrderDetails.addProperty("units", 1);
                }else {
                    jsonObjectOrderDetails.addProperty("units", 2);
                }
            }

            jsonObjectOrderDetails.addProperty("order_type", orderType);
            jsonObjectOrderDetails.addProperty("name", qrFor);
//            jsonObjectOrderDetails.addProperty("sku", qrItem.getQr_id());
            jsonObjectOrderDetails.addProperty("sku", "product id: 001");
            jsonObjectOrderDetails.addProperty("selling_price", orderPrice);
            jsonObjectOrderDetails.addProperty("discount", "");
            jsonObjectOrderDetails.addProperty("tax", "");
            jsonObjectOrderDetails.addProperty("selling_price_currency", "INR");*/

            JsonObject jsonObjectOrderDetails = new JsonObject();

            jsonObjectOrderDetails.addProperty("user_id", preferencesManager.getUserId());
            jsonObjectOrderDetails.addProperty("order_id", paymentId);
            jsonObjectOrderDetails.addProperty("active_partner", active_partner);
            jsonObjectOrderDetails.addProperty("payment_method", "Prepaid");
            jsonObjectOrderDetails.addProperty("shipping_mode", "Surface");

            jsonObjectOrderDetails.addProperty("sub_total", sub_total);
            jsonObjectOrderDetails.addProperty("order_value", order_value);
            jsonObjectOrderDetails.addProperty("declared_value", declared_value);

            jsonObjectOrderDetails.addProperty("is_return", false);
            jsonObjectOrderDetails.addProperty("shipping_is_billing", true);

            jsonObjectOrderDetails.addProperty("courier_company_id", courier_company_id);
            jsonObjectOrderDetails.addProperty("courier_name", courier_name);


// ---------------- PICKUP ADDRESS ----------------
            String pickupLocation = "N/A";
            if (selectedPosition >= 0 && selectedPosition < addressFilteredList.size()) {

                pickupLocation = addressFilteredList.get(selectedPosition).getHouse_no_building() + " "
                        + addressFilteredList.get(selectedPosition).getRoad_or_area();

                String city = addressFilteredList.get(selectedPosition).getCity();
                String state = addressFilteredList.get(selectedPosition).getState();
                String pincode = addressFilteredList.get(selectedPosition).getPincode();
                String name = addressFilteredList.get(selectedPosition).getName();

                // ---------------- SHIPPING OBJECT ----------------
                JsonObject shipping = new JsonObject();
                shipping.addProperty("first_name", name);
                shipping.addProperty("last_name", "_");
                shipping.addProperty("phone", preferencesManager.getUser().getPhone_number());
                shipping.addProperty("email", preferencesManager.getUser().getEmail());
                shipping.addProperty("address1", pickupLocation);
                shipping.addProperty("address2", "");
                shipping.addProperty("city", city);
                shipping.addProperty("state", state);
                shipping.addProperty("country", "India");
                shipping.addProperty("pincode", pincode);

                jsonObjectOrderDetails.add("shipping", shipping);


                // ---------------- BILLING OBJECT ----------------
                JsonObject billing = new JsonObject();
                billing.addProperty("first_name", name);
                billing.addProperty("last_name", "_");
                billing.addProperty("phone", preferencesManager.getUser().getPhone_number());
                billing.addProperty("address1", pickupLocation);
                billing.addProperty("address2", "");
                billing.addProperty("city", city);
                billing.addProperty("state", state);
                billing.addProperty("country", "India");
                billing.addProperty("pincode", pincode);

                jsonObjectOrderDetails.add("billing", billing);

            } else {
                CommonLogic.showTestLog("OrderDetails", "Invalid selectedPosition: " + selectedPosition);
            }


// ---------------- PARCEL OBJECT ----------------
            /*JsonObject parcel = new JsonObject();
            parcel.addProperty("length", 8);
            parcel.addProperty("breadth", 9);
            parcel.addProperty("height", 1);
            parcel.addProperty("weight", 0.05);

            jsonObjectOrderDetails.add("parcel", parcel);*/


// ---------------- ORDER ITEMS ARRAY ----------------
            JsonArray orderItemsArray = new JsonArray();
            JsonObject orderItem = new JsonObject();

            if (orderType.equalsIgnoreCase("vehicle")) {

                orderItem.addProperty("vehicle_id", vehicleDetails.getVehicle_id());
                orderItem.addProperty("order_type", "Prepaid");
                orderItem.addProperty("name", qrFor);
                orderItem.addProperty("sku", "QR-001");

                if (vehicleDetails.getVehicle_class().toLowerCase().contains("2wn")) {
                    orderItem.addProperty("units", 1);
                } else {
                    orderItem.addProperty("units", 2);
                }

                orderItem.addProperty("selling_price", orderPrice);
                orderItem.addProperty("selling_price_currency", "INR");
                orderItem.addProperty("weight", 0.05);

            }

            orderItemsArray.add(orderItem);

            jsonObjectOrderDetails.add("order_items", orderItemsArray);


// ✅ Log for debugging
            CommonLogic.showTestLog(TAG, "createOrder Final JSON to send: " + jsonObjectOrderDetails.toString());


            ApiCall.callApi(TAG,
                    OrderQRPage.this,
                    APIData.CREATE_ORDER,
                    jsonObjectOrderDetails, "post",
                    new ApiCall.ApiResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject responseBody, boolean status, String message) {
                            CommonLogic.showTestLog(TAG, "createOrder response : " + responseBody.toString());
                            if (status) {
                                showOrderSuccessDialog();
                                // You can access the data if needed
                                // JSONObject data = responseBody.optJSONObject("data");
                            } else {
                                Toast.makeText(OrderQRPage.this, "Unable to create order.", Toast.LENGTH_SHORT).show();
                            }
                            loadingDialog.dismiss();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, "createOrder onError: " + errorMessage);
                            Toast.makeText(OrderQRPage.this, "Unable to create order.", Toast.LENGTH_SHORT).show();
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
            );


        } catch (Exception e) {
            loadingDialog.dismiss();
            CommonLogic.showTestLog(TAG, "createOrder Error: " +  e.getMessage());
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
//        Constant.showToast(activity, "Error in payment: " + s, 1);
//        Constant.showToast(AccountActivity.this, "Payment has been cancelled or failed ", 1);
//        BaseUrl.transactionFailed(TAG, AccountActivity.this, addedAmount, "Wallet","Transaction failed or Cancelled");
    }

    @SuppressLint("GestureBackNavigation")
    public void showOrderMaintenanceDialog(Activity context, GarageItemModel model) {
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
        tvTitle.setText("Delivery Partner Not Available");

        TextView tvSubTitle = view.findViewById(R.id.tvSubTitle);
        tvSubTitle.setText("Currently, delivery partners are not available in your area.\n" +
                "We’re actively working to bring you a smoother and better experience very soon.\n" +
                "Meanwhile, you can download your virtual QR and continue using our services.\n" +
                "Thank you for your patience ❤\uFE0F");

        Button openLoginPageBtn = view.findViewById(R.id.openProfileBtn);
        openLoginPageBtn.setText("OK");

        openLoginPageBtn.setOnClickListener(v -> {
            if (model != null) {
                disableHideContentSecureForNextNavigation();
                Intent virtualQRPage = new Intent(context, VirtualQR.class);
                virtualQRPage.putExtra("virtualQRDetails", model);
                context.startActivity(virtualQRPage);
                dialog.dismiss();
                context.finishAffinity();
            }else {
                getOnBackPressedDispatcher().onBackPressed();}
        });

        dialog.setCancelable(false);
        dialog.show();
    }
}