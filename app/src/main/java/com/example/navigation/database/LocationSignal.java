package com.example.navigation.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "location_signals")
public class LocationSignal {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private double latitude;
    private double longitude;

    @ColumnInfo(name = "signal_strength")
    private int signalStrength;

    @ColumnInfo(name = "sim")
    private String sim;

    public LocationSignal(double latitude, double longitude, int signalStrength, String sim) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.signalStrength = signalStrength;
        this.sim = sim;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getSignalStrength() { return signalStrength; }
    public String getSim() { return sim; }

    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setSignalStrength(int signalStrength) { this.signalStrength = signalStrength; }
    public void setSim(String sim) { this.sim = sim; }
}