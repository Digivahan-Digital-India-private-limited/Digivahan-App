package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.R;
import com.digivahan.data.model.NearByServiceItem;
import com.digivahan.data.model.TipsItemModel;
import com.digivahan.databinding.NearByServiceDesignBinding;
import com.digivahan.databinding.TipsItemDesignBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.tips.TipsDetailsActivity;
import com.digivahan.utils.CommonLogic;

import java.util.ArrayList;

public class NearByServiceAdapter extends RecyclerView.Adapter<NearByServiceAdapter.CLViewHolder> {
    String TAG = "NearByServiceAdapterData";
    Context context;
    ArrayList<NearByServiceItem> list;

    public NearByServiceAdapter(Context context, ArrayList<NearByServiceItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NearByServiceAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NearByServiceDesignBinding binding = NearByServiceDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NearByServiceAdapter.CLViewHolder holder, int position) {
        NearByServiceItem nearByServiceItem = list.get(position);

        ImageHelperMethods.loadImage(TAG, context, nearByServiceItem.getIcon(), holder.binding.serviceIcon, R.drawable.address_empty_image);

        holder.binding.serviceTitle.setText(nearByServiceItem.getTitle());

        holder.binding.serviceItem.setOnClickListener(v -> {
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            CommonLogic.openNearbyService(context, nearByServiceItem.getService_type());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        NearByServiceDesignBinding binding;
        public CLViewHolder(NearByServiceDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
