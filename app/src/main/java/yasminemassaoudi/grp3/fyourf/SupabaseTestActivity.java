package yasminemassaoudi.grp3.fyourf;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Test Activity to verify Supabase integration
 */
public class SupabaseTestActivity extends AppCompatActivity {
    
    private static final String TAG = "SupabaseTest";
    private TextView resultTextView;
    private Button testConnectionBtn;
    private Button testInsertBtn;
    private Button testFetchBtn;
    private SupabaseLocationService supabaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supabase_test);

        // Initialize views
        resultTextView = findViewById(R.id.resultTextView);
        testConnectionBtn = findViewById(R.id.testConnectionBtn);
        testInsertBtn = findViewById(R.id.testInsertBtn);
        testFetchBtn = findViewById(R.id.testFetchBtn);

        // Initialize Supabase service
        try {
            supabaseService = new SupabaseLocationService();
            appendResult("✓ Supabase service initialized successfully\n");
            appendResult("URL: " + Config.SUPABASE_URL + "\n\n");
        } catch (Exception e) {
            appendResult("✗ Error initializing Supabase: " + e.getMessage() + "\n");
            Log.e(TAG, "Initialization error", e);
        }

        // Test connection button
        testConnectionBtn.setOnClickListener(v -> testConnection());

        // Test insert button
        testInsertBtn.setOnClickListener(v -> testInsert());

        // Test fetch button
        testFetchBtn.setOnClickListener(v -> testFetch());
    }

    private void testConnection() {
        appendResult("\n--- Testing Connection ---\n");
        appendResult("Supabase URL: " + Config.SUPABASE_URL + "\n");
        appendResult("API Key configured: " + (Config.SUPABASE_ANON_KEY != null && !Config.SUPABASE_ANON_KEY.isEmpty()) + "\n");
        
        Toast.makeText(this, "Connection test started", Toast.LENGTH_SHORT).show();
        
        // Try to fetch data to test connection
        testFetch();
    }

    private void testInsert() {
        appendResult("\n--- Testing Insert ---\n");
        
        try {
            // Test data
            String testPhone = "+1234567890";
            double testLat = 36.8065;
            double testLon = 10.1815;
            
            appendResult("Inserting test location:\n");
            appendResult("Phone: " + testPhone + "\n");
            appendResult("Latitude: " + testLat + "\n");
            appendResult("Longitude: " + testLon + "\n");
            
            // Insert using Supabase service
            supabaseService.addOrUpdateLocation(testPhone, testLat, testLon);
            
            appendResult("✓ Insert request sent successfully\n");
            Toast.makeText(this, "Insert test completed", Toast.LENGTH_SHORT).show();
            
            // Wait a bit then fetch to verify
            new android.os.Handler().postDelayed(this::testFetch, 2000);
            
        } catch (Exception e) {
            appendResult("✗ Insert error: " + e.getMessage() + "\n");
            Log.e(TAG, "Insert error", e);
            Toast.makeText(this, "Insert failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void testFetch() {
        appendResult("\n--- Testing Fetch ---\n");
        
        try {
            supabaseService.getAllLocations().thenAccept(locations -> {
                runOnUiThread(() -> {
                    if (locations != null && !locations.isEmpty()) {
                        appendResult("✓ Fetched " + locations.size() + " location(s)\n\n");
                        
                        for (SupabaseLocationService.LocationEntry location : locations) {
                            appendResult("Phone: " + location.phone + "\n");
                            appendResult("Lat: " + location.latitude + ", Lon: " + location.longitude + "\n");
                            appendResult("Timestamp: " + location.timestamp + "\n");
                            appendResult("---\n");
                        }
                        
                        Toast.makeText(this, "Fetch successful: " + locations.size() + " locations", Toast.LENGTH_SHORT).show();
                    } else {
                        appendResult("✓ Fetch successful but no data found\n");
                        appendResult("The table might be empty\n");
                        Toast.makeText(this, "No locations found in database", Toast.LENGTH_SHORT).show();
                    }
                });
            }).exceptionally(throwable -> {
                runOnUiThread(() -> {
                    appendResult("✗ Fetch error: " + throwable.getMessage() + "\n");
                    Log.e(TAG, "Fetch error", throwable);
                    Toast.makeText(this, "Fetch failed: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
                return null;
            });
            
        } catch (Exception e) {
            appendResult("✗ Fetch error: " + e.getMessage() + "\n");
            Log.e(TAG, "Fetch error", e);
            Toast.makeText(this, "Fetch failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void appendResult(String text) {
        runOnUiThread(() -> {
            resultTextView.append(text);
            Log.d(TAG, text.trim());
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (supabaseService != null) {
            supabaseService.shutdown();
        }
    }
}

