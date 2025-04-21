package com.example.navigation;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

// Define the API interface
public interface ApiService {

    // For /chatbot endpoint (POST)
    @POST("/chatbot")
    Call<ApiResponse> getChatbotResponse(@Body ChatbotRequest request);

    // For /predict endpoint (GET)
    @GET("/predict")
    Call<PredictionResponse> getPrediction(@Query("location") String location);

    // For /compare endpoint (GET)
    @GET("/compare")
    Call<ComparisonResponse> compareSignals(@Query("location1") String location1, @Query("location2") String location2);
}
