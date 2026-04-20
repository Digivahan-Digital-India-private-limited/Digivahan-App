package com.digivahan.data.api;

import androidx.annotation.NonNull;

import com.digivahan.data.local.PreferencesManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {
    private PreferencesManager manager;

    public AuthInterceptor(PreferencesManager manager) {
        this.manager = manager;
    }

    @NonNull
    @Override
    public Response intercept(okhttp3.Interceptor.Chain chain) throws IOException {
        String token = manager.getAuthToken(); // ✔ your saved token

        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(request);
    }
}

