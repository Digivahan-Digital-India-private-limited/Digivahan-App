package com.digivahan.ui.Activities.documentVault;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.adapters.VehicleDocumentListAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.ActivityDocumentVaultBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.deliveryAddress.SetDefaultAddressPage;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentVaultActivity extends BaseActivity {

    String TAG = "DocumentVaultActivityData";
    ActivityDocumentVaultBinding binding;

    AshDialog loadingDialog;

    PreferencesManager manager;

    String docAccessType = "check", vehicleId = "", vehicleOwnerId = "";

    VehicleDocumentListAdapter documentListAdapter;
    ArrayList<GarageItemModel.vehicleDocuments> vehicleDocumentsArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentVaultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        loadingDialog = new AshDialog(DocumentVaultActivity.this, "Please wait", "");
        manager = new PreferencesManager(DocumentVaultActivity.this);

        getOnBackPressedDispatcher().addCallback(DocumentVaultActivity.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.notificationBellLayout.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Document Vault");

        try {
            vehicleId = getIntent().getStringExtra("vehicleId");
            vehicleOwnerId = getIntent().getStringExtra("vehicleOwnerId");
            docAccessType = getIntent().getStringExtra("docAccessType");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, "docAccessType not found");
        }

        if (!docAccessType.equalsIgnoreCase("verify")) {
            documentListAdapter = new VehicleDocumentListAdapter(DocumentVaultActivity.this, vehicleId, true, vehicleDocumentsArrayList, new VehicleDocumentListAdapter.OnDeleteClickListener() {
                @Override
                public void onDeleteClick(int listSize) {
                    if (listSize < 1){
                        binding.otpMessage.setVisibility(View.VISIBLE);
                        binding.noteText.setVisibility(View.VISIBLE);
                        binding.otpView.setOtp("");
                    }
                }
            });
            binding.otpMessage.setVisibility(View.VISIBLE);
        } else {
            documentListAdapter = new VehicleDocumentListAdapter(DocumentVaultActivity.this, vehicleId, false, vehicleDocumentsArrayList);
        }
        binding.otherDocumentList.setAdapter(documentListAdapter);

        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            back();
        });

        if (!docAccessType.equalsIgnoreCase("verify")) {
            checkDocAccessCode();
        }

        if (docAccessType.equalsIgnoreCase("verify")) {
            binding.otpView.setOtpCompleteListener(this::verifyDocAccessCode);
        }
    }

    private void verifyDocAccessCode(String otp) {
        JsonObject jsonObjectDocAccessRequest = new JsonObject();
        jsonObjectDocAccessRequest.addProperty("user_id", vehicleOwnerId);
        jsonObjectDocAccessRequest.addProperty("vehicle_id", vehicleId);
        jsonObjectDocAccessRequest.addProperty("security_code", otp);

        CommonLogic.showTestLog(TAG, "sendNotification param: " + jsonObjectDocAccessRequest.toString());

        loadingDialog.show();

        // --- Step 2: Make API call ---
        ApiClient.getApiService(DocumentVaultActivity.this).commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.DOC_ACCESS_VERIFICATION, jsonObjectDocAccessRequest).enqueue(new Callback<JsonObject>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                // Always dismiss loader
                loadingDialog.dismiss();

                try {
                    JSONObject docData = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);
                    CommonLogic.showTestLog(TAG, "response: " + docData.toString());


                    boolean status = Objects.requireNonNull(docData).has("success") && docData.getBoolean("success");
                    String message = "";
                    if (Objects.requireNonNull(docData).has("message")) {
                        message = docData.getString("message");
                    }

                    if (status) {
                        vehicleDocumentsArrayList.clear();
                        vehicleDocumentsArrayList.addAll(APIHelper.convertJsonArrayToList(docData.getJSONObject("data").getJSONArray("documents"), GarageItemModel.vehicleDocuments.class));
                        documentListAdapter.notifyDataSetChanged();

                        if (!vehicleDocumentsArrayList.isEmpty()) {
                            binding.documentLayout.setVisibility(View.VISIBLE);
                        } else {
                            binding.documentLayout.setVisibility(View.GONE);
                        }

                        binding.otpLayout.setVisibility(View.GONE);

                    } else {
                        CommonLogic.showTestLog(TAG, "http_code:- " + docData.getString("http_code"));
                        Toast.makeText(DocumentVaultActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    CommonLogic.showTestLog(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                loadingDialog.dismiss();

                // ❌ Network or unexpected failure
                JsonObject errorObj = new JsonObject();
                errorObj.addProperty("status", false);
                errorObj.addProperty("message", t.getMessage());
            }
        });

    }

    private void checkDocAccessCode() {
        JsonObject jsonObjectDocAccessRequest = new JsonObject();
        jsonObjectDocAccessRequest.addProperty("user_id", manager.getUserId());
        jsonObjectDocAccessRequest.addProperty("vehicle_id", vehicleId);

        CommonLogic.showTestLog(TAG, "sendNotification param: " + jsonObjectDocAccessRequest.toString());

        loadingDialog.show();


        ApiCall.callApi(TAG,
                DocumentVaultActivity.this,
                APIData.DOC_ACCESS_CHECK,
                jsonObjectDocAccessRequest, "post",
                new ApiCall.ApiResponseCallback() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, "response: " + responseBody.toString());

                                vehicleDocumentsArrayList.clear();
                                vehicleDocumentsArrayList.addAll(APIHelper.convertJsonArrayToList(responseBody.getJSONArray("vehicle_doc_data"), GarageItemModel.vehicleDocuments.class));

                                binding.otpView.setOtpReadOnly(true);

                                if (!vehicleDocumentsArrayList.isEmpty()) {
                                    binding.documentLayout.setVisibility(View.VISIBLE);
                                    binding.otpView.setOtp(responseBody.getString("security_code"));
                                } else {
                                    binding.documentLayout.setVisibility(View.GONE);
                                    binding.noteText.setVisibility(View.VISIBLE);
                                }

                                documentListAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(DocumentVaultActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "response error: " + e);
                        }
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(QROwnerContactDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void back() {
        new AlertDialog.Builder(DocumentVaultActivity.this)
                .setTitle("Alert")
                .setMessage("Are you sure? You want to close this page.")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    disableHideContentSecureForNextNavigation();
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}