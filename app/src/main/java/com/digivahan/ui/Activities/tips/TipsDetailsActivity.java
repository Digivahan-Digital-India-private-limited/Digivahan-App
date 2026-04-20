package com.digivahan.ui.Activities.tips;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.adapters.TipsDetailsAdapter;
import com.digivahan.data.model.TipsItemModel;
import com.digivahan.databinding.ActivityTipsDetailsBinding;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

import java.util.ArrayList;
import java.util.List;

public class TipsDetailsActivity extends BaseActivity {

    String TAG = "TipsDetailsActivityData";
    ActivityTipsDetailsBinding binding;
    RecyclerView recyclerTips;
    List<TipsItemModel.Point> tipList = new ArrayList<>();

    TipsItemModel tipsItemDetails;
    TipsDetailsAdapter tipsDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTipsDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Tips");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });

        try {
            tipsItemDetails = (TipsItemModel) getIntent().getSerializableExtra("tipsDetails");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        recyclerTips = findViewById(R.id.recyclerTips);
        recyclerTips.setLayoutManager(new LinearLayoutManager(this));

        tipsDetailsAdapter = new TipsDetailsAdapter(TipsDetailsActivity.this, tipList);
        recyclerTips.setAdapter(tipsDetailsAdapter);
        setData(tipsItemDetails);

    }

    private void setData(TipsItemModel tipsItemDetails) {
        tipList.clear();
        tipList.addAll(tipsItemDetails.points);
        tipsDetailsAdapter.notifyDataSetChanged();

        for (TipsItemModel.Point point : tipList) {
            Log.d(TAG, "👉 " + point.message + " (" + point.icon + ")");
        }

        ImageHelperMethods.loadImage(TAG, TipsDetailsActivity.this, tipsItemDetails.banner, binding.ivHeader, R.drawable.tips_temp_img);


    }
}