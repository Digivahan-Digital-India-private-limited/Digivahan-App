package com.digivahan.ui.Activities.BBPSServices.FasTag;

public class FasTagAdditionalDetails {
    String Name, Value;


    public FasTagAdditionalDetails() {}

    public FasTagAdditionalDetails(String name, String value) {
        Name = name;
        Value = value;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
