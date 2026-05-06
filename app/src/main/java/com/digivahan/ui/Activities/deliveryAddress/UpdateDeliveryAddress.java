package com.digivahan.ui.Activities.deliveryAddress;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.digivahan.databinding.ActivityUpdateDeliveryAddressBinding;
import com.digivahan.other.CustomDialog.AshDialog;
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

public class UpdateDeliveryAddress extends BaseActivity {
    String TAG = "UpdateDeliveryAddressData";
    ActivityUpdateDeliveryAddressBinding binding;
    PreferencesManager preferencesManager;

    ArrayList<AddressBookModel> addressFilteredList = new ArrayList<>();
    ArrayList<AddressBookModel> addressList = new ArrayList<>();
    AshDialog loadingDialog;

    @Override
    protected void onResume() {
        super.onResume();
        setDeliveryAddress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateDeliveryAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Address Book");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> onBackPressed());
        binding.deliveryAddressLayout.closeBtnLayout.setVisibility(View.GONE);

        getOnBackPressedDispatcher().addCallback(UpdateDeliveryAddress.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        preferencesManager = new PreferencesManager(UpdateDeliveryAddress.this);
        loadingDialog = new AshDialog(UpdateDeliveryAddress.this, "Please wait", "Getting Data...");


        binding.deliveryAddressLayout.addButtonLayout.setVisibility(View.GONE);

        binding.addAddress.setOnClickListener(view -> {
            disableHideContentSecureForNextNavigation();
            Intent addDeliveryAddress = new Intent(UpdateDeliveryAddress.this, AddEditDeliveryAddress.class);
            addDeliveryAddress.putExtra("hit_type" , "add");
            startActivity(addDeliveryAddress);
        });

        binding.deliveryAddressLayout.etSearch.addTextChangedListener(new TextWatcher() {
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

        setAddressesListData();
    }

    private void setDeliveryAddress() {

        JsonObject jsonObjectDeliveryAddress = new JsonObject();
        jsonObjectDeliveryAddress.addProperty("user_id", preferencesManager.getUserId());
        jsonObjectDeliveryAddress.addProperty("details_type", "address_book");

        loadingDialog.show();

        ApiClient.getApiService(UpdateDeliveryAddress.this)
                .commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.GET_USER_DETAILS, jsonObjectDeliveryAddress)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        try {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                            boolean status = Objects.requireNonNull(responseBody).optBoolean("success", false);
                            String message = responseBody.optString("message", "Server error, Please try after some time.");

                            if (status) {
                                try {
                                    JSONArray addressArray = responseBody.getJSONArray("data");

                                    // Clear existing list before adding new data
                                    addressFilteredList.clear();
                                    addressList.clear();

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

                                        if (model.isDefault_status()) {
                                            ArrayList<AddressBookModel> tempList = new ArrayList<>();
                                            tempList.add(model);
                                            tempList.addAll(addressFilteredList);

                                            addressFilteredList.clear();
                                            addressFilteredList.addAll(tempList);

                                            addressList.clear();
                                            addressList.addAll(tempList);
                                        } else {
                                            addressFilteredList.add(model);
                                            addressList.add(model);
                                        }
                                    }

                                    setAddressesListData();

                                    if (addressList.isEmpty()){
                                        binding.emptyLayout.setVisibility(View.VISIBLE);
                                        binding.deliveryAddressLayout.mainLayout.setVisibility(View.GONE);
                                    }else {
                                        binding.emptyLayout.setVisibility(View.GONE);
                                        binding.deliveryAddressLayout.mainLayout.setVisibility(View.VISIBLE);
                                    }

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


    public void setAddressesListData() {
        // Clear old views to avoid duplicates
        binding.deliveryAddressLayout.addressContainer.removeAllViews();

        for (int i = 0; i < addressFilteredList.size(); i++) {
            AddressBookModel model = addressFilteredList.get(i);
            View item = LayoutInflater.from(this).inflate(R.layout.item_address, binding.deliveryAddressLayout.addressContainer, false);

            TextView tvName = item.findViewById(R.id.tvName);
            TextView tvAddress = item.findViewById(R.id.tvAddress);
            TextView tvPhone = item.findViewById(R.id.tvPhone);
            TextView editBtn = item.findViewById(R.id.editBtn);
            Button btnDeliver = item.findViewById(R.id.btnDeliver);
            btnDeliver.setVisibility(View.GONE);
            ImageView deleteBtn = item.findViewById(R.id.deleteBtn);
            LinearLayout buttonLayout = item.findViewById(R.id.buttonLayout);
            LinearLayout itemLayout = item.findViewById(R.id.itemLayout);
            RadioButton rbSelect = item.findViewById(R.id.rbSelect);

            // Set data
            tvName.setText(model.getName());
            tvAddress.setText(model.getHouse_no_building() + ", " + model.getRoad_or_area() + ", " + model.getCity() + ", " + model.getState() + ", " + model.getPincode());
            tvPhone.setText(model.getContact_no());

            // Set initial visibility and checked state
            if (model.isDefault_status()) {
                buttonLayout.setVisibility(View.VISIBLE);
            } else {
                buttonLayout.setVisibility(View.GONE);
            }

            rbSelect.setChecked(model.isDefault_status());

            // When clicking anywhere on the item
            int finalI = i;
            itemLayout.setOnClickListener(v -> {
                updateSelection(binding.deliveryAddressLayout.addressContainer, finalI);
            });

               /* // When clicking only on the RadioButton
                int finalI1 = i;
                rbSelect.setOnClickListener(v -> {
                    selectedPosition = finalI1; // update selected position
                    updateSelection(binding.deliveryAddressLayout.addressContainer, finalI1);
                });*/

            deleteBtn.setOnClickListener(v -> {

                if (model.isDefault_status()){
                    Toast.makeText(this, "Can't delete default address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (addressFilteredList.size() < 2){
                    Toast.makeText(this, "There should be at least one address", Toast.LENGTH_SHORT).show();
                    return;
                }

                deleteAddress(model.get_id());
            });

            editBtn.setOnClickListener(v -> {
                disableHideContentSecureForNextNavigation();
                Intent editDeliveryAddress = new Intent(UpdateDeliveryAddress.this, AddEditDeliveryAddress.class);
                editDeliveryAddress.putExtra("hit_type" , "edit");
                editDeliveryAddress.putExtra("addressItemData" , model);
                startActivity(editDeliveryAddress);
            });

            binding.deliveryAddressLayout.addressContainer.addView(item);
        }

    }

    private void updateSelection(LinearLayout container, int selectedPosition) {
        int count = container.getChildCount();

        for (int j = 0; j < count; j++) {
            View child = container.getChildAt(j);

            LinearLayout buttonLayout = child.findViewById(R.id.buttonLayout);
            RadioButton rbSelect = child.findViewById(R.id.rbSelect);

            if (j == selectedPosition) {
                buttonLayout.setVisibility(View.VISIBLE);
                rbSelect.setChecked(true);
            } else {
                buttonLayout.setVisibility(View.GONE);
                rbSelect.setChecked(false);
            }
        }
    }

    private void deleteAddress(String addressId){
        loadingDialog.show();
        // Show confirmation dialog before exiting registration
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure? You want to remove it.")
                .setCancelable(false)
                .setPositiveButton("Yes, delete", (dialog, which) -> {


                    // to test values
                    JsonObject jsonObjectDeleteAddress = new JsonObject();
                    jsonObjectDeleteAddress.addProperty("user_id", preferencesManager.getUserId());
                    jsonObjectDeleteAddress.addProperty("address_id", addressId);

                    CommonLogic.showTestLog(TAG, "deleteAddress params:- " + jsonObjectDeleteAddress);


                    // --- Step 2: Make API call ---
                    ApiClient.getApiService(UpdateDeliveryAddress.this).
                            commonPOSTMethodToHitAllAPIsWithUrlBody(APIData.DELETE_ADDRESS , jsonObjectDeleteAddress).enqueue(new Callback<JsonObject>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            JSONObject responseBody = APIHelper.getResponseData(TAG, response);

                            boolean status = Objects.requireNonNull(responseBody).optBoolean("status", false);
                            String message = responseBody.optString("message", "Server error, Please try after some time.");

                            Toast.makeText(UpdateDeliveryAddress.this, message, Toast.LENGTH_SHORT).show();

                            setDeliveryAddress();
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            // ❌ Network or unexpected failure
                            JsonObject errorObj = new JsonObject();
                            errorObj.addProperty("status", false);
                            errorObj.addProperty("message", t.getMessage());
                            loadingDialog.dismiss();
                        }
                    });

                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    loadingDialog.dismiss();
                })
                .show();
    }
}