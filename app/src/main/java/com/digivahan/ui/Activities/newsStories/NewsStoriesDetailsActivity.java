package com.digivahan.ui.Activities.newsStories;

import android.os.Bundle;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.model.NewsStoriesItemModel;
import com.digivahan.databinding.ActivityNewsStoriesDetailsBinding;
import com.digivahan.ui.Activities.invoice.DownloadInvoice;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.TimeUtils;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

public class NewsStoriesDetailsActivity extends BaseActivity {

    String TAG = "NewsStoriesDetailsActivityData";
    ActivityNewsStoriesDetailsBinding binding;
    NewsStoriesItemModel newsStoriesItemData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewsStoriesDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        getOnBackPressedDispatcher().addCallback(NewsStoriesDetailsActivity.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        binding.backBtn.setOnClickListener(v -> back());

        try {
            newsStoriesItemData = (NewsStoriesItemModel) getIntent().getSerializableExtra("newsStoriesData");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        ImageHelperMethods.loadImage(TAG, NewsStoriesDetailsActivity.this, newsStoriesItemData.getBanner(), binding.banner, R.drawable.temp_img1);

        binding.newsHeading.setText(newsStoriesItemData.getHeading());
        binding.newsTime.setText(TimeUtils.convertDateFormat(newsStoriesItemData.getCreatedAt(), "dd MMM yyyy"));
        binding.newsDescription.setText(CommonLogic.convertHtmlToString(newsStoriesItemData.getNews()));

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }
}