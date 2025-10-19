package yasminemassaoudi.grp3.fyourf;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private FusedLocationProviderClient fusedLocationClient;
    private static final long LOCATION_TIMEOUT = 30000; // 30 seconds timeout

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService started");

        String senderNumber = intent.getStringExtra("senderNumber");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationAndSendSms(senderNumber);

        // Schedule service stop after timeout
        new Thread(() -> {
            try {
                Thread.sleep(LOCATION_TIMEOUT);
                stopSelf();
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted: " + e.getMessage());
            }
        }).start();

        return START_NOT_STICKY;
    }

    private void getLocationAndSendSms(String senderNumber) {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                        @NonNull
                        @Override
                        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                            return null;
                        }

                        @Override
                        public boolean isCancellationRequested() {
                            return false;
                        }

                        public void onCanceled(OnTokenCanceledListener onTokenCanceledListener) {
                        }
                    })
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            long timestamp = System.currentTimeMillis();

                            Log.d(TAG, "Location obtained - Lat: " + latitude + " Lon: " + longitude);

                            String message = "POSITION:" + latitude + "," + longitude + ";time:" + timestamp;
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(senderNumber, null, message, null, null);

                            Log.d(TAG, "Location SMS sent to: " + senderNumber);
                        } else {
                            Log.w(TAG, "Location is null, trying last known location");
                            getLastKnownLocation(senderNumber);
                        }
                        stopSelf();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting location: " + e.getMessage());
                        stopSelf();
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied: " + e.getMessage());
            stopSelf();
        }
    }

    private void getLastKnownLocation(String senderNumber) {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            long timestamp = System.currentTimeMillis();

                            String message = "POSITION:" + latitude + "," + longitude + ";time:" + timestamp;
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(senderNumber, null, message, null, null);

                            Log.d(TAG, "Last known location sent to: " + senderNumber);
                        }
                        stopSelf();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied: " + e.getMessage());
            stopSelf();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
