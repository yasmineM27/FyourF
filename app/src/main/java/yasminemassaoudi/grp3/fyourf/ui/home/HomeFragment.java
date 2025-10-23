package yasminemassaoudi.grp3.fyourf.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yasminemassaoudi.grp3.fyourf.LocationDatabase;
import yasminemassaoudi.grp3.fyourf.R;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "HomeFragment";
    private GoogleMap mMap;
    private LocationDatabase locationDatabase;
    private TextView friendCountText;
    private FloatingActionButton refreshMapBtn;
    private Map<String, Marker> markerMap = new HashMap<>();
    private BroadcastReceiver locationUpdateReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        friendCountText = view.findViewById(R.id.friendCountText);
        refreshMapBtn = view.findViewById(R.id.refreshMapBtn);

        locationDatabase = new LocationDatabase(getContext());

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup refresh button
        refreshMapBtn.setOnClickListener(v -> {
            loadFriendLocations();
            Toast.makeText(getContext(), "Map refreshed", Toast.LENGTH_SHORT).show();
        });

        // Setup broadcast receiver for real-time updates
        setupLocationUpdateReceiver();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready");
        mMap = googleMap;

        // Configure map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Set default camera position (world view)
        LatLng defaultPosition = new LatLng(20.0, 0.0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 2f));

        // Load friend locations
        loadFriendLocations();
    }

    private void setupLocationUpdateReceiver() {
        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String phoneNumber = intent.getStringExtra("phoneNumber");
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                double longitude = intent.getDoubleExtra("longitude", 0.0);

                Log.d(TAG, "Received location update broadcast for " + phoneNumber);

                // Reload all locations to update the map
                loadFriendLocations();

                Toast.makeText(context, "📍 Location updated for " + phoneNumber, Toast.LENGTH_SHORT).show();
            }
        };

        // Register receiver
        if (getContext() != null) {
            ContextCompat.registerReceiver(
                    getContext(),
                    locationUpdateReceiver,
                    new IntentFilter("LOCATION_UPDATED"),
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );
        }
    }

    private void loadFriendLocations() {
        if (mMap == null) {
            Log.w(TAG, "Map not ready yet");
            return;
        }

        // Clear existing markers
        mMap.clear();
        markerMap.clear();

        // Get all locations from database
        List<LocationDatabase.LocationEntry> locations = locationDatabase.getAllLocations();

        Log.d(TAG, "Loading " + locations.size() + " friend locations");

        int validLocationCount = 0;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasValidLocations = false;

        for (LocationDatabase.LocationEntry entry : locations) {
            // Skip invalid or pending locations
            if ((entry.latitude == 0.0 && entry.longitude == 0.0) ||
                (entry.latitude == 999.0 && entry.longitude == 999.0)) {
                Log.d(TAG, "Skipping invalid location for " + entry.phoneNumber);
                continue;
            }

            validLocationCount++;
            LatLng position = new LatLng(entry.latitude, entry.longitude);

            // Add marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(entry.phoneNumber)
                    .snippet("📍 " + String.format("%.6f, %.6f", entry.latitude, entry.longitude))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            Marker marker = mMap.addMarker(markerOptions);
            markerMap.put(entry.phoneNumber, marker);

            boundsBuilder.include(position);
            hasValidLocations = true;

            Log.d(TAG, "Added marker for " + entry.phoneNumber + " at " + position);
        }

        // Update friend count text
        if (validLocationCount == 0) {
            friendCountText.setText("No friends' locations yet");
        } else if (validLocationCount == 1) {
            friendCountText.setText("1 friend's location");
        } else {
            friendCountText.setText(validLocationCount + " friends' locations");
        }

        // Adjust camera to show all markers
        if (hasValidLocations) {
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 100; // padding in pixels
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e(TAG, "Error adjusting camera: " + e.getMessage());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload locations when fragment becomes visible
        if (mMap != null) {
            loadFriendLocations();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver
        if (getContext() != null && locationUpdateReceiver != null) {
            try {
                getContext().unregisterReceiver(locationUpdateReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered: " + e.getMessage());
            }
        }
    }
}
