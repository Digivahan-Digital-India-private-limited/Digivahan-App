package com.digivahan.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.databinding.ItemSliderImageBinding;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    String TAG = "ImageSliderAdapterData";
    private Context context;
    private List<String> imageList;

    public ImageSliderAdapter(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSliderImageBinding binding = ItemSliderImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SliderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        ImageHelperMethods.loadImage(TAG, context, imageList.get(position), holder.binding.sliderImage, R.drawable.ic_vehicle_default);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ItemSliderImageBinding  binding;

        SliderViewHolder(ItemSliderImageBinding  binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

