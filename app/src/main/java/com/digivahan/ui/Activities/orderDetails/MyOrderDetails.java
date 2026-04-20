package com.digivahan.ui.Activities.orderDetails;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ashu.ashuutils.APIHelper;
import com.digivahan.data.api.ApiClient;
import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.adapters.OtherServicesAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.OrderItemModel;
import com.digivahan.data.model.OtherServicesModel;
import com.digivahan.databinding.ActivityMyOrderDetailsBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.invoice.DownloadInvoice;
import com.digivahan.ui.Activities.notification.ViewNotification;
import com.digivahan.ui.Activities.qr.ByNewQR;
import com.digivahan.ui.Activities.review.ShareReviewPage;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.TimeUtils;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyOrderDetails extends BaseActivity {
    String TAG = "MyOrderDetailsData";
    ActivityMyOrderDetailsBinding binding;

    ArrayList<OtherServicesModel> list = new ArrayList<>();

    OrderItemModel orderDetails;

    AshDialog loadingDialog;
    PreferencesManager preferencesManager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("My Order");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        try {
            orderDetails = (OrderItemModel) getIntent().getSerializableExtra("orderDetails");
            CommonLogic.showTestLog(TAG, "vehicleId: " + String.valueOf(Objects.requireNonNull(orderDetails).getVehicle_id()));

            if (!orderDetails.isIs_prepared() && (orderDetails.getOrder_status() != null && !orderDetails.getOrder_status().isEmpty() && !orderDetails.getOrder_status().equalsIgnoreCase("CANCELED"))){
                binding.cancelOrder.setVisibility(View.VISIBLE);
            }
            else {
                binding.cancelOrder.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        loadingDialog = new AshDialog(MyOrderDetails.this, "Please wait", "");

        preferencesManager = new PreferencesManager(MyOrderDetails.this);

        binding.orderedOn.setText( "Ordered On " +TimeUtils.convertDateFormat(Objects.requireNonNull(orderDetails).getOrder_date(), "dd MMM yyyy"));
        binding.orderFor.setText( Objects.requireNonNull(orderDetails).getName() + " On " +TimeUtils.convertDateFormat(Objects.requireNonNull(orderDetails).getOrder_date(), "dd MMM yyyy hh:mm a"));

        if (String.valueOf(orderDetails.getShip_rocket_order_id()) != null && !String.valueOf(orderDetails.getShip_rocket_order_id()).isEmpty()
                && !String.valueOf(orderDetails.getShip_rocket_order_id()).equalsIgnoreCase("0")) {
            binding.orderId.setText("Order ID: " + Objects.requireNonNull(orderDetails).getShip_rocket_order_id());
        }

        String orderStatus = orderDetails.getOrder_status();
        if (orderDetails.getShip_rocket_status() != null && !orderDetails.getShip_rocket_status().isEmpty()) {
            orderStatus = orderDetails.getShip_rocket_status();
        }
        binding.orderStatus.setText("Status : " + orderStatus);

        binding.deliveryAddress.setText("Delivery Address: " + orderDetails.getShipping_address());

        OtherServicesModel model = new OtherServicesModel("Vehicle Information", R.drawable.vehicle_info_icon);
        list.add(model);

        OtherServicesModel model1 = new OtherServicesModel("Check Challan", R.drawable.vehicle_info_icon);
        list.add(model1);

        OtherServicesModel model2 = new OtherServicesModel("Service History", R.drawable.services_icon);
        list.add(model2);

        OtherServicesAdapter otherServicesAdapter = new OtherServicesAdapter(MyOrderDetails.this, list);
        binding.rvServices.setAdapter(otherServicesAdapter);


        binding.trackOrder.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent trackOrder = new Intent(MyOrderDetails.this, TrackOrderPage.class);
            trackOrder.putExtra("orderDetails", orderDetails);
            startActivity(trackOrder);
        });

        binding.buyNewQR.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent orderDetailsPage = new Intent(MyOrderDetails.this, OrderQRPage.class);
            orderDetailsPage.putExtra("qr_id", orderDetails.getSku());
            String orderType = "";
            if (orderDetails.getVehicle_id() != null && !orderDetails.getOrder_type().isEmpty()){
                orderType = "vehicle";
            }
            orderDetailsPage.putExtra("orderType", orderType);
            if (orderDetails.getOrder_type().equalsIgnoreCase("vehicle")){
                orderDetailsPage.putExtra("vehicleId", orderDetails.getVehicle_id());
                orderDetailsPage.putExtra("qrFor", orderDetails.getName());
            }
            startActivity(orderDetailsPage);
        });

        binding.shareApp.setOnClickListener(view -> {
            CommonLogic.shareAppWithImage(MyOrderDetails.this, loadingDialog);
        });

        binding.byForOther.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent byNewQR = new Intent(MyOrderDetails.this, ByNewQR.class);
            startActivity(byNewQR);
        });


        binding.reviewBtn.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent reviewPage = new Intent(MyOrderDetails.this, ShareReviewPage.class);
            reviewPage.putExtra("orderDetails", orderDetails);
            startActivity(reviewPage);
        });


        binding.orderDetails.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent myOrderFullDetailsPage = new Intent(MyOrderDetails.this, MyOrderFullDetails.class);
            myOrderFullDetailsPage.putExtra("orderDetails", orderDetails);
            startActivity(myOrderFullDetailsPage);
        });

        binding.downloadInvoice.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent downloadInvoicePage = new Intent(MyOrderDetails.this, DownloadInvoice.class);
            downloadInvoicePage.putExtra("orderDetails", orderDetails);
            startActivity(downloadInvoicePage);
        });

        binding.cancelOrder.setOnClickListener(view -> {
            new AlertDialog.Builder(MyOrderDetails.this)
                    .setTitle("Cancel Order")
                    .setMessage("Are you sure? You want to cancel it.")
                    .setCancelable(false)
                    .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelOrder())
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();

        });
    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    private void cancelOrder() {
        loadingDialog.show();
        JsonObject jsonObjectNotificationList = new JsonObject();

        jsonObjectNotificationList.addProperty("order_id", orderDetails.getOrderId());
        jsonObjectNotificationList.addProperty("user_id", preferencesManager.getUserId());


// ✅ Log for debugging
        CommonLogic.showTestLog(TAG, "📦 Final JSON to send: " + jsonObjectNotificationList.toString());

        ApiCall.callApi(TAG,
                MyOrderDetails.this,
                APIData.CANCEL_ORDER,
                jsonObjectNotificationList, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, responseBody.toString());

                                Toast.makeText(MyOrderDetails.this, "Your order has been cancelled", Toast.LENGTH_SHORT).show();
                                binding.cancelOrder.setVisibility(View.GONE);
                                orderDetails.setOrder_status("CANCELED");
                                orderDetails.setShip_rocket_status("CANCELED");
                                binding.orderStatus.setText("Status : CANCELED");

                                // You can access the data if needed
                                // JSONObject data = responseBody.optJSONObject("data");
                            } else {
                                Toast.makeText(MyOrderDetails.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }

                        loadingDialog.dismiss();

                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

}