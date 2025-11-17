# 📚 Référence API - FyourF MySQL Sync

## 🌐 Configuration de Base

```
Base URL: http://192.168.56.1/servicephp/
Méthode: GET ou POST
Format: JSON
Encodage: UTF-8
```

---

## 📡 Endpoints Disponibles

### 1️⃣ verify_connection.php

**Description**: Vérifier la connexion à MySQL

**Méthode**: GET

**URL**:
```
http://192.168.56.1/servicephp/verify_connection.php
```

**Réponse Succès (200)**:
```json
{
  "success": true,
  "message": "Connexion MySQL réussie",
  "database_info": {
    "server": "localhost",
    "database": "fyourf_db",
    "version": "5.7.30"
  },
  "tables": ["positions", "trajectories"],
  "statistics": {
    "positions_count": 42,
    "trajectories_count": 5
  }
}
```

**Réponse Erreur**:
```json
{
  "success": false,
  "error": "Erreur de connexion à la base de données"
}
```

---

### 2️⃣ get_all.php

**Description**: Récupérer toutes les positions

**Méthode**: GET

**URL**:
```
http://192.168.56.1/servicephp/get_all.php
```

**Paramètres Optionnels**:
- `limit`: Nombre maximum de résultats (défaut: 100)
- `offset`: Décalage (défaut: 0)

**Réponse Succès**:
```json
{
  "success": true,
  "count": 3,
  "data": [
    {
      "idposition": 1,
      "longitude": 10.1815,
      "latitude": 36.8065,
      "numero": "+21612345678",
      "pseudo": "User1",
      "timestamp": "1234567890000",
      "created_at": "2025-11-06 10:30:45"
    },
    {
      "idposition": 2,
      "longitude": 10.1820,
      "latitude": 36.8070,
      "numero": "+21612345678",
      "pseudo": "User1",
      "timestamp": "1234567920000",
      "created_at": "2025-11-06 10:31:15"
    }
  ]
}
```

---

### 3️⃣ get_statistics.php

**Description**: Récupérer les statistiques des trajets

**Méthode**: GET

**URL**:
```
http://192.168.56.1/servicephp/get_statistics.php
```

**Paramètres Optionnels**:
- `numero`: Filtrer par numéro de téléphone
- `start_date`: Date de début (YYYY-MM-DD)
- `end_date`: Date de fin (YYYY-MM-DD)
- `limit`: Nombre maximum de trajets (défaut: 50)

**Exemple**:
```
http://192.168.56.1/servicephp/get_statistics.php?numero=+21612345678&limit=10
```

**Réponse Succès**:
```json
{
  "success": true,
  "global_stats": {
    "total_trajectories": 2,
    "total_distance_km": 5.45,
    "total_duration_ms": 1800000,
    "average_speed_kmh": 10.9
  },
  "trajectories": [
    {
      "id_trajectory": 1,
      "numero": "+21612345678",
      "pseudo": "User1",
      "start_time": "1234567890000",
      "end_time": "1234568790000",
      "duration_ms": 900000,
      "total_distance_km": 2.5,
      "average_speed_kmh": 10.0,
      "point_count": 30,
      "created_at": "2025-11-06 10:30:45"
    }
  ]
}
```

---

### 4️⃣ get_trajectory.php

**Description**: Récupérer les positions d'un trajet

**Méthode**: GET

**URL**:
```
http://192.168.56.1/servicephp/get_trajectory.php
```

**Paramètres Requis**:
- `numero`: Numéro de téléphone

**Paramètres Optionnels**:
- `limit`: Nombre maximum de positions (défaut: 1000)

**Exemple**:
```
http://192.168.56.1/servicephp/get_trajectory.php?numero=+21612345678
```

**Réponse Succès**:
```json
{
  "success": true,
  "count": 2,
  "data": [
    {
      "latitude": 36.8065,
      "longitude": 10.1815,
      "timestamp": "1234567890000",
      "distance_from_previous": 0,
      "created_at": "2025-11-06 10:30:45"
    },
    {
      "latitude": 36.8070,
      "longitude": 10.1820,
      "timestamp": "1234567920000",
      "distance_from_previous": 0.056,
      "created_at": "2025-11-06 10:31:15"
    }
  ]
}
```

---

### 5️⃣ save_trajectory.php

**Description**: Sauvegarder un trajet complet

**Méthode**: POST

**URL**:
```
http://192.168.56.1/servicephp/save_trajectory.php
```

**Content-Type**: application/json

