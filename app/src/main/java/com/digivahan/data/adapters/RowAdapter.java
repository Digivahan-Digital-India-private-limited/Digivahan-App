package com.digivahan.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.ComparisonItemModel;

import java.util.List;

public class RowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_BOOL = 1;

    private final List<ComparisonItemModel> data;

    public RowAdapter(List<ComparisonItemModel> data) {
        this.data = data;
    }

    @Override public int getItemViewType(int position) {
        return data.get(position).isBoolean ? TYPE_BOOL : TYPE_TEXT;
    }

    @NonNull
    @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_BOOL) {
            return new BoolVH(inf.inflate(R.layout.item_row_boolean, parent, false));
        }
        return new TextVH(inf.inflate(R.layout.item_row_compare, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        ComparisonItemModel item = data.get(position);
        if (getItemViewType(position) == TYPE_BOOL) {
            BoolVH vh = (BoolVH) h;
            vh.tvLabel.setText(item.label);

            vh.ivLeft.setImageResource(item.highlightLeft ? R.drawable.ic_check_green : R.drawable.ic_cross_red);
            vh.ivRight.setImageResource(item.highlightRight ? R.drawable.ic_check_green : R.drawable.ic_cross_red);

            vh.ivLeft.setBackgroundResource(item.highlightLeft ? R.drawable.bg_pill_green : R.drawable.bg_pill_gray);
            vh.ivRight.setBackgroundResource(item.highlightRight ? R.drawable.bg_pill_green : R.drawable.bg_pill_gray);
        } else {
            TextVH vh = (TextVH) h;
            vh.tvLabel.setText(item.label);
            vh.tvLeft.setText(item.leftText);
            vh.tvRight.setText(item.rightText);

            vh.tvLeft.setBackgroundResource(item.highlightLeft ? R.drawable.bg_pill_green : R.drawable.bg_pill_gray);
            vh.tvRight.setBackgroundResource(item.highlightRight ? R.drawable.bg_pill_green : R.drawable.bg_pill_gray);
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class TextVH extends RecyclerView.ViewHolder {
        TextView tvLabel, tvLeft, tvRight;
        TextVH(@NonNull View v) {
            super(v);
            tvLabel = v.findViewById(R.id.tvLabel);
            tvLeft  = v.findViewById(R.id.tvLeft);
            tvRight = v.findViewById(R.id.tvRight);
        }
    }

    static class BoolVH extends RecyclerView.ViewHolder {
        TextView tvLabel;
        ImageView ivLeft, ivRight;
        BoolVH(@NonNull View v) {
            super(v);
            tvLabel = v.findViewById(R.id.tvLabel);
            ivLeft  = v.findViewById(R.id.ivLeftBool);
            ivRight = v.findViewById(R.id.ivRightBool);
        }
    }
}

