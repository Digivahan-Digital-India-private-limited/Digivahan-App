package com.digivahan.data.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.ChallanModel;
import com.digivahan.data.model.OffenceModel;
import com.digivahan.databinding.CheckChallanItemDesignBinding;
import com.digivahan.databinding.TipsItemDesignBinding;
import com.digivahan.utils.CommonLogic;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CheckChallanItemAdapter extends RecyclerView.Adapter<CheckChallanItemAdapter.CLViewHolder> {
    String TAG = "CheckChallanItemAdapterData";
    Context context;
    ArrayList<ChallanModel> list;

    public CheckChallanItemAdapter(Context context, ArrayList<ChallanModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CheckChallanItemAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CheckChallanItemDesignBinding binding = CheckChallanItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckChallanItemAdapter.CLViewHolder holder, int position) {
        ChallanModel model = list.get(position);

        setChallanData(holder, model);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        CheckChallanItemDesignBinding binding;
        public CLViewHolder(CheckChallanItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void setChallanData(@NonNull CheckChallanItemAdapter.CLViewHolder holder, ChallanModel challan) {
        CommonLogic.showTestLog(TAG, "🧩 setChallanData() called with challan: " + new Gson().toJson(challan));

        if (challan == null) {
            CommonLogic.showTestLog(TAG, "❌ Challan model is null, cannot set UI");
            Toast.makeText(context, "Invalid challan data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vehicle Info
        CommonLogic.showTestLog(TAG, "🚗 Setting vehicle info in UI");
        holder.binding.rcNumber.setText("RC Number: " + getSafeText(challan.getRcNumber()));
        holder.binding.accusedName.setText("Accused: " + getSafeText(challan.getAccusedName()));
        holder.binding.accusedFatherName.setText("Father’s Name: " + getSafeText(challan.getAccusedFatherName()));

        // Challan Summary
        CommonLogic.showTestLog(TAG, "💳 Setting challan summary data");
        holder.binding.challanNumber.setText("Challan No: " + getSafeText(challan.getChallanNumber()));
        holder.binding.challanDate.setText(getSafeText(challan.getChallanDate()));
        holder.binding.challanStatus.setText(getSafeText(challan.getChallanStatus()));
        holder.binding.challanAmount.setText("₹" + getSafeText(challan.getChallanAmount()));
        holder.binding.challanPlace.setText("Place: " + getSafeText(challan.getChallanPlace()));

        // Status color
        if ("Pending".equalsIgnoreCase(challan.getChallanStatus())) {
            CommonLogic.showTestLog(TAG, "⚠️ Challan status is PENDING — applying warning color");
            holder.binding.challanStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));
        } else {
            CommonLogic.showTestLog(TAG, "✅ Challan status is not pending — applying primary color");
            holder.binding.challanStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }

        // ✅ Set offences dynamically
        holder.binding.offencesContainer.removeAllViews();
        List<OffenceModel> offences = challan.getOffences();
        CommonLogic.showTestLog(TAG, "🧾 Total offences found: " + (offences != null ? offences.size() : 0));

        if (offences != null && !offences.isEmpty()) {
            for (OffenceModel offence : offences) {
                TextView offenceText = new TextView(context);
                offenceText.setText("• " + getSafeText(offence.getOffence_name()) +
                        " (" + getSafeText(offence.getMotor_vehicle_act()) + ")");
                offenceText.setTextSize(14);
                offenceText.setTypeface(Typeface.DEFAULT_BOLD);
                offenceText.setTextColor(ContextCompat.getColor(context, R.color.text_heading));
                offenceText.setPadding(8, 6, 8, 6);
                holder.binding.offencesContainer.addView(offenceText);

                CommonLogic.showTestLog(TAG, "📜 Added offence to UI: " +
                        offence.getOffence_name() + " | Act: " + offence.getMotor_vehicle_act());
            }
        } else {
            CommonLogic.showTestLog(TAG, "ℹ️ No offences found, showing placeholder text");
            TextView emptyView = new TextView(context);
            emptyView.setText("No offences recorded.");
            emptyView.setTextColor(ContextCompat.getColor(context, R.color.text_sub_heading_2));
            emptyView.setPadding(8, 4, 8, 4);
            holder.binding.offencesContainer.addView(emptyView);
        }

        CommonLogic.showTestLog(TAG, "🎯 Challan data successfully set in UI");
    }

    /**
     * Utility to handle null or empty text
     */
    private String getSafeText(String text) {
        return text == null || text.trim().isEmpty() ? "NA" : text.trim();
    }
}
