# FyourF - Documentation Complète

## Table des Matières
1. [Vue d'ensemble](#vue-densemble)
2. [Architecture de l'Application](#architecture-de-lapplication)
3. [Fonctionnalités Principales](#fonctionnalités-principales)
4. [Permissions et Configuration](#permissions-et-configuration)
5. [Composants Techniques](#composants-techniques)
6. [Base de Données](#base-de-données)
7. [Services et Background Processing](#services-et-background-processing)
8. [Guide d'Utilisation](#guide-dutilisation)
9. [Dépannage](#dépannage)

---

## Vue d'ensemble

**FyourF** est une application Android de localisation en temps réel qui permet aux utilisateurs de partager leur position géographique via SMS. L'application fonctionne même lorsque le téléphone est verrouillé ou l'application fermée, grâce à des services en arrière-plan et des permissions spéciales.

### Caractéristiques Principales
- Partage de localisation automatique via SMS
- Fonctionnement en arrière-plan (téléphone verrouillé)
- Historique des localisations
- Notifications push
- Visualisation sur carte Google Maps
- Gestion des paramètres utilisateur

---

## Architecture de l'Application

### Structure des Packages
```
yasminemassaoudi.grp3.fyourf/
├── MainActivity.java                 # Activité principale
├── MapActivity.java                  # Affichage de la carte
├── SmsReceiver.java                  # Réception SMS (BroadcastReceiver)
├── LocationService.java              # Service de localisation (Foreground)
├── NotificationHelper.java           # Gestion des notifications
├── LocationDatabase.java             # Base de données des localisations
├── NotificationDatabase.java         # Base de données des notifications
├── LocationUtils.java                # Utilitaires de localisation
├── NavigationManager.java            # Gestion de la navigation
├── HistoryFragment.java              # Fragment historique
├── NotificationsFragment.java        # Fragment notifications
├── SettingsFragment.java             # Fragment paramètres
└── ui/
    ├── home/HomeFragment.java        # Fragment accueil
    └── dashboard/DashboardFragment.java  # Fragment tableau de bord
```

### Flux de Données

**User 1 (Demandeur) → User 2 (Répondeur)**
```
1. User 1 envoie SMS "find friends" → User 2
2. SmsReceiver (User 2) détecte le message
3. LocationService démarre en foreground
4. Récupération de la localisation GPS
5. Envoi SMS "POSITION:lat,lon;time:timestamp" → User 1
6. SmsReceiver (User 1) reçoit la position
7. Stockage dans LocationDatabase + NotificationDatabase
8. Affichage notification + mise à jour carte
```

---

## Fonctionnalités Principales

### 1. Localisation en Temps Réel

#### Comment ça fonctionne
- **Déclencheur**: Réception d'un SMS contenant "find friends"
- **Service**: `LocationService` (Foreground Service)
- **API**: Google Play Services Location API
- **Précision**: HIGH_ACCURACY (GPS + réseau)
- **Timeout**: 30 secondes

#### Code Principal
**SmsReceiver.java** (lignes 76-83)
```java
if (lowerBody.contains(LOCATION_REQUEST_KEYWORD) || 
    lowerBody.contains("give me your location")) {
    handleLocationRequest(context, senderNumber);
}
```

**LocationService.java** (lignes 125-169)
- Utilise `FusedLocationProviderClient`
- Priorité: `Priority.PRIORITY_HIGH_ACCURACY`
- Fallback: Dernière position connue si échec

#### Format du Message
```
POSITION:latitude,longitude;time:timestamp
Exemple: POSITION:48.8566,2.3522;time:1698765432000
```

### 2. Historique des Localisations

#### Fonctionnalités
- Affichage de toutes les localisations reçues
- Tri par date (plus récent en premier)
- Tri par numéro de téléphone
- Export des données
- Visualisation sur carte (clic sur entrée)

#### Base de Données
**Table**: `location_history`
- `id`: INTEGER PRIMARY KEY
- `phone`: TEXT
- `latitude`: REAL
- `longitude`: REAL
- `timestamp`: LONG

#### Code
**HistoryFragment.java**
- `loadLocationHistory()`: Charge les données
- `sortByDate()`: Tri chronologique
- `sortByPhone()`: Tri alphabétique
- `exportHistory()`: Export texte

### 3. Notifications

#### Types de Notifications

**A. Notification de Localisation Reçue**
- Titre: "Location from [numéro]"
- Contenu: Adresse + coordonnées + heure
- Actions:
  - "View Map": Ouvre MapActivity
  - "All Notifications": Ouvre NotificationsFragment

**B. Notification de Service Foreground**
- Titre: "Getting Location"
- Contenu: "Sharing location with [numéro]"
- Importance: LOW (non intrusive)

#### Configuration
**NotificationHelper.java** (lignes 21-89)
- Canal: `location_channel`
- Importance: HIGH
- Son: Configurable (Settings)
- Vibration: Configurable (Settings)

#### Base de Données
**Table**: `notifications`
- `id`: INTEGER PRIMARY KEY
- `phone_number`: TEXT
- `latitude`: REAL
- `longitude`: REAL
- `timestamp`: TEXT
- `is_read`: INTEGER (0/1)

### 4. Carte Interactive

#### Fonctionnalités
- Affichage Google Maps
- Marqueur de position
- Zoom automatique
- Adresse géocodée
- Bouton "Navigate" (Google Maps externe)

#### Code
**MapActivity.java**
- Utilise Google Maps SDK
- Géocodage inverse (coordonnées → adresse)
- Intent vers Google Maps pour navigation

### 5. Paramètres

#### Options Disponibles
- **Notifications**: Activer/Désactiver
- **Son**: Activer/Désactiver
- **Vibration**: Activer/Désactiver
- **Auto-réponse**: Activer/Désactiver (future)

#### Stockage
- SharedPreferences
- Clés: `notifications_enabled`, `sound_enabled`, `vibration_enabled`

---

## Permissions et Configuration

### Permissions Requises

#### AndroidManifest.xml (lignes 5-22)

**SMS**
```xml
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
```

**Localisation**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

**Services et Batterie**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

**Autres**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Demande de Permissions Runtime

#### MainActivity.java (lignes 80-146)

**Permissions Standard** (Android 12+)
- SMS (SEND, RECEIVE, READ)
- Localisation (FINE, COARSE)
- Notifications (POST_NOTIFICATIONS)

**Permission Background Location** (Android 10+)
- Dialogue explicatif avant demande
- Instruction: "Allow all the time"
- Nécessaire pour fonctionnement en arrière-plan

**Optimisation Batterie**
- Dialogue explicatif
- Redirection vers paramètres système
- Permet au service de fonctionner sans interruption

---

## Composants Techniques

### 1. SmsReceiver (BroadcastReceiver)

#### Rôle
Intercepte tous les SMS entrants et traite les messages de localisation.

#### Priorité
```xml
<intent-filter android:priority="999">
```
Haute priorité pour traitement rapide.

#### Traitement des Messages

**A. Requête de Localisation**
```java
if (messageBody.contains("find friends")) {
    handleLocationRequest(context, senderNumber);
}
```

**B. Réponse de Localisation**
```java
if (messageBody.startsWith("POSITION:")) {
    handleLocationResponse(context, messageBody, senderNumber);
}
```

**C. Messages d'Erreur**
```java
if (messageBody.startsWith("ERROR:")) {
    // Affichage toast
}
```

#### Code Principal (SmsReceiver.java)
- `onReceive()`: Point d'entrée
- `handleLocationRequest()`: Démarre LocationService
- `handleLocationResponse()`: Parse et stocke la position

### 2. LocationService (Foreground Service)

#### Caractéristiques
- Type: Foreground Service
- Foreground Service Type: `location`
- Notification obligatoire (Android 8+)
- Timeout: 30 secondes

#### Cycle de Vie
```
onCreate() → onStartCommand() → getLocationAndSendSms() → sendLocationSms() → stopSelf()
```

#### Gestion des Erreurs
- Permission refusée → SMS d'erreur
- Localisation indisponible → Dernière position connue
- Timeout → SMS d'erreur "Location timeout"

#### Code Principal (LocationService.java)
- `getLocationAndSendSms()`: Récupération GPS
- `getLastKnownLocation()`: Fallback
- `sendLocationSms()`: Envoi SMS
- `sendErrorSms()`: Gestion erreurs

### 3. NotificationHelper

#### Fonctionnalités
- Création de canaux de notification
- Génération d'ID uniques
- Actions personnalisées
- Configuration son/vibration

#### ID de Notification
```java
int notificationId = Math.abs(phoneHash + timeComponent);
```
Basé sur hash du numéro + minute actuelle.

---

## Base de Données

### LocationDatabase

#### Schéma
```sql
CREATE TABLE location_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phone TEXT,
    latitude REAL,
    longitude REAL,
    timestamp LONG
)
```

#### Méthodes
- `addLocation()`: Insert ou Update
- `getAllLocations()`: Récupération triée par timestamp DESC
- `getLocationByPhone()`: Recherche par numéro

### NotificationDatabase

#### Schéma
```sql
CREATE TABLE notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phone_number TEXT,
    latitude REAL,
    longitude REAL,
    timestamp TEXT,
    is_read INTEGER DEFAULT 0
)
```

#### Méthodes
- `addNotification()`: Ajout notification
- `getAllNotifications()`: Liste complète
- `markAsRead()`: Marquer comme lu
- `markAllAsRead()`: Tout marquer
- `deleteNotification()`: Suppression
- `deleteAllNotifications()`: Suppression totale

---

## Services et Background Processing

### Foreground Service

#### Pourquoi Foreground?
- Android 8+ interdit services background
- Notification obligatoire
- Pas de limitation de temps
- Priorité haute pour le système

#### Configuration
```java
startForeground(FOREGROUND_ID, createForegroundNotification(senderNumber));
```

### Optimisation Batterie

#### Problème
Android Doze Mode peut tuer les services en arrière-plan.

#### Solution
```java
Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
intent.setData(Uri.parse("package:" + packageName));
startActivity(intent);
```

### Wake Lock

#### Utilisation
Maintient le CPU actif pendant récupération GPS.

#### Permission
```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## Guide d'Utilisation

### Configuration Initiale

1. **Installation**
   - Installer l'APK
   - Accepter toutes les permissions

2. **Permissions Critiques**
   - SMS: Obligatoire
   - Localisation: "Allow all the time"
   - Batterie: Désactiver l'optimisation

3. **Vérification**
   - Ouvrir Settings
   - Vérifier que notifications sont activées

### Utilisation User 1 (Demandeur)

1. Ouvrir l'application
2. Aller dans "Dashboard"
3. Entrer le numéro de téléphone de User 2
4. Cliquer "Send Request"
5. Attendre la réponse (notification)
6. Cliquer sur notification pour voir la carte

### Utilisation User 2 (Répondeur)

**Aucune action requise!**
- L'application répond automatiquement
- Fonctionne même si téléphone verrouillé
- Notification discrète pendant envoi

### Consultation Historique

1. Aller dans "History"
2. Voir toutes les localisations
3. Cliquer sur une entrée pour voir la carte
4. Utiliser boutons de tri si besoin

### Gestion Notifications

1. Aller dans "Notifications"
2. Voir toutes les notifications
3. Cliquer pour voir détails
4. Marquer comme lu / Supprimer

---

## Dépannage

### Problème: Pas de réponse automatique

**Causes possibles:**
1. Permission SMS non accordée
2. Permission localisation refusée
3. Optimisation batterie active
4. Service tué par le système

**Solutions:**
1. Vérifier permissions dans Settings Android
2. Désactiver optimisation batterie
3. Redémarrer l'application
4. Vérifier que "Allow all the time" est sélectionné

### Problème: Localisation imprécise

**Causes:**
1. GPS désactivé
2. Mauvaise réception satellite
3. Mode économie d'énergie

**Solutions:**
1. Activer GPS haute précision
2. Aller à l'extérieur
3. Désactiver mode économie

### Problème: Notifications ne s'affichent pas

**Causes:**
1. Notifications désactivées dans Settings
2. Canal de notification bloqué
3. Mode Ne Pas Déranger actif

**Solutions:**
1. Vérifier Settings > Notifications
2. Réinstaller l'application
3. Désactiver Ne Pas Déranger

### Problème: Application se ferme

**Causes:**
1. Mémoire insuffisante
2. Conflit avec autre application
3. Bug système

**Solutions:**
1. Libérer de la mémoire
2. Redémarrer le téléphone
3. Vérifier les logs (Logcat)

---

## Notes Techniques

### API Google Maps
- Clé API: Configurée dans AndroidManifest.xml
- SDK: Google Play Services Maps 19.2.0
- Géocodage: Geocoder Android

### Versions Android
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 35 (Android 15)
- Compile SDK: 35

### Dépendances Principales
```gradle
implementation 'com.google.android.gms:play-services-location:21.3.0'
implementation 'com.google.android.gms:play-services-maps:19.2.0'
implementation 'androidx.preference:preference:1.2.1'
```

---

## Sécurité et Confidentialité

### Données Stockées Localement
- Historique des localisations
- Notifications
- Préférences utilisateur

### Aucune Transmission Serveur
- Tout fonctionne en peer-to-peer via SMS
- Pas de serveur central
- Pas de tracking tiers

### Recommandations
- Utiliser uniquement avec contacts de confiance
- Vérifier régulièrement l'historique
- Supprimer les anciennes données si nécessaire

---

## Développement Futur

### Fonctionnalités Prévues
- Chiffrement des SMS
- Partage de localisation en continu
- Groupes de contacts
- Géofencing
- Mode urgence (SOS)

### Améliorations Techniques
- Migration vers WorkManager
- Support Kotlin
- Tests unitaires
- CI/CD

---

**Version**: 1.0  
**Dernière mise à jour**: 2025-10-25  
**Développeur**: Yasmine Massaoudi - Groupe 3

