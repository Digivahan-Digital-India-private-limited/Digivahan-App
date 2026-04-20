package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.data.model.OrderItemModel;
import com.digivahan.databinding.OrderItemDesignBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.orderDetails.MyOrderDetails;
import com.digivahan.utils.CommonLogic;
import com.ashu.ashuutils.TimeUtils;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;
import java.util.Objects;

public class OrderListItemAdapter extends RecyclerView.Adapter<OrderListItemAdapter.OLIViewHolder> {
    String TAG = "OrderListItemData";
    Activity context;
    ArrayList<OrderItemModel> list;

    public OrderListItemAdapter(Activity context, ArrayList<OrderItemModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public OrderListItemAdapter.OLIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OrderItemDesignBinding binding = OrderItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OLIViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrderListItemAdapter.OLIViewHolder holder, int position) {
        OrderItemModel model = list.get(position);

        if (!CommonMethods.getCurrentDate(context, "dd MMM yyyy").equalsIgnoreCase(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getCreatedAt()),"dd MMM yyyy"))){
            holder.binding.orderDate.setText(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getCreatedAt()), "dd MMM yyyy"));
        }else {
            holder.binding.orderDate.setText(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getCreatedAt()), "hh:mm a"));
        }

//        holder.binding.orderDate.setText(TimeUtils.convertDateFormat(model.getOrder_date(), "dd MMM yyyy hh:mm a"));
        holder.binding.orderFor.setText(model.getName());

        if (String.valueOf(model.getShip_rocket_order_id()) != null && !String.valueOf(model.getShip_rocket_order_id()).isEmpty()
                && !String.valueOf(model.getShip_rocket_order_id()).equalsIgnoreCase("0")) {
            holder.binding.orderId.setText(String.valueOf(model.getShip_rocket_order_id()));
        }

        String orderStatus = model.getOrder_status();
        if (model.getShip_rocket_status() != null && !model.getShip_rocket_status().isEmpty()) {
            orderStatus = model.getShip_rocket_status();
        }
        holder.binding.status.setText("Status: " + orderStatus);

        holder.binding.getRoot().setOnClickListener(v -> {
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            Intent MyOrderDetailsPage = new Intent(context, MyOrderDetails.class);
            CommonLogic.showTestLog(TAG, "vehicleId: " + String.valueOf(Objects.requireNonNull(model).getVehicle_id()));
            MyOrderDetailsPage.putExtra("orderDetails", model);
            context.startActivity(MyOrderDetailsPage);
//            context.finish();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class OLIViewHolder extends RecyclerView.ViewHolder {
        OrderItemDesignBinding binding;

        public OLIViewHolder(OrderItemDesignBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}
