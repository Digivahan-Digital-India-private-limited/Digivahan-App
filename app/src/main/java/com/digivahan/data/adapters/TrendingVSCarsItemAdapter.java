package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.TrendingVSCarsModel;
import com.digivahan.databinding.TrendingCarComparisionItemBinding;
import com.digivahan.databinding.TrendingCarItemBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.vsCars.TrendingVSCarDetailsActivity;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.ArrayList;

public class TrendingVSCarsItemAdapter extends RecyclerView.Adapter<TrendingVSCarsItemAdapter.CLViewHolder> {

    String TAG = "TrendingVSCarsItemAdapterData";
    Context context;
    ArrayList<TrendingVSCarsModel> list;

    public TrendingVSCarsItemAdapter(Context context, ArrayList<TrendingVSCarsModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TrendingVSCarsItemAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TrendingCarComparisionItemBinding binding = TrendingCarComparisionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingVSCarsItemAdapter.CLViewHolder holder, int position) {

        TrendingVSCarsModel trendingVSCarsModel = list.get(position);

        ImageHelperMethods.loadImage(TAG, context, trendingVSCarsModel.car1Data.imageUrl.split(",")[0], holder.binding.carImg1, R.drawable.ic_vehicle_default);
        holder.binding.tvCarName1.setText(trendingVSCarsModel.car1Data.modelName);
        holder.binding.tvBrand1.setText(trendingVSCarsModel.car1Data.brandName);
        holder.binding.tvPrice1.setText(trendingVSCarsModel.car1Data.priceDisplay);

        ImageHelperMethods.loadImage(TAG, context, trendingVSCarsModel.car2Data.imageUrl.split(",")[0], holder.binding.carImg2, R.drawable.ic_vehicle_default);
        holder.binding.tvCarName2.setText(trendingVSCarsModel.car2Data.modelName);
        holder.binding.tvBrand2.setText(trendingVSCarsModel.car2Data.brandName);
        holder.binding.tvPrice2.setText(trendingVSCarsModel.car2Data.priceDisplay);

        holder.binding.trendingVSCarItemLayout.setOnClickListener(v -> {
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            Intent documentPage = new Intent(context, TrendingVSCarDetailsActivity.class);
            documentPage.putExtra("VSDetails", trendingVSCarsModel);
            context.startActivity(documentPage);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        TrendingCarComparisionItemBinding binding;
        public CLViewHolder(TrendingCarComparisionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
