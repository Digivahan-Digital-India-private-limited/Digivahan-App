package com.digivahan.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class ChallanModel implements Parcelable, Serializable {

    private String accusedName;
    private String accusedFatherName;
    private String rcNumber;
    private String challanNumber;
    private int challanId;
    private String challanDate;
    private String challanStatus;
    private String challanAmount;
    private String rcStateCode;
    private String challanPaymentSource;
    private String rtoOfficeName;
    private String challanPaymentDate;
    private String challanPlace;
    private ArrayList<OffenceModel> offences;

    public ChallanModel() {}

    protected ChallanModel(Parcel in) {
        accusedName = in.readString();
        accusedFatherName = in.readString();
        rcNumber = in.readString();
        challanNumber = in.readString();
        challanId = in.readInt();
        challanDate = in.readString();
        challanStatus = in.readString();
        challanAmount = in.readString();
        rcStateCode = in.readString();
        challanPaymentSource = in.readString();
        rtoOfficeName = in.readString();
        challanPaymentDate = in.readString();
        challanPlace = in.readString();
        offences = in.createTypedArrayList(OffenceModel.CREATOR);
    }

    public static final Creator<ChallanModel> CREATOR = new Creator<ChallanModel>() {
        @Override
        public ChallanModel createFromParcel(Parcel in) {
            return new ChallanModel(in);
        }

        @Override
        public ChallanModel[] newArray(int size) {
            return new ChallanModel[size];
        }
    };

    public String getAccusedName() {
        return accusedName;
    }

    public void setAccusedName(String accusedName) {
        this.accusedName = accusedName;
    }

    public String getAccusedFatherName() {
        return accusedFatherName;
    }

    public void setAccusedFatherName(String accusedFatherName) {
        this.accusedFatherName = accusedFatherName;
    }

    public String getRcNumber() {
        return rcNumber;
    }

    public void setRcNumber(String rcNumber) {
        this.rcNumber = rcNumber;
    }

    public String getChallanNumber() {
        return challanNumber;
    }

    public void setChallanNumber(String challanNumber) {
        this.challanNumber = challanNumber;
    }

    public int getChallanId() {
        return challanId;
    }

    public void setChallanId(int challanId) {
        this.challanId = challanId;
    }

    public String getChallanDate() {
        return challanDate;
    }

    public void setChallanDate(String challanDate) {
        this.challanDate = challanDate;
    }

    public String getChallanStatus() {
        return challanStatus;
    }

    public void setChallanStatus(String challanStatus) {
        this.challanStatus = challanStatus;
    }

    public String getChallanAmount() {
        return challanAmount;
    }

    public void setChallanAmount(String challanAmount) {
        this.challanAmount = challanAmount;
    }

    public String getRcStateCode() {
        return rcStateCode;
    }

    public void setRcStateCode(String rcStateCode) {
        this.rcStateCode = rcStateCode;
    }

    public String getChallanPaymentSource() {
        return challanPaymentSource;
    }

    public void setChallanPaymentSource(String challanPaymentSource) {
        this.challanPaymentSource = challanPaymentSource;
    }

    public String getRtoOfficeName() {
        return rtoOfficeName;
    }

    public void setRtoOfficeName(String rtoOfficeName) {
        this.rtoOfficeName = rtoOfficeName;
    }

    public String getChallanPaymentDate() {
        return challanPaymentDate;
    }

    public void setChallanPaymentDate(String challanPaymentDate) {
        this.challanPaymentDate = challanPaymentDate;
    }

    public String getChallanPlace() {
        return challanPlace;
    }

    public void setChallanPlace(String challanPlace) {
        this.challanPlace = challanPlace;
    }

    public ArrayList<OffenceModel> getOffences() {
        return offences;
    }

    public void setOffences(ArrayList<OffenceModel> offences) {
        this.offences = offences;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accusedName);
        dest.writeString(accusedFatherName);
        dest.writeString(rcNumber);
        dest.writeString(challanNumber);
        dest.writeInt(challanId);
        dest.writeString(challanDate);
        dest.writeString(challanStatus);
        dest.writeString(challanAmount);
        dest.writeString(rcStateCode);
        dest.writeString(challanPaymentSource);
        dest.writeString(rtoOfficeName);
        dest.writeString(challanPaymentDate);
        dest.writeString(challanPlace);
        dest.writeTypedList(offences);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "ChallanModel{" +
                "accusedName='" + accusedName + '\'' +
                ", rcNumber='" + rcNumber + '\'' +
                ", challanNumber='" + challanNumber + '\'' +
                ", challanDate='" + challanDate + '\'' +
                ", challanStatus='" + challanStatus + '\'' +
                ", challanAmount='" + challanAmount + '\'' +
                ", challanPlace='" + challanPlace + '\'' +
                ", offences=" + offences +
                '}';
    }


}
