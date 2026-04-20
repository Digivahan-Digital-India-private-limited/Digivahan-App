package com.digivahan.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class OffenceModel implements Parcelable, Serializable {

    private String offence_name;
    private String offence_fine;
    private String motor_vehicle_act;

    public OffenceModel() {}

    public OffenceModel(String offence_name, String offence_fine, String motor_vehicle_act) {
        this.offence_name = offence_name;
        this.offence_fine = offence_fine;
        this.motor_vehicle_act = motor_vehicle_act;
    }

    protected OffenceModel(Parcel in) {
        offence_name = in.readString();
        offence_fine = in.readString();
        motor_vehicle_act = in.readString();
    }

    public static final Creator<OffenceModel> CREATOR = new Creator<OffenceModel>() {
        @Override
        public OffenceModel createFromParcel(Parcel in) {
            return new OffenceModel(in);
        }

        @Override
        public OffenceModel[] newArray(int size) {
            return new OffenceModel[size];
        }
    };

    public String getOffence_name() {
        return offence_name;
    }

    public void setOffence_name(String offence_name) {
        this.offence_name = offence_name;
    }

    public String getOffence_fine() {
        return offence_fine;
    }

    public void setOffence_fine(String offence_fine) {
        this.offence_fine = offence_fine;
    }

    public String getMotor_vehicle_act() {
        return motor_vehicle_act;
    }

    public void setMotor_vehicle_act(String motor_vehicle_act) {
        this.motor_vehicle_act = motor_vehicle_act;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(offence_name);
        dest.writeString(offence_fine);
        dest.writeString(motor_vehicle_act);
    }

    @Override
    public String toString() {
        return "OffenceModel{" +
                "offence_name='" + offence_name + '\'' +
                ", offence_fine='" + offence_fine + '\'' +
                ", motor_vehicle_act='" + motor_vehicle_act + '\'' +
                '}';
    }
}
