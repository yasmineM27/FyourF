package yasminemassaoudi.grp3.fyourf;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

/**
 * Activité pour gérer le tracking automatique de position
 * Permet de démarrer/arrêter le tracking et visualiser le trajet en temps réel
 */
public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    private static final String TAG = "TrackingActivity";
    
    // UI Elements
    private EditText pseudoInput;
    private EditText numeroInput;
    private EditText intervalInput;
    private Button startTrackingBtn;
    private Button stopTrackingBtn;
    private Button viewTrajectoryBtn;
    private TextView statusText;
    private TextView positionCountText;
    private TextView durationText;
    
    // Map
    private GoogleMap mMap;
    private List<LatLng> trajectoryPoints;
    
    // Service
    private TrackingService trackingService;
    private boolean serviceBound = false;
    
    // Broadcast Receiver
    private BroadcastReceiver trackingReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        
        // Set up ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📍 Tracking GPS");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initializeViews();
        setupMap();
        setupBroadcastReceiver();
        
        trajectoryPoints = new ArrayList<>();
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
        
        // Valeurs par défaut
        intervalInput.setText("30");
        
        // Listeners
        startTrackingBtn.setOnClickListener(v -> startTracking());
        stopTrackingBtn.setOnClickListener(v -> stopTracking());
        viewTrajectoryBtn.setOnClickListener(v -> viewFullTrajectory());
        
        // État initial
        updateUIState(false);
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
        
        Log.d(TAG, "Map ready");
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
        String pseudo = pseudoInput.getText().toString().trim();
        String numero = numeroInput.getText().toString().trim();
        String intervalStr = intervalInput.getText().toString().trim();
        
        // Validation
        if (numero.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un numéro", Toast.LENGTH_SHORT).show();
            return;
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
            Toast.makeText(this, "Intervalle invalide", Toast.LENGTH_SHORT).show();
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
        
        // Réinitialiser la carte
        trajectoryPoints.clear();
        if (mMap != null) {
            mMap.clear();
        }
        
        Toast.makeText(this, "Tracking démarré", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Tracking démarré - Intervalle: " + interval + "ms");
    }
    
    private void stopTracking() {
        if (serviceBound && trackingService != null) {
            trackingService.stopTracking();
            unbindService(serviceConnection);
            serviceBound = false;
        }
        
        Intent serviceIntent = new Intent(this, TrackingService.class);
        stopService(serviceIntent);
        
        Toast.makeText(this, "Tracking arrêté", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Tracking arrêté");
    }
    
    private void addPositionToMap(Position position) {
        if (mMap == null || !position.isValid()) {
            return;
        }
        
        LatLng point = new LatLng(position.getLatitude(), position.getLongitude());
        trajectoryPoints.add(point);
        
        // Ajouter un marqueur
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(position.getDisplayName())
                .snippet("Position #" + trajectoryPoints.size())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        
        // Dessiner la ligne du trajet
        if (trajectoryPoints.size() > 1) {
            mMap.addPolyline(new PolylineOptions()
                    .addAll(trajectoryPoints)
                    .color(0xFF0095F6)
                    .width(8));
        }
        
        // Centrer la caméra sur la dernière position
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
        
        Log.d(TAG, "Position ajoutée à la carte: " + point);
    }
    
    private void viewFullTrajectory() {
        if (trajectoryPoints.isEmpty()) {
            Toast.makeText(this, "Aucune position enregistrée", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Ajuster la caméra pour voir tout le trajet
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : trajectoryPoints) {
            builder.include(point);
        }
        
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        
        Toast.makeText(this, "Trajet complet: " + trajectoryPoints.size() + " positions", 
                      Toast.LENGTH_SHORT).show();
    }
    
    private void updateUIState(boolean isTracking) {
        startTrackingBtn.setEnabled(!isTracking);
        stopTrackingBtn.setEnabled(isTracking);
        pseudoInput.setEnabled(!isTracking);
        numeroInput.setEnabled(!isTracking);
        intervalInput.setEnabled(!isTracking);
        
        if (isTracking) {
            statusText.setText("🟢 Tracking actif");
            statusText.setTextColor(0xFF4CAF50);
        } else {
            statusText.setText("🔴 Tracking inactif");
            statusText.setTextColor(0xFFF44336);
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
    protected void onDestroy() {
        super.onDestroy();
        
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

