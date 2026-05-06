package com.digivahan.ui.Activities.garage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.ashu.ashuutils.ImagePickerAppConstants;
import com.ashu.ashuutils.fileUtils.FileUtils;
import com.ashu.ashuutils.fileUtils.image.ImagePicker;
import com.ashu.ashuutils.fileUtils.image.ImageProcessingUtils;
import com.ashu.ashuutils.models.CompressFileData;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.SavedImageData;
import com.digivahan.databinding.ActivityMyGarageDocumentPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.chat.ChatActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyGarageDocumentPage extends BaseActivity {
    String TAG = "MyGarageDocumentPageData";
    ActivityMyGarageDocumentPageBinding binding;
    PreferencesManager manager;

    private GarageItemModel vehicleInfo;
    File documentFile;
    String documentPath;

    AshDialog loadingDialog;

    String selectedDocumentType = "Aadhar";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyGarageDocumentPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("My Garage");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            back();
        });

        loadingDialog = new AshDialog(MyGarageDocumentPage.this, "Please wait", "");

        try {
            if (getIntent().hasExtra("vehicleInfo")) {
                vehicleInfo = (GarageItemModel) getIntent().getSerializableExtra("vehicleInfo");
                CommonLogic.showTestLog(TAG, "vehicle_id: " + vehicleInfo.getVehicle_id());
            }

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        getOnBackPressedDispatcher().addCallback(MyGarageDocumentPage.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        manager = new PreferencesManager(MyGarageDocumentPage.this);

        List<String> documentTypeList = new ArrayList<>();
        documentTypeList.add("Aadhar");
        documentTypeList.add("Pollution");
        documentTypeList.add("Insurance");
        documentTypeList.add("RC");
        documentTypeList.add("Pancard");
        documentTypeList.add("Driving Licence");
//        documentTypeList.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                documentTypeList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDocumentType.setAdapter(adapter);

        /* ✅ Item Selected Listener */
        binding.spinnerDocumentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedDocumentType = parent.getItemAtPosition(position).toString();
                binding.etDocumentNumber.setText("");
                switch (selectedDocumentType) {
                    case "Aadhar" ->
                            CommonLogic.setupField(null, "aadhar", binding.etDocumentNumber);
                    case "Pollution" ->
                            CommonLogic.setupField(null, "pollution", binding.etDocumentNumber);
                    case "Insurance" ->
                            CommonLogic.setupField(null, "insurance", binding.etDocumentNumber);
                    case "RC" -> CommonLogic.setupField(null, "rc", binding.etDocumentNumber);
                    case "Pancard" -> CommonLogic.setupField(null, "pan", binding.etDocumentNumber);
                    case "Driving Licence" ->
                            CommonLogic.setupField(null, "driving_licence", binding.etDocumentNumber);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                CommonLogic.showTestLog(TAG, "⚠️ Spinner: Nothing selected");
            }
        });


        binding.pickDocument.setOnClickListener(v -> {
            ImagePicker.takePictureFromCamera(TAG, MyGarageDocumentPage.this, ImagePickerAppConstants.IMAGE_REQUEST, false);
        });

        binding.btnUpload.setOnClickListener(view -> {

            if (documentFile == null || documentPath == null || documentPath.isEmpty()) {
                Toast.makeText(this, "Please upload document.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.etDocumentName.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter document name", Toast.LENGTH_SHORT).show();
                binding.etDocumentName.setError("filed cant be empty");
                return;
            }

            if (binding.etDocumentNumber.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter document number", Toast.LENGTH_SHORT).show();
                binding.etDocumentNumber.setError("filed cant be empty");
                return;
            }

            switch (selectedDocumentType) {
                case "Aadhar" ->{
                    if (!CommonLogic.validateField(binding.etDocumentNumber, "aadhar")){
                        return;
                    }
                }
                case "Pollution" -> {
                    if (!CommonLogic.validateField(binding.etDocumentNumber, "pollution")){
                        return;
                    }
                }
                case "Insurance" ->{
                    if (!CommonLogic.validateField(binding.etDocumentNumber, "insurance")){
                        return;
                    }
                }
                case "RC" ->{
                    if (!CommonLogic.validateField(binding.etDocumentNumber, "")){
                        return;
                    }
                }
                case "Pancard" -> {
                    if (!CommonLogic.validateField(binding.etDocumentNumber, "pan")){
                        return;
                    }
                }
                case "Driving Licence" ->{
                    if (!CommonLogic.validateField(binding.etDocumentNumber, "driving_licence")){
                        return;
                    }
                }
            }


            loadingDialog.show();

            String userId = manager.getUserId() != null ? manager.getUserId() : "";
            String vehicleId = vehicleInfo.getVehicle_id() != null ? vehicleInfo.getVehicle_id() : "";
            String safeDocName = binding.etDocumentName.getText().toString().trim();
            String documentNumber = binding.etDocumentNumber.getText().toString().trim();
            String safeDocumentPath = documentPath != null ? documentPath : "";

            String documentType = "";

            if (binding.spinnerDocumentType.getSelectedItem() != null) {
                documentType = binding.spinnerDocumentType
                        .getSelectedItem()
                        .toString()
                        .toLowerCase();
            }

            /* 🔍 TEST LOGS — VERY IMPORTANT */
            CommonLogic.showTestLog(TAG, "🚀 Upload Params Check");
            CommonLogic.showTestLog(TAG, "👤 userId: " + userId);
            CommonLogic.showTestLog(TAG, "🚗 vehicleId: " + vehicleId);
            CommonLogic.showTestLog(TAG, "📄 documentType: " + documentType);
            CommonLogic.showTestLog(TAG, "📝 docName: " + safeDocName);
            CommonLogic.showTestLog(TAG, "🔢 documentNumber: " + documentNumber);
            CommonLogic.showTestLog(TAG, "📁 documentPath: " + safeDocumentPath);
            CommonLogic.showTestLog(TAG, "🖼 file exists: " + (documentFile != null && documentFile.exists()));
            CommonLogic.showTestLog(TAG, "🖼 file size (KB): " +
                    (documentFile != null ? documentFile.length() / 1024 : 0));

            CommonMethods.uploadDocument(
                    MyGarageDocumentPage.this,
                    documentFile,
                    safeDocumentPath,
                    userId,
                    vehicleId,
                    documentType,
                    safeDocName,
                    documentNumber,
                    new CommonMethods.ImageUploadCallback() {
                        @Override
                        public void onUploadSuccess(SavedImageData uploadedImage) {
//                            onBackPressed();
                        }

                        @Override
                        public void onUploadError(String errorMessage) {
                            loadingDialog.dismiss();
                            Toast.makeText(MyGarageDocumentPage.this, "❌ " + errorMessage, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onUploadJSON(JSONObject message) {
                            try {
                                String status = message.getString("success");
                                if (status.equalsIgnoreCase("true")) {
                                    disableHideContentSecureForNextNavigation();
                                    Toast.makeText(MyGarageDocumentPage.this, "✅ Document uploaded successfully", Toast.LENGTH_SHORT).show();
                                    Intent uploadDocument = new Intent(MyGarageDocumentPage.this, VehicleInformation.class);
                                    uploadDocument.putExtra("vehicleId", vehicleId);
                                    startActivity(uploadDocument);
                                    finish();
                                } else {
                                    Toast.makeText(MyGarageDocumentPage.this, "✅ unable to upload Document", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                CommonLogic.showTestLog(TAG, e.getMessage());
                            }


                            loadingDialog.dismiss();
                        }
                    }
            );
        });

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        Intent uploadDocument = new Intent(MyGarageDocumentPage.this, VehicleInformation.class);
        uploadDocument.putExtra("vehicleData", vehicleInfo);
        startActivity(uploadDocument);
        finish();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImagePickerAppConstants.IMAGE_REQUEST && resultCode == RESULT_OK) {
            ImageProcessingUtils.handleCameraImage(TAG, MyGarageDocumentPage.this, FileUtils.getImagePath(MyGarageDocumentPage.this), null, false,null, new FileUtils.FileCallback() {
                @Override
                public void onFileReady(CompressFileData selectedImageData) {
                    documentFile = selectedImageData.getFileFormat();
                    documentPath = selectedImageData.getFilePath();

                    binding.selectedDocument.setText("Selected Document:\n" + CommonLogic.getFileNameFromPath(MyGarageDocumentPage.this, documentPath));

                    CommonLogic.showTestLog(TAG, "📄 Document ready: " + documentPath);
                }
            });
        }
    }
}