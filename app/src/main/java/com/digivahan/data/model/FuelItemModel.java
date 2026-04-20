package com.digivahan.data.model;

public class FuelItemModel {
    String state, petrol, diesel, cng;

    public FuelItemModel() {}

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPetrol() {
        return petrol;
    }

    public void setPetrol(String petrol) {
        this.petrol = petrol;
    }

    public String getDiesel() {
        return diesel;
    }

    public void setDiesel(String diesel) {
        this.diesel = diesel;
    }

    public String getCng() {
        return cng;
    }

    public void setCng(String cng) {
        this.cng = cng;
    }
}
