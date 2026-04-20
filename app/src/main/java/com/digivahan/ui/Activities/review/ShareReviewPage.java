package com.digivahan.ui.Activities.review;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.adapters.SelectedImageListAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.CompressFileData;
import com.digivahan.data.model.OrderItemModel;
import com.digivahan.data.model.SavedImageData;
import com.digivahan.databinding.ActivityShareReviewPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class ShareReviewPage extends BaseActivity {

    String TAG = "ShareReviewPageData";

    ActivityShareReviewPageBinding binding;

    PreferencesManager preferencesManager;
    AshDialog loadingDialog;

    File reviewImageFile;
    String reviewImagePath;

    boolean isCameraSelected = false;
    OrderItemModel orderDetails;

    JsonArray mediaArray = new JsonArray();

    ArrayList<SavedImageData> selectedImageList = new ArrayList<>();
    SelectedImageListAdapter selectedImageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShareReviewPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Share review");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        try {
            orderDetails = (OrderItemModel) getIntent().getSerializableExtra("orderDetails");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });

        loadingDialog = new AshDialog(ShareReviewPage.this, "Please wait", "");

        selectedImageListAdapter = new SelectedImageListAdapter(ShareReviewPage.this, selectedImageList, loadingDialog);
        binding.rvSelectedImages.setAdapter(selectedImageListAdapter);


        preferencesManager = new PreferencesManager(ShareReviewPage.this);

        binding.pickImage.setOnClickListener(v -> {
            CommonLogic.showPickImageDialog(ShareReviewPage.this,
                    CommonLogic.PROFILE_IMAGE_REQUEST, false, new CommonLogic.CameraSelectionCallback() {
                        @Override
                        public void onCameraSelected(boolean isCamera) {
                            isCameraSelected = isCamera;
                        }
                    });
        });

        binding.ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            if (rating < 1f) {
                ratingBar1.setRating(1f);
                return;
            }

            String title = "";
            String message = "";

            switch ((int) rating) {
                case 1:
                    title = "Decent Experience Overall";
                    message = "It was okay, but I think it can get even better with small improvements.";
                    break;
                case 2:
                    title = "Good but Can Improve";
                    message = "I liked the product, though there’s room for improvement in a few areas.";
                    break;
                case 3:
                    title = "Satisfied with My Purchase";
                    message = "Everything was as expected. A nice experience overall, I’d recommend trying it.";
                    break;
                case 4:
                    title = "Really Impressed with the Quality";
                    message = "I’m happy with the product and the service. Great work by the team!";
                    break;
                case 5:
                    title = "Absolutely Loved It!";
                    message = "Fantastic experience! The product quality and service were beyond expectations. Highly recommended!";
                    break;
            }

            binding.etTitle.setText(title);
            binding.etReview.setText(message);
        });


        binding.btnPost.setOnClickListener(v -> {

            JsonObject reviewObject = new JsonObject();

            reviewObject.addProperty("user_id", preferencesManager.getUserId());
            reviewObject.addProperty("order_id", orderDetails.getOrderId());
            reviewObject.addProperty("product_type", "qr");
            reviewObject.addProperty("rating", binding.ratingBar.getRating());
            reviewObject.addProperty("review_title", binding.etTitle.getText().toString());
            reviewObject.addProperty("review_text", binding.etReview.getText().toString());


            // ✅ Create a proper JSON array for attachments
            JsonArray attachmentsArray = new JsonArray();
            for (SavedImageData s : selectedImageList) {
                attachmentsArray.add(s.getImage_url());
            }

            reviewObject.add("product_image", attachmentsArray);

// Print or log it
            CommonLogic.showTestLog(TAG, reviewObject.toString());

            loadingDialog.show();



// ✅ Log for debugging
            CommonLogic.showTestLog(TAG, "📦 Final JSON to send: " + reviewObject.toString());

            ApiCall.callApi(TAG,
                    ShareReviewPage.this,
                    APIData.SEND_FEEDBACK,
                    reviewObject, "post",
                    new ApiCall.ApiResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject responseBody, boolean status, String message) {
                            try {
                                if (status) {
                                    Toast.makeText(ShareReviewPage.this, message, Toast.LENGTH_SHORT).show();
                                    getOnBackPressedDispatcher().onBackPressed();
                                } else {
                                    Toast.makeText(ShareReviewPage.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                CommonLogic.showTestLog(TAG, e.getMessage());
                            }

                            loadingDialog.dismiss();

                        }

                        @Override
                        public void onError(String errorMessage) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, errorMessage);
//                                Toast.makeText(OrderDetailsPage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadingDialog.show();
        if (requestCode == CommonLogic.PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            CommonLogic.handleImagePick(data, isCameraSelected, false, preferencesManager.getString(PreferencesManager.IMAGE_PATH, ""),
                    ShareReviewPage.this, null, null, new CommonLogic.FileCallback() {
                        @Override
                        public void onFileReady(CompressFileData selectedImageData) {
                            CommonLogic.showTestLog(TAG, "selectedImageData: File- "+selectedImageData.getFileFormat() + " path: " + selectedImageData.getFilePath());

                            CommonMethods.uploadSingleImage(
                                    ShareReviewPage.this,
                                    selectedImageData.getFileFormat(), selectedImageData.getFilePath(), Constants.profile,
                                    new CommonMethods.ImageUploadCallback() {
                                        @Override
                                        public void onUploadSuccess(SavedImageData uploadedImage) {
//                                            Toast.makeText(UpdateBasicDetails.this, "Upload successful!", Toast.LENGTH_SHORT).show();

                                            selectedImageList.add(uploadedImage);
                                            selectedImageListAdapter.notifyDataSetChanged();
                                            loadingDialog.dismiss();
                                            // you can call deleteProfileImage(selectedImage.getFile_id()) here if needed
                                        }

                                        @Override
                                        public void onUploadError(String errorMessage) {
                                            loadingDialog.dismiss();
                                            Toast.makeText(ShareReviewPage.this, errorMessage, Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onUploadJSON(JSONObject errorMessage) {
                                        }
                                    }
                            );
                        }
                    });

            CommonLogic.showTestLog(TAG, "reviewImagePath:- "+ reviewImagePath);
        }

        else if (requestCode == CommonLogic.CAMARA_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            isCameraSelected = true;
            CommonLogic.takePictureFromCamera(ShareReviewPage.this, CommonLogic.PROFILE_IMAGE_REQUEST, false);
        } else if (requestCode == CommonLogic.STORAGE_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            isCameraSelected = false;
            CommonLogic.choosePictureFromGallery(ShareReviewPage.this, CommonLogic.PROFILE_IMAGE_REQUEST);
        }

        else {
            loadingDialog.dismiss();
        }
    }
}