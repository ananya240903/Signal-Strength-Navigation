package com.example.navigation;

public class LocationComparison {
    private String location;
    private String best_sim;
    private double best_signal_strength;

    // Getter and Setter for 'location'
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter and Setter for 'best_sim'
    public String getBestSim() {
        return best_sim;
    }

    public void setBestSim(String best_sim) {
        this.best_sim = best_sim;
    }

    // Getter and Setter for 'best_signal_strength'
    public double getBestSignalStrength() {
        return best_signal_strength;
    }

    public void setBestSignalStrength(double best_signal_strength) {
        this.best_signal_strength = best_signal_strength;
    }
}

