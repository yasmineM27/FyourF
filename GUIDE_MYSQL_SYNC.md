# 🗄️ Guide Complet: Synchronisation MySQL et Vérification des Données

## 📋 Table des Matières
1. [Vérifier la Connexion MySQL](#vérifier-la-connexion-mysql)
2. [Tester les Endpoints PHP](#tester-les-endpoints-php)
3. [Vérifier les Données Sauvegardées](#vérifier-les-données-sauvegardées)
4. [Utiliser l'Application](#utiliser-lapplication)
5. [Dépannage](#dépannage)

---

## ✅ Vérifier la Connexion MySQL

### Étape 1: Vérifier que MySQL est Démarré

**Windows (XAMPP):**
```
1. Ouvrez XAMPP Control Panel
2. Cliquez sur "Start" pour MySQL
3. Vérifiez que le statut est "Running"
```

**Vérifier via CMD:**
```bash
tasklist | findstr mysqld.exe
```

### Étape 2: Vérifier la Base de Données

```bash
# Ouvrir MySQL
mysql -u root -p

# Voir les bases de données
SHOW DATABASES;

# Sélectionner la base
USE fyourf_db;

# Voir les tables
SHOW TABLES;

# Voir les positions
SELECT * FROM positions;

# Voir les trajets
SELECT * FROM trajectories;
```

### Étape 3: Tester via PhpMyAdmin

```
URL: http://localhost/phpmyadmin/
Utilisateur: root
Mot de passe: (vide par défaut)
```

---

## 🧪 Tester les Endpoints PHP

### 1. Vérifier la Connexion

**URL:**
```
http://192.168.56.1/servicephp/verify_connection.php
```

**Réponse attendue:**
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
    "positions_count": 10,
    "trajectories_count": 2
  }
}
```

### 2. Récupérer Toutes les Positions

**URL:**
```
http://192.168.56.1/servicephp/get_all.php
```

**Réponse:**
```json
{
  "success": true,
  "count": 10,
  "data": [
    {
      "idposition": 1,
      "longitude": 10.1815,
      "latitude": 36.8065,
      "numero": "+21612345678",
      "pseudo": "User1",
      "timestamp": "1234567890000"
    }
  ]
}
```

### 3. Récupérer les Statistiques

**URL:**
```
http://192.168.56.1/servicephp/get_statistics.php?numero=+21612345678
```

**Réponse:**
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
      "duration_ms": 900000,
      "total_distance_km": 2.5,
      "average_speed_kmh": 10.0,
      "point_count": 30
    }
  ]
}
```

### 4. Récupérer un Trajet Spécifique

**URL:**
```
http://192.168.56.1/servicephp/get_trajectory.php?numero=+21612345678
```

---

## 💾 Vérifier les Données Sauvegardées

### Via MySQL Command Line

```sql
-- Voir toutes les positions
SELECT COUNT(*) as total_positions FROM positions;

-- Voir les positions par utilisateur
SELECT numero, COUNT(*) as count FROM positions GROUP BY numero;

-- Voir les trajets
SELECT * FROM trajectories;

-- Voir les statistiques d'un trajet
SELECT 
    id_trajectory,
    numero,
    duration_ms,
    total_distance_km,
    average_speed_kmh,
    point_count,
    created_at
FROM trajectories
WHERE numero = '+21612345678'
ORDER BY created_at DESC;

-- Voir les positions d'un trajet
SELECT 
    latitude,
    longitude,
    timestamp,
    created_at
FROM positions
WHERE numero = '+21612345678'
ORDER BY timestamp ASC;
```

### Via PhpMyAdmin

1. Ouvrez http://localhost/phpmyadmin/
2. Sélectionnez la base `fyourf_db`
3. Cliquez sur la table `positions` ou `trajectories`
4. Visualisez les données

---

## 🚀 Utiliser l'Application

### Flux de Synchronisation

```
1. Ouvrir l'application FyourF
2. Entrer le numéro et pseudo
3. Cliquer sur "▶️ Démarrer"
   ↓
   Les positions sont enregistrées localement
   ↓
4. Cliquer sur "⏹️ Arrêter"
   ↓
   Les données sont automatiquement sauvegardées dans MySQL
   ↓
5. Voir le message de succès
   ↓
   Les données sont maintenant dans la base de données
```

### Vérifier la Sauvegarde

**Immédiatement après l'arrêt du tracking:**

```bash
# Vérifier dans MySQL
mysql -u root -p fyourf_db
SELECT * FROM trajectories ORDER BY created_at DESC LIMIT 1;
```

**Ou via l'API:**

```bash
curl http://192.168.56.1/servicephp/get_statistics.php?numero=+21612345678
```

---

## 🔧 Dépannage

### Problème: "Impossible de se connecter au serveur"

**Solutions:**
1. Vérifiez que Apache est démarré (XAMPP)
2. Vérifiez l'IP dans Config.java
3. Testez: `ping 192.168.56.1`
4. Testez dans le navigateur: `http://192.168.56.1/`

### Problème: "Erreur de connexion à la base de données"

**Solutions:**
1. Vérifiez que MySQL est démarré
2. Vérifiez les paramètres dans config.php
3. Testez: `mysql -u root -p`
4. Vérifiez que la base `fyourf_db` existe

### Problème: "Aucune donnée sauvegardée"

**Solutions:**
1. Vérifiez les logs: `adb logcat | grep TrackingSyncManager`
2. Vérifiez que le tracking a enregistré des positions
3. Vérifiez que l'arrêt du tracking a déclenché la sauvegarde
4. Testez manuellement l'endpoint save_trajectory.php

### Problème: "Les positions ne s'affichent pas sur la carte"

**Solutions:**
1. Vérifiez les permissions GPS
2. Vérifiez que le GPS est activé
3. Vérifiez que l'intervalle de tracking n'est pas trop long
4. Vérifiez les logs: `adb logcat | grep TrackingActivity`

---

## 📊 Commandes Utiles

### Voir les Logs de l'Application

```bash
adb logcat | grep "TrackingActivity\|TrackingSyncManager"
```

### Voir les Logs MySQL

```bash
# Windows XAMPP
C:\xampp\mysql\data\mysql_error.log
```

### Voir les Logs PHP

```bash
# Windows XAMPP
C:\xampp\apache\logs\error.log
C:\xampp\apache\logs\access.log
```

### Réinitialiser la Base de Données

```bash
mysql -u root -p < database_simple.sql
```

---

## ✨ Résumé

| Étape | Action | Vérification |
|-------|--------|-------------|
| 1 | Démarrer MySQL | `tasklist \| findstr mysqld` |
| 2 | Démarrer Apache | XAMPP Control Panel |
| 3 | Tester connexion | `http://localhost/servicephp/verify_connection.php` |
| 4 | Utiliser l'app | Démarrer/arrêter tracking |
| 5 | Vérifier données | `SELECT * FROM trajectories;` |

---

**Status**: ✅ Tous les scripts PHP créés et testés

