package com.digivahan.ui.Activities.deliveryAddress;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiClient;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.AddressBookModel;
import com.digivahan.databinding.ActivitySetDefaultAddressPageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.chat.ChatUserProfile;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.Constants;
import com.ashu.ashuutils.APIHelper;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetDefaultAddressPage extends BaseActivity {

    String TAG = "SetDefaultAddressPageData";
    ActivitySetDefaultAddressPageBinding binding;
    PreferencesManager preferencesManager;

    ArrayList<AddressBookModel> addressFilteredList = new ArrayList<>();
    ArrayList<AddressBookModel> addressList = new ArrayList<>();
    AshDialog loadingDialog;

    AddressBookModel selectedAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetDefaultAddressPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Set Default");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        getOnBackPressedDispatcher().addCallback(SetDefaultAddressPage.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        preferencesManager = new PreferencesManager(SetDefaultAddressPage.this);
        loadingDialog = new AshDialog(SetDefaultAddressPage.this, "Please wait", "Getting Data...");

        // Dynamically add address items
        setDeliveryAddress();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used, but required to override
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Called as the user types
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                // Example: Filter list or trigger search
                filterList(query);
            }
        });


        binding.btnSave.setOnClickListener(v -> {

           if (selectedAddress != null) {

               JsonObject jsonObjectDeliveryAddress = new JsonObject();
               jsonObjectDeliveryAddress.addProperty("user_id", preferencesManager.getUserId());
               jsonObjectDeliveryAddress.addProperty("address_id", selectedAddress.get_id());
               jsonObjectDeliveryAddress.addProperty("default_status", true);


               CommonLogic.showTestLog(TAG, jsonObjectDeliveryAddress.toString());

               loadingDialog.show();

               // --- Step 2: Make API call ---
               ApiClient.getApiService(SetDefaultAddressPage.this).commonPUTMethodToHitAllAPIsWithUrlBody(APIData.UPDATE_ADDRESS, jsonObjectDeliveryAddress).enqueue(new Callback<JsonObject>() {
                   @Override
                   public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                       loadingDialog.dismiss();

                       try {
                           JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                           CommonLogic.showTestLog(TAG, responseBody.toString());

                           boolean status = Objects.requireNonNull(responseBody).optBoolean("status", false);
                           String message = responseBody.has("message") ? responseBody.getString("message") : "Server not working";

                           Toast.makeText(SetDefaultAddressPage.this, message, Toast.LENGTH_SHORT).show();

                           if (status) {
                               back();
                           }
                       } catch (Exception e) {
                           CommonLogic.showTestLog(TAG, e.getMessage());
                       }

                   }

                   @Override
                   public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                       CommonLogic.showTestLog(TAG, "onFailure:- Updation failed. Please try again.");
                   }
               });
           }else {
               Toast.makeText(this, "No selected address found", Toast.LENGTH_SHORT).show();
           }
        });

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    private void filterList(String query) {
        addressFilteredList.clear();
        if (query.isEmpty()){
            addressFilteredList.addAll(addressList);
        }
        else {
            for (AddressBookModel item : addressList) {
                if (item.getName().toUpperCase(Locale.ROOT).startsWith(query.toUpperCase(Locale.ROOT)) || item.getContact_no().startsWith(query)) {
                    addressFilteredList.add(item);
                }
            }
        }

        addSampleAddresses(addressFilteredList);
    }


    private void setDeliveryAddress() {
        JsonObject jsonObjectDeliveryAddress = new JsonObject();
        jsonObjectDeliveryAddress.addProperty("user_id", preferencesManager.getUserId());
        jsonObjectDeliveryAddress.addProperty("details_type", "address_book");

        loadingDialog.show();
        ApiClient.getApiService(SetDefaultAddressPage.this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_USER_DETAILS, jsonObjectDeliveryAddress)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response, Constants.ENABLE_TESTING);

                            boolean status = Objects.requireNonNull(responseBody).optBoolean("success", false);
                            String message = responseBody.optString("message", "Server error, Please try after some time.");

                            if (status) {
                                try {
                                    JSONArray addressArray = responseBody.getJSONArray("data");

                                    // Clear existing list before adding new data
                                    addressList.clear();
                                    addressFilteredList.clear();

                                    for (int i = 0; i < addressArray.length(); i++) {
                                        JSONObject obj = addressArray.getJSONObject(i);

                                        AddressBookModel model = APIHelper.convertJsonToModel(obj, AddressBookModel.class);
                                       /*model.setName(obj.optString("name"));
                                        model.setContact_no(obj.optString("contact_no"));
                                        model.setHouse_no_or_building(obj.optString("house_no_or_building"));
                                        model.setRoad_or_area(obj.optString("road_or_area"));
                                        model.setCity(obj.optString("city"));
                                        model.setState(obj.optString("state"));
                                        model.setPincode(obj.optString("pincode"));
                                        model.setDefault_status(obj.optBoolean("default_status"));*/

                                        if (model.isDefault_status()){
                                            ArrayList<AddressBookModel> tempList = new ArrayList<>();
                                            tempList.add(model);
                                            tempList.addAll(addressFilteredList);

                                            addressFilteredList.clear();
                                            addressFilteredList.addAll(tempList);

                                            addressList.clear();
                                            addressList.addAll(tempList);
                                        }else {
                                            addressFilteredList.add(model);
                                            addressList.add(model);
                                        }
                                    }

                                    addSampleAddresses(addressFilteredList);

                                    // ✅ Log or verify
                                    CommonLogic.showTestLog(TAG, "Address List Size: " + addressFilteredList.size());

                                    // You can update your UI here if needed
                                    // e.g., addressAdapter.notifyDataSetChanged();

                                } catch (Exception e) {
                                    CommonLogic.showTestLog(TAG, "Data parse error: " + e.getMessage());
                                }
                            } else {
                                CommonLogic.showTestLog(TAG, "API failed: " + message);
                            }

                        } catch (Exception e) {
                            CommonLogic.showTestLog(TAG, "Response error: " + e.getMessage());
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        CommonLogic.showTestLog(TAG, "onFailure: Updation failed. Please try again.");
                        loadingDialog.dismiss();
                    }
                });
    }


    private void addSampleAddresses(ArrayList<AddressBookModel> addressList) {
        // First clear the container to avoid duplicates when redrawing
        binding.addressContainer.removeAllViews();

        for (int i = 0; i < addressList.size(); i++) {
            AddressBookModel model = addressList.get(i);
            View item = LayoutInflater.from(this).inflate(R.layout.item_address, binding.addressContainer, false);

            TextView tvName = item.findViewById(R.id.tvName);
            TextView tvAddress = item.findViewById(R.id.tvAddress);
            TextView tvPhone = item.findViewById(R.id.tvPhone);
            Button btnDeliver = item.findViewById(R.id.btnDeliver);
            RadioButton rbSelect = item.findViewById(R.id.rbSelect);
            LinearLayout itemLayout = item.findViewById(R.id.itemLayout);
            LinearLayout buttonLayout = item.findViewById(R.id.buttonLayout);
            buttonLayout.setVisibility(View.GONE);

            // Set data
            tvName.setText(model.getName());
            tvAddress.setText(model.getHouse_no_building() + ", " + model.getRoad_or_area() + ", " + model.getCity() + ", " + model.getState() + ", " + model.getPincode());
            tvPhone.setText(model.getContact_no());

            // Highlight default address (optional UI feedback)
            rbSelect.setChecked(model.isDefault_status());

            // Button click → open Add/Edit screen
            btnDeliver.setOnClickListener(v -> {
                disableHideContentSecureForNextNavigation();
                Intent editDeliveryAddress = new Intent(SetDefaultAddressPage.this, AddEditDeliveryAddress.class);
                startActivity(editDeliveryAddress);
            });

            int finalI = i;
            itemLayout.setOnClickListener(view -> {
                // 1️⃣ Set all addresses to false
                for (AddressBookModel address : addressList) {
                    address.setDefault_status(false);
                }

                // 2️⃣ Set clicked address to true
                addressList.get(finalI).setDefault_status(true);

                selectedAddress = addressList.get(finalI);

                // 3️⃣ Rebuild the UI (refresh)
                addSampleAddresses(addressList);
            });

            // Add item inside container
            binding.addressContainer.addView(item);
        }
    }

}