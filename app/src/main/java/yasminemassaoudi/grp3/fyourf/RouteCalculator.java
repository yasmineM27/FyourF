package yasminemassaoudi.grp3.fyourf;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe pour calculer les statistiques de trajet et les distances
 */
public class RouteCalculator {
    
    private static final String TAG = "RouteCalculator";
    
    /**
     * Calcule la distance totale entre tous les points
     */
    public static double calculateTotalDistance(List<LatLng> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            totalDistance += calculateDistance(points.get(i), points.get(i + 1));
        }
        
        Log.d(TAG, "Distance totale: " + totalDistance + "m");
        return totalDistance;
    }
    
    /**
     * Calcule la distance entre deux points (en mètres)
     */
    public static double calculateDistance(LatLng from, LatLng to) {
        if (from == null || to == null) {
            return 0.0;
        }
        
        float[] results = new float[1];
        Location.distanceBetween(
                from.latitude, from.longitude,
                to.latitude, to.longitude,
                results
        );
        
        return results[0]; // Retourne en mètres
    }
    
    /**
     * Calcule la distance directe (à vol d'oiseau)
     */
    public static double calculateDirectDistance(LatLng from, LatLng to) {
        return calculateDistance(from, to);
    }
    
    /**
     * Calcule le temps estimé pour parcourir une distance à une vitesse donnée
     * @param distanceKm Distance en km
     * @param speedKmh Vitesse en km/h
     * @return Temps en secondes
     */
    public static long calculateEstimatedTime(double distanceKm, double speedKmh) {
        if (speedKmh <= 0) {
            return 0;
        }
        
        double timeHours = distanceKm / speedKmh;
        long timeSeconds = (long) (timeHours * 3600);
        
        Log.d(TAG, "Temps estimé: " + timeSeconds + "s pour " + distanceKm + "km à " + speedKmh + "km/h");
        return timeSeconds;
    }
    
    /**
     * Formate le temps en HH:MM:SS
     */
    public static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
    
    /**
     * Calcule la vitesse moyenne
     * @param distanceKm Distance en km
     * @param timeSeconds Temps en secondes
     * @return Vitesse en km/h
     */
    public static double calculateAverageSpeed(double distanceKm, long timeSeconds) {
        if (timeSeconds <= 0) {
            return 0.0;
        }
        
        double timeHours = timeSeconds / 3600.0;
        double speedKmh = distanceKm / timeHours;
        
        Log.d(TAG, "Vitesse moyenne: " + speedKmh + " km/h");
        return speedKmh;
    }
    
    /**
     * Calcule l'efficacité du trajet
     * @param directDistance Distance directe (vol d'oiseau)
     * @param actualDistance Distance réelle parcourue
     * @return Efficacité en pourcentage (0-100)
     */
    public static double calculateEfficiency(double directDistance, double actualDistance) {
        if (actualDistance <= 0) {
            return 0.0;
        }
        
        double efficiency = (directDistance / actualDistance) * 100.0;
        efficiency = Math.min(efficiency, 100.0); // Max 100%
        
        Log.d(TAG, "Efficacité: " + efficiency + "%");
        return efficiency;
    }
    
    /**
     * Détecte les points aberrants (vitesse excessive)
     */
    public static List<Integer> detectOutliers(List<LatLng> points, long timeIntervalSeconds) {
        List<Integer> outliers = new ArrayList<>();
        
        if (points == null || points.size() < 2) {
            return outliers;
        }
        
        // Vitesse max raisonnable: 200 km/h
        double maxSpeedKmh = 200.0;
        double maxSpeedMs = maxSpeedKmh / 3.6;
        
        for (int i = 0; i < points.size() - 1; i++) {
            double distance = calculateDistance(points.get(i), points.get(i + 1));
            double speed = distance / timeIntervalSeconds; // m/s
            
            if (speed > maxSpeedMs) {
                outliers.add(i);
                Log.w(TAG, "Point aberrant détecté à l'index " + i + ": " + speed + " m/s");
            }
        }
        
        return outliers;
    }
    
    /**
     * Calcule le temps d'arrêt (points sans mouvement)
     */
    public static long calculateStopTime(List<LatLng> points, long timeIntervalSeconds, double stopThresholdMeters) {
        long stopTime = 0;
        
        if (points == null || points.size() < 2) {
            return stopTime;
        }
        
        for (int i = 0; i < points.size() - 1; i++) {
            double distance = calculateDistance(points.get(i), points.get(i + 1));
            
            if (distance < stopThresholdMeters) {
                stopTime += timeIntervalSeconds;
            }
        }
        
        Log.d(TAG, "Temps d'arrêt: " + stopTime + "s");
        return stopTime;
    }
    
    /**
     * Calcule les statistiques complètes du trajet
     */
    public static TrajectoryStats calculateStats(List<LatLng> points, long durationSeconds) {
        TrajectoryStats stats = new TrajectoryStats();
        
        if (points == null || points.isEmpty()) {
            return stats;
        }
        
        stats.pointCount = points.size();
        stats.totalDistance = calculateTotalDistance(points);
        stats.directDistance = calculateDistance(points.get(0), points.get(points.size() - 1));
        stats.averageSpeed = calculateAverageSpeed(stats.totalDistance / 1000.0, durationSeconds);
        stats.efficiency = calculateEfficiency(stats.directDistance, stats.totalDistance);
        stats.duration = durationSeconds;
        
        Log.d(TAG, "Stats calculées: " + stats);
        return stats;
    }
    
    /**
     * Calcule le plus court chemin entre deux points (distance directe)
     * @param from Point de départ
     * @param to Point d'arrivée
     * @return Distance en mètres
     */
    public static double calculateShortestPath(LatLng from, LatLng to) {
        return calculateDistance(from, to);
    }

    /**
     * Calcule le plus court chemin à travers une liste de points
     * @param points Liste de points
     * @return Distance totale du plus court chemin
     */
    public static double calculateShortestPathThroughPoints(List<LatLng> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }

        // Le plus court chemin est la distance directe du premier au dernier point
        double shortestPath = calculateDistance(points.get(0), points.get(points.size() - 1));
        Log.d(TAG, "Plus court chemin: " + shortestPath + "m");
        return shortestPath;
    }

    /**
     * Calcule la distance entre deux points avec détails
     * @param from Point de départ
     * @param to Point d'arrivée
     * @return Objet DistanceInfo avec détails
     */
    public static DistanceInfo calculateDistanceWithDetails(LatLng from, LatLng to) {
        DistanceInfo info = new DistanceInfo();

        if (from == null || to == null) {
            return info;
        }

        // Distance directe
        info.directDistance = calculateDistance(from, to);

        // Latitude et longitude
        info.fromLatitude = from.latitude;
        info.fromLongitude = from.longitude;
        info.toLatitude = to.latitude;
        info.toLongitude = to.longitude;

        // Calcul de la différence
        info.latitudeDifference = Math.abs(to.latitude - from.latitude);
        info.longitudeDifference = Math.abs(to.longitude - from.longitude);

        // Calcul de la direction (azimut)
        info.bearing = calculateBearing(from, to);

        Log.d(TAG, "Distance avec détails: " + info);
        return info;
    }

    /**
     * Calcule l'azimut (direction) entre deux points
     * @param from Point de départ
     * @param to Point d'arrivée
     * @return Azimut en degrés (0-360)
     */
    public static double calculateBearing(LatLng from, LatLng to) {
        double lat1 = Math.toRadians(from.latitude);
        double lat2 = Math.toRadians(to.latitude);
        double lon1 = Math.toRadians(from.longitude);
        double lon2 = Math.toRadians(to.longitude);

        double dLon = lon2 - lon1;

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                   Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        bearing = (bearing + 360) % 360; // Normaliser entre 0 et 360

        Log.d(TAG, "Azimut: " + bearing + "°");
        return bearing;
    }

    /**
     * Calcule la distance entre deux points en km
     * @param from Point de départ
     * @param to Point d'arrivée
     * @return Distance en km
     */
    public static double calculateDistanceInKm(LatLng from, LatLng to) {
        return calculateDistance(from, to) / 1000.0;
    }

    /**
     * Calcule la distance entre deux points en miles
     * @param from Point de départ
     * @param to Point d'arrivée
     * @return Distance en miles
     */
    public static double calculateDistanceInMiles(LatLng from, LatLng to) {
        return calculateDistanceInKm(from, to) * 0.621371;
    }

    /**
     * Classe pour stocker les informations de distance
     */
    public static class DistanceInfo {
        public double directDistance = 0.0; // en mètres
        public double fromLatitude = 0.0;
        public double fromLongitude = 0.0;
        public double toLatitude = 0.0;
        public double toLongitude = 0.0;
        public double latitudeDifference = 0.0;
        public double longitudeDifference = 0.0;
        public double bearing = 0.0; // Azimut en degrés

        @Override
        public String toString() {
            return "DistanceInfo{" +
                    "directDistance=" + directDistance +
                    "m, bearing=" + bearing +
                    "°, from=(" + fromLatitude + "," + fromLongitude +
                    "), to=(" + toLatitude + "," + toLongitude + ")" +
                    '}';
        }
    }

    /**
     * Classe pour stocker les statistiques du trajet
     */
    public static class TrajectoryStats {
        public int pointCount = 0;
        public double totalDistance = 0.0; // en mètres
        public double directDistance = 0.0; // en mètres
        public double averageSpeed = 0.0; // en km/h
        public double efficiency = 0.0; // en %
        public long duration = 0; // en secondes

        @Override
        public String toString() {
            return "TrajectoryStats{" +
                    "pointCount=" + pointCount +
                    ", totalDistance=" + totalDistance +
                    ", directDistance=" + directDistance +
                    ", averageSpeed=" + averageSpeed +
                    ", efficiency=" + efficiency +
                    ", duration=" + duration +
                    '}';
        }
    }
}

