package com.digivahan.data.api;

import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface ApiSet extends APIData {

    // Check App Version
 /*   @FormUrlEncoded
    @POST(GET_VERSION)
    Call<JsonObject> getVersion(@Field("get_version") String get_version);*/

    // Save App Version
    /*@FormUrlEncoded
    @POST(SET_APP_VERSION)
    Call<JsonObject> setVersion(
            @Field("user_id") String user_id,
            @Field("app_version") String app_version,
            @Field("device_name") String device_name,
            @Field("type") String type
    );*/

    // Login
    @POST(LOGIN)
    Call<JsonObject> login(@Body JsonObject body);

    /*@FormUrlEncoded
    @POST("login")
    Call<JsonObject> login(
            @Field("username") String username,
            @Field("password") String password,
            @Field("login_type") String loginType// phone or email
    );*/



    // POST method to hit all apis with url and json object
    @POST
    Call<JsonObject> commonPOSTMethodToHitAllAPIsWithUrlBody(@Url String url, @Body JsonObject body);

    @POST
    Call<JsonObject> commonPOSTMethodToHitAllAPIsWithUrl(@Url String url);

    @POST
    Call<JsonObject> commonPOSTMethodToHitAllAPIsWithBody(@Body JsonObject body);

    // GET method to hit all apis with url and json object
    @GET
    Call<JsonObject> commonGETMethodToHitAllAPIs(@Url String url);

    @GET(GET_FUEL_PRICE)
    Call<JsonObject> getFuelData();

    // DELETE methods
    @DELETE
    Call<JsonObject> commonDELETEMethodToHitAllAPIsWithUrlBody(@Url String url);

    // PUT method to hit all apis with url and json object
    @PUT
    Call<JsonObject> commonPUTMethodToHitAllAPIsWithUrlBody(@Url String url, @Body JsonObject body);


    // add and remove user device data api
    @POST(ADD_DEVICE_DATA)
    Call<JsonObject> addAndRemoveDeviceData(@Body JsonObject body);


    // To get app data api
    @POST(APP_INFO_DATA)
    Call<JsonObject> getAppData(@Body JsonObject body);

    // user register api
    @POST(USER_REGISTER)
    Call<JsonObject> registerUser(@Body JsonObject body);

    @POST
    Call<JsonObject> verifyRegisterUser(@Url String url, @Body JsonObject body);

    @POST(RESEND_OTP)
    Call<JsonObject> resendUserRegisterOTP(@Body JsonObject body);


    // user forget password api
    @POST(FORGET_PASSWORD)
    Call<JsonObject> forgetUserPassword(@Body JsonObject body);

    @POST
    Call<JsonObject> verifyForgetUserPassword(@Url String url, @Body JsonObject body);


    // user Notification api
    @POST("forget")
    Call<JsonObject> getUserNotificationList(@Body JsonObject body);

    // get Document vault data
    @POST("forget")
    Call<JsonObject> getDocVault(@Body JsonObject body);


    // user Orders api

    @POST("forget")
    Call<JsonObject> createOrder(@Body JsonObject body);

    @POST("forget")
    Call<JsonObject> getUserOrderList(@Body JsonObject body);

    @POST("forget")
    Call<JsonObject> getOrderPrice(@Body JsonObject body);


    // verify email, phone api
    @POST("forget")
    Call<JsonObject> sendOtpForVerifyUserPhoneEmail(@Body JsonObject body);

    @POST
    Call<JsonObject> verifyUserPhoneEmail(@Url String url, @Body JsonObject body);


    // upload images...........................

    // upload single file
    @Multipart
    @POST(UPLOAD_SINGLE_FILE)
    Call<JsonObject> uploadSingleFile(
            @Part("folder_name") RequestBody folder_name,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST(UPLOAD_VEHICLE_FILE)
    Call<JsonObject> uploadUserVehicleFile(
            @Part("user_id") RequestBody user_id,
            @Part("vehicle_id") RequestBody vehicle_id,
            @Part("doc_type") RequestBody doc_type,
            @Part("doc_name") RequestBody doc_name,
            @Part("doc_number") RequestBody doc_number,
            @Part MultipartBody.Part doc_file
    );

    // set profile data
    @Multipart
    @POST(UPLOAD_PROFILE_IMAGE)
    Call<JsonObject> uploadProfileImage(
            @Part MultipartBody.Part file
    );


    @Multipart
    @PUT(UPDATE_USER_DATA)
    Call<JsonObject> setProfileBasicDetails(
            @Part("user_id") RequestBody user_id,
            @Part MultipartBody.Part profile_pic,
            @Part("first_name") RequestBody first_name,
            @Part("last_name") RequestBody last_name,
            @Part("occupation") RequestBody occupation
    );


    @Multipart
    @PUT(UPDATE_USER_DATA)
    Call<JsonObject> setProfilePublicDetails(
            @Part("user_id") RequestBody user_id,
            @Part MultipartBody.Part public_pic,
            @Part("nick_name") RequestBody nick_name,
            @Part("address") RequestBody address,
            @Part("age") RequestBody age,
            @Part("gender") RequestBody gender
    );


    // add, edit, delete emergency contact
    @Multipart
    @POST(ADD_EMERGENCY_CONTACT)
    Call<JsonObject> addEmergencyContact(
            @Part("user_id") RequestBody user_id,
            @Part MultipartBody.Part profile_pic,
            @Part("first_name") RequestBody first_name,
            @Part("last_name") RequestBody last_name,
            @Part("relation") RequestBody relation,
            @Part("phone_number") RequestBody phone_number
    );

    @Multipart
    @PUT(UPDATE_EMERGENCY_CONTACT)
    Call<JsonObject> editEmergencyContact(
            @Part("user_id") RequestBody user_id,
            @Part MultipartBody.Part profile_pic,
            @Part("first_name") RequestBody first_name,
            @Part("last_name") RequestBody last_name,
            @Part("relation") RequestBody relation,
            @Part("phone_number") RequestBody phone_number,
            @Part("contact_id") RequestBody contact_id,
            @Part("public_id") RequestBody public_id
    );

    @GET
    Call<JsonObject> getEmergencyContact(@Url String url);

    @GET(GET_USER_DETAILS)
    Call<JsonObject> getUserDetails(@Body JsonObject body);

    @Multipart
    @POST("provider_profile_updateFile")
    Call<JsonObject> editEmergencyContact(
            @Part("hit_type") RequestBody hit_type,
            @Part("user_id") RequestBody user_id,
            @Part("contact_id") RequestBody contact_id,
            @Part MultipartBody.Part profile_pic,
            @Part("first_name") RequestBody first_name,
            @Part("last_name") RequestBody last_name,
            @Part("relation") RequestBody relation,
            @Part("phone_number") RequestBody phone_number
    );


    @Multipart
    @POST("provider_profile_updateFile")
    Call<JsonObject> deleteEmergencyContact(
            @Part("hit_type") RequestBody hit_type,
            @Part("user_id") RequestBody user_id,
            @Part("contact_id") RequestBody contact_id
    );


    // Add delivery address api
    @POST(ADD_ADDRESS)
    Call<JsonObject> addEditDeliveryAddress(@Body JsonObject body);

    @GET
    Call<JsonObject> getDeliveryAddress(@Url String url);


    // get and set primary api
    @GET
    Call<JsonObject> getPrimary(@Url String url);

    @POST(CHANG_PRIMARY_CONTACT)
    Call<JsonObject> setPrimary(@Body JsonObject body);

    // get details api enum[all,basic_details,public_details,address_book,emergency_contacts,garage,Device_list,Account_status,chat_room]
    @POST("forget")
    Call<JsonObject> getDetails(@Body JsonObject body);

    // change password api
    @POST(CHANGE_PASSWORD)
    Call<JsonObject> changePassword(@Body JsonObject body);


    // set profile data
    @Multipart
    @POST("provider_profile_updateFile")
    Call<JsonObject> submitReview(
            @Part("user_id") RequestBody user_id,
            @Part("order_id") RequestBody order_id,
            @Part("product_type") RequestBody product_type,
            @Part("rating") RequestBody rating,
            @Part("review_title") RequestBody review_title,
            @Part("review_text") RequestBody review_text,
            @Part MultipartBody.Part media_file
    );


    // get fuel price list
   /* @GET(GET_FUEL_PRICE)
    Call<JsonObject> getFuelPrice();*/


    // Add vehicle
    @POST("forget")
    Call<JsonObject> addVehicle(@Body JsonObject body);


    // Trending Cars
    @POST(GET_TRENDING_CARS)
    Call<JsonObject> getTrendingCars();

    // Trending Cars
    @POST(GET_COMPARE_VEHICLE_DATA_SET)
    Call<JsonObject> getCompareVehicleDataSet(@Body JsonObject body);


    // Tips and tricks
    @GET(GET_TIPS_TRICKS)
    Call<JsonObject> getTipsTrick();

    // Benefit Videos
    @GET(GET_BENEFIT_VIDEO)
    Call<JsonObject> getBenefitVideos();


    // get News
    @GET(GET_NEW_LIST)
    Call<JsonObject> getNews();


   /* // get News
    @FormUrlEncoded
    @POST(GET_NEW_LIST)
    Call<JsonObject> getNews(
            @Field("page") String page,
            @Field("limit") String limit
           // @Field("news_type") String news_type
                   // news_type must be one of: automotive, technology, safety, environment, business, government, general, breaking, featured and for all remove news_type
    );*/


    // chat APIs


    @Multipart
    @POST(SEND_CHAT_MESSAGE)
    Call<JsonObject> sendChatMessage(
            @Part("sender_id") RequestBody sender_id,
            @Part("chat_room_id") RequestBody chat_room_id,
            @Part("message") RequestBody message,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part List<MultipartBody.Part> images   // 🔥 MULTIPLE FILES
    );



}

