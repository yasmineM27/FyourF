# FyourF - Application de Tracking GPS avec MySQL

## 📋 Description

Application Android de tracking GPS qui enregistre et affiche les positions géographiques avec synchronisation MySQL en temps réel.

## ✨ Fonctionnalités

### ✅ Fonctionnalités Implémentées

1. **Enregistrement de positions**
   - Enregistrer des positions géographiques (latitude, longitude, pseudo, numéro)
   - Sauvegarde locale (SQLite) et distante (MySQL)

2. **Historique des positions**
   - Afficher l'historique des positions sauvegardées
   - Charger depuis MySQL ou SQLite
   - Trier par date ou numéro de téléphone
   - Rafraîchir depuis MySQL

3. **Tracking automatique (Trajet)**
   - Lancer un tracking avec période configurable (30s, 1min, personnalisé)
   - Enregistrement automatique toutes les X secondes
   - Affichage en temps réel sur carte Google Maps
   - Dessin du trajet avec polyline
   - Service en foreground avec notification

4. **Affichage sur carte**
   - Charger les positions depuis le serveur
   - Afficher comme marqueurs sur Google Maps
   - Visualiser les trajets complets

5. **Synchronisation**
   - Synchronisation en temps réel avec MySQL
   - Endpoints PHP pour communication
   - Cache local avec SQLite

## 🗄️ Configuration de la Base de Données

### 1. Créer la base de données MySQL

Exécutez le script SQL fourni :

```bash
mysql -u root -p < database_setup.sql
```

Ou manuellement :

```sql
CREATE DATABASE fyourf_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fyourf_db;

CREATE TABLE positions (
    idposition INT AUTO_INCREMENT PRIMARY KEY,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    numero VARCHAR(20) NOT NULL,
    pseudo VARCHAR(100) DEFAULT NULL,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_numero (numero),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. Configurer les fichiers PHP

Modifiez `servicephp/config.php` avec vos paramètres :

```php
<?php
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', 'votre_mot_de_passe');
define('DB_NAME', 'fyourf_db');
?>
```

### 3. Déployer les fichiers PHP

Copiez le dossier `servicephp/` sur votre serveur web :

```
/var/www/html/servicephp/
├── config.php
├── get_all.php
├── add_position.php
├── delete_position.php
└── get_trajectory.php
```

Ou utilisez XAMPP/WAMP :

```
C:/xampp/htdocs/servicephp/
```

### 4. Tester les endpoints

```bash
# Obtenir toutes les positions
curl http://192.168.1.100/servicephp/get_all.php

# Ajouter une position
curl -X POST http://192.168.1.100/servicephp/add_position.php \
  -d "longitude=10.1815&latitude=36.8065&numero=+21612345678&pseudo=Test"

# Supprimer une position
curl -X POST http://192.168.1.100/servicephp/delete_position.php \
  -d "id=1"

# Obtenir un trajet
curl "http://192.168.1.100/servicephp/get_trajectory.php?numero=+21612345678"
```

## 📱 Configuration de l'Application Android

### 1. Configurer l'adresse du serveur

Modifiez `app/src/main/java/yasminemassaoudi/grp3/fyourf/Config.java` :

```java
public class Config {
    // Activer MySQL
    public static final boolean USE_MYSQL = true;
    
    // Configuration du serveur
    public static final String MYSQL_SERVER_IP = "192.168.1.100"; // Votre IP
    public static final String MYSQL_SERVER_PORT = "80";
    public static final String MYSQL_SERVICE_FOLDER = "servicephp";
}
```

### 2. Obtenir votre adresse IP

**Windows :**
```cmd
ipconfig
```
Cherchez "Adresse IPv4"

**Linux/Mac :**
```bash
ifconfig
# ou
ip addr show
```

### 3. Configurer Google Maps API

1. Obtenez une clé API Google Maps : https://console.cloud.google.com/
2. Ajoutez-la dans `app/src/main/AndroidManifest.xml` :

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="VOTRE_CLE_API_ICI" />
```

## 🚀 Utilisation

### 1. Démarrer le Tracking

1. Ouvrez l'application
2. Cliquez sur le bouton FAB vert (📍) ou menu "Tracking GPS"
3. Entrez votre numéro de téléphone
4. (Optionnel) Entrez un pseudo
5. Configurez l'intervalle (30s par défaut, minimum 10s)
6. Cliquez sur "▶️ Démarrer"

