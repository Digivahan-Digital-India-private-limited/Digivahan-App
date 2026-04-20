package com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FasTagEntity.class}, version = 2)
public abstract class FasTagAppDatabase extends RoomDatabase {

    private static FasTagAppDatabase instance;

    public abstract FasTagDao fasTagDao();

    public static synchronized FasTagAppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FasTagAppDatabase.class,
                            "fastag_db"
                    )
                    .fallbackToDestructiveMigration() // ✅ important
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}