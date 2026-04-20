package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.data.model.FasTagCardModel;
import com.digivahan.databinding.FastagBankItemDesignBinding;
import com.digivahan.utils.CommonLogic;

import java.util.ArrayList;

public class FasTagBankItemAdapter extends RecyclerView.Adapter<FasTagBankItemAdapter.CLViewHolder> {
    String TAG = "TipsItemAdapterData";
    Context context;
    ArrayList<FasTagCardModel> list;
    private FasTagBankItemAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FasTagCardModel model);
    }

    public FasTagBankItemAdapter(Context context, ArrayList<FasTagCardModel> list, FasTagBankItemAdapter.OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FasTagBankItemAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FastagBankItemDesignBinding binding = FastagBankItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FasTagBankItemAdapter.CLViewHolder holder, int position) {
        FasTagCardModel fasTagCardModel = list.get(position);

        CommonLogic.loadSvg(context, holder.binding.icon, fasTagCardModel.getIconUrl());

        holder.binding.title.setText(fasTagCardModel.getBillerName());

        if (fasTagCardModel.isAvailable() && fasTagCardModel.getBillerStatus().equalsIgnoreCase("ACTIVE")){
            holder.binding.message.setVisibility(ViewGroup.GONE);
        }else {
            holder.binding.message.setVisibility(ViewGroup.VISIBLE);
        }

        holder.binding.getRoot().setOnClickListener(v -> {

            if (listener != null) {
                listener.onItemClick(fasTagCardModel);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        FastagBankItemDesignBinding binding;
        public CLViewHolder(FastagBankItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    
}
