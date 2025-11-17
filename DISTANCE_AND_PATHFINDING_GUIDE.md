# 📍 Guide - Calcul de Distance et Plus Court Chemin

## 🎯 Nouvelles Fonctionnalités Ajoutées

### 1. ✅ Calcul de Distance Entre 2 Points

#### Méthode Simple
```java
// Distance en mètres
double distanceMeters = RouteCalculator.calculateDistance(point1, point2);

// Distance en km
double distanceKm = RouteCalculator.calculateDistanceInKm(point1, point2);

// Distance en miles
double distanceMiles = RouteCalculator.calculateDistanceInMiles(point1, point2);
```

#### Exemple Complet
```java
LatLng paris = new LatLng(48.8566, 2.3522);
LatLng lyon = new LatLng(45.7640, 4.8357);

// Calculer distance
double distanceKm = RouteCalculator.calculateDistanceInKm(paris, lyon);
System.out.println("Distance Paris-Lyon: " + distanceKm + " km");
// Résultat: ~465 km
```

---

### 2. ✅ Plus Court Chemin

#### Chemin Direct Entre 2 Points
```java
// Le plus court chemin est la distance directe (vol d'oiseau)
double shortestPath = RouteCalculator.calculateShortestPath(point1, point2);
```

#### Chemin À Travers Plusieurs Points
```java
List<LatLng> points = new ArrayList<>();
points.add(new LatLng(48.8566, 2.3522)); // Paris
points.add(new LatLng(48.8566, 2.3522)); // Point intermédiaire
points.add(new LatLng(45.7640, 4.8357)); // Lyon

// Plus court chemin = distance directe du premier au dernier point
double shortestPath = RouteCalculator.calculateShortestPathThroughPoints(points);
```

---

### 3. ✅ Informations Détaillées de Distance

#### Classe DistanceInfo
```java
RouteCalculator.DistanceInfo info = 
    RouteCalculator.calculateDistanceWithDetails(point1, point2);

// Accéder aux informations
double distance = info.directDistance; // en mètres
double bearing = info.bearing; // azimut en degrés
double latDiff = info.latitudeDifference;
double lonDiff = info.longitudeDifference;
```

#### Exemple Complet
```java
LatLng from = new LatLng(48.8566, 2.3522); // Paris
LatLng to = new LatLng(45.7640, 4.8357);   // Lyon

RouteCalculator.DistanceInfo info = 
    RouteCalculator.calculateDistanceWithDetails(from, to);

System.out.println("Distance: " + info.directDistance + " m");
System.out.println("Direction: " + info.bearing + "°");
System.out.println("De: (" + info.fromLatitude + ", " + info.fromLongitude + ")");
System.out.println("À: (" + info.toLatitude + ", " + info.toLongitude + ")");
```

---

### 4. ✅ Calcul d'Azimut (Direction)

#### Obtenir la Direction Entre 2 Points
```java
// Azimut en degrés (0-360)
double bearing = RouteCalculator.calculateBearing(point1, point2);

// Interprétation:
// 0° = Nord
// 90° = Est
// 180° = Sud
// 270° = Ouest
```

#### Exemple
```java
LatLng from = new LatLng(48.8566, 2.3522); // Paris
LatLng to = new LatLng(45.7640, 4.8357);   // Lyon

double bearing = RouteCalculator.calculateBearing(from, to);

String direction;
if (bearing >= 337.5 || bearing < 22.5) direction = "Nord";
else if (bearing >= 22.5 && bearing < 67.5) direction = "Nord-Est";
else if (bearing >= 67.5 && bearing < 112.5) direction = "Est";
else if (bearing >= 112.5 && bearing < 157.5) direction = "Sud-Est";
else if (bearing >= 157.5 && bearing < 202.5) direction = "Sud";
else if (bearing >= 202.5 && bearing < 247.5) direction = "Sud-Ouest";
else if (bearing >= 247.5 && bearing < 292.5) direction = "Ouest";
else direction = "Nord-Ouest";

System.out.println("Direction: " + direction + " (" + bearing + "°)");
```

---

## 📊 Comparaison des Méthodes

| Méthode | Retour | Unité | Cas d'Usage |
|---------|--------|-------|------------|
| `calculateDistance()` | double | Mètres | Calcul brut |
| `calculateDistanceInKm()` | double | Km | Affichage utilisateur |
| `calculateDistanceInMiles()` | double | Miles | Utilisateurs US |
| `calculateShortestPath()` | double | Mètres | Plus court chemin |
| `calculateBearing()` | double | Degrés | Direction/Navigation |
| `calculateDistanceWithDetails()` | DistanceInfo | Objet | Informations complètes |