### 2. Visualiser le Trajet

- Le trajet s'affiche en temps réel sur la carte
- Les positions sont reliées par une ligne bleue
- Cliquez sur "🗺️ Voir trajet complet" pour voir tout le trajet

### 3. Arrêter le Tracking

- Cliquez sur "⏹️ Arrêter"
- Le service s'arrête et les données sont sauvegardées

### 4. Consulter l'Historique

1. Allez dans l'onglet "History"
2. Cliquez sur "Refresh" pour charger depuis MySQL
3. Triez par date ou numéro
4. Exportez si nécessaire

## 📂 Structure du Projet

```
FyourF/
├── app/src/main/java/yasminemassaoudi/grp3/fyourf/
│   ├── Config.java                    # Configuration MySQL
│   ├── MySQLConfig.java               # URLs des endpoints
│   ├── MySQLLocationService.java      # Service MySQL
│   ├── Position.java                  # Modèle Position
│   ├── TrackingService.java           # Service de tracking
│   ├── TrackingActivity.java          # Interface de tracking
│   ├── MainActivity.java              # Activité principale
│   ├── HistoryFragment.java           # Fragment historique
│   └── ...
├── servicephp/
│   ├── config.php                     # Configuration BDD
│   ├── get_all.php                    # Récupérer toutes les positions
│   ├── add_position.php               # Ajouter une position
│   ├── delete_position.php            # Supprimer une position
│   └── get_trajectory.php             # Récupérer un trajet
├── database_setup.sql                 # Script de création BDD
└── README_SETUP.md                    # Ce fichier
```

## 🔧 Dépannage

### Erreur de connexion MySQL

1. Vérifiez que le serveur web est démarré (Apache/Nginx)
2. Vérifiez que MySQL est démarré
3. Testez l'URL dans un navigateur : `http://192.168.1.100/servicephp/get_all.php`
4. Vérifiez les logs PHP : `/var/log/apache2/error.log`

### Permissions Android

L'application demande :
- ✅ Localisation (fine et coarse)
- ✅ Localisation en arrière-plan
- ✅ Notifications
- ✅ Internet
- ✅ Service en foreground

Acceptez toutes les permissions pour un fonctionnement optimal.

### Le tracking ne fonctionne pas

1. Vérifiez que le GPS est activé
2. Vérifiez les permissions de localisation
3. Désactivez l'optimisation de batterie pour l'app
4. Vérifiez que Config.USE_MYSQL = true
5. Vérifiez la connexion réseau

## 📊 Endpoints API

### GET /servicephp/get_all.php
Récupère toutes les positions

**Réponse :**
```json
{
  "success": true,
  "positions": [
    {
      "idposition": 1,
      "longitude": 10.1815,
      "latitude": 36.8065,
      "numero": "+21612345678",
      "pseudo": "User1",
      "timestamp": 1234567890000
    }
  ]
}
```

### POST /servicephp/add_position.php
Ajoute une position

**Paramètres :**
- longitude (double)
- latitude (double)
- numero (string)
- pseudo (string, optionnel)
- timestamp (long)

### POST /servicephp/delete_position.php
Supprime une position

**Paramètres :**
- id (int)

### GET /servicephp/get_trajectory.php
Récupère un trajet

**Paramètres :**
- numero (string, requis)
- start (long, optionnel)
- end (long, optionnel)

## 📝 Notes

- Les positions sont sauvegardées à la fois en local (SQLite) et distant (MySQL)
- Le cache local permet de fonctionner hors ligne
- Le service de tracking fonctionne en arrière-plan
- Les notifications permettent de contrôler le tracking

## 🔐 Sécurité

⚠️ **Important :** Ce code est pour développement/test uniquement.

Pour la production :
- Ajoutez une authentification aux endpoints PHP
- Utilisez HTTPS
- Validez toutes les entrées
- Limitez les requêtes (rate limiting)
- Créez un utilisateur MySQL dédié avec permissions limitées

## 📞 Support

Pour toute question ou problème, vérifiez :
1. Les logs Android (Logcat)
2. Les logs PHP (error.log)
3. La configuration réseau
4. Les permissions de l'application

