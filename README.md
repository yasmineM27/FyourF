# FyourF - Application de Localisation en Temps Réel

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Version](https://img.shields.io/badge/Version-1.0-blue.svg)](https://github.com/)

Application Android permettant le partage automatique de localisation via SMS, fonctionnant même lorsque le téléphone est verrouillé ou l'application fermée.

---

## Table des Matières

- [Fonctionnalités](#fonctionnalités)
- [Installation](#installation)
- [Configuration](#configuration)
- [Utilisation](#utilisation)
- [Documentation](#documentation)
- [Technologies](#technologies)
- [Permissions](#permissions)
- [Captures d'écran](#captures-décran)
- [Dépannage](#dépannage)
- [Contribution](#contribution)
- [Licence](#licence)

---

## Fonctionnalités

### Principales

✅ **Partage de localisation automatique via SMS**
- Envoi automatique de la position GPS en réponse à un SMS
- Fonctionne même si le téléphone est verrouillé
- Aucune interaction utilisateur requise

✅ **Fonctionnement en arrière-plan**
- Service Foreground pour garantir l'exécution
- Permissions de localisation en arrière-plan
- Exemption d'optimisation batterie

✅ **Historique des localisations**
- Stockage de toutes les positions reçues
- Tri par date ou numéro de téléphone
- Export des données

✅ **Notifications intelligentes**
- Notification à chaque localisation reçue
- Actions rapides (Voir carte, Toutes les notifications)
- Gestion du statut lu/non lu

✅ **Visualisation sur carte**
- Intégration Google Maps
- Affichage de l'adresse géocodée
- Navigation vers la position

✅ **Paramètres personnalisables**
- Activation/désactivation des notifications
- Configuration son et vibration
- Préférences sauvegardées

---

## Installation

### Prérequis

- Android Studio Arctic Fox ou supérieur
- JDK 11 ou supérieur
- Android SDK 24 (Android 7.0) minimum
- Appareil Android ou émulateur avec Google Play Services

### Étapes d'installation

1. **Cloner le repository**
```bash
git clone https://github.com/votre-username/FyourF.git
cd FyourF
```

2. **Ouvrir dans Android Studio**
- File → Open → Sélectionner le dossier FyourF

3. **Synchroniser Gradle**
- Android Studio synchronisera automatiquement les dépendances
- Attendre la fin du processus

4. **Configurer Google Maps API**
- Obtenir une clé API Google Maps: [Google Cloud Console](https://console.cloud.google.com/)
- Ajouter la clé dans `app/src/main/AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="VOTRE_CLE_API_ICI" />
```

5. **Compiler et installer**
- Build → Make Project
- Run → Run 'app'

---

## Configuration

### Permissions critiques

Lors du premier lancement, l'application demandera les permissions suivantes:

#### 1. SMS (Obligatoire)
- Envoi de SMS
- Réception de SMS
- Lecture de SMS

#### 2. Localisation (Obligatoire)
- Localisation précise (GPS)
- Localisation approximative (réseau)
- **Localisation en arrière-plan** → Sélectionner "Allow all the time"

#### 3. Notifications (Recommandé)
- Affichage des notifications

#### 4. Optimisation batterie (Critique)
- Désactiver l'optimisation batterie pour FyourF
- Permet au service de fonctionner sans interruption

### Configuration post-installation

1. **Ouvrir l'application**
2. **Accepter toutes les permissions**
3. **Aller dans Settings**
   - Vérifier que les notifications sont activées
   - Configurer son et vibration selon préférence
4. **Tester la fonctionnalité**
   - Envoyer un SMS "find friends" depuis un autre téléphone
   - Vérifier la réception de la réponse

---

## Utilisation

### Scénario 1: Demander la localisation (User 1)

1. Ouvrir l'application FyourF
2. Aller dans l'onglet **Dashboard**
3. Entrer le numéro de téléphone de la personne (User 2)
4. Cliquer sur **"Send Request"**
5. Attendre la notification de réponse
6. Cliquer sur la notification pour voir la carte
7. Optionnel: Cliquer "Navigate" pour ouvrir Google Maps

### Scénario 2: Répondre automatiquement (User 2)

**Aucune action requise!**

L'application détecte automatiquement le SMS "find friends" et:
1. Démarre le service de localisation
2. Récupère la position GPS actuelle
3. Envoie un SMS de réponse avec les coordonnées
4. Affiche une notification discrète pendant le processus

### Consulter l'historique

1. Aller dans l'onglet **History**
2. Voir toutes les localisations reçues
3. Cliquer sur une entrée pour voir la carte
4. Utiliser les boutons de tri:
   - **Sort by Date**: Plus récent en premier
   - **Sort by Phone**: Ordre alphabétique
5. Cliquer **Export** pour exporter les données

### Gérer les notifications

1. Aller dans l'onglet **Notifications**
2. Voir toutes les notifications reçues
3. Cliquer sur une notification pour voir les détails
4. Actions disponibles:
   - **Mark as Read**: Marquer comme lu
   - **Delete**: Supprimer
   - **Mark All as Read**: Tout marquer
   - **Delete All**: Tout supprimer

---

## Documentation

### Documents disponibles

📄 **[DOCUMENTATION.md](DOCUMENTATION.md)**
- Guide complet de toutes les fonctionnalités
- Explication détaillée du code
- Base de données et schémas
- Services et background processing
- Guide de dépannage complet

📐 **[ARCHITECTURE.md](ARCHITECTURE.md)**
- Diagrammes d'architecture (Mermaid)
- Flux de communication détaillé
- Modèle de données
- Cycle de vie des services
- Design patterns utilisés

### Diagrammes

L'application utilise une architecture en couches:

```
┌─────────────────────────────────┐
│   User Interface Layer          │
│   (Activities, Fragments)       │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│   Business Logic Layer          │
│   (Services, Receivers)         │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│   Data Layer                    │
│   (SQLite, SharedPreferences)   │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│   External Services             │
│   (SMS, GPS, Maps, Notif)       │
└─────────────────────────────────┘
```

Voir [ARCHITECTURE.md](ARCHITECTURE.md) pour les diagrammes détaillés.

---

## Technologies

### Frameworks et Bibliothèques

- **Android SDK**: 24-35 (Android 7.0 - Android 15)
- **Java**: Version 11
- **Google Play Services**:
  - Location API 21.3.0
  - Maps API 19.2.0
- **AndroidX**:
  - AppCompat 1.7.1
  - Material Design 1.13.0
  - Navigation 2.9.5
  - Preference 1.2.1
- **SQLite**: Base de données locale
- **Gradle**: 8.10.1

### APIs Android utilisées

- **SmsManager**: Envoi de SMS
- **BroadcastReceiver**: Réception de SMS
- **FusedLocationProviderClient**: Localisation GPS
- **NotificationManager**: Notifications push
- **Geocoder**: Conversion coordonnées → adresse
- **Google Maps SDK**: Affichage carte interactive

---

## Permissions

### Déclarées dans AndroidManifest.xml

```xml
<!-- SMS -->
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />

<!-- Localisation -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Services -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Autres -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Pourquoi ces permissions?

- **SMS**: Communication peer-to-peer sans serveur
- **Localisation fine**: Précision GPS maximale
- **Localisation background**: Fonctionnement téléphone verrouillé
- **Foreground Service**: Garantie d'exécution
- **Battery Optimization**: Éviter kill par Doze mode
- **Wake Lock**: Maintenir CPU actif pendant GPS
- **Boot Completed**: Redémarrage automatique (futur)
- **Notifications**: Alertes utilisateur

---

## Dépannage

### Problème: Pas de réponse automatique

**Solutions:**
1. Vérifier que toutes les permissions sont accordées
2. S'assurer que "Allow all the time" est sélectionné pour la localisation
3. Désactiver l'optimisation batterie pour FyourF
4. Redémarrer l'application

### Problème: Localisation imprécise

**Solutions:**
1. Activer le GPS haute précision dans les paramètres Android
2. Aller à l'extérieur pour meilleure réception satellite
3. Désactiver le mode économie d'énergie

### Problème: Notifications ne s'affichent pas

**Solutions:**
1. Vérifier Settings → Notifications → FyourF → Activé
2. Désactiver le mode "Ne Pas Déranger"
3. Vérifier que le canal de notification n'est pas bloqué

### Plus de solutions

Consultez la section **Dépannage** dans [DOCUMENTATION.md](DOCUMENTATION.md) pour plus de détails.

---

## Structure du Projet

```
FyourF/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/yasminemassaoudi/grp3/fyourf/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── MapActivity.java
│   │   │   │   ├── SmsReceiver.java
│   │   │   │   ├── LocationService.java
│   │   │   │   ├── NotificationHelper.java
│   │   │   │   ├── LocationDatabase.java
│   │   │   │   ├── NotificationDatabase.java
│   │   │   │   ├── LocationUtils.java
│   │   │   │   ├── NavigationManager.java
│   │   │   │   ├── HistoryFragment.java
│   │   │   │   ├── NotificationsFragment.java
│   │   │   │   ├── SettingsFragment.java
│   │   │   │   └── ui/
│   │   │   │       ├── home/
│   │   │   │       └── dashboard/
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   ├── drawable/
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle.kts
├── DOCUMENTATION.md
├── ARCHITECTURE.md
├── README.md
└── build.gradle.kts
```

---

## Sécurité et Confidentialité

### Données stockées

- **Localement uniquement**: Aucune transmission vers serveur externe
- **Historique des localisations**: SQLite local
- **Notifications**: SQLite local
- **Préférences**: SharedPreferences local

### Recommandations

- ⚠️ Utiliser uniquement avec des contacts de confiance
- 🔒 Vérifier régulièrement l'historique
- 🗑️ Supprimer les anciennes données si nécessaire
- 🔐 Envisager le chiffrement des SMS (fonctionnalité future)

---

## Améliorations Futures

### Fonctionnalités prévues

- [ ] Chiffrement des SMS (AES-256)
- [ ] Partage de localisation en continu
- [ ] Groupes de contacts
- [ ] Géofencing (alertes zone)
- [ ] Mode urgence (SOS)
- [ ] Support multi-langues
- [ ] Thème sombre

### Améliorations techniques

- [ ] Migration vers Kotlin
- [ ] Utilisation de Room Database
- [ ] Coroutines pour async
- [ ] Tests unitaires et UI
- [ ] CI/CD avec GitHub Actions
- [ ] Migration vers WorkManager

---

## Contribution

Les contributions sont les bienvenues! Pour contribuer:

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

---

## Auteurs

**Yasmine Massaoudi** - Groupe 3
- Email: yasmine.massaoudi@example.com
- GitHub: [@yasminemassaoudi](https://github.com/yasminemassaoudi)

---

## Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## Remerciements

- Google Play Services pour les APIs Location et Maps
- Android Open Source Project
- Material Design Components
- Communauté Android

---

## Support

Pour toute question ou problème:

1. Consulter [DOCUMENTATION.md](DOCUMENTATION.md)
2. Vérifier la section [Dépannage](#dépannage)
3. Ouvrir une issue sur GitHub
4. Contacter l'auteur

---

**Version**: 1.0  
**Dernière mise à jour**: 2025-10-25  
**Status**: ✅ Production Ready

---

Made with ❤️ by Yasmine Massaoudi

