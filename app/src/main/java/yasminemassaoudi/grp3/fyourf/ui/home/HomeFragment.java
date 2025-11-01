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

import yasminemassaoudi.grp3.fyourf.Config;
import yasminemassaoudi.grp3.fyourf.Download;
import yasminemassaoudi.grp3.fyourf.Loading;
import yasminemassaoudi.grp3.fyourf.LocationDatabase;
import yasminemassaoudi.grp3.fyourf.MySQLLocationService;
import yasminemassaoudi.grp3.fyourf.R;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "HomeFragment";
    private GoogleMap mMap;
    private LocationDatabase locationDatabase;
    private MySQLLocationService mysqlService;
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

        // Initialiser le service MySQL si activé
        if (Config.USE_MYSQL) {
            mysqlService = new MySQLLocationService();
            Log.d(TAG, "✓ Service MySQL initialisé");
        }

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

        // Toujours charger depuis la base locale d'abord (rapide et fiable)
        loadFromLocalDatabase();

        // Si MySQL est activé, synchroniser en arrière-plan
        if (Config.USE_MYSQL) {
            syncFromMySQL();
        }
    }

    /**
     * Synchronise depuis MySQL en arrière-plan
     */
    private void syncFromMySQL() {
        Log.d(TAG, "Synchronisation depuis MySQL en arrière-plan...");

        new Loading(new Loading.LoadingCallback() {
            @Override
            public void onLoadingComplete(List<Loading.PositionData> positions) {
                Log.d(TAG, "✓ Synchronisation MySQL réussie: " + positions.size() + " positions");

                // Sauvegarder dans la base locale
                for (Loading.PositionData pos : positions) {
                    if (pos.isValid()) {
                        locationDatabase.addLocation(pos.numero, pos.latitude, pos.longitude);
                    }
                }

                // Recharger et afficher
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadFromLocalDatabase();
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "✓ Synchronisé avec MySQL", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onLoadingError(String error) {
                Log.w(TAG, "Synchronisation MySQL échouée: " + error);
                // Pas de toast d'erreur, on utilise déjà les données locales
            }
        }).start();
    }

    /**
     * Charge les positions depuis MySQL
     */
    private void loadFromMySQL() {
        Log.d(TAG, "Chargement depuis MySQL...");

        // Utiliser Loading (Thread) pour charger les données
        new Loading(new Loading.LoadingCallback() {
            @Override
            public void onLoadingComplete(List<Loading.PositionData> positions) {
                Log.d(TAG, "✓ Positions chargées depuis MySQL: " + positions.size());

                // Sauvegarder dans la base locale pour cache
                for (Loading.PositionData pos : positions) {
                    locationDatabase.addLocation(pos.numero, pos.latitude, pos.longitude);
                }

                // Afficher sur la carte
                displayPositionsOnMap(positions);
            }

            @Override
            public void onLoadingError(String error) {
                Log.e(TAG, "✗ Erreur chargement MySQL: " + error);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Erreur MySQL: " + error, Toast.LENGTH_SHORT).show();
                }

                // Fallback: charger depuis la base locale
                loadFromLocalDatabase();
            }
        }).start();
    }

    /**
     * Charge les positions depuis la base de données locale
     */
    private void loadFromLocalDatabase() {
        Log.d(TAG, "Chargement depuis la base locale...");

        // Clear existing markers (vérifier que mMap n'est pas null)
        if (mMap != null) {
            mMap.clear();
        }
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

            // Add marker (vérifier que mMap n'est pas null)
            if (mMap != null) {
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
        if (hasValidLocations && mMap != null) {
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 100; // padding in pixels
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e(TAG, "Error adjusting camera: " + e.getMessage());
            }
        }
    }

    /**
     * Affiche les positions sur la carte
     */
    private void displayPositionsOnMap(List<Loading.PositionData> positions) {
        if (mMap == null) return;

        // Clear existing markers
        mMap.clear();
        markerMap.clear();

        int validLocationCount = 0;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasValidLocations = false;

        for (Loading.PositionData pos : positions) {
            if (!pos.isValid()) {
                continue;
            }

            validLocationCount++;
            LatLng position = new LatLng(pos.latitude, pos.longitude);

            // Add marker
            String title = pos.pseudo != null && !pos.pseudo.isEmpty() ? pos.pseudo : pos.numero;
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet("📍 " + String.format("%.6f, %.6f", pos.latitude, pos.longitude))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            Marker marker = mMap.addMarker(markerOptions);
            markerMap.put(pos.numero, marker);

            boundsBuilder.include(position);
            hasValidLocations = true;
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
                int padding = 100;
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

        // Fermer le service MySQL
        if (mysqlService != null) {
            mysqlService.shutdown();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify map reference
        mMap = null;
    }



}
