package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.NotificationItemModel;
import com.digivahan.databinding.NotificationItemDesignBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.ui.Activities.documentVault.DocumentVaultActivity;
import com.digivahan.ui.Activities.notification.ViewNotification;
import com.digivahan.utils.CommonLogic;
import com.ashu.ashuutils.TimeUtils;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.utils.CommonMethods;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationItemAdapter extends RecyclerView.Adapter<NotificationItemAdapter.NIViewHolder> {
    String TAG = "NotificationItemAdapterData";
    Activity context;
    ArrayList<NotificationItemModel> list;

    PreferencesManager manager;

    public NotificationItemAdapter(Activity context, ArrayList<NotificationItemModel> list) {
        this.context = context;
        this.list = list;
        manager = new PreferencesManager(context);
    }

    @NonNull
    @Override
    public NotificationItemAdapter.NIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        NotificationItemDesignBinding binding = NotificationItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new NIViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationItemAdapter.NIViewHolder holder, int position) {
        NotificationItemModel model = list.get(position);

        String senderId = model.getSender_id();
        String senderName = model.getSender_name();
        String vehicleId = model.getVehicle_id();
        String chatRoomId = model.getChat_room_id();
        String issueType = model.getIssue_type();
        String message = model.getMessage();


        holder.binding.tvName.setText(senderName);
        holder.binding.tvSubtitle.setText(model.getNotification_title());
        if (!CommonMethods.getCurrentDate(context, "dd MMM yyyy").equalsIgnoreCase(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getCreatedAt()),"dd MMM yyyy"))){
            holder.binding.tvTime.setText(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getCreatedAt()), "dd MMM yyyy"));
        }else {
            holder.binding.tvTime.setText(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getCreatedAt()), "hh:mm a"));
        }

        ImageHelperMethods.loadImage(TAG, context, model.getSender_pic(), holder.binding.imgProfile, R.drawable.temp_profile_icon);

        if (!model.isSeen_status()){
            holder.binding.unseenDot.setVisibility(View.VISIBLE);
        }else {
            holder.binding.unseenDot.setVisibility(View.GONE);
        }

        holder.binding.btnChatNow.setVisibility(View.VISIBLE);

        /*if (model.getNotification_type().equalsIgnoreCase("chat") || model.getNotification_type().equalsIgnoreCase("doc_access")){
            if (chatRoomId != null && !chatRoomId.isEmpty() && !chatRoomId.equalsIgnoreCase("empty")) {
                holder.binding.btnChatNow.setVisibility(View.VISIBLE);
            }else if (model.getNotification_type().equalsIgnoreCase("doc_access")){
                holder.binding.btnChatNow.setVisibility(View.VISIBLE);
                holder.binding.btnChatNow.setText("View Code");
            }
//            else {holder.binding.btnChatNow.setVisibility(View.GONE);}
        }else {
            holder.binding.btnChatNow.setVisibility(View.GONE);
        }*/

        String finalSenderId = senderId;
        String finalChatRoomId = chatRoomId;
        holder.binding.btnChatNow.setOnClickListener(v -> {
            setSeenNotificationTrue(context, model.get_id());
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            Intent viewNotification = new Intent(context, ViewNotification.class);
            viewNotification.putExtra("notificationData", model);
            CommonLogic.showTestLog(TAG, "chatRoomId: "+ finalChatRoomId + " receiverId: " + finalSenderId);
            if (model.getNotification_type().equalsIgnoreCase("doc_access")) {
                viewNotification = new Intent(context, DocumentVaultActivity.class);
                viewNotification.putExtra("docAccessType", "check");
                viewNotification.putExtra("vehicleId", vehicleId);
                viewNotification.putExtra("vehicleOwnerId", finalSenderId);
            }else if (model.getNotification_type().equalsIgnoreCase("chat")) {
                viewNotification = new Intent(context, ChatActivity.class);

                if ((model.getIssue_type().equalsIgnoreCase("accident_alert") && (finalSenderId == null ||
                        finalSenderId.isEmpty()))){
                    viewNotification = new Intent(context, ViewNotification.class);
                }

                viewNotification.putExtra("docAccessType", "check");
                viewNotification.putExtra("vehicleId", vehicleId);
                viewNotification.putExtra("vehicleOwnerId", finalSenderId);
            }
            viewNotification.putExtra("chatRoomId", finalChatRoomId);
            viewNotification.putExtra("receiverId", finalSenderId);
            context.startActivity(viewNotification);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class NIViewHolder extends RecyclerView.ViewHolder {
        NotificationItemDesignBinding binding;
        public NIViewHolder(NotificationItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setSeenNotificationTrue(Activity context, String notificationId) {
        // --- Step 1: Prepare request JSON ---
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("user_id", manager.getUserId());
        requestBody.addProperty("notification_id", notificationId);

        ApiCall.callApi(TAG,
                context,
                APIData.SET_NOTIFICATION_SEEN,
                requestBody, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {

                        // wright a code if you need
                    }


                    @Override
                    public void onError(String errorMessage) {
                        CommonLogic.showTestLog(TAG, errorMessage);
                    }
                }
        );
    }
}
