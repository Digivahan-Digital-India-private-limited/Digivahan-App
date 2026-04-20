package com.digivahan.data.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.digivahan.data.local.PreferencesManager;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;
    private static String androidDeviceId;
    private static final long TIMEOUT = 60; // seconds


    private static OkHttpClient getOkHttpClient(Context context) {
        if (androidDeviceId == null) {
            @SuppressLint("HardwareIds")
            String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            androidDeviceId = id;
        }

        PreferencesManager manager = new PreferencesManager(context);
        CommonLogic.showTestLog("ApiClient", "token:- " + manager.getAuthToken());

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new AuthInterceptor(manager))
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String path = original.url().encodedPath();

                    Request.Builder builder = original.newBuilder()
//                            .header("Accept", "application/json")
                            .header("device_type", "android")
                            .header("device_id", androidDeviceId);

                    if (path.contains("profile-image") || path.contains("challan-plus")) {
                        builder.header("Content-Type", "application/json");
                        if (path.contains("challan-plus")){
                            builder.header("accessToken", "648695275fda1269b36e1e32fbc1299e:31a824059ff2cb62cbff86d892cd9b84");
                        }
                    }

                    return chain.proceed(builder.method(original.method(), original.body()).build());
                })
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

    }

    /*public static ApiSet getBBPSServices(Context context) {
        if (androidDeviceId == null) {
            @SuppressLint("HardwareIds")
            String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            androidDeviceId = id;
        }

        PreferencesManager manager = new PreferencesManager(context);
        CommonLogic.showTestLog("ApiClient", "token:- " + manager.getAuthToken());

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new AuthInterceptor(manager))
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String path = original.url().encodedPath();

                    Request.Builder builder = original.newBuilder();
                    builder.header("Content-Type", "application/json");
                    builder.header("Accept", "application/json");
                    builder.header("X-Ipay-Auth-Code", "1");
                    builder.header("X-Ipay-Client-Id", "YWY3OTAzYzNlM2ExZTJlOUU4Rb8Jr1aUrRcQW23AqtI=");

                    if (path.equalsIgnoreCase("https://api.instantpay.in/marketplace/utilityPayments/payment")) {
                        builder.header("X-Ipay-Client-Secret", "fc232b945be2e1eaac9656d9f93089d212816454e00a8d83dc9e4ea89d569e44");
                    }else {
                        builder.header("X-Ipay-Client-Secret", "1b373ecadce2f4eb1830101fdb5833bc39cc740355fc8b619408a4c073028588");
                    }

                    builder.header("X-Ipay-Endpoint-Ip", "54.86.50.139");
                    builder.header("X-Ipay-Outlet-Id", "629874");

                    return chain.proceed(builder.method(original.method(), original.body()).build());
                })
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://api.instantpay.in/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiSet.class);

    }*/

    public static ApiSet getApiService(Context context) {
        CommonLogic.showTestLog("ApiClient", "BaseUrl: "+ CommonMethods.getBaseUrl(context));
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(CommonMethods.getBaseUrl(context))
                    .client(getOkHttpClient(context))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiSet.class);
    }

    public static ApiSet getApiServiceWithoutBaseUrl(Context context) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .client(getOkHttpClient(context))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiSet.class);
    }

    public static ApiSet getApiService(Context context, boolean isFile) {
        if (retrofit == null) {
            if (isFile){
                retrofit = new Retrofit.Builder()
                        .baseUrl(CommonMethods.getBaseUrl(context))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            else {
            retrofit = new Retrofit.Builder()
                    .baseUrl(CommonMethods.getBaseUrl(context))
                    .client(getOkHttpClient(context))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            }
        }
        return retrofit.create(ApiSet.class);
    }
}

