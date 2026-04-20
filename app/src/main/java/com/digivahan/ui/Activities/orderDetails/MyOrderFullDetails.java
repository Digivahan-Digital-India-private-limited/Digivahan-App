package com.digivahan.ui.Activities.orderDetails;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.model.OrderItemModel;
import com.digivahan.databinding.ActivityMyOrderFullDetailsBinding;
import com.digivahan.ui.Activities.review.ShareReviewPage;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.TimeUtils;

public class MyOrderFullDetails extends BaseActivity {

    String TAG = "MyOrderFullDetailsData";
    ActivityMyOrderFullDetailsBinding binding;

    OrderItemModel orderDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrderFullDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Your Orders");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        try {
            orderDetails = (OrderItemModel) getIntent().getSerializableExtra("orderDetails");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        if (orderDetails != null){
            binding.orderId.setText(String.valueOf(orderDetails.getShip_rocket_order_id()));
            binding.orderPlaced.setText(TimeUtils.convertDateFormat(orderDetails.getOrder_date(), "dd MMM yyyy"));
            binding.orderFor.setText("QR Code Vehicle Sticker For "+ orderDetails.getName());
            binding.orderPrice.setText(getString(R.string.currency_sign) + orderDetails.getSelling_price());
            binding.paymentMethod.setText(orderDetails.getPayment_method());
            binding.shippingAddress.setText(orderDetails.getShipping_address());
            binding.price.setText(getString(R.string.currency_sign) + orderDetails.getSelling_price());
            binding.total.setText(getString(R.string.currency_sign) + (orderDetails.getSelling_price() * orderDetails.getUnits()));
            binding.grandTotal.setText(getString(R.string.currency_sign) + (orderDetails.getSelling_price() * orderDetails.getUnits()));
        }

        binding.orderNewQR.setOnClickListener(view -> {
            disableHideContentSecureForNextNavigation();
            Intent orderDetailsPage = new Intent(MyOrderFullDetails.this, OrderQRPage.class);
            orderDetailsPage.putExtra("qr_id", orderDetails.getSku());
            String orderType = "";
            if (orderDetails.getVehicle_id() != null && !orderDetails.getOrder_type().isEmpty()){
                orderType = "vehicle";
            }
            orderDetailsPage.putExtra("orderType", orderType);
            startActivity(orderDetailsPage);
        });

        binding.reviewBtn.setOnClickListener(view -> {
            disableHideContentSecureForNextNavigation();
            Intent reviewPage = new Intent(MyOrderFullDetails.this, ShareReviewPage.class);
            reviewPage.putExtra("orderDetails", orderDetails);
            startActivity(reviewPage);
        });

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }
}