/* Fix for MainActivity.java */
package com.example.navigation;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import android.os.Bundle;
import android.telephony.*;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.navigation.database.LocationSignalDao;
import com.example.navigation.database.LocationSignalDatabase;
import com.example.navigation.databinding.ActivityMainBinding;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;


import okhttp3.OkHttpClient;
import okhttp3.Request;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import java.io.IOException;

import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private TextView trackerTextView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TelephonyManager telephonyManager;
    private int signalStrengthValue = -1;
    private LatLng currentLocation;
    private LocationSignalDao locationSignalDao;
    private ActivityMainBinding binding;
    private Polyline currentRouteLine; // top-level variable


    private double extractLatitude(String mapsUrl) {
        try {
            Log.d("MapsURL", "Parsing latitude from: " + mapsUrl);
            Uri uri = Uri.parse(mapsUrl);
            String q = uri.getQueryParameter("q");
            if (q != null) {
                String[] parts = q.split(",");
                return Double.parseDouble(parts[0]);
            }
        } catch (Exception e) {
            Log.e("ExtractLatLng", "Failed to extract latitude", e);
        }
        return 0.0;
    }


    private double extractLongitude(String mapsUrl) {
        try {
            Log.d("MapsURL", "Parsing logitude from: " + mapsUrl);
            Uri uri = Uri.parse(mapsUrl);
            String q = uri.getQueryParameter("q");
            if (q != null) {
                String[] parts = q.split(",");
                return Double.parseDouble(parts[1]);  //
            }
        } catch (Exception e) {
            Log.e("ExtractLatLng", "Failed to extract longitude", e);
        }
        return 0.0;
    }


    private String getMetaDataValue(String name) {
        try {
            ApplicationInfo appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            return bundle != null ? bundle.getString(name) : null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDest = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = "mode=walking"; // instead of driving

        String apiKey = getMetaDataValue("com.google.android.geo.API_KEY"); // get from manifest
        String parameters = strOrigin + "&" + strDest + "&" + mode + "&key=" + apiKey;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }


    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        return poly;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fix: Properly set up the view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fix: Initialize the trackerTextView using binding
        trackerTextView = binding.trackerTextView; // Assuming you have a TextView with id "trackerTextView" in your layout

        // Fix: Set up FAB click listener once using binding
        binding.chatbotFab.setOnClickListener(view -> {
            ChatBotDialog chatDialog = new ChatBotDialog();
            chatDialog.show(getSupportFragmentManager(), "chatbot");
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        locationSignalDao = LocationSignalDatabase.getInstance(this).locationSignalDao();

        requestPermissions();
        initializeSignalStrengthListener();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Remove the duplicate FAB setup code that had null reference
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE},
                    LOCATION_PERMISSION_CODE);
        } else {
            getLastLocation();
        }
    }

    private int getSignalStrengthDbmFromSignal(SignalStrength signalStrength) {
        if (signalStrength != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return -1;
            }

            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            if (cellInfoList != null) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrengthLte lteStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
                        return lteStrength.getDbm();
                    }
                }
            }
        }
        return -1;
    }

    private void initializeSignalStrengthListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                signalStrengthValue = getSignalStrengthDbmFromSignal(signalStrength);
                Log.d("SignalStrength", "Updated Signal Strength: " + signalStrengthValue + " dBm");
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private int getSignalStrengthDbm() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return -1;
        }

        if (signalStrengthValue != -1) {
            return signalStrengthValue;
        }

        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        if (cellInfoList != null) {
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoLte) {
                    CellSignalStrengthLte lteStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
                    signalStrengthValue = lteStrength.getDbm();
                    return signalStrengthValue;
                }
            }
        }
        return -1;
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        int signalStrength = getSignalStrengthDbm();

                        currentLocation = new LatLng(latitude, longitude);
                        Log.d("Location", "Current location set: " + latitude + ", " + longitude);

                        if (myMap != null) {
                            myMap.clear(); // Optional: remove old markers
                            myMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("You are here"));
                            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }

                        if (trackerTextView != null) {
                            String locationText = "Latitude: " + latitude + "\nLongitude: " + longitude +
                                    "\nSignal Strength: " + signalStrength + " dBm";
                            trackerTextView.setText(locationText);
                        }

                    } else {
                        Log.e("LocationError", "Location returned null");
                        Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        if (trackerTextView != null) {
                            trackerTextView.setText("Unable to get current location.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LOCATION_ERROR", "Error fetching location", e);
                    Toast.makeText(MainActivity.this, "Location fetch failed", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        if (currentLocation != null) {
            myMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        } else {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Optional helper if chatbot dialog wants to access location
    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public LocationSignalDao getLocationSignalDao() {
        return locationSignalDao;
    }

    // Keep this method for use in MainActivity
    public static String extractSimProvider(String query) {
        String[] sims = {"Jio", "Airtel", "BSNL", "Vodafone", "Vi"};
        for (String sim : sims) {
            if (query.toLowerCase().contains(sim.toLowerCase())) return sim;
        }
        return null;
    }

    public void getChatbotResponse(String userMessage) {
        ChatbotRequest request = new ChatbotRequest(userMessage);
        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse> call = apiService.getChatbotResponse(request);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.getRecommendations() != null) {
                        sendRecommendationsToChatDialog(apiResponse.getRecommendations());
                    } else {
                        sendToChatBotDialog("No recommendations received from server.");
                    }
                } else {
                    sendToChatBotDialog("Oops! Server error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                sendToChatBotDialog("API Failure: " + t.getMessage());
            }
        });
    }

    private void sendToChatBotDialog(String responseText) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag("chatbot");
        if (frag instanceof ChatBotDialog) {
            ((ChatBotDialog) frag).showBotResponse(responseText);
        }
    }

    private void sendRecommendationsToChatDialog(List<LocationRecommendation> recs) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag("chatbot");
        if (frag instanceof ChatBotDialog) {
            ((ChatBotDialog) frag).showRecommendations(recs);
        }

        if (myMap != null && recs != null) {
            for (LocationRecommendation rec : recs) {
                String loc = rec.getLocation();
                double lat = rec.getLatitude();
                double lng = rec.getLongitude();

                // Log original values
                Log.d("RecDebug", "Loc: " + loc + " | Lat: " + lat + " | Lng: " + lng + " | URL: " + rec.getMapsUrl());

                // Fallback: try parsing from URL if lat/lng are 0
                if ((lat == 0.0 || lng == 0.0) && rec.getMapsUrl() != null) {
                    lat = extractLatitude(rec.getMapsUrl());
                    lng = extractLongitude(rec.getMapsUrl());
                }

                // Final validation before adding marker
                if (lat == 0.0 || lng == 0.0) {
                    Toast.makeText(this, "Invalid coordinates for: " + loc, Toast.LENGTH_SHORT).show();
                    Log.e("MapMarkerError", "Invalid coordinates for: " + loc);
                    continue; // Skip adding this marker
                }

                LatLng position = new LatLng(lat, lng);

                myMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(loc + " (" + rec.getAvgSignalStrength() + " dBm)"));
            }
        }
    }

    public void addMarkerAndZoom(LocationRecommendation rec) {
        Log.d("MapDebug", "Adding marker for: " + rec.getLocation());
        if (myMap == null || rec == null) return;

        double lat = rec.getLatitude();
        double lng = rec.getLongitude();
        Log.d("RouteDebug", "Destination lat: " + lat + ", lng: " + lng);


        //  Only fallback to mapsUrl if BOTH are 0.0
        if ((lat == 0.0 || lng == 0.0) && rec.getMapsUrl() != null) {
            lat = extractLatitude(rec.getMapsUrl());
            lng = extractLongitude(rec.getMapsUrl());
        }

        if (lat == 0.0 || lng == 0.0) {
            Toast.makeText(this, "Invalid coordinates for: " + rec.getLocation(), Toast.LENGTH_SHORT).show();
            Log.e("RouteDebug", "Invalid coordinates for: " + rec.getLocation() + ", mapsUrl=" + rec.getMapsUrl());
            return;
        }

        Log.d("LatLng", "Final destination lat: " + lat + ", lng: " + lng);
        LatLng pos = new LatLng(lat, lng);
        Log.d("RouteDebug", "Final LatLng for routing: " + pos.latitude + ", " + pos.longitude);


        float hue;
        if (rec.getAvgSignalStrength() >= -80) hue = BitmapDescriptorFactory.HUE_GREEN;
        else if (rec.getAvgSignalStrength() >= -95) hue = BitmapDescriptorFactory.HUE_YELLOW;
        else hue = BitmapDescriptorFactory.HUE_RED;

        myMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(rec.getLocation())
                .snippet("Signal: " + rec.getAvgSignalStrength() + " dBm")
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));

        if (currentLocation == null || currentLocation.latitude == 0.0 || currentLocation.longitude == 0.0) {
            Toast.makeText(this, "Waiting for location fix...", Toast.LENGTH_SHORT).show();
            getLastLocation();
            return;
        }

        Log.d("DestinationDebug", "lat: " + lat + ", lng: " + lng);

        drawRouteTo(pos);
    }

    public void drawRouteTo(LatLng destination) {
        if (currentLocation == null || myMap == null) {
            Toast.makeText(this, "Map or current location is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destination == null || destination.latitude == 0.0 || destination.longitude == 0.0) {
            Toast.makeText(this, "Invalid destination coordinates", Toast.LENGTH_SHORT).show();
            Log.e("RouteError", "Destination coordinates are 0.0, cannot draw route.");
            return;
        }

        String apiKey = getMetaDataValue("com.google.android.geo.API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(this, "Google API key is missing", Toast.LENGTH_SHORT).show();
            Log.e("RouteError", "API key not found in manifest");
            return;
        }

        String url = getDirectionsUrl(currentLocation, destination);


        Log.d("RouteURL", "Fetching directions from: " + url);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Route fetch failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("RouteError", "Failed to fetch directions", e);
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("RouteResponse", responseBody);

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray routes = json.getJSONArray("routes");

                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overview = route.optJSONObject("overview_polyline");

                            if (overview != null) {
                                String encodedPolyline = overview.getString("points");
                                JSONArray legs = route.getJSONArray("legs");

                                String distance = legs.getJSONObject(0).getJSONObject("distance").getString("text");
                                String duration = legs.getJSONObject(0).getJSONObject("duration").getString("text");

                                List<LatLng> routePoints = decodePolyline(encodedPolyline);

                                runOnUiThread(() -> {
                                    if (currentRouteLine != null) currentRouteLine.remove();

                                    currentRouteLine = myMap.addPolyline(new PolylineOptions()
                                            .addAll(routePoints)
                                            .color(Color.BLUE)
                                            .width(12f));

                                    Toast.makeText(MainActivity.this,
                                            "Distance: " + distance + " | Duration: " + duration,
                                            Toast.LENGTH_LONG).show();
                                });
                            } else {
                                runOnUiThread(() ->
                                        Toast.makeText(MainActivity.this, "No polyline found in route", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "No route found", Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        Log.e("RouteParseError", "JSON error", e);
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Failed to parse route data", Toast.LENGTH_SHORT).show());
                    }

                } else {
                    Log.e("RouteHTTP", "Response failed: " + response.code() + " - " + response.message());
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Failed to get route: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


}