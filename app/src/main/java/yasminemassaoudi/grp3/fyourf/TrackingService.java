package yasminemassaoudi.grp3.fyourf;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

/**
 * Service de tracking automatique de position
 * Utilise FusedLocationProviderClient pour enregistrer la position périodiquement
 */
public class TrackingService extends Service {
    
    private static final String TAG = "TrackingService";
    private static final String CHANNEL_ID = "tracking_service_channel";
    private static final int NOTIFICATION_ID = 2001;
    
    // Paramètres de tracking
    private long trackingInterval = 30000; // 30 secondes par défaut
    private String userPseudo = "";
    private String userNumero = "";
    
    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    
    // Données du trajet
    private List<Position> trajectoryPositions;
    private boolean isTracking = false;
    private long trackingStartTime;
    private int positionCount = 0;
    
    // MySQL Service
    private MySQLLocationService mysqlService;
    
    // Binder pour communication avec l'Activity
    private final IBinder binder = new TrackingBinder();
    
    public class TrackingBinder extends Binder {
        public TrackingService getService() {
            return TrackingService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TrackingService onCreate");
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mysqlService = new MySQLLocationService();
        trajectoryPositions = new ArrayList<>();
        
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "TrackingService onStartCommand");
        
        if (intent != null) {
            String action = intent.getStringExtra("action");
            
            if ("START_TRACKING".equals(action)) {
                trackingInterval = intent.getLongExtra("interval", 30000);
                userPseudo = intent.getStringExtra("pseudo");
                userNumero = intent.getStringExtra("numero");
                
                startTracking();
            } else if ("STOP_TRACKING".equals(action)) {
                stopTracking();
            }
        }
        
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    /**
     * Démarre le tracking de position
     */
    public void startTracking() {
        if (isTracking) {
            Log.w(TAG, "Tracking déjà en cours");
            return;
        }
        
        // Vérifier les permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permission de localisation non accordée");
            stopSelf();
            return;
        }
        
        isTracking = true;
        trackingStartTime = System.currentTimeMillis();
        positionCount = 0;
        trajectoryPositions.clear();
        
        // Créer la requête de localisation
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, trackingInterval)
                .setMinUpdateIntervalMillis(trackingInterval / 2)
                .setMaxUpdateDelayMillis(trackingInterval * 2)
                .build();
        
        // Créer le callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    handleNewLocation(location);
                }
            }
        };
        
        // Démarrer en foreground
        startForeground(NOTIFICATION_ID, createTrackingNotification());
        
        // Démarrer les mises à jour de localisation
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Log.d(TAG, "✓ Tracking démarré - Intervalle: " + trackingInterval + "ms");
            
            // Broadcast pour notifier l'Activity
            broadcastTrackingStatus(true);
            
        } catch (SecurityException e) {
            Log.e(TAG, "Erreur de permission: " + e.getMessage());
            stopTracking();
        }
    }
    
    /**
     * Arrête le tracking de position
     */
    public void stopTracking() {
        if (!isTracking) {
            Log.w(TAG, "Tracking déjà arrêté");
            return;
        }
        
        isTracking = false;
        
        // Arrêter les mises à jour de localisation
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        
        Log.d(TAG, "✓ Tracking arrêté - Positions enregistrées: " + positionCount);
        
        // Broadcast pour notifier l'Activity
        broadcastTrackingStatus(false);
        
        // Arrêter le service
        stopForeground(true);
        stopSelf();
    }
    
    /**
     * Gère une nouvelle position reçue
     */
    private void handleNewLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        long timestamp = System.currentTimeMillis();
        
        Log.d(TAG, "Nouvelle position: " + latitude + ", " + longitude);
        
        // Créer l'objet Position
        Position position = new Position(longitude, latitude, userNumero, userPseudo);
        position.setTimestamp(timestamp);
        
        // Ajouter à la liste du trajet
        trajectoryPositions.add(position);
        positionCount++;
        
        // Sauvegarder dans MySQL si activé
        if (Config.USE_MYSQL) {
            mysqlService.addOrUpdatePosition(userNumero, latitude, longitude, userPseudo, 
                new MySQLLocationService.OperationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "✓ Position sauvegardée dans MySQL: " + message);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "✗ Erreur MySQL: " + error);
                    }
                });
        }
        
        // Sauvegarder dans la base locale
        LocationDatabase localDb = new LocationDatabase(this);
        localDb.addLocation(userNumero, latitude, longitude);
        
        // Mettre à jour la notification
        updateNotification();
        
        // Broadcast pour mettre à jour l'UI
        broadcastNewPosition(position);
    }
    
    /**
     * Broadcast le statut du tracking
     */
    private void broadcastTrackingStatus(boolean isTracking) {
        Intent intent = new Intent("TRACKING_STATUS_CHANGED");
        intent.putExtra("isTracking", isTracking);
        intent.putExtra("positionCount", positionCount);
        sendBroadcast(intent);
    }
    
    /**
     * Broadcast une nouvelle position
     */
    private void broadcastNewPosition(Position position) {
        Intent intent = new Intent("NEW_TRACKING_POSITION");
        intent.putExtra("position", position);
        intent.putExtra("positionCount", positionCount);
        sendBroadcast(intent);
    }
    
    /**
     * Crée le canal de notification
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Service de tracking de position");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Crée la notification de tracking
     */
    private Notification createTrackingNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📍 Tracking en cours")
                .setContentText("Positions enregistrées: 0")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
    
    /**
     * Met à jour la notification
     */
    private void updateNotification() {
        long duration = (System.currentTimeMillis() - trackingStartTime) / 1000;
        String durationText = String.format("%02d:%02d", duration / 60, duration % 60);
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📍 Tracking en cours")
                .setContentText("Positions: " + positionCount + " | Durée: " + durationText)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .build();
        
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }
    
    // Getters publics pour l'Activity
    
    public boolean isTracking() {
        return isTracking;
    }
    
    public int getPositionCount() {
        return positionCount;
    }
    
    public List<Position> getTrajectoryPositions() {
        return new ArrayList<>(trajectoryPositions);
    }
    
    public long getTrackingDuration() {
        if (!isTracking) return 0;
        return System.currentTimeMillis() - trackingStartTime;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TrackingService onDestroy");
        
        if (isTracking) {
            stopTracking();
        }
        
        if (mysqlService != null) {
            mysqlService.shutdown();
        }
    }
}

