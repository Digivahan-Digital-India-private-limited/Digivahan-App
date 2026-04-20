package com.digivahan.data.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.BenefitsOfQRCodeModel;
import com.digivahan.databinding.TipsItemDesignBinding;
import com.ashu.ashuutils.NavigationUtils;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.ArrayList;

public class BenefitsOfQRCodeAdapter extends RecyclerView.Adapter<BenefitsOfQRCodeAdapter.CLViewHolder> {
    String TAG = "BenefitsOfQRCodeAdapterData";
    Context context;
    ArrayList<BenefitsOfQRCodeModel> list;

    public BenefitsOfQRCodeAdapter(Context context, ArrayList<BenefitsOfQRCodeModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public BenefitsOfQRCodeAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TipsItemDesignBinding binding = TipsItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BenefitsOfQRCodeAdapter.CLViewHolder holder, int position) {

        BenefitsOfQRCodeModel benefitsOfQRCodeModel = list.get(position);

        ImageHelperMethods.loadImage(TAG, context, benefitsOfQRCodeModel.getVideo_thumbnail(), holder.binding.ivCar, R.drawable.image_loading);
   
        holder.binding.tipsCardItem.setOnClickListener(v -> {
            NavigationUtils.openBrowser((Activity) context, benefitsOfQRCodeModel.getVideo_url());
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        TipsItemDesignBinding binding;
        public CLViewHolder(TipsItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
