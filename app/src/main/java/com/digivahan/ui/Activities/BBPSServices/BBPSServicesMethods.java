package com.digivahan.ui.Activities.BBPSServices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.ashu.ashuutils.APIHelper;
import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.model.FasTagCardModel;
import com.digivahan.data.model.FuelItemModel;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BBPSServicesMethods {

    public static ArrayList<FasTagCardModel> getBillersList(String TAG, Context context,
                                                            int pageNumber, int recordsPerPage,
                                                            String categoryKey) {

        ArrayList<FasTagCardModel> fasTagCardList = new ArrayList<>();

        JsonObject requestBody = new JsonObject();

        // Pagination
        JsonObject pagination = new JsonObject();
        pagination.addProperty("pageNumber", pageNumber);
        pagination.addProperty("recordsPerPage", recordsPerPage);

        // Filters
        JsonObject filters = new JsonObject();
        filters.addProperty("categoryKey", categoryKey);
        filters.addProperty("updatedAfterDate", "");

        requestBody.add("pagination", pagination);
        requestBody.add("filters", filters);

        /*ApiClient.getBBPSServices(context)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_BILLER_LIST, requestBody)
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {

                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                            boolean status = responseBody.has("statuscode") &&
                                    responseBody.getString("statuscode").equalsIgnoreCase("TXN");

                            if (status) {

                                JSONObject dataObj = responseBody.getJSONObject("data");
                                JSONArray recordsArray = dataObj.getJSONArray("records");

                                for (int i = 0; i < recordsArray.length(); i++) {

                                    JSONObject obj = recordsArray.getJSONObject(i);

                                    FasTagCardModel model = new FasTagCardModel();

                                    model.setBillerId(obj.optString("billerId"));
                                    model.setBillerName(obj.optString("billerName"));
                                    model.setCategoryKey(obj.optString("categoryKey"));
                                    model.setType(obj.optString("type"));
                                    model.setCategoryName(obj.optString("categoryName"));
                                    model.setCoverageCity(obj.optString("coverageCity"));
                                    model.setCoverageState(obj.optString("coverageState"));
                                    model.setCoveragePincode(obj.optInt("coveragePincode"));
                                    model.setUpdatedDate(obj.optString("updatedDate"));
                                    model.setBillerStatus(obj.optString("billerStatus"));
                                    model.setAvailable(obj.optBoolean("isAvailable"));
                                    model.setIconUrl(obj.optString("iconUrl"));

                                    fasTagCardList.add(model);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });*/

        return fasTagCardList; // ⚠️ This will return empty initially (async call)
    }
}
