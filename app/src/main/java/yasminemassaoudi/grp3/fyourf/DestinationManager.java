package yasminemassaoudi.grp3.fyourf;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Classe pour gérer les destinations et calculer les temps de trajet
 */
public class DestinationManager {
    
    private static final String TAG = "DestinationManager";
    
    private LatLng currentLocation;
    private LatLng destination;
    private double averageSpeedKmh = 50.0; // Vitesse moyenne par défaut
    
    public DestinationManager() {
        this.currentLocation = null;
        this.destination = null;
    }
    
    /**
     * Définit la position actuelle
     */
    public void setCurrentLocation(LatLng location) {
        this.currentLocation = location;
        Log.d(TAG, "Position actuelle définie: " + location);
    }
    
    /**
     * Définit la destination
     */
    public void setDestination(LatLng destination) {
        this.destination = destination;
        Log.d(TAG, "Destination définie: " + destination);
    }
    
    /**
     * Définit la vitesse moyenne (en km/h)
     */
    public void setAverageSpeed(double speedKmh) {
        this.averageSpeedKmh = Math.max(speedKmh, 1.0); // Minimum 1 km/h
        Log.d(TAG, "Vitesse moyenne définie: " + this.averageSpeedKmh + " km/h");
    }
    
    /**
     * Obtient la destination actuelle
     */
    public LatLng getDestination() {
        return destination;
    }
    
    /**
     * Obtient la position actuelle
     */
    public LatLng getCurrentLocation() {
        return currentLocation;
    }
    
    /**
     * Calcule la distance jusqu'à la destination
     */
    public double getDistanceToDestination() {
        if (currentLocation == null || destination == null) {
            return 0.0;
        }
        
        double distance = RouteCalculator.calculateDistance(currentLocation, destination);
        Log.d(TAG, "Distance à la destination: " + distance + "m");
        return distance;
    }
    
    /**
     * Calcule le temps estimé pour atteindre la destination
     */
    public long getEstimatedTimeToDestination() {
        if (currentLocation == null || destination == null) {
            return 0;
        }
        
        double distanceKm = getDistanceToDestination() / 1000.0;
        long timeSeconds = RouteCalculator.calculateEstimatedTime(distanceKm, averageSpeedKmh);
        
        Log.d(TAG, "Temps estimé: " + timeSeconds + "s");
        return timeSeconds;
    }
    
    /**
     * Obtient le temps estimé formaté (HH:MM:SS)
     */
    public String getFormattedEstimatedTime() {
        long timeSeconds = getEstimatedTimeToDestination();
        return RouteCalculator.formatTime(timeSeconds);
    }
    
    /**
     * Obtient les informations de destination formatées
     */
    public String getDestinationInfo() {
        if (destination == null) {
            return "Aucune destination définie";
        }
        
        double distanceKm = getDistanceToDestination() / 1000.0;
        String timeFormatted = getFormattedEstimatedTime();
        
        return String.format(
                "📍 Destination\n" +
                "Latitude: %.6f\n" +
                "Longitude: %.6f\n" +
                "Distance: %.2f km\n" +
                "Temps estimé: %s\n" +
                "Vitesse moyenne: %.1f km/h",
                destination.latitude,
                destination.longitude,
                distanceKm,
                timeFormatted,
                averageSpeedKmh
        );
    }
    
    /**
     * Calcule le pourcentage de progression
     */
    public double getProgressPercentage(double distanceTraveled) {
        if (currentLocation == null || destination == null) {
            return 0.0;
        }
        
        double totalDistance = getDistanceToDestination() + distanceTraveled;
        if (totalDistance <= 0) {
            return 0.0;
        }
        
        double progress = (distanceTraveled / totalDistance) * 100.0;
        progress = Math.min(progress, 100.0); // Max 100%
        
        Log.d(TAG, "Progression: " + progress + "%");
        return progress;
    }
    
    /**
     * Vérifie si la destination a été atteinte
     */
    public boolean isDestinationReached(double thresholdMeters) {
        if (currentLocation == null || destination == null) {
            return false;
        }
        
        double distance = getDistanceToDestination();
        boolean reached = distance <= thresholdMeters;
        
        if (reached) {
            Log.d(TAG, "Destination atteinte!");
        }
        
        return reached;
    }
    
    /**
     * Calcule la déviation par rapport à la route directe
     */
    public double getDeviationFromDirectPath(double distanceTraveled) {
        if (currentLocation == null || destination == null) {
            return 0.0;
        }
        
        double directDistance = RouteCalculator.calculateDistance(currentLocation, destination);
        double deviation = distanceTraveled - directDistance;
        
        Log.d(TAG, "Déviation: " + deviation + "m");
        return deviation;
    }
    
    /**
     * Obtient les informations de progression formatées
     */
    public String getProgressInfo(double distanceTraveled) {
        double distanceToDestination = getDistanceToDestination();
        double totalDistance = distanceTraveled + distanceToDestination;
        double progressPercent = getProgressPercentage(distanceTraveled);
        String timeRemaining = getFormattedEstimatedTime();
        
        return String.format(
                "📊 Progression\n" +
                "Distance parcourue: %.2f km\n" +
                "Distance restante: %.2f km\n" +
                "Distance totale: %.2f km\n" +
                "Progression: %.1f%%\n" +
                "Temps restant: %s",
                distanceTraveled / 1000.0,
                distanceToDestination / 1000.0,
                totalDistance / 1000.0,
                progressPercent,
                timeRemaining
        );
    }
    
    /**
     * Réinitialise le gestionnaire
     */
    public void reset() {
        this.currentLocation = null;
        this.destination = null;
        this.averageSpeedKmh = 50.0;
        Log.d(TAG, "Gestionnaire réinitialisé");
    }
    
    /**
     * Obtient un résumé complet
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DESTINATION MANAGER ===\n");
        sb.append("Position actuelle: ").append(currentLocation).append("\n");
        sb.append("Destination: ").append(destination).append("\n");
        sb.append("Vitesse moyenne: ").append(averageSpeedKmh).append(" km/h\n");
        
        if (destination != null) {
            sb.append("Distance: ").append(String.format("%.2f", getDistanceToDestination() / 1000.0)).append(" km\n");
            sb.append("Temps estimé: ").append(getFormattedEstimatedTime()).append("\n");
        }
        
        return sb.toString();
    }
}

