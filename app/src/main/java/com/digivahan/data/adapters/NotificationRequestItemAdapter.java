package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.NotificationRequestCardItemModel;
import com.digivahan.databinding.NotificationRequestCardDesignBinding;

import java.util.List;

public class NotificationRequestItemAdapter extends RecyclerView.Adapter<NotificationRequestItemAdapter.NRViewHolder>{

    private Context context;
    private List<NotificationRequestCardItemModel> cardItems;

    private OnItemClickListener listener;
    int clickedPosition;
    boolean notificationSend = false;

    public NotificationRequestItemAdapter(Context context, List<NotificationRequestCardItemModel> cardItems, int clickedPosition, OnItemClickListener listener) {
        this.context = context;
        this.cardItems = cardItems;
        this.clickedPosition = clickedPosition;
        this.listener = listener;
    }

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(NotificationRequestCardItemModel item, int clickedPosition);
    }


    @NonNull
    @Override
    public NotificationRequestItemAdapter.NRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NotificationRequestCardDesignBinding binding = NotificationRequestCardDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NRViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationRequestItemAdapter.NRViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.ivIcon.setImageResource(cardItems.get(position).getIconRes());
        holder.binding.tvTitle.setText(cardItems.get(position).getTitle());

        if (clickedPosition == position){
            holder.binding.cardLayout.setBackgroundResource(R.drawable.bg_card_selected);
        }else {
            if (notificationSend){
                holder.binding.cardLayout.setBackgroundResource(R.drawable.bg_card_unselected);
            }else {
                holder.binding.cardLayout.setBackgroundResource(R.drawable.bg_card);
            }
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null ) {
                if (!notificationSend || position == clickedPosition) {
                    clickedPosition = position;
                    notifyDataSetChanged();
                    listener.onItemClick(cardItems.get(position), position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    public static class NRViewHolder extends RecyclerView.ViewHolder {
        NotificationRequestCardDesignBinding binding;
        public NRViewHolder(NotificationRequestCardDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void setFixedNotification(){
        notificationSend = true;
        notifyDataSetChanged();
    }
}
