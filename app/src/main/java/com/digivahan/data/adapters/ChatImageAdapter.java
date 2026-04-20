package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.fileUtils.FileUtils;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.List;

public class ChatImageAdapter extends RecyclerView.Adapter<ChatImageAdapter.ImageViewHolder>{
    String TAG = "ChatImageAdapterData";
    Context context;
    List<String> imagePaths;
    ImageView viewImage, viewImageCrossIcon;

    public ChatImageAdapter(Context context, List<String> imagePaths, ImageView viewImage, ImageView viewImageCrossIcon) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.viewImage = viewImage;
        this.viewImageCrossIcon = viewImageCrossIcon;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Use Glide or Picasso to load the image
        ImageHelperMethods.loadImage(TAG, context, imagePaths.get(position), holder.imageView, R.drawable.image_loading);


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!imagePaths.get(position).isEmpty()){
                    FileUtils.downloadImageIfNotExists(TAG, context, imagePaths.get(position), viewImage, viewImageCrossIcon, Constants.appImageFolder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
