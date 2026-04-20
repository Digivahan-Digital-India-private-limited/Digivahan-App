package com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "fastag_table",
        indices = {@Index(value = {"vehicleNumber"}, unique = true)}
)
public class FasTagEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String billerId, vehicleNumber, billerName, categoryKey, type,
            categoryName, coverageCity, coverageState, updatedDate,
            billerStatus, iconUrl;

    public int coveragePincode;
    public boolean isAvailable;

    double minAmount, maxAmount;
}