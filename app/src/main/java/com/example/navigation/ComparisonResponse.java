package com.example.navigation;

import java.util.List;

public class ComparisonResponse {
    private List<LocationComparison> comparison_results;

    // Getter and Setter for 'comparison_results'
    public List<LocationComparison> getComparisonResults() {
        return comparison_results;
    }

    public void setComparisonResults(List<LocationComparison> comparison_results) {
        this.comparison_results = comparison_results;
    }
}

