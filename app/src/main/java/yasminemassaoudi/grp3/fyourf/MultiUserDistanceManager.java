package yasminemassaoudi.grp3.fyourf;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * 📍 MultiUserDistanceManager - Gère les distances entre utilisateurs
 */
public class MultiUserDistanceManager {

    private static final String TAG = "MultiUserDistanceManager";

    /**
     * Classe interne pour stocker les informations de distance entre 2 users
     */
    public static class UserDistance {
        public int user1Id;
        public int user2Id;
        public String user1Pseudo;
        public String user2Pseudo;
        public double distanceMeters;
        public double distanceKm;
        public double distanceMiles;
        public long timeRemainingSeconds;
        public double directionDegrees;
        public double user1Lat;
        public double user1Lon;
        public double user2Lat;
        public double user2Lon;

        public UserDistance(int user1Id, int user2Id, String user1Pseudo, String user2Pseudo) {
            this.user1Id = user1Id;
            this.user2Id = user2Id;
            this.user1Pseudo = user1Pseudo;
            this.user2Pseudo = user2Pseudo;
        }

        @Override
        public String toString() {
            return String.format(
                    "Distance %s ↔ %s: %.2f km (%.0f°)",
                    user1Pseudo, user2Pseudo, distanceKm, directionDegrees
            );
        }
    }

    /**
     * Calculer la distance entre 2 utilisateurs
     */
    public static UserDistance calculateDistance(
            int user1Id, int user2Id,
            String user1Pseudo, String user2Pseudo,
            LatLng user1Location, LatLng user2Location,
            double averageSpeedKmh) {

        UserDistance userDistance = new UserDistance(user1Id, user2Id, user1Pseudo, user2Pseudo);

        if (user1Location == null || user2Location == null) {
            Log.w(TAG, "⚠️ Une ou plusieurs positions manquent");
            return userDistance;
        }

        // Calculer distance
        userDistance.distanceMeters = RouteCalculator.calculateDistance(user1Location, user2Location);
        userDistance.distanceKm = userDistance.distanceMeters / 1000.0;
        userDistance.distanceMiles = RouteCalculator.calculateDistanceInMiles(user1Location, user2Location);

        // Calculer direction
        userDistance.directionDegrees = RouteCalculator.calculateBearing(user1Location, user2Location);

        // Calculer temps restant
        userDistance.timeRemainingSeconds = RouteCalculator.calculateEstimatedTime(
                userDistance.distanceKm,
                averageSpeedKmh
        );

        // Stocker les coordonnées
        userDistance.user1Lat = user1Location.latitude;
        userDistance.user1Lon = user1Location.longitude;
        userDistance.user2Lat = user2Location.latitude;
        userDistance.user2Lon = user2Location.longitude;

        Log.d(TAG, "✓ " + userDistance.toString());
        return userDistance;
    }

    /**
     * Obtenir la direction cardinale
     */
    public static String getCardinalDirection(double bearing) {
        if (bearing >= 337.5 || bearing < 22.5) return "Nord ⬆️";
        if (bearing >= 22.5 && bearing < 67.5) return "Nord-Est ↗️";
        if (bearing >= 67.5 && bearing < 112.5) return "Est ➡️";
        if (bearing >= 112.5 && bearing < 157.5) return "Sud-Est ↘️";
        if (bearing >= 157.5 && bearing < 202.5) return "Sud ⬇️";
        if (bearing >= 202.5 && bearing < 247.5) return "Sud-Ouest ↙️";
        if (bearing >= 247.5 && bearing < 292.5) return "Ouest ⬅️";
        return "Nord-Ouest ↖️";
    }

    /**
     * Formater le temps restant
     */
    public static String formatTimeRemaining(long seconds) {
        if (seconds <= 0) return "0s";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    /**
     * Vérifier si deux utilisateurs sont proches
     */
    public static boolean isNearby(double distanceMeters, double thresholdMeters) {
        return distanceMeters <= thresholdMeters;
    }

    /**
     * Vérifier si deux utilisateurs sont très proches
     */
    public static boolean isVeryClose(double distanceMeters) {
        return isNearby(distanceMeters, 100); // Moins de 100 mètres
    }

    /**
     * Vérifier si deux utilisateurs sont proches
     */
    public static boolean isClose(double distanceMeters) {
        return isNearby(distanceMeters, 1000); // Moins de 1 km
    }

    /**
     * Obtenir la description de la distance
     */
    public static String getDistanceDescription(double distanceMeters) {
        if (distanceMeters < 50) {
            return "🎯 Très très proche!";
        } else if (distanceMeters < 100) {
            return "🎯 Très proche!";
        } else if (distanceMeters < 500) {
            return "📍 Proche";
        } else if (distanceMeters < 1000) {
            return "📍 À proximité";
        } else if (distanceMeters < 5000) {
            return "🚗 Pas loin";
        } else if (distanceMeters < 10000) {
            return "🛣️ Assez loin";
        } else {
            return "✈️ Très loin";
        }
    }

    /**
     * Obtenir la couleur pour la distance (pour affichage sur carte)
     */
    public static int getDistanceColor(double distanceMeters) {
        if (distanceMeters < 100) {
            return 0xFF00FF00; // Vert
        } else if (distanceMeters < 500) {
            return 0xFFFFFF00; // Jaune
        } else if (distanceMeters < 1000) {
            return 0xFFFFA500; // Orange
        } else {
            return 0xFFFF0000; // Rouge
        }
    }

    /**
     * Formater les informations de distance pour affichage
     */
    public static String formatDistanceInfo(UserDistance userDistance) {
        return String.format(
                "📍 %s ↔ %s\n" +
                "📏 Distance: %.2f km\n" +
                "🧭 Direction: %s (%.0f°)\n" +
                "⏱️ Temps: %s\n" +
                "%s",
                userDistance.user1Pseudo,
                userDistance.user2Pseudo,
                userDistance.distanceKm,
                getCardinalDirection(userDistance.directionDegrees),
                userDistance.directionDegrees,
                formatTimeRemaining(userDistance.timeRemainingSeconds),
                getDistanceDescription(userDistance.distanceMeters)
        );
    }

    /**
     * Calculer la distance moyenne entre plusieurs utilisateurs
     */
    public static double calculateAverageDistance(UserDistance[] distances) {
        if (distances == null || distances.length == 0) return 0;

        double sum = 0;
        for (UserDistance d : distances) {
            sum += d.distanceMeters;
        }
        return sum / distances.length;
    }

    /**
     * Trouver l'utilisateur le plus proche
     */
    public static UserDistance findClosestUser(UserDistance[] distances) {
        if (distances == null || distances.length == 0) return null;

        UserDistance closest = distances[0];
        for (UserDistance d : distances) {
            if (d.distanceMeters < closest.distanceMeters) {
                closest = d;
            }
        }
        return closest;
    }

    /**
     * Trouver l'utilisateur le plus loin
     */
    public static UserDistance findFarthestUser(UserDistance[] distances) {
        if (distances == null || distances.length == 0) return null;

        UserDistance farthest = distances[0];
        for (UserDistance d : distances) {
            if (d.distanceMeters > farthest.distanceMeters) {
                farthest = d;
            }
        }
        return farthest;
    }
}

