# 💡 Exemples Pratiques - Distance et Plus Court Chemin

## 🎯 Exemples Rapides

### Exemple 1: Distance Simple
```java
LatLng point1 = new LatLng(36.8065, 10.1815); // Tunis
LatLng point2 = new LatLng(35.7595, 10.5765); // Sfax

double distanceKm = RouteCalculator.calculateDistanceInKm(point1, point2);
System.out.println("Distance Tunis-Sfax: " + distanceKm + " km");
// Résultat: ~330 km
```

### Exemple 2: Plus Court Chemin
```java
List<LatLng> trajectory = new ArrayList<>();
trajectory.add(new LatLng(36.8065, 10.1815)); // Tunis
trajectory.add(new LatLng(36.5, 10.5));      // Point intermédiaire
trajectory.add(new LatLng(35.7595, 10.5765)); // Sfax

double shortestPath = RouteCalculator.calculateShortestPathThroughPoints(trajectory);
System.out.println("Plus court chemin: " + shortestPath + " m");
```

### Exemple 3: Direction (Azimut)
```java
LatLng from = new LatLng(36.8065, 10.1815);
LatLng to = new LatLng(35.7595, 10.5765);

double bearing = RouteCalculator.calculateBearing(from, to);
System.out.println("Direction: " + bearing + "°");
// 0° = Nord, 90° = Est, 180° = Sud, 270° = Ouest
```

---

## 📱 Intégration dans TrackingActivity

### Exemple 4: Afficher Distance en Temps Réel
```java
private void updateDistanceDisplay() {
    if (currentUserLocation == null || destinationLocation == null) {
        return;
    }
    
    // Calculer distance
    double distanceKm = RouteCalculator.calculateDistanceInKm(
        currentUserLocation,
        destinationLocation
    );
    
    // Calculer temps estimé (vitesse moyenne: 50 km/h)
    long timeSeconds = RouteCalculator.calculateEstimatedTime(distanceKm, 50);
    String timeStr = RouteCalculator.formatTime(timeSeconds);
    
    // Afficher
    String text = String.format(
        "📍 Distance: %.2f km\n⏱️ Temps: %s",
        distanceKm,
        timeStr
    );
    distanceTextView.setText(text);
}
```

### Exemple 5: Afficher Direction de Navigation
```java
private void updateDirectionDisplay() {
    if (currentUserLocation == null || destinationLocation == null) {
        return;
    }
    
    // Calculer azimut
    double bearing = RouteCalculator.calculateBearing(
        currentUserLocation,
        destinationLocation
    );
    
    // Convertir en direction cardinale
    String direction = getCardinalDirection(bearing);
    
    // Afficher
    String text = String.format(
        "🧭 Direction: %s (%.0f°)",
        direction,
        bearing
    );
    directionTextView.setText(text);
}

private String getCardinalDirection(double bearing) {
    if (bearing >= 337.5 || bearing < 22.5) return "Nord ⬆️";
    if (bearing >= 22.5 && bearing < 67.5) return "Nord-Est ↗️";
    if (bearing >= 67.5 && bearing < 112.5) return "Est ➡️";
    if (bearing >= 112.5 && bearing < 157.5) return "Sud-Est ↘️";
    if (bearing >= 157.5 && bearing < 202.5) return "Sud ⬇️";
    if (bearing >= 202.5 && bearing < 247.5) return "Sud-Ouest ↙️";
    if (bearing >= 247.5 && bearing < 292.5) return "Ouest ⬅️";
    return "Nord-Ouest ↖️";
}
```

### Exemple 6: Vérifier Proximité
```java
private void checkProximity() {
    if (currentUserLocation == null || destinationLocation == null) {
        return;
    }
    
    double distanceMeters = RouteCalculator.calculateDistance(
        currentUserLocation,
        destinationLocation
    );
    
    if (distanceMeters < 50) {
        Toast.makeText(this, "🎉 Destination très proche!", Toast.LENGTH_SHORT).show();
    } else if (distanceMeters < 100) {
        Toast.makeText(this, "📍 Destination proche!", Toast.LENGTH_SHORT).show();
    } else if (distanceMeters < 500) {
        Toast.makeText(this, "🚶 Destination à moins de 500m", Toast.LENGTH_SHORT).show();
    }
}
```

### Exemple 7: Calculer Efficacité
```java
private void calculateEfficiency() {
    if (trajectoryPoints.isEmpty()) {
        return;
    }
    
    // Distance directe (vol d'oiseau)
    LatLng start = trajectoryPoints.get(0);
    LatLng end = trajectoryPoints.get(trajectoryPoints.size() - 1);
    double directDistance = RouteCalculator.calculateDistance(start, end);
    
    // Distance réelle parcourue
    double actualDistance = RouteCalculator.calculateTotalDistance(trajectoryPoints);
    
    // Efficacité
    double efficiency = RouteCalculator.calculateEfficiency(directDistance, actualDistance);
    
    // Afficher
    String text = String.format(
        "📊 Efficacité: %.1f%%\n" +
        "📏 Distance directe: %.2f km\n" +
        "🛣️ Distance réelle: %.2f km",
        efficiency,
        directDistance / 1000.0,
        actualDistance / 1000.0
    );
    efficiencyTextView.setText(text);
}
```

