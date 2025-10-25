package yasminemassaoudi.grp3.fyourf;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import yasminemassaoudi.grp3.fyourf.MainActivity;
import yasminemassaoudi.grp3.fyourf.MapActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "location_channel";
    
    public static void showLocationNotification(Context context, String friendPhone, double latitude, double longitude) {
        Log.d("NotificationHelper", "Creating notification for: " + friendPhone + " at " + latitude + "," + longitude);
        // Generate unique notification ID based on phone number and timestamp
        int notificationId = generateNotificationId(friendPhone);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Updates",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for received friend locations");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Intent to open map directly
        Intent mapIntent = new Intent(context, MapActivity.class);
        mapIntent.putExtra("latitude", latitude);
        mapIntent.putExtra("longitude", longitude);
        mapIntent.putExtra("friendName", friendPhone);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent mapPendingIntent = PendingIntent.getActivity(context, notificationId, mapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Intent to open notifications
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("navigate_to_notifications", true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, notificationId + 1, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String address = LocationUtils.getAddressFromCoordinates(context, latitude, longitude);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Location from " + friendPhone)
                .setContentText("Tap to view on map")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText( address + "\n" +
                                 String.format("%.6f, %.6f", latitude, longitude) + "\n" +
                                 new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date())))
                .setAutoCancel(true)
                .setContentIntent(mapPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .addAction(android.R.drawable.ic_dialog_map, "View Map", mapPendingIntent)
                .addAction(android.R.drawable.ic_menu_agenda, "All Notifications", notificationPendingIntent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("sound_enabled", true)) {
            builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        if (prefs.getBoolean("vibration_enabled", true)) {
            builder.setVibrate(new long[]{0, 500, 250, 500});
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
            Log.d("NotificationHelper", "Notification sent with ID: " + notificationId);
        } else {
            Log.e("NotificationHelper", "NotificationManager is null");
        }
    }
    
    private static int generateNotificationId(String phoneNumber) {
        // Generate a unique ID based on phone number hash + current minute
        // This ensures multiple notifications from same number in same minute get same ID (update existing)
        // but different numbers or different minutes get different IDs
        int phoneHash = phoneNumber.hashCode();
        int timeComponent = (int) (System.currentTimeMillis() / 60000); // Per minute
        return Math.abs(phoneHash + timeComponent);
    }
}
