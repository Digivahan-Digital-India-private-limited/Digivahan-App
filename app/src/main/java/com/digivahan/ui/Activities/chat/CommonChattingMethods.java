package com.digivahan.ui.Activities.chat;

import android.content.Context;

import androidx.annotation.NonNull;

import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.SavedImageData;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;

import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface CommonChattingMethods {
    public static void sendMessageAPI(String TAG, Context context, String chatRoomId,
                                      String message, ArrayList<SavedImageData> selectedImages, String latitude, String longitude, PreferencesManager manager, AshDialog loadingDialog) {
        JsonObject jsonObjectMessage = new JsonObject();
        jsonObjectMessage.addProperty("chat_room_id", chatRoomId);
        jsonObjectMessage.addProperty("sender_id", manager.getUserId());
        jsonObjectMessage.addProperty("message", message);

        CommonLogic.showTestLog(TAG, jsonObjectMessage.toString());

//        loadingDialog.show();


        List<MultipartBody.Part> imageParts = new ArrayList<>();

        if (selectedImages != null && !selectedImages.isEmpty()) {
            for (SavedImageData file : selectedImages) {

                String extension = "";
                int dotIndex = file.getImageFile().getName().lastIndexOf(".");
                if (dotIndex > 0) {
                    extension = file.getImageFile().getName().substring(dotIndex + 1).toLowerCase();
                }

                String mimeType =
                        extension.equals("png") ? "image/png" :
                                (extension.equals("jpg") || extension.equals("jpeg")) ? "image/jpeg" :
                                        "application/octet-stream";

                RequestBody fileBody =
                        RequestBody.create(file.getImageFile(), MediaType.parse(mimeType));

                MultipartBody.Part imagePart =
                        MultipartBody.Part.createFormData(
                                "images",   // 🔥 MUST MATCH POSTMAN KEY
                                file.getImageFile().getName(),
                                fileBody
                        );

                imageParts.add(imagePart);

                CommonLogic.showTestLog(TAG,
                        "📷 Image Added -> " + file.getImageFile().getName() + " (" + file.getImageFile().length() / 1024 + " KB)");
            }
        }

        // 🧾 Prepare text fields
        RequestBody senderIdRequest =
                RequestBody.create(manager.getUserId(), MediaType.parse("text/plain"));

        RequestBody chatRoomIdRequest =
                RequestBody.create(chatRoomId, MediaType.parse("text/plain"));

        RequestBody messageRequest =
                RequestBody.create(message, MediaType.parse("text/plain"));

        RequestBody latitudeRequest =
                RequestBody.create(latitude, MediaType.parse("text/plain"));

        RequestBody longitudeRequest =
                RequestBody.create(longitude, MediaType.parse("text/plain"));



//        loadingDialog.show();

        Call<JsonObject> sendMessageRequest =
                ApiClient.getApiService(context).sendChatMessage(
                        senderIdRequest,
                        chatRoomIdRequest,
                        messageRequest, latitudeRequest, longitudeRequest,
                        imageParts
                );

        // --- Step 2: Make API call ---
        sendMessageRequest.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                // Always dismiss loader
                loadingDialog.dismiss();

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response);
                    CommonLogic.showTestLog(TAG, responseBody.toString());

                    boolean status = responseBody.has("status") && responseBody.getBoolean("status");
                    String message = responseBody.has("message") ? responseBody.getString("message") : "";


                } catch (Exception e) {
                    CommonLogic.showTestLog(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                CommonLogic.showTestLog(TAG, "onFailure:- Updation failed. Please try again.");
            }
        });
    }
}
