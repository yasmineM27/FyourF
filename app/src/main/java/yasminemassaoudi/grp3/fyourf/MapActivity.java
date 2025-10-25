package yasminemassaoudi.grp3.fyourf;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CircleOptions;
import android.graphics.Color;

import yasminemassaoudi.grp3.fyourf.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private String friendName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get data from intent
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        friendName = getIntent().getStringExtra("friendName");

        Log.d(TAG, "MapActivity started with coordinates: " + latitude + ", " + longitude);
        Log.d(TAG, "Friend name: " + friendName);

        // Validate coordinates
        if (latitude == 0.0 && longitude == 0.0) {
            Log.e(TAG, "Invalid coordinates received");
            Toast.makeText(this, "Invalid location data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set up ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Location: " + (friendName != null ? friendName : "Friend"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "MapFragment not found");
            Toast.makeText(this, "Map initialization failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready");
        mMap = googleMap;

        try {
            LatLng friendLocation = new LatLng(latitude, longitude);
            Log.d(TAG, "Setting marker at: " + friendLocation.toString());

            // Configure map settings
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setAllGesturesEnabled(true);

            // Add marker with custom icon
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(friendLocation)
                    .title(friendName != null ? friendName : "Friend's Location")
                    .snippet(" Lat: " + String.format("%.6f", latitude) + "\n Lon: " + String.format("%.6f", longitude))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            mMap.addMarker(markerOptions);

            // Add accuracy circle
            mMap.addCircle(new CircleOptions()
                    .center(friendLocation)
                    .radius(100) // 100 meters radius
                    .fillColor(0x220095F6)
                    .strokeColor(0xFF0095F6)
                    .strokeWidth(2));

            // Move camera to location with animation
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(friendLocation, 16f));

            // Get address asynchronously
            new Thread(() -> {
                try {
                    String address = LocationUtils.getAddressFromCoordinates(this, latitude, longitude);
                    runOnUiThread(() -> {
                        if (address != null && !address.isEmpty()) {
                            Toast.makeText(this,  address, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error getting address: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up map: " + e.getMessage());
            Toast.makeText(this, "Error displaying location on map", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MapActivity destroyed");
    }
}
