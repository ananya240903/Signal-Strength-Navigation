package com.example.navigation;

public class LocationRecommendation {
    private String location;
    private double avg_signal_strength;
    private String maps_url;
    private double latitude;
    private double longitude;

    // Getters
    public String getLocation() { return location; }
    public double getAvgSignalStrength() { return avg_signal_strength; }
    public String getMapsUrl() { return maps_url; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setLocation(String location) { this.location = location; }
    public void setAvgSignalStrength(double avg_signal_strength) { this.avg_signal_strength = avg_signal_strength; }
    public void setMapsUrl(String maps_url) { this.maps_url = maps_url; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}


