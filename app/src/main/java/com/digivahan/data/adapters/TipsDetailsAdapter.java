package com.digivahan.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.TipsItemModel;
import com.digivahan.databinding.TipsDetailsItemDesignBinding;
import com.digivahan.databinding.TipsItemDesignBinding;
import com.digivahan.databinding.TrendingCarItemBinding;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.List;

public class TipsDetailsAdapter extends RecyclerView.Adapter<TipsDetailsAdapter.CLViewHolder> {
    String TAG = "TipsDetailsAdapterData";
    Context context;
    List<TipsItemModel.Point> list;

    public TipsDetailsAdapter(Context context, List<TipsItemModel.Point> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TipsDetailsAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TipsDetailsItemDesignBinding binding = TipsDetailsItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TipsDetailsAdapter.CLViewHolder holder, int position) {
        TipsItemModel.Point point = list.get(position);
        ImageHelperMethods.loadImage(TAG, context, point.icon, holder.binding.ivIcon, R.drawable.circle_bg);
        holder.binding.tvTipText.setText(point.message);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        TipsDetailsItemDesignBinding binding;
        public CLViewHolder(TipsDetailsItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
