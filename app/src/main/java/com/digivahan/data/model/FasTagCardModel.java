package com.digivahan.data.model;

import java.io.Serializable;

public class FasTagCardModel implements Serializable {

    String billerId, vehicleNumber,
    billerName, categoryKey, type, categoryName, coverageCity, coverageState, updatedDate, billerStatus, iconUrl, enquiryReferenceId, customerName, walletBalance;
    int coveragePincode;
    boolean isAvailable;

    double minAmount, maxAmount;
    public FasTagCardModel() {}

    public String getWalletBalance() {
        return walletBalance;
    }

    public double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(double minAmount) {
        this.minAmount = minAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public void setWalletBalance(String walletBalance) {
        this.walletBalance = walletBalance;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEnquiryReferenceId() {
        return enquiryReferenceId;
    }

    public void setEnquiryReferenceId(String enquiryReferenceId) {
        this.enquiryReferenceId = enquiryReferenceId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getBillerId() {
        return billerId;
    }

    public void setBillerId(String billerId) {
        this.billerId = billerId;
    }

    public String getBillerName() {
        return billerName;
    }

    public void setBillerName(String billerName) {
        this.billerName = billerName;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCoverageCity() {
        return coverageCity;
    }

    public void setCoverageCity(String coverageCity) {
        this.coverageCity = coverageCity;
    }

    public String getCoverageState() {
        return coverageState;
    }

    public void setCoverageState(String coverageState) {
        this.coverageState = coverageState;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getBillerStatus() {
        return billerStatus;
    }

    public void setBillerStatus(String billerStatus) {
        this.billerStatus = billerStatus;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getCoveragePincode() {
        return coveragePincode;
    }

    public void setCoveragePincode(int coveragePincode) {
        this.coveragePincode = coveragePincode;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
