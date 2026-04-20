package com.digivahan.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.HowToUseQRItemModel;
import com.digivahan.databinding.TipsItemDesignBinding;

import java.util.ArrayList;

public class HowToUseQRAdapter extends RecyclerView.Adapter<HowToUseQRAdapter.CLViewHolder> {

    Context context;
    ArrayList<HowToUseQRItemModel> list;

    public HowToUseQRAdapter(Context context, ArrayList<HowToUseQRItemModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HowToUseQRAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TipsItemDesignBinding binding = TipsItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HowToUseQRAdapter.CLViewHolder holder, int position) {

        if (!list.isEmpty()) {
            int actualPosition = position % list.size();
            int imageResourceId = list.get(actualPosition).getImageResource();
            holder.binding.ivCar.setImageResource(imageResourceId);
            holder.binding.message.setText(list.get(actualPosition).getMessage());
        }

        /*holder.binding.tipsCardItem.setOnClickListener(v -> {
            Intent documentPage = new Intent(context, TipsDetailsActivity.class);
            context.startActivity(documentPage);
        });*/

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
