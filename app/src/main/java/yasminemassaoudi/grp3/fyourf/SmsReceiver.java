package yasminemassaoudi.grp3.fyourf;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.preference.PreferenceManager;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private static final String LOCATION_REQUEST_KEYWORD = "find friends";

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        String format = intent.getExtras().getString("format");

        for (Object pdu : pdus) {
            SmsMessage smsMessage;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            }

            String messageBody = smsMessage.getMessageBody();
            String senderNumber = smsMessage.getOriginatingAddress();

            Log.d(TAG, "SMS received from: " + senderNumber + " Message: " + messageBody);

            if (messageBody.toLowerCase().contains(LOCATION_REQUEST_KEYWORD) ||
                    messageBody.toLowerCase().contains("give me your location")) {
                Log.d(TAG, "Location request detected from: " + senderNumber);

                Intent locationServiceIntent = new Intent(context, yasminemassaoudi.grp3.fyourf.LocationService.class);
                locationServiceIntent.putExtra("senderNumber", senderNumber);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(locationServiceIntent);
                } else {
                    context.startService(locationServiceIntent);
                }
            }
            else if (messageBody.startsWith("POSITION:")) {
                handleLocationResponse(context, messageBody, senderNumber);
            }
        }
    }

    private void handleLocationResponse(Context context, String messageBody, String senderNumber) {
        try {
            String[] parts = messageBody.split(";");
            String positionPart = parts[0].replace("POSITION:", "");
            String[] coords = positionPart.split(",");

            if (coords.length < 2) {
                Log.e(TAG, "Invalid coordinate format");
                return;
            }

            double latitude = Double.parseDouble(coords[0]);
            double longitude = Double.parseDouble(coords[1]);

            Log.d(TAG, "Location parsed - Lat: " + latitude + " Lon: " + longitude);

            yasminemassaoudi.grp3.fyourf.LocationDatabase db = new yasminemassaoudi.grp3.fyourf.LocationDatabase(context);
            db.addLocation(senderNumber, latitude, longitude);

            yasminemassaoudi.grp3.fyourf.NotificationDatabase notifDb = new yasminemassaoudi.grp3.fyourf.NotificationDatabase(context);
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            notifDb.addNotification(senderNumber, latitude, longitude, timestamp);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("notifications_enabled", true)) {
                yasminemassaoudi.grp3.fyourf.NotificationHelper.showLocationNotification(context, senderNumber, latitude, longitude);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing location: " + e.getMessage());
        }
    }
}
