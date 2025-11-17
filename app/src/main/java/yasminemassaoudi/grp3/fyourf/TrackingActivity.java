package yasminemassaoudi.grp3.fyourf;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activité pour gérer le tracking automatique de position
 * Permet de démarrer/arrêter le tracking et visualiser le trajet en temps réel
 */
public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    private static final String TAG = "TrackingActivity";
    private static final String SAVED_TRAJECTORY_POINTS = "trajectory_points";
    private static final String SAVED_TRACKING_START_TIME = "tracking_start_time";
    private static final String SAVED_TOTAL_DISTANCE = "total_distance";

    // UI Elements
    private EditText pseudoInput;
    private EditText numeroInput;
    private EditText intervalInput;
    private Button startTrackingBtn;
    private Button stopTrackingBtn;
    private Button viewTrajectoryBtn;
    private Button statsBtn;
    private TextView statusText;
    private TextView positionCountText;
    private TextView durationText;
    private TextView distanceText;
    private TextView speedText;

    // Map
    private GoogleMap mMap;
    private List<LatLng> trajectoryPoints;
    private LatLng currentUserLocation;
    private com.google.android.gms.maps.model.Marker currentLocationMarker;

    // Service
    private TrackingService trackingService;
    private boolean serviceBound = false;

    // Broadcast Receiver
    private BroadcastReceiver trackingReceiver;

    // Location tracking
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> permissionLauncher;

    // Timer for duration
    private Handler durationHandler;
    private Runnable durationRunnable;
    private long trackingStartTime = 0;

    // Distance tracking
    private double totalDistance = 0.0;
    private LatLng lastRecordedPoint = null;

    // MySQL Sync
    private TrackingSyncManager syncManager;
    private double averageSpeedKmh = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // Initialize sync manager
        syncManager = new TrackingSyncManager(this);

        // Set up ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📍 Tracking GPS");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupPermissionLauncher();
        setupMap();
        setupBroadcastReceiver();

        trajectoryPoints = new ArrayList<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        durationHandler = new Handler(Looper.getMainLooper());

        // Restore state if available
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }
    
    private void initializeViews() {
        pseudoInput = findViewById(R.id.pseudoInput);
        numeroInput = findViewById(R.id.numeroInput);
        intervalInput = findViewById(R.id.intervalInput);
        startTrackingBtn = findViewById(R.id.startTrackingBtn);
        stopTrackingBtn = findViewById(R.id.stopTrackingBtn);
        viewTrajectoryBtn = findViewById(R.id.viewTrajectoryBtn);
        statusText = findViewById(R.id.statusText);
        positionCountText = findViewById(R.id.positionCountText);
        durationText = findViewById(R.id.durationText);
        distanceText = findViewById(R.id.distanceText);
        speedText = findViewById(R.id.speedText);
        statsBtn = findViewById(R.id.statsBtn);

        // Valeurs par défaut
        intervalInput.setText("30");

        // Listeners avec vérification null
        if (startTrackingBtn != null) {
            startTrackingBtn.setOnClickListener(v -> {
                Log.d(TAG, "Start button clicked");
                startTracking();
            });
        }

        if (stopTrackingBtn != null) {
            stopTrackingBtn.setOnClickListener(v -> {
                Log.d(TAG, "Stop button clicked");
                stopTracking();
            });
        }

        if (viewTrajectoryBtn != null) {
            viewTrajectoryBtn.setOnClickListener(v -> {
                Log.d(TAG, "View trajectory button clicked");
                viewFullTrajectory();
            });
        }

        if (statsBtn != null) {
            statsBtn.setOnClickListener(v -> {
                Log.d(TAG, "Stats button clicked");
                showStatisticsDialog();
            });
        }

        // État initial
        updateUIState(false);
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if (fineLocationGranted || coarseLocationGranted) {
                        Log.d(TAG, "Location permissions granted");
                        enableMyLocation();
                    } else {
                        Toast.makeText(this, "Permissions de localisation refusées", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trackingMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Position par défaut (Tunis)
        LatLng defaultPosition = new LatLng(36.8065, 10.1815);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 12));

        // Request location permissions and enable MyLocation
        requestLocationPermissions();

        Log.d(TAG, "Map ready");
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (mMap == null) return;

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                getCurrentUserLocation();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error enabling MyLocation: " + e.getMessage());
        }
    }

    private void getCurrentUserLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                updateCurrentLocationMarker();
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error getting current location: " + e.getMessage());
        }
    }

    private void updateCurrentLocationMarker() {
        if (mMap == null || currentUserLocation == null) return;

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(currentUserLocation)
                .title("Ma position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }
    
    private void setupBroadcastReceiver() {
        trackingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                
                if ("TRACKING_STATUS_CHANGED".equals(action)) {
                    boolean isTracking = intent.getBooleanExtra("isTracking", false);
                    int count = intent.getIntExtra("positionCount", 0);
                    updateUIState(isTracking);
                    positionCountText.setText("Positions: " + count);
                    
                } else if ("NEW_TRACKING_POSITION".equals(action)) {
                    Position position = intent.getParcelableExtra("position");
                    int count = intent.getIntExtra("positionCount", 0);
                    
                    if (position != null) {
                        addPositionToMap(position);
                        positionCountText.setText("Positions: " + count);
                    }
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("TRACKING_STATUS_CHANGED");
        filter.addAction("NEW_TRACKING_POSITION");
        ContextCompat.registerReceiver(this, trackingReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }
    
    private void startTracking() {
        try {
            String pseudo = pseudoInput.getText().toString().trim();
            String numero = numeroInput.getText().toString().trim();
            String intervalStr = intervalInput.getText().toString().trim();

            // Validation
            if (numero.isEmpty()) {
                Toast.makeText(this, "❌ Veuillez entrer un numéro", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Numero vide");
                return;
            }

            if (pseudo.isEmpty()) {
                pseudo = "User_" + System.currentTimeMillis();
                Log.d(TAG, "Pseudo auto-généré: " + pseudo);
            }

            long interval = 30000; // 30 secondes par défaut
            try {
                int seconds = Integer.parseInt(intervalStr);
                if (seconds < 10) {
                    Toast.makeText(this, "❌ Intervalle minimum: 10 secondes", Toast.LENGTH_SHORT).show();
                    return;
                }
                interval = seconds * 1000L;
            } catch (NumberFormatException e) {
                Toast.makeText(this, "❌ Intervalle invalide", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erreur parsing intervalle: " + e.getMessage());
                return;
            }

            // Vérifier les permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "❌ Permissions GPS requises", Toast.LENGTH_SHORT).show();
                requestLocationPermissions();
                return;
            }

            // Démarrer le service
            Intent serviceIntent = new Intent(this, TrackingService.class);
            serviceIntent.putExtra("action", "START_TRACKING");
            serviceIntent.putExtra("pseudo", pseudo);
            serviceIntent.putExtra("numero", numero);
            serviceIntent.putExtra("interval", interval);

            startService(serviceIntent);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

            // Réinitialiser la carte et les données
            trajectoryPoints.clear();
            totalDistance = 0.0;
            lastRecordedPoint = null;
            trackingStartTime = System.currentTimeMillis();
            averageSpeedKmh = 0.0;

            if (mMap != null) {
                mMap.clear();
                // Re-add current location marker
                if (currentUserLocation != null) {
                    updateCurrentLocationMarker();
                }
            }

            // Start duration timer
            startDurationTimer();

            // Update UI
            updateUIState(true);

            Toast.makeText(this, "✅ Tracking démarré - Intervalle: " + intervalStr + "s", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Tracking démarré - Numero: " + numero + " | Intervalle: " + interval + "ms");
        } catch (Exception e) {
            Log.e(TAG, "Erreur startTracking: " + e.getMessage(), e);
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopTracking() {
        try {
            Log.d(TAG, "Arrêt du tracking...");

            // Stop duration timer first
            stopDurationTimer();

            // Stop service
            if (serviceBound && trackingService != null) {
                trackingService.stopTracking();
                try {
                    unbindService(serviceConnection);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Service not bound: " + e.getMessage());
                }
                serviceBound = false;
            }

            Intent serviceIntent = new Intent(this, TrackingService.class);
            stopService(serviceIntent);

            // Update UI
            updateUIState(false);

            // Save trajectory to MySQL
            if (!trajectoryPoints.isEmpty()) {
                saveTrajectoryToMySQL();
            } else {
                Toast.makeText(this, "⚠️ Aucune position enregistrée", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, "✅ Tracking arrêté", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Tracking arrêté - Points: " + trajectoryPoints.size() + " | Distance: " + totalDistance + "m");
        } catch (Exception e) {
            Log.e(TAG, "Erreur stopTracking: " + e.getMessage(), e);
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTrajectoryToMySQL() {
        if (trajectoryPoints.isEmpty()) {
            Log.w(TAG, "Aucun point de trajet à sauvegarder");
            return;
        }

        String numero = numeroInput.getText().toString().trim();
        String pseudo = pseudoInput.getText().toString().trim();
        long durationMs = System.currentTimeMillis() - trackingStartTime;
        long endTime = System.currentTimeMillis();

        Log.d(TAG, "Sauvegarde du trajet: " + numero + " - " + trajectoryPoints.size() + " points");

        syncManager.saveTrajectory(
                numero,
                pseudo,
                new ArrayList<>(trajectoryPoints),
                durationMs,
                totalDistance / 1000.0, // Convertir en km
                averageSpeedKmh,
                trackingStartTime,
                endTime,
                new TrackingSyncManager.SyncCallback() {
                    @Override
                    public void onSyncSuccess(String message) {
                        Log.d(TAG, "Trajet sauvegardé: " + message);
                        Toast.makeText(TrackingActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSyncError(String error) {
                        Log.e(TAG, "Erreur sauvegarde: " + error);
                        Toast.makeText(TrackingActivity.this, "Erreur: " + error, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSyncProgress(int current, int total) {
                        Log.d(TAG, "Progression: " + current + "/" + total);
                    }
                }
        );
    }

    private void startDurationTimer() {
        durationRunnable = new Runnable() {
            @Override
            public void run() {
                if (trackingStartTime > 0) {
                    long elapsedTime = System.currentTimeMillis() - trackingStartTime;
                    updateDurationDisplay(elapsedTime);
                    durationHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };
        durationHandler.post(durationRunnable);
    }

    private void stopDurationTimer() {
        if (durationRunnable != null) {
            durationHandler.removeCallbacks(durationRunnable);
        }
    }

    private void updateDurationDisplay(long elapsedMillis) {
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / (1000 * 60)) % 60;
        long hours = (elapsedMillis / (1000 * 60 * 60));

        String durationStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        durationText.setText("Durée: " + durationStr);

        // Update average speed if we have distance
        if (totalDistance > 0 && elapsedMillis > 0) {
            averageSpeedKmh = (totalDistance / 1000.0) / (elapsedMillis / (1000.0 * 3600.0));
            speedText.setText(String.format(Locale.getDefault(), "Vitesse moy: %.2f km/h", averageSpeedKmh));
        }
    }
    
    private void addPositionToMap(Position position) {
        if (mMap == null || !position.isValid()) {
            return;
        }

        LatLng point = new LatLng(position.getLatitude(), position.getLongitude());
        trajectoryPoints.add(point);

        // Calculate distance from last point
        if (lastRecordedPoint != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    lastRecordedPoint.latitude, lastRecordedPoint.longitude,
                    point.latitude, point.longitude,
                    results
            );
            totalDistance += results[0]; // results[0] is in meters
            distanceText.setText(String.format(Locale.getDefault(), "Distance: %.2f km", totalDistance / 1000.0));
        }
        lastRecordedPoint = point;

        // Add marker with different colors for start and end
        float markerColor;
        String markerTitle;
        if (trajectoryPoints.size() == 1) {
            // First point - green
            markerColor = BitmapDescriptorFactory.HUE_GREEN;
            markerTitle = "🟢 Départ";
        } else {
            // Other points - blue
            markerColor = BitmapDescriptorFactory.HUE_AZURE;
            markerTitle = "Position #" + trajectoryPoints.size();
        }

        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(markerTitle)
                .snippet(position.getDisplayName())
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

        // Draw trajectory line with improved polyline
        if (trajectoryPoints.size() > 1) {
            mMap.addPolyline(new PolylineOptions()
                    .addAll(trajectoryPoints)
                    .color(0xFF0095F6)
                    .width(10)
                    .geodesic(true));
        }

        // Center camera on last position
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));

        Log.d(TAG, "Position ajoutée à la carte: " + point + " | Distance totale: " + totalDistance + "m");
    }
    
    private void viewFullTrajectory() {
        try {
            if (trajectoryPoints.isEmpty()) {
                Toast.makeText(this, "⚠️ Aucune position enregistrée", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "viewFullTrajectory: Aucun point");
                return;
            }

            if (mMap == null) {
                Toast.makeText(this, "❌ Carte non disponible", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "viewFullTrajectory: mMap est null");
                return;
            }

            // Adjust camera to see entire trajectory
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : trajectoryPoints) {
                builder.include(point);
            }

            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

            Toast.makeText(this, "🗺️ Trajet complet: " + trajectoryPoints.size() + " positions",
                          Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Trajet complet affiché: " + trajectoryPoints.size() + " points");
        } catch (Exception e) {
            Log.e(TAG, "Erreur viewFullTrajectory: " + e.getMessage(), e);
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatisticsDialog() {
        try {
            if (trajectoryPoints.isEmpty()) {
                Toast.makeText(this, "⚠️ Aucune donnée de tracking", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "showStatisticsDialog: Aucun point");
                return;
            }

            long elapsedTime = System.currentTimeMillis() - trackingStartTime;
            long seconds = (elapsedTime / 1000) % 60;
            long minutes = (elapsedTime / (1000 * 60)) % 60;
            long hours = (elapsedTime / (1000 * 60 * 60));

            double speedKmh = 0;
            if (elapsedTime > 0) {
                speedKmh = (totalDistance / 1000.0) / (elapsedTime / (1000.0 * 3600.0));
            }

            LatLng startPoint = trajectoryPoints.get(0);
            LatLng endPoint = trajectoryPoints.get(trajectoryPoints.size() - 1);

            String stats = String.format(Locale.getDefault(),
                    "📊 STATISTIQUES DU TRAJET\n\n" +
                    "⏱️ Durée: %02d:%02d:%02d\n" +
                    "📏 Distance: %.2f km\n" +
                    "🚀 Vitesse moyenne: %.2f km/h\n" +
                    "📍 Nombre de points: %d\n" +
                    "🟢 Départ: %.6f, %.6f\n" +
                    "🔴 Arrivée: %.6f, %.6f\n\n" +
                    "💾 Statut: Prêt à être sauvegardé",
                    hours, minutes, seconds,
                    totalDistance / 1000.0,
                    speedKmh,
                    trajectoryPoints.size(),
                    startPoint.latitude, startPoint.longitude,
                    endPoint.latitude, endPoint.longitude
            );

            new AlertDialog.Builder(this)
                    .setTitle("📊 Statistiques du Trajet")
                    .setMessage(stats)
                    .setPositiveButton("Fermer", (dialog, which) -> {
                        Log.d(TAG, "Dialog fermé");
                    })
                    .setNegativeButton("💾 Sauvegarder", (dialog, which) -> {
                        Log.d(TAG, "Sauvegarde depuis dialog");
                        saveTrajectoryToMySQL();
                    })
                    .setCancelable(true)
                    .show();

            Log.d(TAG, "Dialog statistiques affiché");
        } catch (Exception e) {
            Log.e(TAG, "Erreur showStatisticsDialog: " + e.getMessage(), e);
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateUIState(boolean isTracking) {
        try {
            if (startTrackingBtn != null) {
                startTrackingBtn.setEnabled(!isTracking);
                startTrackingBtn.setAlpha(isTracking ? 0.5f : 1.0f);
            }

            if (stopTrackingBtn != null) {
                stopTrackingBtn.setEnabled(isTracking);
                stopTrackingBtn.setAlpha(isTracking ? 1.0f : 0.5f);
            }

            if (pseudoInput != null) pseudoInput.setEnabled(!isTracking);
            if (numeroInput != null) numeroInput.setEnabled(!isTracking);
            if (intervalInput != null) intervalInput.setEnabled(!isTracking);
            if (viewTrajectoryBtn != null) viewTrajectoryBtn.setEnabled(!isTracking);

            if (statusText != null) {
                if (isTracking) {
                    statusText.setText("🟢 Tracking actif");
                    statusText.setTextColor(0xFF4CAF50);
                } else {
                    statusText.setText("🔴 Tracking inactif");
                    statusText.setTextColor(0xFFF44336);
                }
            }

            Log.d(TAG, "UI State updated - Tracking: " + isTracking);
        } catch (Exception e) {
            Log.e(TAG, "Erreur updateUIState: " + e.getMessage(), e);
        }
    }
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackingService.TrackingBinder binder = (TrackingService.TrackingBinder) service;
            trackingService = binder.getService();
            serviceBound = true;
            Log.d(TAG, "Service connecté");
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            trackingService = null;
            Log.d(TAG, "Service déconnecté");
        }
    };
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save trajectory points
        double[] lats = new double[trajectoryPoints.size()];
        double[] lons = new double[trajectoryPoints.size()];
        for (int i = 0; i < trajectoryPoints.size(); i++) {
            lats[i] = trajectoryPoints.get(i).latitude;
            lons[i] = trajectoryPoints.get(i).longitude;
        }
        outState.putDoubleArray("lats", lats);
        outState.putDoubleArray("lons", lons);

        // Save tracking data
        outState.putLong(SAVED_TRACKING_START_TIME, trackingStartTime);
        outState.putDouble(SAVED_TOTAL_DISTANCE, totalDistance);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        double[] lats = savedInstanceState.getDoubleArray("lats");
        double[] lons = savedInstanceState.getDoubleArray("lons");

        if (lats != null && lons != null) {
            trajectoryPoints.clear();
            for (int i = 0; i < lats.length; i++) {
                trajectoryPoints.add(new LatLng(lats[i], lons[i]));
            }
        }

        trackingStartTime = savedInstanceState.getLong(SAVED_TRACKING_START_TIME, 0);
        totalDistance = savedInstanceState.getDouble(SAVED_TOTAL_DISTANCE, 0.0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopDurationTimer();

        if (trackingReceiver != null) {
            try {
                unregisterReceiver(trackingReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered");
            }
        }

        if (serviceBound) {
            unbindService(serviceConnection);
        }
    }
}

