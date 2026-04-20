package com.digivahan.ui.Activities.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.digivahan.data.repository.LoginRepository;

import org.json.JSONObject;

public class LoginViewModel extends AndroidViewModel {

    private LoginRepository repository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new LoginRepository(application);
    }

    public LiveData<JSONObject> login(String user, String password, boolean isOldUser) {
        return repository.login(user, password, isOldUser);
    }
}

