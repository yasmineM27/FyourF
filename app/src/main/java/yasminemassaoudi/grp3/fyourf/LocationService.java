package yasminemassaoudi.grp3.fyourf;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "location_service_channel";
    private static final int FOREGROUND_ID = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private static final long LOCATION_TIMEOUT = 30000; // 30 seconds timeout
    private boolean locationSent = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationService onCreate");
        createNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService started with intent: " + intent);

        String senderNumber = intent != null ? intent.getStringExtra("senderNumber") : null;
        Log.d(TAG, "Sender number: " + senderNumber);

        if (senderNumber == null) {
            Log.e(TAG, "No sender number provided - stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        // Check SMS permission before starting
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "SMS SEND permission not granted!");
            stopSelf();
            return START_NOT_STICKY;
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted!");
            sendErrorSms(senderNumber, "Location permission not available");
            stopSelf();
            return START_NOT_STICKY;
        }

        // Start as foreground service
        startForeground(FOREGROUND_ID, createForegroundNotification(senderNumber));

        getLocationAndSendSms(senderNumber);

        // Schedule service stop after timeout
        new Thread(() -> {
            try {
                Thread.sleep(LOCATION_TIMEOUT);
                if (!locationSent) {
                    Log.d(TAG, "Timeout reached without sending location, stopping service");
                    sendErrorSms(senderNumber, "Location timeout");
                }
                stopSelf();
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted: " + e.getMessage());
            }
        }).start();

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Getting location for sharing");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    private Notification createForegroundNotification(String senderNumber) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Getting Location")
                .setContentText("Sharing location with " + senderNumber)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void getLocationAndSendSms(String senderNumber) {
        try {
            Log.d(TAG, "Requesting current location");
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                        @NonNull
                        @Override
                        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                            return this;
                        }

                        @Override
                        public boolean isCancellationRequested() {
                            return false;
                        }
                    })
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            long timestamp = System.currentTimeMillis();

                            Log.d(TAG, "Location obtained - Lat: " + latitude + " Lon: " + longitude);
                            sendLocationSms(senderNumber, latitude, longitude, timestamp);
                        } else {
                            Log.w(TAG, "Current location is null, trying last known location");
                            getLastKnownLocation(senderNumber);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting current location: " + e.getMessage());
                        e.printStackTrace();
                        getLastKnownLocation(senderNumber);
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied: " + e.getMessage());
            sendErrorSms(senderNumber, "Location permission denied");
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error getting location: " + e.getMessage());
            e.printStackTrace();
            sendErrorSms(senderNumber, "Unable to get location");
            stopSelf();
        }
    }

    private void getLastKnownLocation(String senderNumber) {
        try {
            Log.d(TAG, "Requesting last known location");
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            long timestamp = location.getTime();

                            Log.d(TAG, "Using last known location - Lat: " + latitude + " Lon: " + longitude);
                            sendLocationSms(senderNumber, latitude, longitude, timestamp);
                        } else {
                            Log.w(TAG, "No last known location available");
                            sendErrorSms(senderNumber, "Location not available");
                            stopSelf();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting last known location: " + e.getMessage());
                        e.printStackTrace();
                        sendErrorSms(senderNumber, "Failed to get location");
                        stopSelf();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied for last known: " + e.getMessage());
            sendErrorSms(senderNumber, "Location permission denied");
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error getting last location: " + e.getMessage());
            e.printStackTrace();
            sendErrorSms(senderNumber, "Location service error");
            stopSelf();
        }
    }

    private void sendLocationSms(String senderNumber, double latitude, double longitude, long timestamp) {
        if (locationSent) {
            Log.d(TAG, "Location already sent, skipping");
            return;
        }

        try {
            String message = "POSITION:" + latitude + "," + longitude + ";time:" + timestamp;
            SmsManager smsManager = SmsManager.getDefault();

            Log.d(TAG, "Attempting to send SMS: " + message + " to: " + senderNumber);

            smsManager.sendTextMessage(senderNumber, null, message, null, null);

            locationSent = true;
            Log.d(TAG, "Location SMS sent successfully to: " + senderNumber);

            // Stop service after brief delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    stopSelf();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Sleep interrupted: " + e.getMessage());
                    stopSelf();
                }
            }).start();

        } catch (SecurityException e) {
            Log.e(TAG, "SMS permission denied: " + e.getMessage());
            e.printStackTrace();
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
            e.printStackTrace();
            stopSelf();
        }
    }

    private void sendErrorSms(String senderNumber, String errorMessage) {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Cannot send error SMS - no permission");
                return;
            }

            SmsManager smsManager = SmsManager.getDefault();
            String message = "ERROR: " + errorMessage;
            smsManager.sendTextMessage(senderNumber, null, message, null, null);
            Log.d(TAG, "Error SMS sent to: " + senderNumber + " - " + errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send error SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LocationService destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}