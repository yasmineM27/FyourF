package yasminemassaoudi.grp3.fyourf;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to interact with Supabase using REST API
 * Uses simple HTTP requests instead of Supabase SDK
 */
public class SupabaseLocationService {

    private static final String TAG = "SupabaseService";
    private static final String SUPABASE_URL = Config.SUPABASE_URL;
    private static final String SUPABASE_ANON_KEY = Config.SUPABASE_ANON_KEY;
    private static final String TABLE_NAME = "location_history";

    private final ExecutorService executorService;

    public SupabaseLocationService() {
        this.executorService = Executors.newFixedThreadPool(3);
    }

    /**
     * Add or update a location in Supabase
     */
    public void addOrUpdateLocation(String phone, double latitude, double longitude) {
        executorService.execute(() -> {
            try {
                // First, check if location exists
                String checkUrl = SUPABASE_URL + "/rest/v1/" + TABLE_NAME + "?phone=eq." + phone;
                String existingData = makeGetRequest(checkUrl);

                JSONArray existingArray = new JSONArray(existingData);

                if (existingArray.length() > 0) {
                    // Update existing record
                    updateLocation(phone, latitude, longitude);
                } else {
                    // Insert new record
                    insertLocation(phone, latitude, longitude);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in addOrUpdateLocation", e);
            }
        });
    }

    /**
     * Insert a new location
     */
    private void insertLocation(String phone, double latitude, double longitude) {
        try {
            String urlString = SUPABASE_URL + "/rest/v1/" + TABLE_NAME;
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);

            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("phone", phone);
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);
            jsonBody.put("timestamp", System.currentTimeMillis());

            // Send request
            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 201 || responseCode == 200) {
                Log.d(TAG, "✓ Location inserted successfully for " + phone);
            } else {
                Log.e(TAG, "✗ Insert failed with code: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "Error inserting location", e);
        }
    }

    /**
     * Update an existing location
     */
    private void updateLocation(String phone, double latitude, double longitude) {
        try {
            String urlString = SUPABASE_URL + "/rest/v1/" + TABLE_NAME + "?phone=eq." + phone;
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);

            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);
            jsonBody.put("timestamp", System.currentTimeMillis());

            // Send request
            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 204) {
                Log.d(TAG, "✓ Location updated successfully for " + phone);
            } else {
                Log.e(TAG, "✗ Update failed with code: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "Error updating location", e);
        }
    }



    /**
     * Get all locations from Supabase
     */
    public CompletableFuture<List<LocationEntry>> getAllLocations() {
        CompletableFuture<List<LocationEntry>> future = new CompletableFuture<>();

        executorService.execute(() -> {
            try {
                String urlString = SUPABASE_URL + "/rest/v1/" + TABLE_NAME + "?order=timestamp.desc";
                String response = makeGetRequest(urlString);

                List<LocationEntry> locations = parseLocations(response);
                future.complete(locations);

                Log.d(TAG, "✓ Retrieved " + locations.size() + " locations");

            } catch (Exception e) {
                Log.e(TAG, "Error getting all locations", e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Make a GET request to Supabase
     */
    private String makeGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("GET request failed with code: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        conn.disconnect();

        return response.toString();
    }

    /**
     * Parse JSON response to LocationEntry list
     */
    private List<LocationEntry> parseLocations(String jsonResponse) {
        List<LocationEntry> locations = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String phone = obj.optString("phone", "");
                double latitude = obj.optDouble("latitude", 0.0);
                double longitude = obj.optDouble("longitude", 0.0);
                long timestamp = obj.optLong("timestamp", System.currentTimeMillis());

                locations.add(new LocationEntry(phone, latitude, longitude, timestamp));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing locations", e);
        }

        return locations;
    }

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * LocationEntry class to hold location data
     */
    public static class LocationEntry {
        public String phone;
        public String phoneNumber;
        public double latitude;
        public double longitude;
        public long timestamp;

        public LocationEntry(String phone, double latitude, double longitude, long timestamp) {
            this.phone = phone;
            this.phoneNumber = phone;
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "LocationEntry{" +
                    "phone='" + phone + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
