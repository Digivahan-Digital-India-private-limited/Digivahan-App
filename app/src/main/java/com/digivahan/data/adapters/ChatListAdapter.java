package com.digivahan.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.ChatItemModel;
import com.digivahan.data.model.MembersModel;
import com.digivahan.databinding.ChatItemDesignBinding;
import com.digivahan.utils.CommonLogic;
import com.ashu.ashuutils.TimeUtils;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.CLViewHolder> {
    String TAG = "ChatListAdapterData";
    Context context;
    ArrayList<ChatItemModel> list;
    PreferencesManager manager;
    ImageView viewImage, viewImageCrossIcon;
    ArrayList<MembersModel> roomMembersList;

    public ChatListAdapter(Context context, ArrayList<ChatItemModel> list, ArrayList<MembersModel> roomMembersList, ImageView viewImage, ImageView viewImageCrossIcon) {
        this.context = context;
        this.list = list;
        this.viewImage = viewImage;
        this.viewImageCrossIcon = viewImageCrossIcon;
        this.roomMembersList = roomMembersList;
        manager = new PreferencesManager(context);
    }

    @NonNull
    @Override
    public ChatListAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatItemDesignBinding binding = ChatItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.CLViewHolder holder, int position) {
        ChatItemModel model = list.get(position);

        CommonLogic.showTestLog(TAG, "ids: " + model.getSenderId() + " " + manager.getUser().getPhone_number() + " " + manager.getUser().getEmail());

        if (model.getSenderId().equalsIgnoreCase(manager.getUserId())) {
            holder.binding.senderLayout.setVisibility(View.VISIBLE);
            if (!model.getMessage().equalsIgnoreCase("empty")) {
                holder.binding.tvSenderMessage.setText(model.getMessage());
            } else {
                holder.binding.tvSenderMessage.setText("");
            }

            if (!CommonMethods.getCurrentDate(context, "dd MMM yyyy").equalsIgnoreCase(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getMessageTimestamp()), "dd MMM yyyy"))) {
                holder.binding.tvSenderTime.setText(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getMessageTimestamp()), "dd MMM yyyy"));
            } else {
                holder.binding.tvSenderTime.setText(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getMessageTimestamp()), "hh:mm a"));
            }


            if (model.getLatitude() != null && !model.getLatitude().isEmpty() &&
                    model.getLongitude() != null && !model.getLongitude().isEmpty()) {
                holder.binding.senderLocationCard.setVisibility(View.VISIBLE);
                holder.binding.senderLocationCard.setOnClickListener(v -> {
                    CommonLogic.showTestLog(TAG, "Location Clicked");
                });
            } else {
                holder.binding.senderLocationCard.setVisibility(View.GONE);
            }

            // Set up a GridLayoutManager with 2 columns
            GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
            holder.binding.senderRecyclerView.setLayoutManager(layoutManager);

            // Set up the adapter
            ChatImageAdapter adapter = new ChatImageAdapter(context, model.getImages(), viewImage, viewImageCrossIcon);
            holder.binding.senderRecyclerView.setAdapter(adapter);

            if (!model.getImages().isEmpty()) {
                holder.binding.senderRecyclerView.setVisibility(View.VISIBLE);
            } else {
                holder.binding.senderRecyclerView.setVisibility(View.GONE);
            }

            holder.binding.receiverLayout.setVisibility(View.GONE);

            /*if(model.isSending()){
                holder.statusIcon.setVisibility(View.VISIBLE);
            }else{
                holder.statusIcon.setVisibility(View.GONE);
            }*/

        } else {
            holder.binding.senderLayout.setVisibility(View.GONE);

            holder.binding.receiverLayout.setVisibility(View.VISIBLE);

            if (roomMembersList != null && !roomMembersList.isEmpty()) {
                for (MembersModel membersModel : roomMembersList) {
                    if (membersModel.getUser_id().equalsIgnoreCase(model.getSenderId())) {
                        holder.binding.tvReceiverName.setText(membersModel.getFirst_name() + " " + membersModel.getLast_name());
                        ImageHelperMethods.loadImage(TAG, context, membersModel.getProfile_pic_url(), holder.binding.ivReceiverProfile, R.drawable.temp_profile_icon);
                        break;
                    }
                }
            }

            if (model.getLatitude() != null && !model.getLatitude().isEmpty() &&
                    model.getLongitude() != null && !model.getLongitude().isEmpty()) {
                holder.binding.receiverLocationCard.setVisibility(View.VISIBLE);
                holder.binding.receiverLocationCard.setOnClickListener(v -> {
                    CommonLogic.showTestLog(TAG, "Location Clicked");
                    CommonLogic.openGoogleMaps(context, Double.parseDouble(model.getLatitude()), Double.parseDouble(model.getLongitude()));
                });
            } else {
                holder.binding.receiverLocationCard.setVisibility(View.GONE);
            }

            if (!model.getMessage().equalsIgnoreCase("empty")) {
                holder.binding.tvReceiverMessage.setText(model.getMessage());
            } else {
                holder.binding.tvReceiverMessage.setText("");
            }

            if (!CommonMethods.getCurrentDate(context, "dd MMM yyyy").equalsIgnoreCase(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getMessageTimestamp()), "dd MMM yyyy"))) {
                holder.binding.tvReceiverTime.setText(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getMessageTimestamp()), "dd MMM yyyy"));
            } else {
                holder.binding.tvReceiverTime.setText(TimeUtils.convertDateFormat(CommonLogic.convertUtcToDeviceTime(TAG, model.getMessageTimestamp()), "hh:mm a"));
            }

            // Set up a GridLayoutManager with 2 columns
            GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
            holder.binding.receiverRecyclerView.setLayoutManager(layoutManager);

            // Set up the adapter
            ChatImageAdapter adapter = new ChatImageAdapter(context, model.getImages(), viewImage, viewImageCrossIcon);
            holder.binding.receiverRecyclerView.setAdapter(adapter);

            if (!model.getImages().isEmpty()) {
                holder.binding.receiverRecyclerView.setVisibility(View.VISIBLE);
            } else {
                holder.binding.receiverRecyclerView.setVisibility(View.GONE);
            }
        }

       /* holder.binding.receiverLayout.setOnClickListener(v -> {
            Intent documentPage = new Intent(context, DocumentVaultActivity.class);
            context.startActivity(documentPage);
        });*/

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        ChatItemDesignBinding binding;

        public CLViewHolder(ChatItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
