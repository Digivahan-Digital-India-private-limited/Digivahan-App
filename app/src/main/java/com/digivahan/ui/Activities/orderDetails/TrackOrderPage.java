package com.digivahan.ui.Activities.orderDetails;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.OrderItemModel;
import com.digivahan.databinding.ActivityTrackOrderPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.TimeUtils;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TrackOrderPage extends BaseActivity {
    String TAG = "TrackOrderPageData";
    ActivityTrackOrderPageBinding binding;

    OrderItemModel orderDetails;

    AshDialog loadingDialog;
    PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackOrderPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Track your order");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> onBackPressed());

        try {
            orderDetails = (OrderItemModel) getIntent().getSerializableExtra("orderDetails");
            if (orderDetails != null && orderDetails.getAwb_code() != null && !orderDetails.getAwb_code().isEmpty() && !orderDetails.getAwb_code().equalsIgnoreCase("NA")
                    && !orderDetails.getAwb_code().equalsIgnoreCase("N/A")){
                binding.webView.setVisibility(View.VISIBLE);
                setupWebView();
                binding.webView.loadUrl("https://shiprocket.co/tracking/" + orderDetails.getAwb_code());
            }else {
                binding.orderDetailsPage.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            binding.orderDetailsPage.setVisibility(View.VISIBLE);
            CommonLogic.showTestLog(TAG, e.getMessage());
        }
        if (orderDetails != null) {
            if (String.valueOf(orderDetails.getShip_rocket_order_id()) != null && !String.valueOf(orderDetails.getShip_rocket_order_id()).isEmpty()
                    && !String.valueOf(orderDetails.getShip_rocket_order_id()).equalsIgnoreCase("0")) {
                binding.orderId.setText(String.valueOf(Objects.requireNonNull(orderDetails).getShip_rocket_order_id()));
            }else {
                binding.orderId.setText(String.valueOf(orderDetails.getOrderId()));
            }

            String orderStatus = orderDetails.getOrder_status();
            if (orderDetails.getShip_rocket_status() != null && !orderDetails.getShip_rocket_status().isEmpty()) {
                orderStatus = orderDetails.getShip_rocket_status();
            }

            binding.orderStatus.setText(orderStatus);
            binding.deliveryAddress.setText(orderDetails.getBilling_address());
            binding.receiverName.setText(orderDetails.getBilling_customer_name());
            binding.orderDate.setText(TimeUtils.convertDateFormat(orderDetails.getOrder_date(), "dd MMM yyyy"));

            if (orderStatus == null || orderStatus.equalsIgnoreCase("CANCELED")){
                binding.stageContainer.setVisibility(View.GONE);
                binding.orderTrackChartLayout.setVisibility(View.GONE);
            }else if (orderDetails.getShip_rocket_status().equalsIgnoreCase("NEW") ||
                    orderDetails.getShip_rocket_status().equalsIgnoreCase("PENDING") ||
                    orderStatus.equalsIgnoreCase("NEW") ||
                    orderStatus.equalsIgnoreCase("PENDING")){
                binding.orderTrackChartLayout.setVisibility(View.VISIBLE);
                updateDeliveryStages(1);
            }

            if (orderDetails.isIs_prepared()){
                binding.orderPrintingStatus.setText("Order printed successfully");
                getSetOrderData();
            }
        }

        loadingDialog = new AshDialog(TrackOrderPage.this, "Please wait", "");

        preferencesManager = new PreferencesManager(TrackOrderPage.this);

    }

    private void setupWebView() {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.getSettings().setAllowFileAccess(false);
        binding.webView.getSettings().setAllowContentAccess(false);

        binding.webView.setWebChromeClient(new WebChromeClient());

        binding.webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            @Override
            public void onPageFinished(WebView view, String url) {

            }

            @Override
            public void onReceivedError(WebView view,
                                        WebResourceRequest request,
                                        WebResourceError error) {

            }
        });
    }

    private void updateDeliveryStages(int completedStages) {
        int totalStages = binding.stageContainer.getChildCount(); // Total stage layouts (LinearLayouts)

        for (int i = 0; i < totalStages; i++) {
            // Each stage container (LinearLayout with FrameLayout + TextView)
            View stageContainer = binding.stageContainer.getChildAt(i);

            if (stageContainer instanceof LinearLayout) {
                LinearLayout stageLayout = (LinearLayout) stageContainer;

                // FrameLayout is the first child inside this LinearLayout
                FrameLayout frameLayout = (FrameLayout) stageLayout.getChildAt(0);

                if (frameLayout != null && frameLayout.getChildCount() >= 2) {
                    View lineView = frameLayout.getChildAt(0);   // Line at index 0
                    View circleView = frameLayout.getChildAt(1); // Circle at index 1

                    // TextView for label (second child of LinearLayout)
                    TextView labelView = (TextView) stageLayout.getChildAt(1);

                    // --- Update Circle ---
                    if (i < completedStages) {
                        circleView.setBackgroundResource(R.drawable.circle_green);
                        Log.d("StageUpdate", "Circle " + i + " set to GREEN");
                    } else {
                        circleView.setBackgroundResource(R.drawable.circle_pending);
                        Log.d("StageUpdate", "Circle " + i + " set to PENDING");
                    }

                    // --- Update Line ---
                    // If current stage is completed or before completed stage
                    // Example: completedStages = 2 → lines for Stage 0 and Stage 1 are GREEN
                    if (lineView != null) {
                        if (i < completedStages) {
                            lineView.setBackgroundResource(R.drawable.line_green);
                            Log.d("StageUpdate", "Line " + i + " set to GREEN");
                        } else {
                            lineView.setBackgroundResource(R.drawable.line_pending);
                            Log.d("StageUpdate", "Line " + i + " set to PENDING");
                        }
                    }

                    // --- Update Text Label ---
                    if (labelView != null) {
                        if (i < completedStages) {
                            labelView.setTextColor(ContextCompat.getColor(this, R.color.secondary_button));
                            labelView.setTypeface(null, Typeface.BOLD); // Highlight completed stage
                            Log.d("StageUpdate", "Label " + i + " set to GREEN & BOLD");
                        } else {
                            labelView.setTextColor(ContextCompat.getColor(this, R.color.Unselected_Button_1));
                            labelView.setTypeface(null, Typeface.NORMAL);
                            Log.d("StageUpdate", "Label " + i + " set to GRAY & NORMAL");
                        }
                    }
                }
            }
        }
    }



    private void getSetOrderData() {
        loadingDialog.show();
        JsonObject jsonObjectNotificationList = new JsonObject();

        jsonObjectNotificationList.addProperty("user_id", preferencesManager.getUserId());
        jsonObjectNotificationList.addProperty("order_id", orderDetails.getOrderId());
        jsonObjectNotificationList.addProperty("shiprocket_orderId", orderDetails.getShip_rocket_order_id());


// ✅ Log for debugging
        CommonLogic.showTestLog(TAG, "📦 Final JSON to send: " + jsonObjectNotificationList.toString());

        ApiCall.callApi(TAG,
                TrackOrderPage.this,
                APIData.TRACK_ORDER,
                jsonObjectNotificationList, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, responseBody.toString());

                                bindTrackingData(responseBody);

                                // You can access the data if needed
                                // JSONObject data = responseBody.optJSONObject("data");
                            } else {
                                Toast.makeText(TrackOrderPage.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
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


    private void bindTrackingData(JSONObject response) {

        CommonLogic.showTestLog(TAG, "📦 bindTrackingData() called");

        if (response == null) {
            CommonLogic.showTestLog(TAG, "❌ Response is NULL");
            return;
        }

        CommonLogic.showTestLog(TAG, "📥 Full Response: " + response.toString());

        JSONObject tracking = response.optJSONObject("tracking");
        if (tracking == null) {
            CommonLogic.showTestLog(TAG, "❌ 'tracking' object is NULL");
            return;
        }

        JSONObject data = tracking.optJSONObject("data");
        if (data == null) {
            CommonLogic.showTestLog(TAG, "❌ 'tracking.data' object is NULL");
            return;
        }

        // Optional: change background based on status
        /*if ("CANCELED".equalsIgnoreCase(status)) {
            binding.orderStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
        } else {
            binding.orderStatus.setBackgroundResource(R.drawable.button_bg2);
        }*/

        CommonLogic.showTestLog(TAG, "📊 Tracking Data: " + data.toString());

        /* ---------------- ORDER ID ---------------- */
        int orderId = data.optInt("id", 0);
        CommonLogic.showTestLog(TAG, "🆔 Order ID: " + orderId);
        binding.orderId.setText(String.valueOf(orderId));

        /* ---------------- STATUS ---------------- */
        String status = data.optString("status", "N/A");
        CommonLogic.showTestLog(TAG, "📌 Order Status: " + status);
        binding.orderStatus.setText(status);

        /* ---------------- DELIVERY ADDRESS ---------------- */
        String city = data.optString("customer_city", "N/A");
        String pincode = data.optString("customer_pincode", "N/A");
        String deliveryAddress = city + ", " + pincode;

        CommonLogic.showTestLog(TAG, "📍 Delivery Address: " + deliveryAddress);
        binding.deliveryAddress.setText(deliveryAddress);

        /* ---------------- CREATED DATE ---------------- */
        String orderDate = data.optString("order_date", "N/A");
        CommonLogic.showTestLog(TAG, "📅 Order Date: " + orderDate);
        binding.orderDate.setText(orderDate);

        /* ---------------- ESTIMATED DATE ---------------- */
        String sla = data.optString("sla", "");
        CommonLogic.showTestLog(TAG, "⏳ SLA / Estimated: " + sla);

        if (!sla.isEmpty()) {
            binding.estimatedData.setText(sla);
        } else {
            binding.estimatedDataLayout.setVisibility(View.GONE);
        }

        /* ---------------- RECEIVER ---------------- */
        String receiver = data.optString("customer_name", "N/A");
        CommonLogic.showTestLog(TAG, "👤 Receiver Name: " + receiver);
        binding.receiverName.setText(receiver);

        /* ---------------- STATUS CODE ---------------- */
        int statusCode = data.optInt("status_code", 0);
        CommonLogic.showTestLog(TAG, "🔢 Status Code: " + statusCode);
        updateStageUI(statusCode);

        /* ---------------- TIMELINE ---------------- */
        bindTimeline(data);
    }



    private void updateStageUI(int statusCode) {

        CommonLogic.showTestLog(TAG, "🎯 updateStageUI() called with statusCode: " + statusCode);

        binding.tvStage1.setTextColor(getColor(R.color.black));
        binding.tvStage2.setTextColor(getColor(R.color.black));
        binding.tvStage3.setTextColor(getColor(R.color.black));
        binding.tvStage4.setTextColor(getColor(R.color.black));

        if (statusCode >= 1) {
            CommonLogic.showTestLog(TAG, "✔ Stage 1 (Pending) completed");
            binding.tvStage1.setTextColor(getColor(R.color.primary_button));
        }
        if (statusCode >= 2) {
            CommonLogic.showTestLog(TAG, "✔ Stage 2 (Processing) completed");
            binding.tvStage2.setTextColor(getColor(R.color.primary_button));
        }
        if (statusCode >= 3) {
            CommonLogic.showTestLog(TAG, "✔ Stage 3 (Shipped) completed");
            binding.tvStage3.setTextColor(getColor(R.color.primary_button));
        }
        if (statusCode >= 6) {
            CommonLogic.showTestLog(TAG, "✔ Stage 4 (Delivered) completed");
            binding.tvStage4.setTextColor(getColor(R.color.primary_button));
        }
    }


    private void bindTimeline(JSONObject data) {

        CommonLogic.showTestLog(TAG, "🧾 bindTimeline() called");

        /* -------- Order Placed -------- */
        String createdAt = data.optString("created_at", "N/A");
        CommonLogic.showTestLog(TAG, "🕒 Order Placed At: " + createdAt);

        binding.tvStepDateTime.setText(createdAt);
        binding.tvStepLocation.setText("At Digivahan");

        showOrHideStep(binding.layoutOrderPlaced, createdAt);

        /* -------- Order Processed -------- */
        String processedDate = data.optString("channel_created_at", "N/A");
        CommonLogic.showTestLog(TAG, "🖨 Order Processed At (API): " + processedDate);

        binding.orderProcessedData.setText(processedDate);
        showOrHideStep(binding.layoutOrderProcessed, processedDate);

        /*if (processedDate.isEmpty() && !createdAt.equals("N/A")) {

            // 🔁 Fallback: Created date + 1 day
            processedDate = addDaysToDate(createdAt, 1);

            CommonLogic.showTestLog(
                    TAG,
                    "🔁 Processed date missing → using fallback (Created + 1 day): "
                            + processedDate
            );

            binding.orderProcessedData.setText(processedDate);

        } else if (!processedDate.isEmpty()) {

            binding.orderProcessedData.setText(processedDate);

        } else {
            CommonLogic.showTestLog(TAG, "⚠ Both createdAt & processedDate are invalid");
            binding.orderProcessedData.setText("N/A");
        }*/

        /* -------- Order Shipped -------- */
        JSONObject shipments = data.optJSONObject("shipments");
        String shippedDate = "";
        if (shipments != null) {

            shippedDate = shipments.optString("shipped_date", "N/A");
            CommonLogic.showTestLog(TAG, "🚚 Shipped Date: " + shippedDate);

          /*  if (shippedDate.isEmpty() && !shippedDate.equals("N/A")) {
                shippedDate = addDaysToDate(processedDate, 1);
                CommonLogic.showTestLog(TAG, "⚠ Shipped date missing");
            }*/

            binding.orderShippedData.setText(shippedDate);
            showOrHideStep(binding.layoutOrderShipped, shippedDate);

            /* -------- Ready to pick -------- */
            binding.orderReadyPickUpData.setText(shippedDate);
            showOrHideStep(binding.layoutOrderReadyPickup, shippedDate);

        } else {
            CommonLogic.showTestLog(TAG, "⚠ Shipments object is NULL");
        }




        /* -------- Out for Delivery -------- */
        String ofdDate = data.optString("out_for_delivery_date", "N/A");
        CommonLogic.showTestLog(TAG, "📦 Out for Delivery Date: " + ofdDate);

        /*if (ofdDate.isEmpty() && !shippedDate.equals("N/A")) {
            ofdDate = addDaysToDate(shippedDate, 1);
            CommonLogic.showTestLog(TAG, "⚠ OFD date missing");
        }*/

        binding.orderOutForDeliveryData.setText(ofdDate.isEmpty() || ofdDate.equalsIgnoreCase("null") ? "N/A" : ofdDate);

        showOrHideStep(binding.layoutOrderOFD, ofdDate);

        /* -------- Delivered -------- */
        String deliveredDate = data.optString("delivered_date", "N/A");
        CommonLogic.showTestLog(TAG, "🏁 Delivered Date: " + deliveredDate);

        /*if (deliveredDate.isEmpty()) {
//            deliveredDate = addDaysToDate(processedDate, 1);
            CommonLogic.showTestLog(TAG, "⚠ Delivered date missing");
        }*/

        binding.orderDeliveredData.setText(deliveredDate.isEmpty() || deliveredDate.equalsIgnoreCase("null") ? "N/A" : deliveredDate);
        showOrHideStep(binding.layoutOrderDelivered, deliveredDate);
    }

    private void showOrHideStep(View layout, String dateValue) {

        if (dateValue == null ||
                dateValue.trim().isEmpty() ||
                dateValue.equalsIgnoreCase("N/A") ||
                dateValue.equalsIgnoreCase("null")) {

            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
        }
    }





    private String optString(JSONObject obj, String key) {
        return obj != null ? obj.optString(key, "N/A") : "N/A";
    }

    private JSONObject optObject(JSONObject obj, String key) {
        return obj != null ? obj.optJSONObject(key) : null;
    }


    private String addDaysToDate(String inputDate, int daysToAdd) {
        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);

            Date date = inputFormat.parse(inputDate);
            if (date == null) return "N/A";

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, daysToAdd);

            return inputFormat.format(cal.getTime());

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, "❌ Date parse error: " + e.getMessage());
            return "N/A";
        }
    }



}