package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.EmergencyContactModel;
import com.digivahan.databinding.EmergencyContactDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.emergencyContacts.EditEmergencyContacts;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyContactListAdapter extends RecyclerView.Adapter<EmergencyContactListAdapter.CLViewHolder> {
    String TAG = "EmergencyContactListAdapterData";
    Context context;
    ArrayList<EmergencyContactModel> list;

    PreferencesManager preferencesManager;

    boolean isEditable;
    AshDialog loadingDialog;
    OnContactDeleteListener deleteListener;


    public EmergencyContactListAdapter(Context context, ArrayList<EmergencyContactModel> list, boolean isEditable, AshDialog loadingDialog, OnContactDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.isEditable = isEditable;
        this.loadingDialog = loadingDialog;
        this.deleteListener = deleteListener;
        preferencesManager = new PreferencesManager(context);
    }

    @NonNull
    @Override
    public EmergencyContactListAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EmergencyContactDesignBinding binding = EmergencyContactDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyContactListAdapter.CLViewHolder holder, int position) {
        EmergencyContactModel model = list.get(position);

        if (isEditable) {
            holder.binding.editBtnLayout.setVisibility(View.VISIBLE);
        } else {
            holder.binding.editBtnLayout.setVisibility(View.GONE);
        }

        ImageHelperMethods.loadImage(TAG, context, model.getProfile_pic(), holder.binding.userImage, R.drawable.temp_profile_icon);
        holder.binding.userName.setText(model.getFirst_name() + " " + model.getLast_name());
        holder.binding.userNumber.setText(model.getPhone_number());

        holder.binding.editBtn.setOnClickListener(v -> {
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            Intent editEmergencyContactPage = new Intent(context, EditEmergencyContacts.class);
            editEmergencyContactPage.putExtra("hit_type", "edit");
            editEmergencyContactPage.putExtra("emergencyContactItemData", model);
            context.startActivity(editEmergencyContactPage);
        });

        holder.binding.deleteBtn.setOnClickListener(v -> {
            if (list.size() < 2){
                Toast.makeText(context, "There should be at least one contact", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteContact(model.get_id(), position);
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

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }


    public interface OnContactDeleteListener {
        void onContactDeleted(int listSize);

        void onContactDeleteFailed(String errorMessage);
    }


    private void deleteContact(String contactId, int itemPosition) {
        // Show confirmation dialog before exiting registration
        new AlertDialog.Builder(context)
                .setTitle("Delete Contact")
                .setMessage("Are you sure? You want to remove it.")
                .setCancelable(false)
                .setPositiveButton("Yes, delete", (dialog, which) -> {

                    // to check data
                    JsonObject jsonObjectDeleteEmergencyContact = new JsonObject();
                    jsonObjectDeleteEmergencyContact.addProperty("user_id", preferencesManager.getUserId());
                    jsonObjectDeleteEmergencyContact.addProperty("contact_id", contactId);

                    CommonLogic.showTestLog(TAG, jsonObjectDeleteEmergencyContact.toString());

                    loadingDialog.show();

                    // --- Step 2: Make API call ---
                    ApiClient.getApiService(context).commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.DELETE_EMERGENCY_CONTACT, jsonObjectDeleteEmergencyContact).enqueue(new Callback<JsonObject>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            try {
                                JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                                CommonLogic.showTestLog(TAG, responseBody.toString());

                                boolean status = Objects.requireNonNull(responseBody).optBoolean("status", false);
                                String message = responseBody.optString("message", "Server error, Please try after some time.");

                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                                list.remove(itemPosition);
                                notifyItemChanged(itemPosition);

                                // 🔥 CALLBACK SUCCESS
                                if (deleteListener != null) {
                                    deleteListener.onContactDeleted(list.size());
                                } else {
                                    if (deleteListener != null) {
                                        deleteListener.onContactDeleteFailed(message);
                                    }
                                }
                            } catch (Exception e) {
                                CommonLogic.showTestLog(TAG, e.getMessage());
                            }

                            loadingDialog.dismiss();
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            // ❌ Network or unexpected failure
                            JsonObject errorObj = new JsonObject();
                            errorObj.addProperty("status", false);
                            errorObj.addProperty("message", t.getMessage());

                            loadingDialog.dismiss();

                            if (deleteListener != null) {
                                deleteListener.onContactDeleteFailed(t.getMessage());
                            }
                        }
                    });

                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}
