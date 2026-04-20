package com.digivahan.ui.Activities.qr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.QRDataModel;
import com.digivahan.databinding.ActivityVirtualQrBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Objects;

public class VirtualQR extends BaseActivity {
    String TAG = "VirtualQRData";
    ActivityVirtualQrBinding binding;
    GarageItemModel virtualQRDetails;

    PreferencesManager manager;

    AshDialog loadingDialog;
    QRDataModel qrItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVirtualQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("My  Virtual QR Code");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                Intent intent = new Intent(VirtualQR.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


        manager = new PreferencesManager(VirtualQR.this);
        loadingDialog = new AshDialog(VirtualQR.this, "Please wait", "");

        try {
            virtualQRDetails = (GarageItemModel) getIntent().getSerializableExtra("virtualQRDetails");
            checkQR(Objects.requireNonNull(virtualQRDetails).getVehicle_id());
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        binding.vehicleName.setText(virtualQRDetails.getVehicle_name());
        binding.vehicleNumber.setText(virtualQRDetails.getVehicle_number());
        binding.ownerName.setText(virtualQRDetails.getOwner_name());
        binding.emailField.setText(manager.getUser().getEmail());
        binding.phoneNumber.setText(manager.getUser().getPhone_number());

        binding.downloadQR.setOnClickListener(view -> {
            if (qrItem != null && qrItem.get_id() != null && !qrItem.get_id().isEmpty()) {
                downloadImage(qrItem.getQr_id());
            }else {
                Toast.makeText(this, "Unable to download QR Code.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkQR(String vehicleId) {
        JsonObject jsonObjectFuelPrice = new JsonObject();
        jsonObjectFuelPrice.addProperty("user_id", manager.getUserId());
        jsonObjectFuelPrice.addProperty("vehicle_id", vehicleId);

        CommonLogic.showTestLog(TAG, "checkQR params: " + jsonObjectFuelPrice.toString());

        loadingDialog.show();

        ApiCall.callApi(TAG,
                VirtualQR.this,
                APIData.CHECK_QR_CODE,
                jsonObjectFuelPrice, "post",
                new ApiCall.ApiResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        CommonLogic.showTestLog(TAG, "showAddVehicleBottomSheet: " + responseBody.toString());
                        try {
                            if (status) {
                                qrItem = APIHelper.convertJsonToModel(responseBody.getJSONObject("data"), QRDataModel.class);
                                ImageHelperMethods.loadImage(TAG, VirtualQR.this, qrItem.getQr_img(), binding.qrCodeImage, R.drawable.temp_qr);
                                CommonLogic.showTestLog(TAG, "QR Data: " + qrItem);
                                loadingDialog.dismiss();
                            } else {
                                createQRCode(vehicleId);
                            }
                        } catch (Exception e) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, "QR Data error: " + e);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(MyGarageActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void createQRCode(String vehicleId) {
        JsonObject jsonObjectCreateQR = new JsonObject();
        jsonObjectCreateQR.addProperty("unit", 1);


// ✅ Log for debugging
        CommonLogic.showTestLog(TAG, "createQRCode Final JSON to send: " + jsonObjectCreateQR.toString());

        loadingDialog.show();

        ApiCall.callApi(TAG,
                VirtualQR.this,
                APIData.CREATE_QR_CODE,
                jsonObjectCreateQR, "post",
                new ApiCall.ApiResponseCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, responseBody.toString());


                                if (responseBody.has("data") && responseBody.getJSONArray("data").length() > 0) {
                                    qrItem = APIHelper.convertJsonToModel(responseBody.getJSONArray("data").getJSONObject(0), QRDataModel.class);
                                    CommonLogic.showTestLog(TAG, "QR Data: " + qrItem.toString());

                                    if (qrItem != null) {
                                        assignQR(vehicleId, qrItem);
                                    } else {
                                        Toast.makeText(VirtualQR.this, "Can't create your Order right now.", Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                } else {
                                    Toast.makeText(VirtualQR.this, "Can't create your Order right now.", Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();
                                }

                            } else {
                                loadingDialog.dismiss();
                                Toast.makeText(VirtualQR.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }

                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void assignQR(String vehicleId, QRDataModel qrItem) {
        JsonObject jsonObjectCreateQR = new JsonObject();

        jsonObjectCreateQR.addProperty("qr_id", qrItem.getQr_id());
        jsonObjectCreateQR.addProperty("assign_to", manager.getUserId());
        jsonObjectCreateQR.addProperty("assigned_by", "user");
        jsonObjectCreateQR.addProperty("product_type", "vehicle");
        jsonObjectCreateQR.addProperty("vehicle_id", vehicleId);


// ✅ Log for debugging
        CommonLogic.showTestLog(TAG, "createQRCode Final JSON to send: " + jsonObjectCreateQR.toString());

        loadingDialog.show();

        ApiCall.callApi(TAG,
                VirtualQR.this,
                APIData.ASSIGN_QR_CODE,
                jsonObjectCreateQR, "post",
                new ApiCall.ApiResponseCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, responseBody.toString());
                                ImageHelperMethods.loadImage(TAG, VirtualQR.this, qrItem.getQr_img(), binding.qrCodeImage, R.drawable.temp_qr);

                            } else {
                                loadingDialog.dismiss();
                                Toast.makeText(VirtualQR.this, "QR not created, please try after some time.", Toast.LENGTH_SHORT).show();
                            }
                            loadingDialog.dismiss();
                        } catch (Exception e) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }

                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private static final int STORAGE_PERMISSION_REQUEST = 101;

    private void downloadImage(String qrId) {

        loadingDialog.show();

        CommonLogic.showTestLog(TAG, APIData.GET_QR_TEMPLATE + qrId);

        JsonObject jsonObjectVehicleType = new JsonObject();
        jsonObjectVehicleType.addProperty("template_type", "");
        if (virtualQRDetails.getVehicle_class().toLowerCase().contains("2wn")) {
            jsonObjectVehicleType.addProperty("template_type", "bike");
        }


        ApiCall.callApi(TAG,
                VirtualQR.this,
                APIData.GET_QR_TEMPLATE + qrId,
                jsonObjectVehicleType, "post",
                new ApiCall.ApiResponseCallback() {

                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (!status) {
                                loadingDialog.dismiss();
                                Toast.makeText(VirtualQR.this,
                                        "Unable to download QR Code.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONObject QrData = responseBody.getJSONObject("data");

                            if (!QrData.has("template_url")) {
                                loadingDialog.dismiss();
                                Toast.makeText(VirtualQR.this,
                                        "QR template not found.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String templateUrl = QrData.getString("template_url");

                            // 🔹 ANDROID 9 AND BELOW (Permission required)
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {

                                if (ContextCompat.checkSelfPermission(
                                        VirtualQR.this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED) {

                                    ActivityCompat.requestPermissions(
                                            VirtualQR.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            STORAGE_PERMISSION_REQUEST
                                    );
                                    return;
                                }
                            }

                            // 🔹 ANDROID 10+ (NO permission needed)
                            CommonMethods.downloadImageIfNotExists(
                                    TAG,
                                    VirtualQR.this,
                                    templateUrl,
                                    "Digivahan QRs",
                                    loadingDialog
                            );

                        } catch (Exception e) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, e.getMessage());
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, errorMessage);
                    }
                }
        );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                CommonMethods.downloadImageIfNotExists(TAG, VirtualQR.this, qrItem.getQr_img(), "Digivahan QRs", loadingDialog);

            } else {
                Toast.makeText(VirtualQR.this,
                        "Storage permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}