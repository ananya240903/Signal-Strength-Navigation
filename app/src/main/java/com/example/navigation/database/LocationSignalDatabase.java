package com.example.navigation.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {LocationSignal.class}, version = 1)
public abstract class LocationSignalDatabase extends RoomDatabase {

    private static volatile LocationSignalDatabase INSTANCE;

    public abstract LocationSignalDao locationSignalDao();

    public static LocationSignalDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LocationSignalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    LocationSignalDatabase.class, "location_signal_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}


