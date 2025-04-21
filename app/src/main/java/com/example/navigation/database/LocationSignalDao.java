package com.example.navigation.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface LocationSignalDao {

    @Insert
    void insert(LocationSignal locationSignal);

    @Query("SELECT * FROM location_signals")
    List<LocationSignal> getAllSignals();
    @Query("SELECT * FROM location_signals WHERE sim = :sim ORDER BY signal_strength ASC LIMIT 3")
    List<LocationSignal> getTopSignalsForSim(String sim);


}



