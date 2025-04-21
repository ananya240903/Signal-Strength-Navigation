package com.example.navigation;

import java.util.List;

public class ApiResponse {
    private String sim;
    private List<LocationRecommendation> recommendations;

    // Getter and Setter for 'sim'
    public String getSim() {
        return sim;
    }

    public void setSim(String sim) {
        this.sim = sim;
    }

    // Getter and Setter for 'recommendations'
    public List<LocationRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<LocationRecommendation> recommendations) {
        this.recommendations = recommendations;
    }
}

