package yasminemassaoudi.grp3.fyourf;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    private static final String TAG = "LocationUtils";

    /**
     * Convert latitude/longitude to human-readable address
     */
    public static String getAddressFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                if (address.getThoroughfare() != null) {
                    sb.append(address.getThoroughfare()).append(", ");
                }
                if (address.getLocality() != null) {
                    sb.append(address.getLocality()).append(", ");
                }
                if (address.getAdminArea() != null) {
                    sb.append(address.getAdminArea());
                }

                return sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
        }
        return "Lat: " + latitude + ", Lon: " + longitude;
    }

    /**
     * Calculate distance between two coordinates in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Format timestamp to readable date/time
     */
    public static String formatTimestamp(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}
