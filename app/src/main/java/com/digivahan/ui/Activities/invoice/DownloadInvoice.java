package com.digivahan.ui.Activities.invoice;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.model.OrderItemModel;
import com.digivahan.databinding.ActivityDownloadInvoiceBinding;
import com.digivahan.ui.Activities.garage.VehicleInformation;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.TimeUtils;

public class DownloadInvoice extends BaseActivity {
    String TAG = "DownloadInvoiceData";
    ActivityDownloadInvoiceBinding binding;
    OrderItemModel orderDetails;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadInvoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        getOnBackPressedDispatcher().addCallback(DownloadInvoice.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Reminder & Renew");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        try {
            orderDetails = (OrderItemModel) getIntent().getSerializableExtra("orderDetails");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        if (orderDetails != null){
            binding.invoiceTitle.setText("Final Details for Order " + orderDetails.getShip_rocket_status());
            binding.orderId.setText(orderDetails.getShip_rocket_status());
            binding.orderPlaced.setText(TimeUtils.convertDateFormat(orderDetails.getOrder_date(), "dd MMM yyyy"));
            binding.totalPrice.setText(getString(R.string.currency_sign) + (orderDetails.getSelling_price() * orderDetails.getUnits()));
            binding.unitCount.setText("Items Ordered ("+orderDetails.getUnits()+")");
            binding.orderPrice.setText(getString(R.string.currency_sign) + orderDetails.getSelling_price());
            binding.orderFor.setText("QR Code Vehicle Sticker For "+ orderDetails.getName());
            binding.deliveryAddress.setText(orderDetails.getBilling_address());
            binding.orderItePrice.setText(getString(R.string.currency_sign) + orderDetails.getSelling_price());
            binding.orderUnit.setText(String.valueOf(orderDetails.getUnits()));
            binding.total.setText(getString(R.string.currency_sign) + (orderDetails.getSelling_price() * orderDetails.getUnits()));
            binding.grandTotal.setText(getString(R.string.currency_sign) + (orderDetails.getSelling_price() * orderDetails.getUnits()));
        }

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }
}