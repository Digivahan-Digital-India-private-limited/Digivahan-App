package com.digivahan.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "vehicle_challans",
        indices = {@Index(value = {"vehicleNumber"}, unique = true)}
)
public class VehicleChallanEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String vehicleNumber;

    @NonNull
    public String challanJson;

    @NonNull
    public String lastHitServerDate;   // yyyy-MM-dd
    @NonNull
    public String lastHitServerTime;   // hh:mm a

    public long lastHitServerMillis;   // ⭐ MOST IMPORTANT
}


