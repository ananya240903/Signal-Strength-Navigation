package com.example.navigation;

public class PredictionResponse {
    private String location;
    private double predicted_signal_strength;

    // Getter and Setter for 'location'
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter and Setter for 'predicted_signal_strength'
    public double getPredictedSignalStrength() {
        return predicted_signal_strength;
    }

    public void setPredictedSignalStrength(double predicted_signal_strength) {
        this.predicted_signal_strength = predicted_signal_strength;
    }
}

