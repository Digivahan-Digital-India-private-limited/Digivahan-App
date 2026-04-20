package com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FasTagDao {

    @Insert
    void insert(FasTagEntity entity);

    @Update
    void update(FasTagEntity entity);

    @Delete
    void delete(FasTagEntity entity);

    @Query("SELECT * FROM fastag_table ORDER BY id DESC")
    List<FasTagEntity> getAll();

    // ✅ Get by vehicle (for update check)
    @Query("SELECT * FROM fastag_table WHERE vehicleNumber = :vehicle LIMIT 1")
    FasTagEntity getByVehicle(String vehicle);

    // ✅ Delete all
    @Query("DELETE FROM fastag_table")
    void deleteAll();
}