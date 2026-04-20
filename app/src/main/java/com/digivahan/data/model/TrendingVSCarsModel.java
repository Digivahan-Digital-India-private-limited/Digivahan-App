package com.digivahan.data.model;

import java.io.Serializable;

public class TrendingVSCarsModel implements Serializable {

    public String comparisonId, createdAt;

    public TrendingCarsModel car1Data, car2Data;

    public TrendingVSCarsModel() {}

    public String getComparisonId() {
        return comparisonId;
    }

    public void setComparisonId(String comparisonId) {
        this.comparisonId = comparisonId;
    }

    public TrendingCarsModel getCar1Data() {
        return car1Data;
    }

    public void setCar1Data(TrendingCarsModel car1Data) {
        this.car1Data = car1Data;
    }

    public TrendingCarsModel getCar2Data() {
        return car2Data;
    }

    public void setCar2Data(TrendingCarsModel car2Data) {
        this.car2Data = car2Data;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
