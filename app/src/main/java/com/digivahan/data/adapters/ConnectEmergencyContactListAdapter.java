package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.EmergencyContactModel;
import com.digivahan.databinding.EmergencyContactDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.emergencyContacts.EditEmergencyContacts;
import com.digivahan.ui.Activities.qr.ConnectEmergencyContactsPage;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectEmergencyContactListAdapter extends RecyclerView.Adapter<ConnectEmergencyContactListAdapter.CLViewHolder> {
    String TAG = "EmergencyContactListAdapterData";
    Activity context;
    ArrayList<EmergencyContactModel> list;

    PreferencesManager preferencesManager;

    AshDialog loadingDialog;
    Dialog callRequestDialog;


    public ConnectEmergencyContactListAdapter(Activity context, ArrayList<EmergencyContactModel> list, AshDialog loadingDialog, Dialog callRequestDialog) {
        this.context = context;
        this.list = list;
        this.loadingDialog = loadingDialog;
        this.callRequestDialog = callRequestDialog;
        preferencesManager = new PreferencesManager(context);
    }

    @NonNull
    @Override
    public ConnectEmergencyContactListAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EmergencyContactDesignBinding binding = EmergencyContactDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectEmergencyContactListAdapter.CLViewHolder holder, int position) {
        EmergencyContactModel model = list.get(position);

        holder.binding.contactBtnLayout.setVisibility(View.VISIBLE);
        holder.binding.userNumberLayout.setVisibility(View.GONE);

        ImageHelperMethods.loadImage(TAG, context, model.getProfile_pic(), holder.binding.userImage, R.drawable.temp_profile_icon);
        holder.binding.userName.setText(model.getFirst_name() + " " + model.getLast_name());
        holder.binding.userNumber.setText(model.getPhone_number());

        holder.binding.whatsAppBtn.setOnClickListener(v -> {
            CommonMethods.sendWhatsAppAlert(TAG, context, preferencesManager.getUser().getPhone_number(), model.getPhone_number());
        });

        holder.binding.callBtn.setOnClickListener(v -> {
            CommonMethods.showCallRequestDialog(TAG, context, callRequestDialog, preferencesManager.getUser().getPhone_number(), model.getPhone_number());
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        EmergencyContactDesignBinding binding;

        public CLViewHolder(EmergencyContactDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
