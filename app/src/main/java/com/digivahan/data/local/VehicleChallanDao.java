package com.digivahan.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface VehicleChallanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(VehicleChallanEntity entity);

    @Query("SELECT * FROM vehicle_challans WHERE vehicleNumber = :vehicleNumber LIMIT 1")
    VehicleChallanEntity getByVehicleNumber(String vehicleNumber);

    @Query("DELETE FROM vehicle_challans WHERE vehicleNumber = :vehicleNumber")
    void deleteByVehicleNumber(String vehicleNumber);

    @Query("DELETE FROM vehicle_challans")
    void clearAll();
}
