package com.digivahan.ui.Fragments.notification;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.digivahan.data.adapters.NotificationItemAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.NotificationItemModel;
import com.digivahan.databinding.FragmentNotificationPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;


import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;


public class NotificationPageFragment extends Fragment {

    String TAG = "NotificationPageFragmentData";

    FragmentNotificationPageBinding binding;

    ArrayList<NotificationItemModel> notificationItemList = new ArrayList<>();

    NotificationItemAdapter notificationItemAdapter;

    int currentPage = 1, totalPage = 1;

    AshDialog loadingDialog;

    PreferencesManager manager;

    Call<JsonObject> notificationDataCall;

    @Override
    public void onResume() {
        super.onResume();
        getNotificationListData(false); // ✅ load only once

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationDataCall != null) {
            notificationDataCall.cancel();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNotificationPageBinding.inflate(getLayoutInflater(), container, false);

        loadingDialog = new AshDialog(getContext(), "Please wait", "");

        manager = new PreferencesManager(requireContext());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });


        notificationItemAdapter = new NotificationItemAdapter(getActivity(), notificationItemList);
        binding.rvNotificationList.setAdapter(notificationItemAdapter);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            getNotificationListData(true);
        });

        binding.btnNext.setOnClickListener(v -> {
            if (currentPage < totalPage) {
                currentPage++;
                getNotificationListData(false);
            }
        });

        binding.btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                getNotificationListData(false);
            }
        });



        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (notificationItemList.isEmpty()) {
            getNotificationListData(false); // ✅ load only once
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getNotificationListData(boolean isSwiped) {
        notificationItemList.clear();
        notificationItemAdapter.notifyDataSetChanged();

        if (!isSwiped) {
            loadingDialog.show();
        }
        notificationDataCall = ApiCall.callApi(TAG,
                getActivity(),
                APIData.GET_NOTIFICATION + manager.getUserId() + "?current_page=" + currentPage,
                null, "get",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {

                                updatePaginationUI();


                                if (responseBody.has("pagination")){
                                    currentPage = responseBody.getJSONObject("pagination").getInt("current_page");
                                    totalPage = responseBody.getJSONObject("pagination").getInt("total_pages");
                                }

                                JSONArray notificationArray = responseBody
                                        .getJSONArray("data");

                                notificationItemList.clear();

                                notificationItemList.addAll(APIHelper.convertJsonArrayToList(notificationArray, NotificationItemModel.class));

                                // ✅ Log or update your adapter
                                CommonLogic.showTestLog(TAG, "Total Notifications: " + notificationItemList.size());

                                if (notificationItemList.isEmpty()) {
                                    binding.emptyLayout.setVisibility(View.VISIBLE);
                                    binding.swipeRefreshLayout.setVisibility(View.GONE);
                                    binding.paginationLayout.setVisibility(View.GONE);
                                } else {
                                    binding.emptyLayout.setVisibility(View.GONE);
                                    binding.swipeRefreshLayout.setVisibility(View.VISIBLE);
                                }


                                notificationItemAdapter.notifyDataSetChanged();

                            } else {
                                Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            CommonLogic.showTestLog(TAG, "Error parsing JSON: " + e.getMessage());
                        }

                        loadingDialog.dismiss();
                        if (isSwiped) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                        }
                    }


                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
                        if (isSwiped) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );
    }

    private void updatePaginationUI() {

        if (totalPage <= 1) {
            binding.paginationLayout.setVisibility(View.GONE);
            return;
        }

        binding.paginationLayout.setVisibility(View.VISIBLE);

        binding.tvPageInfo.setText(currentPage + " / " + totalPage);

        // Disable / Enable arrows
        binding.btnPrev.setEnabled(currentPage > 1);
        binding.btnNext.setEnabled(currentPage < totalPage);

        binding.btnPrev.setAlpha(currentPage > 1 ? 1f : 0.3f);
        binding.btnNext.setAlpha(currentPage < totalPage ? 1f : 0.3f);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}