### Exemple 8: Afficher Informations Complètes
```java
private void showDetailedDistanceInfo() {
    if (currentUserLocation == null || destinationLocation == null) {
        return;
    }
    
    // Obtenir informations détaillées
    RouteCalculator.DistanceInfo info = 
        RouteCalculator.calculateDistanceWithDetails(
            currentUserLocation,
            destinationLocation
        );
    
    // Afficher dans un dialog
    String message = String.format(
        "📍 INFORMATIONS DE DISTANCE\n\n" +
        "Distance: %.2f km\n" +
        "Direction: %.0f°\n" +
        "Latitude diff: %.4f\n" +
        "Longitude diff: %.4f\n" +
        "De: (%.4f, %.4f)\n" +
        "À: (%.4f, %.4f)",
        info.directDistance / 1000.0,
        info.bearing,
        info.latitudeDifference,
        info.longitudeDifference,
        info.fromLatitude,
        info.fromLongitude,
        info.toLatitude,
        info.toLongitude
    );
    
    new AlertDialog.Builder(this)
        .setTitle("📊 Détails")
        .setMessage(message)
        .setPositiveButton("OK", null)
        .show();
}
```

### Exemple 9: Comparer Distances
```java
private void compareDistances() {
    LatLng point1 = new LatLng(36.8065, 10.1815);
    LatLng point2 = new LatLng(35.7595, 10.5765);
    
    // Différentes unités
    double distanceMeters = RouteCalculator.calculateDistance(point1, point2);
    double distanceKm = RouteCalculator.calculateDistanceInKm(point1, point2);
    double distanceMiles = RouteCalculator.calculateDistanceInMiles(point1, point2);
    
    String text = String.format(
        "Distance:\n" +
        "📏 %,.0f mètres\n" +
        "📍 %.2f km\n" +
        "🗺️ %.2f miles",
        distanceMeters,
        distanceKm,
        distanceMiles
    );
    
    distanceTextView.setText(text);
}
```

### Exemple 10: Calculer Temps de Trajet
```java
private void calculateTravelTime() {
    if (currentUserLocation == null || destinationLocation == null) {
        return;
    }
    
    // Calculer distance
    double distanceKm = RouteCalculator.calculateDistanceInKm(
        currentUserLocation,
        destinationLocation
    );
    
    // Différentes vitesses
    long timeWalking = RouteCalculator.calculateEstimatedTime(distanceKm, 5);    // 5 km/h
    long timeBiking = RouteCalculator.calculateEstimatedTime(distanceKm, 20);    // 20 km/h
    long timeCar = RouteCalculator.calculateEstimatedTime(distanceKm, 80);       // 80 km/h
    
    String text = String.format(
        "⏱️ TEMPS DE TRAJET\n\n" +
        "🚶 À pied: %s\n" +
        "🚴 À vélo: %s\n" +
        "🚗 En voiture: %s",
        RouteCalculator.formatTime(timeWalking),
        RouteCalculator.formatTime(timeBiking),
        RouteCalculator.formatTime(timeCar)
    );
    
    travelTimeTextView.setText(text);
}
```

---

## 🎨 Affichage sur la Carte

### Exemple 11: Afficher Ligne de Distance
```java
private void drawDistanceLine() {
    if (currentUserLocation == null || destinationLocation == null) {
        return;
    }
    
    // Dessiner ligne
    mMap.addPolyline(new PolylineOptions()
        .add(currentUserLocation, destinationLocation)
        .color(Color.RED)
        .width(5)
        .geodesic(true));
    
    // Ajouter marqueurs
    mMap.addMarker(new MarkerOptions()
        .position(currentUserLocation)
        .title("Position actuelle")
        .icon(BitmapDescriptorFactory.defaultMarker(
            BitmapDescriptorFactory.HUE_BLUE)));
    
    mMap.addMarker(new MarkerOptions()
        .position(destinationLocation)
        .title("Destination")
        .icon(BitmapDescriptorFactory.defaultMarker(
            BitmapDescriptorFactory.HUE_RED)));
}
```

---

## 📊 Résumé des Exemples

| # | Exemple | Utilité |
|---|---------|---------|
| 1 | Distance Simple | Calcul basique |
| 2 | Plus Court Chemin | Optimisation |
| 3 | Direction | Navigation |
| 4 | Distance Temps Réel | Affichage dynamique |
| 5 | Direction Navigation | Guidage |
| 6 | Vérifier Proximité | Alertes |
| 7 | Efficacité | Analyse |
| 8 | Informations Complètes | Détails |
| 9 | Comparer Distances | Comparaison |
| 10 | Temps de Trajet | Planification |
| 11 | Ligne sur Carte | Visualisation |

---

**Date**: 2025-11-07
**Status**: ✅ COMPLET
**Build**: ✅ BUILD SUCCESSFUL