---

## 🗺️ Cas d'Usage Pratiques

### 1. Afficher Distance Entre Deux Villes
```java
LatLng city1 = new LatLng(48.8566, 2.3522); // Paris
LatLng city2 = new LatLng(45.7640, 4.8357); // Lyon

double distanceKm = RouteCalculator.calculateDistanceInKm(city1, city2);
String text = String.format("Distance: %.2f km", distanceKm);
distanceTextView.setText(text);
```

### 2. Calculer Temps de Trajet
```java
double distanceKm = RouteCalculator.calculateDistanceInKm(from, to);
double speedKmh = 100; // Vitesse moyenne

long timeSeconds = RouteCalculator.calculateEstimatedTime(distanceKm, speedKmh);
String timeStr = RouteCalculator.formatTime(timeSeconds);

System.out.println("Temps estimé: " + timeStr);
```

### 3. Afficher Direction de Navigation
```java
double bearing = RouteCalculator.calculateBearing(currentLocation, destination);

// Convertir en direction cardinale
String direction = getCardinalDirection(bearing);
directionTextView.setText("Direction: " + direction);
```

### 4. Vérifier Proximité
```java
double distance = RouteCalculator.calculateDistance(currentLocation, destination);

if (distance < 100) { // Moins de 100 mètres
    Toast.makeText(this, "Destination proche!", Toast.LENGTH_SHORT).show();
}
```

### 5. Calculer Efficacité de Trajet
```java
double directDistance = RouteCalculator.calculateDistance(start, end);
double actualDistance = RouteCalculator.calculateTotalDistance(trajectoryPoints);

double efficiency = RouteCalculator.calculateEfficiency(directDistance, actualDistance);
System.out.println("Efficacité: " + efficiency + "%");
```

---

## 🧮 Formules Utilisées

### Distance (Haversine)
```
a = sin²(Δφ/2) + cos(φ1) × cos(φ2) × sin²(Δλ/2)
c = 2 × atan2(√a, √(1−a))
d = R × c

Où:
- φ = latitude
- λ = longitude
- R = rayon terrestre (6371 km)
```

### Azimut (Bearing)
```
y = sin(Δλ) × cos(φ2)
x = cos(φ1) × sin(φ2) − sin(φ1) × cos(φ2) × cos(Δλ)
θ = atan2(y, x)
```

---

## 📱 Intégration dans TrackingActivity

### Exemple: Afficher Distance à Destination
```java
private void updateDestinationDistance() {
    if (currentUserLocation == null || destinationLocation == null) {
        return;
    }
    
    // Calculer distance
    double distanceKm = RouteCalculator.calculateDistanceInKm(
        currentUserLocation, 
        destinationLocation
    );
    
    // Calculer direction
    double bearing = RouteCalculator.calculateBearing(
        currentUserLocation, 
        destinationLocation
    );
    
    // Afficher
    String text = String.format(
        "Distance: %.2f km\nDirection: %.0f°",
        distanceKm,
        bearing
    );
    distanceTextView.setText(text);
}
```

---

## ✨ Avantages

✅ **Précis** - Utilise la formule Haversine
✅ **Flexible** - Plusieurs unités (m, km, miles)
✅ **Complet** - Distance + Direction + Détails
✅ **Performant** - Calculs optimisés
✅ **Facile** - API simple et intuitive

---

## 📊 Résumé des Nouvelles Méthodes

| Méthode | Paramètres | Retour |
|---------|-----------|--------|
| `calculateDistance()` | LatLng, LatLng | double (m) |
| `calculateDistanceInKm()` | LatLng, LatLng | double (km) |
| `calculateDistanceInMiles()` | LatLng, LatLng | double (miles) |
| `calculateShortestPath()` | LatLng, LatLng | double (m) |
| `calculateShortestPathThroughPoints()` | List<LatLng> | double (m) |
| `calculateBearing()` | LatLng, LatLng | double (°) |
| `calculateDistanceWithDetails()` | LatLng, LatLng | DistanceInfo |

---

**Date**: 2025-11-07
**Status**: ✅ COMPLET
**Build**: ✅ BUILD SUCCESSFUL

