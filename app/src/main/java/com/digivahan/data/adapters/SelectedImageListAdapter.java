package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.SavedImageData;
import com.digivahan.databinding.SelectedImageLlistItemDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.ArrayList;

public class SelectedImageListAdapter extends RecyclerView.Adapter<SelectedImageListAdapter.CLViewHolder> {
    String TAG = "SelectedImageListAdapterData";
    Context context;
    ArrayList<SavedImageData> list;
    AshDialog loadingDialog;

    public SelectedImageListAdapter(Context context, ArrayList<SavedImageData> list, AshDialog loadingDialog) {
        this.context = context;
        this.list = list;
        this.loadingDialog = loadingDialog;
    }

    @NonNull
    @Override
    public SelectedImageListAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SelectedImageLlistItemDesignBinding binding = SelectedImageLlistItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedImageListAdapter.CLViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SavedImageData savedImageData = list.get(position);

        ImageHelperMethods.loadImage(TAG, context, savedImageData.getImage_url(), holder.binding.selectedImage, R.drawable.image_loading);

        holder.binding.deleteBtn.setOnClickListener(v -> {
            loadingDialog.show();
            CommonMethods.deleteDocument(TAG, context, savedImageData.getPublic_id(), new CommonMethods.DeleteProfileImageListener() {
                @Override
                public void onDeleteSuccess(String message) {
                    list.remove(position);
                    notifyDataSetChanged();
                    loadingDialog.dismiss();
                    // You can refresh profile image view here
                }

                @Override
                public void onDeleteFailed(String message) {
                    loadingDialog.dismiss();
                    CommonLogic.showTestLog(TAG, "  image error: " + message);
//                    Toast.makeText(getApplicationContext(), "❌ " + message, Toast.LENGTH_SHORT).show();
                }
            });

        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        SelectedImageLlistItemDesignBinding binding;
        public CLViewHolder(SelectedImageLlistItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