**Body (JSON)**:
```json
{
  "numero": "+21612345678",
  "pseudo": "User1",
  "start_time": 1234567890000,
  "end_time": 1234568790000,
  "duration_ms": 900000,
  "total_distance_km": 2.5,
  "average_speed_kmh": 10.0,
  "positions": [
    {
      "latitude": 36.8065,
      "longitude": 10.1815,
      "timestamp": 1234567890000
    },
    {
      "latitude": 36.8070,
      "longitude": 10.1820,
      "timestamp": 1234567920000
    }
  ]
}
```

**Réponse Succès**:
```json
{
  "success": true,
  "message": "Trajet sauvegardé avec succès",
  "trajectory_id": 1,
  "positions_saved": 2
}
```

**Réponse Erreur**:
```json
{
  "success": false,
  "error": "Erreur lors de la sauvegarde du trajet"
}
```

---

### 6️⃣ add_position.php

**Description**: Ajouter ou mettre à jour une position

**Méthode**: GET ou POST

**URL**:
```
http://192.168.56.1/servicephp/add_position.php
```

**Paramètres**:
- `numero`: Numéro de téléphone (requis)
- `pseudo`: Pseudo utilisateur (optionnel)
- `latitude`: Latitude (requis)
- `longitude`: Longitude (requis)
- `timestamp`: Timestamp en ms (optionnel)

**Exemple GET**:
```
http://192.168.56.1/servicephp/add_position.php?numero=+21612345678&pseudo=User1&latitude=36.8065&longitude=10.1815
```

**Réponse Succès**:
```json
{
  "success": true,
  "message": "Position ajoutée/mise à jour avec succès",
  "position_id": 1
}
```

---

## 🔄 Utilisation dans TrackingSyncManager

### Exemple: Sauvegarder un Trajet

```java
TrackingSyncManager syncManager = new TrackingSyncManager(context, "192.168.56.1");

syncManager.saveTrajectory(
    "+21612345678",           // numero
    "User1",                  // pseudo
    trajectoryPoints,         // ArrayList<Position>
    900000,                   // durationMs
    2.5,                      // totalDistanceKm
    10.0,                     // averageSpeedKmh
    1234567890000L,           // startTime
    1234568790000L,           // endTime
    new TrackingSyncManager.SyncCallback() {
        @Override
        public void onSyncSuccess(String message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onSyncError(String error) {
            Toast.makeText(context, "Erreur: " + error, Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onSyncProgress(int current, int total) {
            Log.d("Sync", "Progression: " + current + "/" + total);
        }
    }
);
```

### Exemple: Récupérer les Statistiques

```java
syncManager.getStatistics(
    "+21612345678",
    new TrackingSyncManager.SyncCallback() {
        @Override
        public void onSyncSuccess(String message) {
            // message contient les statistiques en JSON
            Log.d("Stats", message);
        }
        
        @Override
        public void onSyncError(String error) {
            Log.e("Stats", "Erreur: " + error);
        }
        
        @Override
        public void onSyncProgress(int current, int total) {}
    }
);
```

### Exemple: Vérifier la Connexion

```java
syncManager.verifyConnection(
    new TrackingSyncManager.SyncCallback() {
        @Override
        public void onSyncSuccess(String message) {
            Log.d("Connection", "MySQL connecté: " + message);
        }
        
        @Override
        public void onSyncError(String error) {
            Log.e("Connection", "Erreur: " + error);
        }
        
        @Override
        public void onSyncProgress(int current, int total) {}
    }
);
```

---

## 🔐 Sécurité

### Points Importants

1. **Validation des Données**: Tous les paramètres sont validés côté serveur
2. **Prepared Statements**: Utilisés pour prévenir les injections SQL
3. **Encodage UTF-8**: Tous les caractères spéciaux sont gérés
4. **Gestion d'Erreurs**: Les erreurs sont loggées sans révéler les détails sensibles

### Recommandations

- ✅ Utilisez HTTPS en production
- ✅ Ajoutez une authentification API
- ✅ Limitez les requêtes par IP
- ✅ Validez les données côté client et serveur

---

## 📊 Codes de Réponse HTTP

| Code | Signification |
|------|---------------|
| 200 | Succès |
| 400 | Requête invalide |
| 404 | Endpoint non trouvé |
| 500 | Erreur serveur |

---

## 🧪 Tester avec cURL

```bash
# Vérifier la connexion
curl http://192.168.56.1/servicephp/verify_connection.php

# Récupérer toutes les positions
curl http://192.168.56.1/servicephp/get_all.php

# Récupérer les statistiques
curl "http://192.168.56.1/servicephp/get_statistics.php?numero=+21612345678"

# Sauvegarder un trajet
curl -X POST http://192.168.56.1/servicephp/save_trajectory.php \
  -H "Content-Type: application/json" \
  -d @trajectory.json
```

---

**Version**: 1.0
**Date**: 2025-11-06
**Status**: ✅ Complet

