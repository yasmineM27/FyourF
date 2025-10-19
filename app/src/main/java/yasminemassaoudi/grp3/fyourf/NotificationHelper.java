package yasminemassaoudi.grp3.fyourf;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import yasminemassaoudi.grp3.fyourf.MainActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1;

    public static void showLocationNotification(Context context, String friendPhone, double latitude, double longitude) {
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

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("latitude", latitude);
        notificationIntent.putExtra("longitude", longitude);
        notificationIntent.putExtra("friendName", friendPhone);
        notificationIntent.putExtra("navigate_to_notifications", true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String address = LocationUtils.getAddressFromCoordinates(context, latitude, longitude);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Location Received from " + friendPhone)
                .setContentText(address)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Location: " + address + "\n" +
                                "Coordinates: " + String.format("%.4f, %.4f", latitude, longitude)))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("sound_enabled", true)) {
            builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        if (prefs.getBoolean("vibration_enabled", true)) {
            builder.setVibrate(new long[]{0, 500, 250, 500});
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
