package yasminemassaoudi.grp3.fyourf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private static final String LOCATION_REQUEST_KEYWORD = "find friends";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "=== SmsReceiver triggered - Intent action: " + intent.getAction() + " ===");

        // Check if we have SMS permissions
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "SMS RECEIVE permission not granted!");
            return;
        }

        if (intent.getExtras() == null) {
            Log.e(TAG, "Intent extras are null");
            return;
        }

        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        if (pdus == null || pdus.length == 0) {
            Log.e(TAG, "No PDUs in SMS intent");
            return;
        }

        String format = intent.getExtras().getString("format");
        Log.d(TAG, "Processing " + pdus.length + " SMS PDUs with format: " + format);

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating SMS message from PDU: " + e.getMessage());
                continue;
            }

            if (smsMessage == null) {
                Log.e(TAG, "Failed to create SMS message from PDU " + i);
                continue;
            }

            String messageBody = smsMessage.getMessageBody();
            String senderNumber = smsMessage.getOriginatingAddress();

            Log.d(TAG, "SMS #" + i + " received from: " + senderNumber);
            Log.d(TAG, "Message body: " + messageBody);

            if (messageBody == null || senderNumber == null) {
                Log.e(TAG, "Message body or sender number is null");
                continue;
            }

            // Show toast for received SMS
            showToast(context, "SMS from: " + senderNumber);

            // Check for location request
            String lowerBody = messageBody.toLowerCase().trim();
            if (lowerBody.contains(LOCATION_REQUEST_KEYWORD) ||
                    lowerBody.contains("give me your location")) {

                Log.d(TAG, "*** LOCATION REQUEST DETECTED from: " + senderNumber + " ***");
                handleLocationRequest(context, senderNumber);
            }
            // Check for location response
            else if (messageBody.startsWith("POSITION:")) {
                Log.d(TAG, "*** LOCATION RESPONSE DETECTED from: " + senderNumber + " ***");
                handleLocationResponse(context, messageBody, senderNumber);
            }
            // Check for error messages
            else if (messageBody.startsWith("ERROR:")) {
                Log.d(TAG, "Error message received: " + messageBody);
                showToast(context, "Error from " + senderNumber + ": " + messageBody.substring(7));
            }
            else {
                Log.d(TAG, "Regular SMS (not a location message)");
            }
        }
    }

    private void handleLocationRequest(Context context, String senderNumber) {
        // Check all required permissions
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted! Cannot process request");
            showToast(context, "Location permission not available");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "SMS SEND permission not granted! Cannot send location");
            showToast(context, "SMS permission not available");
            return;
        }

        Log.d(TAG, "All permissions granted, starting LocationService");

        Intent locationServiceIntent = new Intent(context, LocationService.class);
        locationServiceIntent.putExtra("senderNumber", senderNumber);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Starting foreground service (Android O+)");
                context.startForegroundService(locationServiceIntent);
            } else {
                Log.d(TAG, "Starting background service");
                context.startService(locationServiceIntent);
            }
            Log.d(TAG, "LocationService start command issued successfully");
            showToast(context, "Sending location to " + senderNumber);
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException starting service: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start LocationService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLocationResponse(Context context, String messageBody, String senderNumber) {
        try {
            Log.d(TAG, "Processing location response from: " + senderNumber);
            Log.d(TAG, "Message body: " + messageBody);

            // Parse message format: POSITION:lat,lon;time:timestamp
            String[] parts = messageBody.split(";");
            if (parts.length < 1) {
                Log.e(TAG, "Invalid message format - no parts found");
                return;
            }

            // Extract position part
            String positionPart = parts[0].replace("POSITION:", "").trim();
            String[] coords = positionPart.split(",");

            if (coords.length < 2) {
                Log.e(TAG, "Invalid coordinate format - coords length: " + coords.length);
                return;
            }

            double latitude = Double.parseDouble(coords[0].trim());
            double longitude = Double.parseDouble(coords[1].trim());

            // Validate coordinates
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                Log.e(TAG, "Invalid coordinate values - Lat: " + latitude + ", Lon: " + longitude);
                return;
            }

            Log.d(TAG, "✓ Location parsed successfully - Lat: " + latitude + ", Lon: " + longitude);

            // Show toast for location received
            showToast(context, " Location received from " + senderNumber);

            // Store in location database
            LocationDatabase db = new LocationDatabase(context);
            db.addLocation(senderNumber, latitude, longitude);
            Log.d(TAG, "✓ Location stored in LocationDatabase");

            // Store in notification database
            NotificationDatabase notifDb = new NotificationDatabase(context);
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault()).format(new java.util.Date());
            notifDb.addNotification(senderNumber, latitude, longitude, timestamp);
            Log.d(TAG, "Notification stored in NotificationDatabase");

            // Show notification if enabled
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("notifications_enabled", true)) {
                NotificationHelper.showLocationNotification(context, senderNumber, latitude, longitude);
                Log.d(TAG, " Location notification shown");
            } else {
                Log.d(TAG, "Notifications disabled in preferences");
            }

            // Broadcast location update for map refresh
            Intent broadcastIntent = new Intent("LOCATION_UPDATED");
            broadcastIntent.putExtra("phoneNumber", senderNumber);
            broadcastIntent.putExtra("latitude", latitude);
            broadcastIntent.putExtra("longitude", longitude);
            context.sendBroadcast(broadcastIntent);
            Log.d(TAG, "✓ Location update broadcast sent");

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing coordinates: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Error processing location response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showToast(Context context, String message) {
        if (context != null) {
            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            handler.post(() -> {
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }
}