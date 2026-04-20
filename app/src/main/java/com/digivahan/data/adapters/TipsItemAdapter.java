package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.TipsItemModel;
import com.digivahan.databinding.TipsItemDesignBinding;
import com.digivahan.databinding.TrendingCarItemBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.tips.TipsDetailsActivity;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.ArrayList;

public class TipsItemAdapter extends RecyclerView.Adapter<TipsItemAdapter.CLViewHolder> {
    String TAG = "TipsItemAdapterData";
    Context context;
    ArrayList<TipsItemModel> list;

    public TipsItemAdapter(Context context, ArrayList<TipsItemModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TipsItemAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TipsItemDesignBinding binding = TipsItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TipsItemAdapter.CLViewHolder holder, int position) {
        if (!list.isEmpty()) {
            int actualPosition = position % list.size();
            TipsItemModel tipsItemModel = list.get(actualPosition);

            ImageHelperMethods.loadImage(TAG, context, tipsItemModel.banner, holder.binding.ivCar, R.drawable.tips_temp_img);

            holder.binding.tipsCardItem.setOnClickListener(v -> {
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent tipsDetailsPage = new Intent(context, TipsDetailsActivity.class);
                tipsDetailsPage.putExtra("tipsDetails", tipsItemModel);
                context.startActivity(tipsDetailsPage);
            });
        }

    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        TipsItemDesignBinding binding;
        public CLViewHolder(TipsItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
