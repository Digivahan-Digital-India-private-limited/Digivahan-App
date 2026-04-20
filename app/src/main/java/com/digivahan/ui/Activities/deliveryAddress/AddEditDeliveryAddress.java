package com.digivahan.ui.Activities.deliveryAddress;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.AddressBookModel;
import com.digivahan.databinding.ActivityAddEditDeliveryAddressBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.chat.ChatUserProfile;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class AddEditDeliveryAddress extends BaseActivity {
    String TAG = "AddEditDeliveryAddressData";

    ActivityAddEditDeliveryAddressBinding binding;

    PreferencesManager manager;

    String hit_type = "add";

    AddressBookModel addressBookModelData;

    AshDialog loadingDialog;

    boolean isPinCodeCorrect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditDeliveryAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        manager = new PreferencesManager(AddEditDeliveryAddress.this);

        loadingDialog = new AshDialog(AddEditDeliveryAddress.this, "Please wait", "");

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Edit Delivery Address");

        try {
            if (getIntent().hasExtra("hit_type")) {
                hit_type = getIntent().getStringExtra("hit_type");
                if (hit_type.equalsIgnoreCase("add")) {
                    binding.toolbarLayout.tvTitle.setText("Add Delivery Address");
                } else {
                    binding.toolbarLayout.tvTitle.setText("Edit Delivery Address");
                    addressBookModelData = (AddressBookModel) getIntent().getSerializableExtra("addressItemData");
                }
            }
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        getOnBackPressedDispatcher().addCallback(AddEditDeliveryAddress.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        CommonLogic.setupField(null, "name", binding.etUserName);
        CommonLogic.setupField(null, "phone", binding.etUserContact);
        CommonLogic.setupField(null, "", binding.etUserHouseNo);
        CommonLogic.setupField(null, "", binding.etStreet);
        CommonLogic.setupField(null, "", binding.etUserRoadAreaColony);
        CommonLogic.setupField(null, "", binding.etLandMark);
        CommonLogic.setupField(null, "", binding.etUserCity);
        CommonLogic.setupField(null, "", binding.etUserState);
        CommonLogic.setupField(null, "pincode", binding.etUserPinCode);




        binding.etUserPinCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    fetchCityStateFromPin(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            back();
        });

        binding.etUserContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                binding.etUserContact.removeTextChangedListener(this);

                String digitsOnly = s.toString().replaceAll("[^0-9]", ""); // keep only digits

                // If number has more than 10 digits → take last 10 digits
                if (digitsOnly.length() > 10) {
                    digitsOnly = digitsOnly.substring(digitsOnly.length() - 10);
                }

                // Update only if text changed
                if (!s.toString().equals(digitsOnly)) {
                    binding.etUserContact.setText(digitsOnly);
                    binding.etUserContact.setSelection(digitsOnly.length());
                }

                binding.etUserContact.addTextChangedListener(this);
            }
        });


        binding.btnSave.setOnClickListener(v -> {

            String name = Objects.requireNonNull(binding.etUserName.getText()).toString().trim();
            String contactNo = Objects.requireNonNull(binding.etUserContact.getText()).toString().trim();
            String buildingNo = Objects.requireNonNull(binding.etUserHouseNo.getText()).toString().trim();
            String area = Objects.requireNonNull(binding.etUserRoadAreaColony.getText()).toString().trim();
            String streetName = Objects.requireNonNull(binding.etStreet.getText()).toString().trim();
            String landmark = Objects.requireNonNull(binding.etLandMark.getText()).toString().trim();
            String pinCode = Objects.requireNonNull(binding.etUserPinCode.getText()).toString().trim();
            String city = Objects.requireNonNull(binding.etUserCity.getText()).toString().trim();
            String state = Objects.requireNonNull(binding.etUserState.getText()).toString().trim();

            // Basic validation
            if (name.isEmpty()) {
                binding.etUserName.setError("Please enter name");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonLogic.isValidName(name)) {
                binding.etUserName.setError(getString(R.string.name_length_error));
                Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
                return;
            } else if (contactNo.isEmpty()) {
                binding.etUserContact.setError("Please enter contact number");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            }  else if (!CommonLogic.isValidPhone(contactNo)) {
                binding.etUserContact.setError("Invalid phone number");
                Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                return;
            } else if (buildingNo.isEmpty()) {
                binding.etUserHouseNo.setError("Please enter house details");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            }  else if (area.isEmpty()) {
                binding.etUserRoadAreaColony.setError("Please enter area");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (streetName.isEmpty()) {
                binding.etStreet.setError("Please enter street");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            }   /*else if (landmark.isEmpty()) {
                binding.etLandMark.setError("Please enter landmark");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            }*/ else if (pinCode.isEmpty()) {
                binding.etUserPinCode.setError("Please enter your pin code");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (city.isEmpty()) {
                binding.etUserCity.setError("Please enter your city");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (state.isEmpty()) {
                binding.etUserState.setError("Please enter your state");
                Toast.makeText(this, getString(R.string.empty_field_string), Toast.LENGTH_SHORT).show();
                return;
            } else if (!isPinCodeCorrect) {
                Toast.makeText(this, "Invalid PIN Code", Toast.LENGTH_SHORT).show();
                binding.etUserPinCode.setError("Invalid PIN Code");
                return;
            }

            JsonObject jsonObjectDeliveryAddress = new JsonObject();
            jsonObjectDeliveryAddress.addProperty("user_id", manager.getUserId());
            if (!hit_type.equalsIgnoreCase("add")){
                jsonObjectDeliveryAddress.addProperty("address_id", addressBookModelData.get_id());
            }
            jsonObjectDeliveryAddress.addProperty("name", name);
            jsonObjectDeliveryAddress.addProperty("contact_no", contactNo);
            jsonObjectDeliveryAddress.addProperty("house_no_building", buildingNo);
            jsonObjectDeliveryAddress.addProperty("street_name", streetName);
            jsonObjectDeliveryAddress.addProperty("road_or_area", area);
            jsonObjectDeliveryAddress.addProperty("landmark", landmark);
            jsonObjectDeliveryAddress.addProperty("city", city);
            jsonObjectDeliveryAddress.addProperty("state", state);
            jsonObjectDeliveryAddress.addProperty("pincode", pinCode);

            boolean default_status = binding.setDefaultOption.isChecked();
            jsonObjectDeliveryAddress.addProperty("default_status", default_status);

            CommonLogic.showTestLog(TAG, jsonObjectDeliveryAddress.toString());

            loadingDialog.show();


            String callAPI = APIData.ADD_ADDRESS;
            String methodType = "post";

            if (!hit_type.equalsIgnoreCase("add")){
                callAPI = APIData.UPDATE_ADDRESS;
                methodType = "put";
            }



            ApiCall.callApi(TAG, AddEditDeliveryAddress.this, callAPI, jsonObjectDeliveryAddress, methodType, new ApiCall.ApiResponseCallback() {
                @Override
                public void onSuccess(JSONObject responseBody, boolean status, String message) {
                    loadingDialog.dismiss();
                    Toast.makeText(AddEditDeliveryAddress.this, message, Toast.LENGTH_SHORT).show();
                    if (status) {
                        back();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    loadingDialog.dismiss();
                    Toast.makeText(AddEditDeliveryAddress.this, "Updation failed. Please try again.", Toast.LENGTH_SHORT).show();
                    CommonLogic.showTestLog(TAG, "onFailure:- Updation failed. Error:- " + errorMessage);
                }
            });
        });

        if (!hit_type.equalsIgnoreCase("add") && addressBookModelData != null){
            binding.etUserName.setText(addressBookModelData.getName());
            binding.etUserContact.setText(addressBookModelData.getContact_no());
            binding.etUserHouseNo.setText(addressBookModelData.getHouse_no_building());
            binding.etStreet.setText(addressBookModelData.getStreet_name());
            binding.etUserRoadAreaColony.setText(addressBookModelData.getRoad_or_area());
            binding.etLandMark.setText(addressBookModelData.getLandmark());
            binding.etUserPinCode.setText(addressBookModelData.getPincode());
            binding.etUserCity.setText(addressBookModelData.getCity());
            binding.etUserState.setText(addressBookModelData.getState());
            binding.setDefaultOption.setChecked(addressBookModelData.isDefault_status());
        }

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }


    private void fetchCityStateFromPin(String pincode) {

        String url = "https://api.postalpincode.in/pincode/" + pincode;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject object = response.getJSONObject(0);
                        String status = object.getString("Status");

                        if (status.equalsIgnoreCase("Success")) {
                            JSONArray postOfficeArray = object.getJSONArray("PostOffice");
                            JSONObject postOffice = postOfficeArray.getJSONObject(0);

                            String city = postOffice.getString("District");
                            String state = postOffice.getString("State");

                            binding.etUserCity.setText(city);
                            binding.etUserState.setText(state);

                            // Optional: lock fields
                            binding.etUserCity.setEnabled(false);
                            binding.etUserState.setEnabled(false);

                            isPinCodeCorrect = true;

                        } else {
                            Toast.makeText(this, "Invalid PIN Code", Toast.LENGTH_SHORT).show();
                            binding.etUserPinCode.setError("Invalid PIN Code");
                            isPinCodeCorrect = false;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

}