package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digivahan.R;
import com.digivahan.data.model.TrendingCarsModel;
import com.digivahan.databinding.TrendingCarItemBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.trendingCar.TrendingCarActivity;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;

public class TrendingCarsItemAdapter extends RecyclerView.Adapter<TrendingCarsItemAdapter.CLViewHolder> {

    String TAG = "TrendingCarsItemAdapterData";

    Context context;
    ArrayList<TrendingCarsModel> list;

    public TrendingCarsItemAdapter(Context context, ArrayList<TrendingCarsModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TrendingCarsItemAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TrendingCarItemBinding binding = TrendingCarItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingCarsItemAdapter.CLViewHolder holder, int position) {

        TrendingCarsModel carsModel = list.get(position);

//        CommonMethods.loadImage(TAG, context, carsModel.imageUrl.split(",")[0], holder.binding.imgCar, R.drawable.ic_vehicle_default);

        Glide.with(context).load(carsModel.imageUrl.split(",")[0]).placeholder(R.drawable.ic_vehicle_default).into(holder.binding.imgCar);

        holder.binding.tvCarName.setText(carsModel.modelName);
        holder.binding.tvBrand.setText(carsModel.brandName);
        holder.binding.tvPrice.setText(carsModel.priceDisplay);

        holder.binding.trendingCarItemLayout.setOnClickListener(v -> {
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            Intent documentPage = new Intent(context, TrendingCarActivity.class);
            documentPage.putExtra("carDetails", carsModel);
            context.startActivity(documentPage);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        TrendingCarItemBinding binding;

        public CLViewHolder(TrendingCarItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
