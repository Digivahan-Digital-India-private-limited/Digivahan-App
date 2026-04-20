package com.digivahan.data.model;

public class PasswordValidationResult {
    public boolean isValid;
    public String message;

    public PasswordValidationResult(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }
}

