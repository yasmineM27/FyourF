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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activité pour gérer le tracking automatique de position
 * Permet de démarrer/arrêter le tracking et visualiser le trajet en temps réel
 *
 * AMÉLIORATIONS:
 * - Traçage de circuit avec détection de retour au point de départ
 * - Threads pour traitement asynchrone des positions
 * - Polylines avec gradient de couleur selon la vitesse
 * - Détection de pause automatique
 * - Statistiques en temps réel améliorées
 */
public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TrackingActivity";
    private static final String SAVED_TRAJECTORY_POINTS = "trajectory_points";
    private static final String SAVED_TRACKING_START_TIME = "tracking_start_time";
    private static final String SAVED_TOTAL_DISTANCE = "total_distance";

    // Circuit detection constants
    private static final float CIRCUIT_DETECTION_RADIUS = 50.0f; // 50 meters
    private static final int MIN_POINTS_FOR_CIRCUIT = 10;

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
    private List<Polyline> polylineSegments; // Pour les segments colorés
    private LatLng currentUserLocation;
    private com.google.android.gms.maps.model.Marker currentLocationMarker;
    private com.google.android.gms.maps.model.Marker startMarker;
    private com.google.android.gms.maps.model.Circle circuitDetectionCircle;

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

    // Speed tracking
    private List<Double> speedHistory;
    private double currentSpeedKmh = 0.0;
    private double maxSpeedKmh = 0.0;

    // MySQL Sync
    private TrackingSyncManager syncManager;
    private double averageSpeedKmh = 0.0;

    // Circuit detection
    private boolean isCircuitDetected = false;
    private LatLng startPoint = null;

    // Pause detection
    private static final float PAUSE_SPEED_THRESHOLD = 1.0f; // km/h
    private static final int PAUSE_DETECTION_COUNT = 3; // consecutive slow points
    private int consecutiveSlowPoints = 0;
    private boolean isPaused = false;
    private long pauseStartTime = 0;
    private long totalPauseTime = 0;

    // Background processing
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // Initialize sync manager
        syncManager = new TrackingSyncManager(this);

        // Set up ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(" Tracking GPS Avancé");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupPermissionLauncher();
        setupMap();
        setupBroadcastReceiver();

        // Initialize collections
        trajectoryPoints = new ArrayList<>();
        polylineSegments = new ArrayList<>();
        speedHistory = new ArrayList<>();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        durationHandler = new Handler(Looper.getMainLooper());
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize thread pool for background processing
        executorService = Executors.newFixedThreadPool(2);

        // Restore state if available
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        Log.d(TAG, "TrackingActivity créée avec améliorations avancées");
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

            // Long press to export
            viewTrajectoryBtn.setOnLongClickListener(v -> {
                Log.d(TAG, "View trajectory button long clicked - Export");
                showExportOptions();
                return true;
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
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        // Position par défaut (Tunis)
        LatLng defaultPosition = new LatLng(36.8065, 10.1815);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 12));

        // Setup map click listeners
        setupMapListeners();

        // Request location permissions and enable MyLocation
        requestLocationPermissions();

        Log.d(TAG, "Map ready avec améliorations");
    }

    /**
     * Configure les listeners de la carte
     */
    private void setupMapListeners() {
        if (mMap == null) return;

        // Long press to show speed legend
        mMap.setOnMapLongClickListener(latLng -> {
            showSpeedLegend();
        });

        // Click on polyline to show segment info
        mMap.setOnPolylineClickListener(polyline -> {
            int index = polylineSegments.indexOf(polyline);
            if (index >= 0 && index < speedHistory.size()) {
                double segmentSpeed = speedHistory.get(index);
                Toast.makeText(this,
                        String.format(Locale.getDefault(), "Segment #%d - Vitesse: %.2f km/h",
                                index + 1, segmentSpeed),
                        Toast.LENGTH_SHORT).show();
            }
        });

        Log.d(TAG, "Map listeners configurés");
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
                Toast.makeText(this, " Veuillez entrer un numéro", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Intervalle minimum: 10 secondes", Toast.LENGTH_SHORT).show();
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
            resetTrackingData();

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
            Log.d(TAG, "Tracking démarré avec améliorations - Numero: " + numero + " | Intervalle: " + interval + "ms");
        } catch (Exception e) {
            Log.e(TAG, "Erreur startTracking: " + e.getMessage(), e);
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Réinitialise toutes les données de tracking
     */
    private void resetTrackingData() {
        trajectoryPoints.clear();
        polylineSegments.clear();
        speedHistory.clear();
        totalDistance = 0.0;
        lastRecordedPoint = null;
        trackingStartTime = System.currentTimeMillis();
        averageSpeedKmh = 0.0;
        currentSpeedKmh = 0.0;
        maxSpeedKmh = 0.0;
        isCircuitDetected = false;
        startPoint = null;

        // Reset pause data
        resetPauseData();

        if (startMarker != null) {
            startMarker.remove();
            startMarker = null;
        }
        if (circuitDetectionCircle != null) {
            circuitDetectionCircle.remove();
            circuitDetectionCircle = null;
        }

        Log.d(TAG, "Données de tracking réinitialisées");
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
        durationText.setText("⏱️ Durée: " + durationStr);

        // Update average speed if we have distance
        if (totalDistance > 0 && elapsedMillis > 0) {
            averageSpeedKmh = (totalDistance / 1000.0) / (elapsedMillis / (1000.0 * 3600.0));

            String speedInfo = String.format(Locale.getDefault(),
                    "🚀 Moy: %.1f | ⚡ Max: %.1f | 🏃 Act: %.1f km/h",
                    averageSpeedKmh, maxSpeedKmh, currentSpeedKmh);
            speedText.setText(speedInfo);
        }
    }
    
    private void addPositionToMap(Position position) {
        if (mMap == null || !position.isValid()) {
            return;
        }

        final LatLng point = new LatLng(position.getLatitude(), position.getLongitude());

        // Process in background thread
        executorService.execute(() -> {
            try {
                // Calculate distance and speed
                float distance = 0;
                double instantSpeed = 0;

                if (lastRecordedPoint != null) {
                    float[] results = new float[1];
                    Location.distanceBetween(
                            lastRecordedPoint.latitude, lastRecordedPoint.longitude,
                            point.latitude, point.longitude,
                            results
                    );
                    distance = results[0]; // in meters

                    // Calculate instantaneous speed (assuming 30s interval)
                    long timeDiff = 30000; // milliseconds
                    instantSpeed = (distance / 1000.0) / (timeDiff / (1000.0 * 3600.0)); // km/h
                }

                final float finalDistance = distance;
                final double finalSpeed = instantSpeed;

                // Update UI on main thread
                mainHandler.post(() -> {
                    trajectoryPoints.add(point);

                    // Update distance
                    if (lastRecordedPoint != null) {
                        totalDistance += finalDistance;
                        distanceText.setText(String.format(Locale.getDefault(), "📏 Distance: %.2f km", totalDistance / 1000.0));

                        // Update speed tracking
                        currentSpeedKmh = finalSpeed;
                        speedHistory.add(currentSpeedKmh);
                        if (currentSpeedKmh > maxSpeedKmh) {
                            maxSpeedKmh = currentSpeedKmh;
                        }

                        // Check for pause
                        checkPauseDetection(currentSpeedKmh);
                    }
                    lastRecordedPoint = point;

                    // Handle first point
                    if (trajectoryPoints.size() == 1) {
                        startPoint = point;
                        addStartMarker(point);
                        addCircuitDetectionCircle(point);
                    } else {
                        // Check for circuit completion
                        checkCircuitCompletion(point);

                        // Draw colored polyline segment
                        drawColoredPolylineSegment(trajectoryPoints.get(trajectoryPoints.size() - 2),
                                                   point, currentSpeedKmh);

                        // Add waypoint marker every 5 points
                        if (trajectoryPoints.size() % 5 == 0) {
                            addWaypointMarker(point, trajectoryPoints.size());
                        }
                    }

                    // Center camera on last position
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));

                    Log.d(TAG, String.format("Position ajoutée: %s | Distance: %.2fm | Vitesse: %.2f km/h",
                            point, totalDistance, currentSpeedKmh));
                });

            } catch (Exception e) {
                Log.e(TAG, "Erreur traitement position: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Ajoute le marqueur de départ avec cercle de détection de circuit
     */
    private void addStartMarker(LatLng point) {
        if (mMap == null) return;

        startMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("🟢 Départ")
                .snippet("Point de départ du trajet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        Log.d(TAG, "Marqueur de départ ajouté: " + point);
    }

    /**
     * Ajoute un cercle de détection de circuit autour du point de départ
     */
    private void addCircuitDetectionCircle(LatLng point) {
        if (mMap == null) return;

        circuitDetectionCircle = mMap.addCircle(new CircleOptions()
                .center(point)
                .radius(CIRCUIT_DETECTION_RADIUS)
                .strokeColor(0x8800FF00)
                .strokeWidth(2)
                .fillColor(0x2200FF00));

        Log.d(TAG, "Cercle de détection de circuit ajouté (rayon: " + CIRCUIT_DETECTION_RADIUS + "m)");
    }

    /**
     * Ajoute un marqueur de waypoint
     */
    private void addWaypointMarker(LatLng point, int number) {
        if (mMap == null) return;

        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("📍 Point #" + number)
                .snippet(String.format(Locale.getDefault(), "Distance: %.2f km", totalDistance / 1000.0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .alpha(0.7f));
    }

    /**
     * Dessine un segment de polyline coloré selon la vitesse
     */
    private void drawColoredPolylineSegment(LatLng start, LatLng end, double speed) {
        if (mMap == null) return;

        // Déterminer la couleur selon la vitesse
        int color = getColorForSpeed(speed);

        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .add(start, end)
                .color(color)
                .width(12)
                .geodesic(true));

        polylineSegments.add(polyline);
    }

    /**
     * Retourne une couleur selon la vitesse
     * Vert: lent (0-10 km/h)
     * Jaune: moyen (10-30 km/h)
     * Orange: rapide (30-50 km/h)
     * Rouge: très rapide (>50 km/h)
     */
    private int getColorForSpeed(double speedKmh) {
        if (speedKmh < 10) {
            return 0xFF00FF00; // Vert
        } else if (speedKmh < 30) {
            return 0xFFFFFF00; // Jaune
        } else if (speedKmh < 50) {
            return 0xFFFF8800; // Orange
        } else {
            return 0xFFFF0000; // Rouge
        }
    }

    /**
     * Vérifie si l'utilisateur est revenu au point de départ (circuit fermé)
     */
    private void checkCircuitCompletion(LatLng currentPoint) {
        if (isCircuitDetected || startPoint == null || trajectoryPoints.size() < MIN_POINTS_FOR_CIRCUIT) {
            return;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                startPoint.latitude, startPoint.longitude,
                currentPoint.latitude, currentPoint.longitude,
                results
        );

        if (results[0] <= CIRCUIT_DETECTION_RADIUS) {
            isCircuitDetected = true;
            onCircuitCompleted();
        }
    }

    /**
     * Appelé quand un circuit est détecté
     */
    private void onCircuitCompleted() {
        Log.d(TAG, "🔄 Circuit détecté! Retour au point de départ");

        // Ajouter un marqueur de fin de circuit
        if (mMap != null && lastRecordedPoint != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(lastRecordedPoint)
                    .title("🔴 Circuit Fermé!")
                    .snippet("Retour au point de départ")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Dessiner une ligne pointillée vers le départ
            if (startPoint != null) {
                mMap.addPolyline(new PolylineOptions()
                        .add(lastRecordedPoint, startPoint)
                        .color(0xFF00FF00)
                        .width(8)
                        .pattern(Arrays.asList(new Dash(30), new Gap(20)))
                        .geodesic(true));
            }
        }

        Toast.makeText(this, "🔄 Circuit fermé détecté!", Toast.LENGTH_LONG).show();

        // Mettre à jour le cercle de détection
        if (circuitDetectionCircle != null) {
            circuitDetectionCircle.setFillColor(0x4400FF00);
            circuitDetectionCircle.setStrokeColor(0xFF00FF00);
            circuitDetectionCircle.setStrokeWidth(4);
        }
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

            // Adjust camera to see entire trajectory with padding
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : trajectoryPoints) {
                builder.include(point);
            }

            LatLngBounds bounds = builder.build();
            int padding = 150; // padding in pixels
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), 1500, null);

            // Show trajectory summary
            String summary = String.format(Locale.getDefault(),
                    "🗺️ Trajet complet\n📍 %d positions | 📏 %.2f km | %s",
                    trajectoryPoints.size(),
                    totalDistance / 1000.0,
                    isCircuitDetected ? "🔄 Circuit fermé" : "➡️ Circuit ouvert");

            Toast.makeText(this, summary, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Trajet complet affiché: " + trajectoryPoints.size() + " points");
        } catch (Exception e) {
            Log.e(TAG, "Erreur viewFullTrajectory: " + e.getMessage(), e);
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Exporte les données du trajet au format texte
     */
    private String exportTrajectoryData() {
        if (trajectoryPoints.isEmpty()) {
            return "Aucune donnée à exporter";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== EXPORT TRAJET GPS ===\n\n");
        sb.append("Utilisateur: ").append(pseudoInput.getText().toString()).append("\n");
        sb.append("Numéro: ").append(numeroInput.getText().toString()).append("\n");
        sb.append("Date: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new java.util.Date(trackingStartTime))).append("\n\n");

        sb.append("--- STATISTIQUES ---\n");
        sb.append("Points: ").append(trajectoryPoints.size()).append("\n");
        sb.append("Distance: ").append(String.format(Locale.getDefault(), "%.2f km", totalDistance / 1000.0)).append("\n");
        sb.append("Vitesse moyenne: ").append(String.format(Locale.getDefault(), "%.2f km/h", averageSpeedKmh)).append("\n");
        sb.append("Vitesse max: ").append(String.format(Locale.getDefault(), "%.2f km/h", maxSpeedKmh)).append("\n");
        sb.append("Circuit fermé: ").append(isCircuitDetected ? "Oui" : "Non").append("\n\n");

        sb.append("--- COORDONNÉES ---\n");
        for (int i = 0; i < trajectoryPoints.size(); i++) {
            LatLng point = trajectoryPoints.get(i);
            sb.append(String.format(Locale.getDefault(), "%d. %.6f, %.6f",
                    i + 1, point.latitude, point.longitude));
            if (i < speedHistory.size()) {
                sb.append(String.format(Locale.getDefault(), " (%.2f km/h)", speedHistory.get(i)));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Affiche les options d'export
     */
    private void showExportOptions() {
        String[] options = {"📋 Copier dans le presse-papier", "📊 Voir les données", "❌ Annuler"};

        new AlertDialog.Builder(this)
                .setTitle("📤 Exporter le trajet")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Copier
                            String data = exportTrajectoryData();
                            android.content.ClipboardManager clipboard =
                                    (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Trajet GPS", data);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(this, "✅ Données copiées dans le presse-papier", Toast.LENGTH_SHORT).show();
                            break;
                        case 1: // Voir
                            showTrajectoryDataDialog();
                            break;
                    }
                })
                .show();
    }

    /**
     * Affiche les données du trajet dans un dialog
     */
    private void showTrajectoryDataDialog() {
        String data = exportTrajectoryData();

        new AlertDialog.Builder(this)
                .setTitle("📊 Données du Trajet")
                .setMessage(data)
                .setPositiveButton("Fermer", null)
                .setNegativeButton("📋 Copier", (dialog, which) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Trajet GPS", data);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "✅ Copié!", Toast.LENGTH_SHORT).show();
                })
                .show();
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

            LatLng startPt = trajectoryPoints.get(0);
            LatLng endPoint = trajectoryPoints.get(trajectoryPoints.size() - 1);

            // Calculate straight line distance (as the crow flies)
            float[] straightLineResults = new float[1];
            Location.distanceBetween(
                    startPt.latitude, startPt.longitude,
                    endPoint.latitude, endPoint.longitude,
                    straightLineResults
            );
            double straightLineDistance = straightLineResults[0] / 1000.0; // km

            String circuitStatus = isCircuitDetected ? "✅ Circuit fermé détecté" : "❌ Circuit ouvert";

            // Calculate active time (excluding pauses)
            long activeTime = elapsedTime - totalPauseTime;
            long activeSec = (activeTime / 1000) % 60;
            long activeMin = (activeTime / (1000 * 60)) % 60;
            long activeHrs = (activeTime / (1000 * 60 * 60));

            String stats = String.format(Locale.getDefault(),
                    "📊 STATISTIQUES AVANCÉES DU TRAJET\n\n" +
                    "⏱️ Durée totale: %02d:%02d:%02d\n" +
                    "⏱️ Durée active: %02d:%02d:%02d\n" +
                    "⏸️ Temps de pause: %d s\n" +
                    "📏 Distance parcourue: %.2f km\n" +
                    "📐 Distance à vol d'oiseau: %.2f km\n" +
                    "🚀 Vitesse moyenne: %.2f km/h\n" +
                    "⚡ Vitesse maximale: %.2f km/h\n" +
                    "🐌 Vitesse actuelle: %.2f km/h\n" +
                    "📍 Nombre de points: %d\n" +
                    "🔄 Circuit: %s\n" +
                    "🟢 Départ: %.6f, %.6f\n" +
                    "🔴 Position actuelle: %.6f, %.6f\n\n" +
                    "💾 Statut: Prêt à être sauvegardé",
                    hours, minutes, seconds,
                    activeHrs, activeMin, activeSec,
                    totalPauseTime / 1000,
                    totalDistance / 1000.0,
                    straightLineDistance,
                    speedKmh,
                    maxSpeedKmh,
                    currentSpeedKmh,
                    trajectoryPoints.size(),
                    circuitStatus,
                    startPt.latitude, startPt.longitude,
                    endPoint.latitude, endPoint.longitude
            );

            new AlertDialog.Builder(this)
                    .setTitle("📊 Statistiques Avancées")
                    .setMessage(stats)
                    .setPositiveButton("Fermer", (dialog, which) -> {
                        Log.d(TAG, "Dialog fermé");
                    })
                    .setNegativeButton("💾 Sauvegarder", (dialog, which) -> {
                        Log.d(TAG, "Sauvegarde depuis dialog");
                        saveTrajectoryToMySQL();
                    })
                    .setNeutralButton("🗺️ Voir trajet", (dialog, which) -> {
                        viewFullTrajectory();
                    })
                    .setCancelable(true)
                    .show();

            Log.d(TAG, "Dialog statistiques avancées affiché");
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

        // Shutdown executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "ExecutorService arrêté");
        }

        Log.d(TAG, "TrackingActivity détruite");
    }

    /**
     * Affiche une légende des couleurs de vitesse
     */
    private void showSpeedLegend() {
        String legend = "🎨 LÉGENDE DES COULEURS\n\n" +
                "🟢 Vert: 0-10 km/h (Lent)\n" +
                "🟡 Jaune: 10-30 km/h (Moyen)\n" +
                "🟠 Orange: 30-50 km/h (Rapide)\n" +
                "🔴 Rouge: >50 km/h (Très rapide)\n\n" +
                "Les segments de trajet sont colorés selon\n" +
                "la vitesse instantanée à ce moment.\n\n" +
                "💡 ASTUCES:\n" +
                "• Appui long sur 'Voir Trajet' pour exporter\n" +
                "• Cliquez sur un segment pour voir sa vitesse\n" +
                "• Le cercle vert détecte les circuits fermés";

        new AlertDialog.Builder(this)
                .setTitle("🎨 Légende & Astuces")
                .setMessage(legend)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Détecte si l'utilisateur est en pause (vitesse très faible)
     */
    private void checkPauseDetection(double speedKmh) {
        if (speedKmh < PAUSE_SPEED_THRESHOLD) {
            consecutiveSlowPoints++;

            if (consecutiveSlowPoints >= PAUSE_DETECTION_COUNT && !isPaused) {
                // Pause détectée
                isPaused = true;
                pauseStartTime = System.currentTimeMillis();
                onPauseDetected();
            }
        } else {
            if (isPaused) {
                // Reprise du mouvement
                onMovementResumed();
            }
            consecutiveSlowPoints = 0;
            isPaused = false;
        }
    }

    /**
     * Appelé quand une pause est détectée
     */
    private void onPauseDetected() {
        Log.d(TAG, "⏸️ Pause détectée - Vitesse < " + PAUSE_SPEED_THRESHOLD + " km/h");

        if (statusText != null) {
            statusText.setText("⏸️ Pause détectée");
            statusText.setTextColor(0xFFFF9800); // Orange
        }

        // Ajouter un marqueur de pause
        if (mMap != null && lastRecordedPoint != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(lastRecordedPoint)
                    .title("⏸️ Pause")
                    .snippet("Arrêt détecté")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .alpha(0.6f));
        }
    }

    /**
     * Appelé quand le mouvement reprend après une pause
     */
    private void onMovementResumed() {
        long pauseDuration = System.currentTimeMillis() - pauseStartTime;
        totalPauseTime += pauseDuration;

        Log.d(TAG, "▶️ Mouvement repris après " + (pauseDuration / 1000) + "s de pause");

        if (statusText != null) {
            statusText.setText("🟢 Tracking actif");
            statusText.setTextColor(0xFF4CAF50); // Vert
        }

        // Ajouter un marqueur de reprise
        if (mMap != null && lastRecordedPoint != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(lastRecordedPoint)
                    .title("▶️ Reprise")
                    .snippet("Mouvement repris")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .alpha(0.6f));
        }

        Toast.makeText(this,
                String.format(Locale.getDefault(), "▶️ Reprise après %ds de pause", pauseDuration / 1000),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Réinitialise les données de pause
     */
    private void resetPauseData() {
        consecutiveSlowPoints = 0;
        isPaused = false;
        pauseStartTime = 0;
        totalPauseTime = 0;
    }
}

