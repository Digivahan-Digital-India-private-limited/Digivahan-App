package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.R;
import com.digivahan.data.model.TipsItemModel;
import com.digivahan.databinding.FastagAdditionalDetailsDesignBinding;
import com.digivahan.databinding.TipsItemDesignBinding;
import com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagAdditionalDetails;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.tips.TipsDetailsActivity;

import java.util.ArrayList;

public class FasTagAdditionalDetailsAdapter extends RecyclerView.Adapter<FasTagAdditionalDetailsAdapter.CLViewHolder> {
    String TAG = "FasTagAdditionalDetailsAdapterData";
    Context context;
    ArrayList<FasTagAdditionalDetails> list;

    public FasTagAdditionalDetailsAdapter(Context context, ArrayList<FasTagAdditionalDetails> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public FasTagAdditionalDetailsAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FastagAdditionalDetailsDesignBinding binding = FastagAdditionalDetailsDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FasTagAdditionalDetailsAdapter.CLViewHolder holder, int position) {
        FasTagAdditionalDetails fasTagAdditionalDetails = list.get(position);

        holder.binding.nameField.setText(fasTagAdditionalDetails.getName());
        holder.binding.valueField.setText(fasTagAdditionalDetails.getValue());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        FastagAdditionalDetailsDesignBinding binding;
        public CLViewHolder(FastagAdditionalDetailsDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
