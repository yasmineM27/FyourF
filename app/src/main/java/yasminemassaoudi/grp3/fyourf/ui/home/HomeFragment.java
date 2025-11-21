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
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private TextView lastUpdateText;
    private MaterialButton refreshMapBtn;
    private MaterialButton centerMapBtn;
    private Map<String, Marker> markerMap = new HashMap<>();
    private BroadcastReceiver locationUpdateReceiver;
    private LatLngBounds lastBounds;
    private long lastUpdateTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_home, container, false);

            // Initialize UI elements
            friendCountText = view.findViewById(R.id.friendCountText);
            lastUpdateText = view.findViewById(R.id.lastUpdateText);
            refreshMapBtn = view.findViewById(R.id.refreshMapBtn);
            centerMapBtn = view.findViewById(R.id.centerMapBtn);


            locationDatabase = new LocationDatabase(getContext());

            // Initialiser le service MySQL si activé
            if (Config.USE_MYSQL) {
                mysqlService = new MySQLLocationService();
                Log.d(TAG, "✅ Service MySQL initialisé");
            }

            // Initialize map
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            // Setup button listeners
            setupButtonListeners();

            // Setup broadcast receiver for real-time updates
            setupLocationUpdateReceiver();

            Log.d(TAG, "HomeFragment créé avec succès");
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Erreur onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return inflater.inflate(R.layout.fragment_home, container, false);
        }
    }

    private void setupButtonListeners() {
        try {
            // Refresh button
            if (refreshMapBtn != null) {
                refreshMapBtn.setOnClickListener(v -> {
                    Log.d(TAG, "Refresh button clicked");
                    loadFriendLocations();
                    updateLastUpdateTime();
                    Toast.makeText(getContext(), "Carte actualisée", Toast.LENGTH_SHORT).show();
                });
            }

            // Center button
            if (centerMapBtn != null) {
                centerMapBtn.setOnClickListener(v -> {
                    Log.d(TAG, "Center button clicked");
                    if (lastBounds != null && mMap != null) {
                        try {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(lastBounds, 100));
                            Toast.makeText(getContext(), "📍 Centrage sur tous les amis", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur centrage: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(getContext(), "⚠️ Aucune position à centrer", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Filter button

        } catch (Exception e) {
            Log.e(TAG, "Erreur setupButtonListeners: " + e.getMessage(), e);
        }
    }

    private void updateLastUpdateTime() {
        try {
            lastUpdateTime = System.currentTimeMillis();
            if (lastUpdateText != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String timeStr = sdf.format(new Date(lastUpdateTime));
                lastUpdateText.setText(timeStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur updateLastUpdateTime: " + e.getMessage());
        }
    }

    private void showFilterDialog() {
        Toast.makeText(getContext(), "🔍 Filtre - Fonctionnalité à venir", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Filter dialog - À implémenter");
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

                Toast.makeText(context, "Location updated for " + phoneNumber, Toast.LENGTH_SHORT).show();
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
        try {
            Log.d(TAG, "Chargement depuis la base locale...");

            // Clear existing markers (vérifier que mMap n'est pas null)
            if (mMap != null) {
                mMap.clear();
            }
            markerMap.clear();

            // Get all locations from database
            List<LocationDatabase.LocationEntry> locations = locationDatabase.getAllLocations();

            Log.d(TAG, "📍 Chargement de " + locations.size() + " positions d'amis");

            int validLocationCount = 0;
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boolean hasValidLocations = false;

            for (LocationDatabase.LocationEntry entry : locations) {
                // Skip invalid or pending locations
                if ((entry.latitude == 0.0 && entry.longitude == 0.0) ||
                    (entry.latitude == 999.0 && entry.longitude == 999.0)) {
                    Log.d(TAG, "Position invalide pour " + entry.phoneNumber);
                    continue;
                }

                validLocationCount++;
                LatLng position = new LatLng(entry.latitude, entry.longitude);

                // Add marker (vérifier que mMap n'est pas null)
                if (mMap != null) {
                    // Utiliser des couleurs différentes pour les marqueurs
                    float hue = (validLocationCount % 10) * 36; // Couleurs variées

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(position)
                            .title(entry.phoneNumber)
                            .snippet("📍 " + String.format("%.6f, %.6f", entry.latitude, entry.longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(hue));

                    Marker marker = mMap.addMarker(markerOptions);
                    markerMap.put(entry.phoneNumber, marker);

                    boundsBuilder.include(position);
                    hasValidLocations = true;

                    Log.d(TAG, "✅ Marqueur ajouté pour " + entry.phoneNumber + " à " + position);
                }
            }

            // Update friend count text with emoji
            if (validLocationCount == 0) {
                friendCountText.setText("0 Friends");
            } else if (validLocationCount == 1) {
                friendCountText.setText("1 Friend");
            } else {
                friendCountText.setText(validLocationCount + " Friends");
            }

            // Update last update time
            updateLastUpdateTime();

            // Adjust camera to show all markers
            if (hasValidLocations && mMap != null) {
                try {
                    lastBounds = boundsBuilder.build();
                    int padding = 100; // padding in pixels
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(lastBounds, padding));
                    Log.d(TAG, "✅ Caméra centrée sur " + validLocationCount + " positions");
                } catch (Exception e) {
                    Log.e(TAG, "❌ Erreur centrage caméra: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur loadFromLocalDatabase: " + e.getMessage(), e);
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
