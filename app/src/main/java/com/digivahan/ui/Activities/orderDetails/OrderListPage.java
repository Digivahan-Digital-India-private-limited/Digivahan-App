package com.digivahan.ui.Activities.orderDetails;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.digivahan.R;
import com.digivahan.data.adapters.OrderListItemAdapter;
import com.digivahan.data.adapters.StatusAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.OrderItemModel;
import com.digivahan.databinding.ActivityOrderListPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderListPage extends BaseActivity {
    String TAG = "OrderListPageData";

    ActivityOrderListPageBinding binding;
    private ArrayList<String> statusList = new ArrayList<>();
    ArrayList<OrderItemModel> orderList = new ArrayList<>();
    ArrayList<OrderItemModel> filteredOrderList = new ArrayList<>();
    private int selectedStatusPosition = 0;

    PreferencesManager preferencesManager;

    AshDialog loadingDialog;

    OrderListItemAdapter orderListItemAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        getOrderList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderListPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("My Orders");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            back();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        loadingDialog = new AshDialog(OrderListPage.this, "Please wait", "");

        preferencesManager = new PreferencesManager(OrderListPage.this);


        // Initialize status list
//        statusList.add("Pending");
//        statusList.add("Progress");
//        statusList.add("Shipped");
//        statusList.add("Completed");

        statusList.add("ALL");
        statusList.add("NEW");
        statusList.add("PENDING");
        statusList.add("CONFIRMED");
        statusList.add("CANCELED");

// Set LayoutManager
        binding.statusRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

// Set Adapter
        StatusAdapter adapter = new StatusAdapter(statusList, 0, (position, status) -> {
            // Handle selected status here
//            Toast.makeText(this, "Selected: " + status, Toast.LENGTH_SHORT).show();
            selectedStatusPosition = position;
            filterOrderList();
        });

        binding.statusRecyclerView.setAdapter(adapter);


        orderListItemAdapter = new OrderListItemAdapter(OrderListPage.this, filteredOrderList);
        binding.orderRecyclerView.setAdapter(orderListItemAdapter);

        CommonMethods.showProfileUpdateDialog(TAG, OrderListPage.this, true);

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    private void filterOrderList(){
        filteredOrderList.clear();
        for (OrderItemModel model : orderList){

            String orderStatus = model.getOrder_status();
            if (model.getShip_rocket_status() != null && !model.getShip_rocket_status().isEmpty()) {
                orderStatus = model.getShip_rocket_status();
            }

            if (statusList.get(selectedStatusPosition).equalsIgnoreCase("ALL") || orderStatus.equalsIgnoreCase(statusList.get(selectedStatusPosition))){
                filteredOrderList.add(model);
            }
        }

        if (filteredOrderList.isEmpty()){
            if (orderList.isEmpty()){
                binding.emptyLayout.setVisibility(View.VISIBLE);
                binding.rvLayout.setVisibility(View.GONE);
            }else {
                binding.emptyLayout.setVisibility(View.VISIBLE);
                binding.rvLayout.setVisibility(View.VISIBLE);
                binding.orderRecyclerView.setVisibility(View.GONE);
            }
        }else {
            binding.emptyLayout.setVisibility(View.GONE);
            binding.rvLayout.setVisibility(View.VISIBLE);
            binding.orderRecyclerView.setVisibility(View.VISIBLE);
        }

        orderListItemAdapter.notifyDataSetChanged();
    }

    private void getOrderList(){
        loadingDialog.show();
        JsonObject jsonObjectNotificationList = new JsonObject();

        jsonObjectNotificationList.addProperty("user_id", preferencesManager.getUserId());


// ✅ Log for debugging
        CommonLogic.showTestLog(TAG, "📦 Final JSON to send: " + jsonObjectNotificationList.toString());

        ApiCall.callApi(TAG,
                OrderListPage.this,
                APIData.GET_ORDER_LIST,
                jsonObjectNotificationList, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, responseBody.toString());

                                orderList.clear();

                                JSONArray ordersList = responseBody.getJSONArray("orders");

                                for (int i = 0; i < ordersList.length(); i++) {

                                    JSONObject orderData = ordersList.optJSONObject(i);

                                    if (orderData == null) return;

                                    OrderItemModel model = new OrderItemModel();

// 🔹 Parent level
                                    model.setOrderId(orderData.optString("_id", ""));
                                    model.setCreatedAt(orderData.optString("createdAt", ""));
                                    model.setUpdatedAt(orderData.optString("updatedAt", ""));

// 🔹 Order data
                                    model.setPayment_order_id(orderData.optString("order_id", ""));
                                    model.setOrder_date(orderData.optString("order_date", ""));
                                    model.setOrder_status(orderData.optString("order_status", ""));
                                    model.setActive_partner(orderData.optString("active_partner", ""));
                                    model.setIs_prepared(orderData.optBoolean("is_prepared", false));
                                    model.setSub_total(orderData.optInt("sub_total", 0));
                                    model.setOrder_value(orderData.optInt("order_value", 0));
                                    model.setPayment_method(orderData.optString("payment_method", ""));
                                    model.setIs_prepaid(orderData.optInt("is_prepaid", 0));
                                    model.setShipping_is_billing(orderData.optInt("shipping_is_billing", 0));
                                    model.setIs_return(orderData.optInt("is_return", 0));
                                    model.setDeclared_value(orderData.optInt("declared_value", 0));

// 🔹 Shipping info
                                    model.setShipping_customer_name(orderData.optString("shipping_customer_name", ""));
                                    model.setShipping_last_name(orderData.optString("shipping_last_name", ""));
                                    model.setShipping_phone(orderData.optString("shipping_phone", ""));
                                    model.setShipping_address(orderData.optString("shipping_address", ""));
                                    model.setShipping_address_2(orderData.optString("shipping_address_2", ""));
                                    model.setShipping_city(orderData.optString("shipping_city", ""));
                                    model.setShipping_state(orderData.optString("shipping_state", ""));
                                    model.setShipping_country(orderData.optString("shipping_country", ""));
                                    model.setShipping_pincode(orderData.optString("shipping_pincode", ""));
                                    model.setShipping_email(orderData.optString("shipping_email", ""));

// 🔹 Product size
                                    model.setLength(orderData.optInt("length", 0));
                                    model.setBreadth(orderData.optInt("breadth", 0));
                                    model.setHeight(orderData.optInt("height", 0));
                                    model.setWeight(orderData.optDouble("weight", 0.0));

// 🔹 Order items array
                                    JSONArray orderItemsArrayObj = orderData.optJSONArray("order_items");

                                    if (orderItemsArrayObj != null && orderItemsArrayObj.length() > 0) {

                                        JSONObject itemObj = orderItemsArrayObj.optJSONObject(0);

                                        if (itemObj != null) {

                                            model.setVehicle_id(itemObj.optString("vehicle_id", ""));
                                            model.setOrder_type(itemObj.optString("order_type", ""));
                                            model.setName(itemObj.optString("name", ""));
                                            model.setSku(itemObj.optString("sku", ""));
                                            model.setUnits(itemObj.optInt("units", 0));
                                            model.setSelling_price(itemObj.optInt("selling_price", 0));
                                            model.setSelling_price_currency(itemObj.optString("selling_price_currency", ""));
                                            model.setDiscount(itemObj.optString("discount", ""));
                                            model.setTax(itemObj.optString("tax", ""));
                                        }
                                    }

// 🔹 Shiprocket object
                                    JSONObject shipRocketObj = orderData.optJSONObject("ship_rocket");

                                    if (shipRocketObj != null) {

                                            model.setShip_rocket_order_id(shipRocketObj.optInt("order_id", 0));
                                        model.setShip_rocket_shipment_id(shipRocketObj.optInt("shipment_id", 0));
                                        model.setShip_rocket_status(shipRocketObj.optString("status", ""));
                                        model.setStatus_code(shipRocketObj.optInt("status_code", 0));
                                        model.setOnboarding_completed_now(shipRocketObj.optInt("onboarding_completed_now", 0));
                                        model.setAwb_code(shipRocketObj.optString("awb_code", ""));
                                        model.setCourier_company_id(shipRocketObj.optString("courier_company_id", ""));
                                        model.setCourier_name(shipRocketObj.optString("courier_name", ""));
                                        model.setNew_channel(shipRocketObj.optBoolean("new_channel", false));
                                    }

                                    // ✅ Add to list
                                    orderList.add(model);
                                }

                                filterOrderList();

                                // You can access the data if needed
                                // JSONObject data = responseBody.optJSONObject("data");
                            } else {
                                Toast.makeText(OrderListPage.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
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