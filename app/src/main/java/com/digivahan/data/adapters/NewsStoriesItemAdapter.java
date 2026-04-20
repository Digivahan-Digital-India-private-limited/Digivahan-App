package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.NewsStoriesItemModel;
import com.digivahan.databinding.NewsStoriesItemDesignBinding;
import com.digivahan.databinding.TrendingCarItemBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.newsStories.NewsStoriesDetailsActivity;
import com.ashu.ashuutils.TimeUtils;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.ArrayList;

public class NewsStoriesItemAdapter extends RecyclerView.Adapter<NewsStoriesItemAdapter.CLViewHolder> {

    String TAG = "NewsStoriesItemAdapterData";
    Context context;
    ArrayList<NewsStoriesItemModel> list;

    public NewsStoriesItemAdapter(Context context, ArrayList<NewsStoriesItemModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NewsStoriesItemAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NewsStoriesItemDesignBinding binding = NewsStoriesItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsStoriesItemAdapter.CLViewHolder holder, int position) {
        if (!list.isEmpty()) {
            int actualPosition = position % list.size();
            NewsStoriesItemModel newsStoriesItemModel = list.get(actualPosition);

            ImageHelperMethods.loadImage(TAG, context, newsStoriesItemModel.getBanner(), holder.binding.ivCar, R.drawable.temp_img1);

            holder.binding.newsHeading.setText(newsStoriesItemModel.getHeading());
            holder.binding.newsTime.setText(TimeUtils.convertDateFormat(newsStoriesItemModel.getCreatedAt(), "dd MMM yyyy"));
            holder.binding.newsDescription.setText(newsStoriesItemModel.getSub_heading());

            holder.binding.cardItem.setOnClickListener(v -> {
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent newsStoryPage = new Intent(context, NewsStoriesDetailsActivity.class);
                newsStoryPage.putExtra("newsStoriesData", newsStoriesItemModel);
                context.startActivity(newsStoryPage);
            });
        }
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        NewsStoriesItemDesignBinding binding;
        public CLViewHolder(NewsStoriesItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
