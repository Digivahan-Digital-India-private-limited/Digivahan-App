package com.digivahan.ui.Activities.qr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.ashu.ashuutils.fileUtils.FileUtils;
import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.QRDataModel;
import com.digivahan.databinding.ActivityScanQrcodeBinding;
import com.digivahan.databinding.ProfileIncompleteDialogDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.MainActivity;

import com.digivahan.ui.Activities.garage.MyGarageActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.NetworkUtils;
import com.ashu.barcodereader.BarcodeCapture;
import com.ashu.barcodereader.BarcodeGraphic;
import com.ashu.barcodereader.mobilevisionbarcodescanner.BarcodeBitmapScanner;
import com.ashu.barcodereader.mobilevisionbarcodescanner.BarcodeRetriever;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScanQRCode extends BaseActivity implements BarcodeRetriever {

    String TAG = "ScanQRCodeData";
    ActivityScanQrcodeBinding binding;
    BarcodeCapture barcodeCapture;
    int checkBarcode = 0;

    boolean showFlash = false;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    PreferencesManager manager;

    AshDialog loadingDialog;

    String scanType = "connect";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanQrcodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.divider.setVisibility(View.GONE);
        binding.toolbarLayout.toolbar.setBackgroundColor(ContextCompat.getColor(ScanQRCode.this, R.color.Background_Color));
//        binding.toolbarLayout.tvTitle.setText("Scan QR Code");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                /*Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);*/
                finish();
            }
        });

        manager = new PreferencesManager(ScanQRCode.this);

        try {
            if (getIntent().hasExtra("scanType")){
                scanType = getIntent().getStringExtra("scanType");
            }
        } catch (Exception e) {
            scanType = "connect";
            CommonLogic.showTestLog(TAG, e.getMessage());
        }


        // to change StatusBar Color
        getWindow().setStatusBarColor(ContextCompat.getColor(ScanQRCode.this, R.color.Background_Color));

        if (scanType.equalsIgnoreCase("assign")){
            binding.scanTitle.setText("Scan To Activate");
            binding.scanMessage.setText("Scan DigiVahan QR Code to Activate");
        }

        loadingDialog = new AshDialog(ScanQRCode.this, "Please wait", "");

        barcodeCapture = (BarcodeCapture) getSupportFragmentManager()
                .findFragmentById(R.id.barcode);

        if (FileUtils.isCameraPermissionGranted(ScanQRCode.this)) {
            barcodeCapture.setRetrieval(this);
            startScanner();
        } else {
            FileUtils.requestCameraPermission(ScanQRCode.this);
        }



        binding.flashBtn.setOnClickListener(view -> {

            showFlash = !showFlash;
            barcodeCapture.setShowFlash(showFlash);
            barcodeCapture.refresh(true);

            if (showFlash) {
                // Flash ON → Blue icon
                binding.flashBtn.setColorFilter(
                        ContextCompat.getColor(this, R.color.color_blue1),
                        PorterDuff.Mode.SRC_IN
                );
            } else {
                // Flash OFF → Primary button color
                binding.flashBtn.setColorFilter(
                        ContextCompat.getColor(this, R.color.primary_button),
                        PorterDuff.Mode.SRC_IN
                );
            }
        });


        binding.galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        // Initialize launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            decodeQRFromImage(imageUri);
                        }
                    }
                }
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }


    // take image from gallery
    private void decodeQRFromImage(Uri imageUri) {
        try {
            // Load image as Bitmap
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Scan bitmap using your BarcodeBitmapScanner
            BarcodeBitmapScanner.scanBitmap(
                    ScanQRCode.this,
                    bitmap,
                    Barcode.QR_CODE, // Only scan QR Codes
                    new BarcodeRetriever() {
                        @Override
                        public void onRetrieved(Barcode barcode) {
                            // Single barcode retrieved
                            if (barcode != null) {
                                String qrData = barcode.displayValue;
//                                Toast.makeText(ScanQRCode.this, "QR Code: " + qrData, Toast.LENGTH_LONG).show();
                                handleQRCodeResult(qrData);
                            } else {
//                                Toast.makeText(ScanQRCode.this, "No QR code found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onRetrievedMultiple(Barcode closestToClick, List<BarcodeGraphic> barcodeGraphics) {
                            // Handle multiple barcodes if your system ever supports that
                            if (closestToClick != null) {
                                String qrData = closestToClick.displayValue;
//                                Toast.makeText(ScanQRCode.this, "Multiple Detected, Closest: " + qrData, Toast.LENGTH_LONG).show();
                                handleQRCodeResult(qrData);
                            } else {
//                                Toast.makeText(ScanQRCode.this, "Multiple QR codes found, but none selected", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
                            // Handles scanning from gallery image
                            if (sparseArray != null && sparseArray.size() > 0) {
                                Barcode barcode = sparseArray.valueAt(0); // Take first detected barcode
                                String qrData = barcode.displayValue;
//                                Toast.makeText(ScanQRCode.this, "QR Code: " + qrData, Toast.LENGTH_LONG).show();
                                handleQRCodeResult(qrData);
                            } else {
//                                Toast.makeText(ScanQRCode.this, "No QR code found in this image", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onRetrievedFailed(String reason) {
//                            Toast.makeText(ScanQRCode.this, "Failed: " + reason, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRequestDenied() {
//                            Toast.makeText(ScanQRCode.this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void handleQRCodeResult(String qrData) {
        qrData = qrData.replace("\"", "").trim();
        barcodeCapture.stopScanning();
        // Handle your QR result here
        CommonLogic.showTestLog(TAG, "QR Code Scanned: " + qrData);

        String qrId = "";
        try {
            if (qrData != null && qrData.contains("/")) {
                qrId = qrData.substring(qrData.lastIndexOf("/") + 1);

// ✅ Log for debugging
                CommonLogic.showTestLog(TAG, "qrId: " + qrId);

                loadingDialog.show();

                ApiCall.callApi(TAG,
                        ScanQRCode.this,
                        APIData.GET_QR_CODE_BY_ID + qrId,
                        null, "get",
                        new ApiCall.ApiResponseCallback() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                                try {
                                    if (status) {
                                        CommonLogic.showTestLog(TAG, "API Success Response: " + responseBody.toString());

                                        if (responseBody.has("data")) {
                                            CommonLogic.showTestLog(TAG, "Response contains data object");

                                            QRDataModel qrItem = APIHelper.convertJsonToModel(responseBody.getJSONObject("data"), QRDataModel.class);

                                            CommonLogic.showTestLog(TAG, "Parsed QR Item: " + qrItem.toString());
                                            CommonLogic.showTestLog(TAG, "Scan Type: " + scanType);
                                            CommonLogic.showTestLog(TAG, "QR Status: " + qrItem.getQr_status());
                                            CommonLogic.showTestLog(TAG, "Assigned To: " + qrItem.getAssigned_to());
                                            CommonLogic.showTestLog(TAG, "Vehicle ID: " + qrItem.getVehicle_id());

                                            // Case 1: Already assigned but trying to assign again
                                            if (scanType.equalsIgnoreCase("assign") &&
                                                    qrItem.getQr_status().equalsIgnoreCase("assigned")) {

                                                CommonLogic.showTestLog(TAG, "QR already assigned. Showing error dialog.");
                                                showScannedDialog(ScanQRCode.this, qrItem, "", "error");
                                                return;
                                            }

                                            // Case 2: Same user already logged in
                                            if (qrItem.getAssigned_to() != null &&
                                                    !qrItem.getAssigned_to().isEmpty() &&
                                                    manager.getUserId().equalsIgnoreCase(qrItem.getAssigned_to())) {

                                                CommonLogic.showTestLog(TAG, "Same user already assigned to this QR on this device.");
                                                showAlertDialog("This user already login into your device.");
                                                return;
                                            }

                                            CommonLogic.showTestLog(TAG, "Creating custom dialog");

                                            android.app.AlertDialog.Builder builder =
                                                    new android.app.AlertDialog.Builder(ScanQRCode.this, R.style.CustomDialogTheme);

                                            @SuppressLint("InflateParams")
                                            View view = LayoutInflater.from(ScanQRCode.this)
                                                    .inflate(R.layout.profile_incomplete_dialog_design, null);

                                            ProfileIncompleteDialogDesignBinding dialogBinding =
                                                    ProfileIncompleteDialogDesignBinding.bind(view);

                                            builder.setView(view);

                                            android.app.AlertDialog dialog = builder.create();

                                            CommonLogic.showTestLog(TAG,
                                                    "Invalid flow detected. ScanType: "
                                                            + scanType + ", QR Status: "
                                                            + qrItem.getQr_status()
                                                            + ", QR Assign_to: "
                                                            + qrItem.getAssigned_to());

                                            // Case 3: Connect flow
                                            if (scanType.equalsIgnoreCase("connect") &&
                                                    qrItem.getQr_status().equalsIgnoreCase("assigned") &&
                                                    qrItem.getAssigned_to() != null &&
                                                    !qrItem.getAssigned_to().isEmpty()) {

                                                CommonLogic.showTestLog(TAG, "Connect flow triggered. Navigating to QROwnerContactDetailsActivity");

                                                disableHideContentSecureForNextNavigation();

                                                Intent intent = new Intent(getApplicationContext(), QROwnerContactDetailsActivity.class);
                                                intent.putExtra("receiverId", qrItem.getAssigned_to());

                                                if (qrItem.getVehicle_id() != null &&
                                                        !qrItem.getVehicle_id().isEmpty()) {

                                                    CommonLogic.showTestLog(TAG, "Passing vehicle ID: " + qrItem.getVehicle_id());
                                                    intent.putExtra("vehicleId", qrItem.getVehicle_id());
                                                }

                                                startActivity(intent);
                                                finish();
                                            }

                                            // Case 4: Assign flow - QR unassigned
                                            else if (scanType.equalsIgnoreCase("assign") &&
                                                    qrItem.getQr_status().equalsIgnoreCase("unassigned")) {

                                                CommonLogic.showTestLog(TAG, "Assign flow triggered. Showing activation dialog.");

                                                dialog.show();

                                                if (dialog.getWindow() != null) {
                                                    dialog.getWindow().setLayout(
                                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                                    );
                                                }

                                                dialog.setCancelable(false);
                                                dialog.setCanceledOnTouchOutside(false);

                                                dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                                                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                        CommonLogic.showTestLog(TAG, "Back button blocked on dialog");
                                                        return true;
                                                    }
                                                    return false;
                                                });

                                                if (dialog.getWindow() != null) {
                                                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                                    dialog.getWindow().setSoftInputMode(
                                                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                                                    );
                                                }

                                                dialogBinding.closeBtn.setVisibility(View.VISIBLE);

                                                dialogBinding.closeBtn.setOnClickListener(v -> {
                                                    CommonLogic.showTestLog(TAG, "Dialog closed by user");
                                                    dialog.dismiss();
                                                    barcodeCapture.refresh(true);
                                                });

                                                dialogBinding.tvTitle.setText("Activate QR Code");
                                                dialogBinding.tvSubTitle.setText("Please add vehicle number to activate this QR code.");

                                                dialogBinding.vehicleNumberField.ivIcon.setVisibility(View.GONE);
                                                dialogBinding.vehicleNumberField.rootLayout.setVisibility(View.VISIBLE);
                                                dialogBinding.vehicleNumberField.etInput.setHint("Vehicle Number");

                                                dialogBinding.vehicleNumberField.etInput.setInputType(
                                                        InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                                                Button openLoginPageBtn = view.findViewById(R.id.openProfileBtn);
                                                openLoginPageBtn.setText("Connect");

                                                openLoginPageBtn.setOnClickListener(v -> {

                                                    String userInput =
                                                            dialogBinding.vehicleNumberField.etInput
                                                                    .getText().toString().trim();

                                                    CommonLogic.showTestLog(TAG, "Vehicle number entered: " + userInput);

                                                    if (userInput.isEmpty()) {

                                                        CommonLogic.showTestLog(TAG, "Vehicle number is empty");

                                                        Toast.makeText(
                                                                ScanQRCode.this,
                                                                "Field can't be empty",
                                                                Toast.LENGTH_SHORT).show();

                                                        dialogBinding.vehicleNumberField.etInput
                                                                .setError("Field can't be empty");

                                                        return;
                                                    }

                                                    CommonLogic.showTestLog(TAG, "Calling checkGarageVehicle API");

                                                    loadingDialog.show();

                                                    checkGarageVehicle(
                                                            ScanQRCode.this,
                                                            qrItem,
                                                            userInput);
                                                });

                                                loadingDialog.dismiss();
                                            }

                                            // Case 5: Invalid flow
                                            else {

                                                CommonLogic.showTestLog(TAG,
                                                        "Invalid flow detected. ScanType: "
                                                                + scanType + ", QR Status: "
                                                                + qrItem.getQr_status());

                                                if (scanType.equalsIgnoreCase("assign")) {

                                                    dialog.dismiss();

                                                    CommonLogic.showTestLog(TAG,
                                                            "Showing scanned dialog error");

                                                    showScannedDialog(
                                                            ScanQRCode.this,
                                                            qrItem,
                                                            "",
                                                            "error");

                                                } else {

                                                    CommonLogic.showTestLog(TAG,
                                                            "Showing invalid QR alert");

                                                    showAlertDialog(
                                                            "Invalid Qr Code, Please try again.");
                                                }
                                            }

                                        } else {

                                            CommonLogic.showTestLog(TAG,
                                                    "Response does NOT contain data");

                                            showAlertDialog(
                                                    "Invalid Qr Code, Please try again.");

                                            loadingDialog.dismiss();
                                        }

                                    } else {

                                        loadingDialog.dismiss();

                                        CommonLogic.showTestLog(TAG,
                                                "API Failed. Message: " + message);

                                        showAlertDialog(
                                                "Invalid Qr Code, Please try again.");
                                    }
                                } catch (Exception e) {
                                    loadingDialog.dismiss();
                                    CommonLogic.showTestLog(TAG, e.getMessage());
                                    showAlertDialog("Invalid Qr Code, Please try again.");
                                }

                            }

                            @Override
                            public void onError(String errorMessage) {
                                loadingDialog.dismiss();
                                CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                showAlertDialog("Invalid Qr Code, Please try again.");
                            }
                        }
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadingDialog.dismiss();
        }
    }

    private void checkGarageVehicle(Activity context, QRDataModel qrItem, String vehicleNumber) {

        PreferencesManager preferencesManager = new PreferencesManager(context);

        CommonLogic.showTestLog(TAG, "user id" + preferencesManager.getUserId());

        loadingDialog.show();
        CommonMethods.getGarageVehicleList(TAG,
                context,
                preferencesManager.getUserId(),
                new CommonMethods.GarageListCallback() {

                    @Override
                    public void onSuccess(ArrayList<GarageItemModel> garageList) {

                        loadingDialog.dismiss();
                        boolean isVehicleAvailable = false;

                        for (GarageItemModel model : garageList){
                            if (model.getVehicle_number().equalsIgnoreCase(vehicleNumber)){
                                isVehicleAvailable = true;
                                break;
                            }
                        }

                        if (isVehicleAvailable){
                            showScannedDialog(ScanQRCode.this, qrItem, vehicleNumber, "assign");
                        }else {
                            showScannedDialog(ScanQRCode.this, qrItem, vehicleNumber, "noVehicle");
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        loadingDialog.dismiss();
                        CommonLogic.showTestLog(TAG, "getGarageVehicleList: " + errorMessage);
                    }
                }
        );

    }

    public void showScannedDialog(Activity context, QRDataModel qrItem, String vehicleNumber, String responseType) {
        loadingDialog.dismiss();
        // Create a dialog instance
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.crop_image_dialog_design);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize dialog elements
        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        TextView btnNo = dialog.findViewById(R.id.btnNo);
        TextView btnYes = dialog.findViewById(R.id.btnYes);

        if (responseType.equalsIgnoreCase("assign")) {
            // Set message
            dialogMessage.setText("Are you sure you want to connect this "+ "("+ vehicleNumber +")" +" vehicle?");
        }else if (responseType.equalsIgnoreCase("noVehicle")) {
            dialogMessage.setText("There is no "+ "("+ vehicleNumber +")" +" vehicle in your garage, Please add it first?");
            btnNo.setText("Retry");
            btnYes.setText("Add Vehicle");
        }else if (responseType.equalsIgnoreCase("error")) {
//            dialogMessage.setText("Invalid or already used QR code");
            dialogMessage.setText("Either the QR code is invalid or it has already been used by another user");
            btnNo.setText("quit");
            btnYes.setText("Retry");
        }

        // true condition if user not have dl
        // Handle button clicks
        btnNo.setOnClickListener(v -> {
            dialog.dismiss();
            if (responseType.equalsIgnoreCase("error")) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            if (responseType.equalsIgnoreCase("assign")) {
                assignQR(vehicleNumber, qrItem);
            } else if (responseType.equalsIgnoreCase("error")) {
                barcodeCapture.refresh(true);
            } else if (responseType.equalsIgnoreCase("noVehicle")) {
                disableHideContentSecureForNextNavigation();
                Intent mainPage = new Intent(ScanQRCode.this, MyGarageActivity.class);
                startActivity(mainPage);
                finish();
            }

        });

        // Show the dialog
        dialog.show();

        // Show the dialog
        dialog.show();

// Force full width
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

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
                ScanQRCode.this,
                APIData.ASSIGN_QR_CODE,
                jsonObjectCreateQR, "post",
                new ApiCall.ApiResponseCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                        try {
                            if (status) {
                                CommonLogic.showTestLog(TAG, responseBody.toString());
                                Toast.makeText(ScanQRCode.this, "QR Code activated successfully", Toast.LENGTH_SHORT).show();
                                getOnBackPressedDispatcher().onBackPressed();

                            } else {
                                loadingDialog.dismiss();
                                Toast.makeText(ScanQRCode.this, "QR not activated, please try after some time.", Toast.LENGTH_SHORT).show();
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

    private void showAlertDialog(String message) {
        // Example: navigate or pass data
        loadingDialog.dismiss();

        if (!NetworkUtils.Connected(ScanQRCode.this)){
            message = "Please check your internet connection.";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCode.this)
                .setTitle("Alert")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> {
                    barcodeCapture.refresh(true);
                    dialog.dismiss();
                }).setNegativeButton("quit", (dialogInterface, i) -> getOnBackPressedDispatcher().onBackPressed());
        builder.show();
    }


    @Override
    public void onRetrieved(Barcode barcode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String qrData = barcode.displayValue;
                handleQRCodeResult(qrData);
                /*
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCode.this)
                        .setTitle("code retrieved")
                        .setMessage(message)
                        .setPositiveButton("Ok", (dialog, which) -> {
                            Intent main = new Intent(getApplicationContext(), QROwnerContactDetailsActivity.class);
                            finish();
                            startActivity(main);
                        });
                builder.show();*/



                /*// keys to to data.. id, name, key_qrcode, expiring_date, grp_join_appr_wall, created_by, is_start_now, strt_date, is_freeze
                if (!barcode.displayValue.isEmpty()){
                    if (checkBarcode == 0){
                        checkBarcode = 1;
                        barcodeCapture.stopScanning();
                        dialog.show();
                        JSONObject qrResult;
                        try {
                            qrResult = new JSONObject(barcode.displayValue);

                            Call<JsonObject> call = ApiController.getInstance(ScanQRCode.this).getapi().getGroupDetail(qrResult.optString("id"));

                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    Gson gson = new Gson();
                                    JSONObject jsonResponse = ApiSet.getResponseData(response);
                                    if (jsonResponse.optString("status").equals("true")){
                                        JSONObject dataJsonObject = jsonResponse.optJSONObject("data");
                                        GroupInfoModel groupInfoModel = gson.fromJson(Objects.requireNonNull(dataJsonObject).toString(), GroupInfoModel.class);
                                        if (!qrResult.optString("key_qrcode").equals(groupInfoModel.getKey_qrcode())){
                                            showDialog("This QR has been expired");
                                        }else if (qrResult.optString("is_freeze").equals("1")){
                                            showDialog("This group has been freeze, you can't join it right now.");
                                        }
                                        else if (qrResult.optString("grp_join_appr_wall").equals("1")){
                                            sendGroupJoinRequest(qrResult, groupInfoModel);
                                        }
                                        else if (qrResult.optString("grp_join_appr_wall").equals("0")){

                                            Call<JsonObject> call1 = ApiController.getInstance(ScanQRCode.this).getapi()
                                                    .addGroupMember(qrResult.optString("id"), helper.getCurrentUserData().getId());
                                            call1.enqueue(new Callback<JsonObject>() {
                                                  @Override
                                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                    dialog.dismiss();
                                                    helper.setCurrentGroup(groupInfoModel, true);
                                                    Intent groupInfo = new Intent(getApplicationContext(), GroupItemInfoPage.class);
                                                    finish();
                                                    startActivity(groupInfo);
                                                }

                                                @Override
                                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    }
                                    else {
                                        dialog.dismiss();
                                        showDialog("This group is no longer exist");
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    dialog.dismiss();
                                }
                            });

                        } catch (JSONException e) {
                            //throw new RuntimeException(e);
                            dialog.dismiss();
                        }

                    }
                }*/
            }
        });
        barcodeCapture.stopScanning();
    }

    @Override
    public void onRetrievedMultiple(Barcode closetToClick, List<BarcodeGraphic> barcodeGraphics) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = "Code selected : " + closetToClick.displayValue + "\n\nother " +
                        "codes in frame include : \n";
                for (int index = 0; index < barcodeGraphics.size(); index++) {
                    Barcode barcode = barcodeGraphics.get(index).getBarcode();
//                    message += (index + 1) + ". " + barcode.displayValue + "\n";

                    handleQRCodeResult(barcode.displayValue);
                    break;
                }
                /*AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCode.this)
                        .setTitle("code retrieved")
                        .setMessage(message);
                builder.show();*/
            }
        });
    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
        for (int i = 0; i < sparseArray.size(); i++) {
            Barcode barcode = sparseArray.valueAt(i);
            Log.e("value", barcode.displayValue);
        }
    }

    @Override
    public void onRetrievedFailed(String reason) {

    }

    @Override
    public void onPermissionRequestDenied() {

    }

    public void showDialog(String message) {
//        dialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCode.this)
                .setTitle("code retrieved")
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkBarcode = 0;
                        dialogInterface.dismiss();
                        barcodeCapture.refresh(true);
                    }
                });
        builder.show();

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FileUtils.CAMARA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                CommonLogic.showTestLog(TAG, "Camera permission granted");

                if (barcodeCapture != null) {
                    barcodeCapture.setRetrieval(this);
                    startScanner(); // 🔥 THIS FIXES BLACK SCREEN
                }

            } else {
                CommonLogic.showTestLog(TAG, "Camera permission denied");

                showPermissionDeniedDialog();
            }
        }
    }

    private void startScanner() {
        if (barcodeCapture != null) {
            barcodeCapture.refresh(true);
            CommonLogic.showTestLog(TAG, "Scanner started/refreshed");
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Camera Permission Required")
                .setMessage("Camera access is required to scan QR codes.")
                .setCancelable(false)
                .setPositiveButton("Refresh", (dialog, which) -> {
                    disableHideContentSecureForNextNavigation();
                    Intent userRequestPage = new Intent(this, ScanQRCode.class);
                    userRequestPage.putExtra("scanType", scanType);
                    startActivity(userRequestPage);
                    finish();

                    /*if (FileUtils.isCameraPermissionGranted(ScanQRCode.this)) {
                        barcodeCapture.setRetrieval(this);
                        startScanner();
                    } else {
                        FileUtils.requestCameraPermission(ScanQRCode.this);
                    }*/
                })
                .setNegativeButton("Cancel", (dialog, which) -> getOnBackPressedDispatcher().onBackPressed())
                .show();
    }



    /*public void sendGroupJoinRequest(JSONObject jsonObject, GroupInfoModel groupInfoModel){
        Call<JsonObject> call = ApiController.getInstance(ScanQRCode.this).getapi().
                sendGroupJoinRequest(jsonObject.optString("created_by"), "2", jsonObject.optString("id"),
                        jsonObject.optString("key_qrcode"), helper.getCurrentUserData().getId());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                dialog.dismiss();
                JSONObject jsonResponse = ApiSet.getResponseData(response);
                if (jsonResponse.optString("status").equals("true") ){
                    showDialog("Request has been send to admin");
                }
                else if (jsonResponse.optString("status").equals("false") &&
                        jsonResponse.optString("message").toLowerCase(Locale.ROOT).
                                equalsIgnoreCase("Already requested".toLowerCase(Locale.ROOT))) {
                    showDialog("Request already has been send to admin");
                } else {
                    if (jsonResponse.optString("message").toLowerCase(Locale.ROOT).
                            equalsIgnoreCase("Already member of that group".toLowerCase(Locale.ROOT))){
                        helper.setCurrentGroup(groupInfoModel, true);
                        Intent groupInfo = new Intent(getApplicationContext(), GroupItemInfoPage.class);
                        finish();
                        startActivity(groupInfo);
                    }
                    else {
                        showDialog("Unable to read QR");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                dialog.dismiss();
            }
        });
    }*/

}