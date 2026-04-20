package com.digivahan.data.model;

public class VehicleCardModel {

    private String carNumber;
    private String pucDate;
    private int carImage;

    public VehicleCardModel() {}

    public VehicleCardModel(String carNumber, String pucDate, int carImage) {
        this.carNumber = carNumber;
        this.pucDate = pucDate;
        this.carImage = carImage;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getPucDate() {
        return pucDate;
    }

    public void setPucDate(String pucDate) {
        this.pucDate = pucDate;
    }

    public int getCarImage() {
        return carImage;
    }

    public void setCarImage(int carImage) {
        this.carImage = carImage;
    }
}
