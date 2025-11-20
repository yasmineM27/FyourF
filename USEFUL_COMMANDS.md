# 🔧 Commandes Utiles - FyourF

**Date**: 2025-11-07
**Status**: 📋 Référence

---

## 🏗️ Build & Compilation

### Compiler le Projet
```bash
# Compiler uniquement le Java
.\gradlew.bat compileDebugJavaWithJavac

# Compiler avec ressources
.\gradlew.bat compileDebugSources

# Build complet
.\gradlew.bat build

# Build avec logs détaillés
.\gradlew.bat build --info
```

### Nettoyer le Projet
```bash
# Nettoyer les fichiers générés
.\gradlew.bat clean

# Nettoyer et compiler
.\gradlew.bat clean build
```

---

## 📱 Installation & Exécution

### Installer l'APK
```bash
# Installer en debug
.\gradlew.bat installDebug

# Installer et lancer
.\gradlew.bat installDebug
adb shell am start -n yasminemassaoudi.grp3.fyourf/.MainActivity

# Installer sur un appareil spécifique
adb devices
adb -s <device_id> install app/build/outputs/apk/debug/app-debug.apk
```

### Lancer l'Application
```bash
# Lancer l'app
adb shell am start -n yasminemassaoudi.grp3.fyourf/.MainActivity

# Lancer avec logs
adb logcat | grep "FyourF"

# Lancer les tests
.\gradlew.bat connectedAndroidTest
```

---

## 🗄️ MySQL & Base de Données

### Connexion MySQL
```bash
# Connexion locale
mysql -h localhost -u root -p

# Connexion distante
mysql -h 192.168.56.1 -u root -p

# Connexion avec base de données
mysql -h 192.168.56.1 -u root -p fyourf_db
```

### Exécuter les Scripts SQL
```bash
# Exécuter le script complet
mysql -h 192.168.56.1 -u root -p fyourf_db < database_complete_setup.sql

# Exécuter depuis PhpMyAdmin
# 1. Aller à http://192.168.56.1/phpmyadmin/
# 2. Sélectionner fyourf_db
# 3. Onglet SQL
# 4. Copier-coller le script
# 5. Exécuter
```

### Requêtes MySQL Utiles
```sql
-- Voir les tables
SHOW TABLES;

-- Voir les utilisateurs
SELECT * FROM users;

-- Voir les connexions
SELECT * FROM user_connections;

-- Voir les distances
SELECT * FROM user_distances;

-- Compter les utilisateurs
SELECT COUNT(*) FROM users;

-- Voir les dernières positions
SELECT * FROM latest_positions;

-- Voir les statistiques
SELECT * FROM user_statistics;

-- Voir les amis connectés
SELECT * FROM connected_friends;

-- Nettoyer les anciennes positions
CALL clean_old_positions(30);

-- Obtenir un trajet
CALL get_trajectory('+21612345678', 0, 0);
```

---

## 🌐 PHP & API

### Tester les Endpoints

#### Créer un Utilisateur
```bash
curl -X POST http://192.168.56.1/servicephp/users/create_user.php \
  -H "Content-Type: application/json" \
  -d '{
    "numero": "+21612345678",
    "pseudo": "TestUser",
    "email": "test@example.com",
    "phone": "+21612345678"
  }'
```

#### Récupérer un Utilisateur
```bash
curl http://192.168.56.1/servicephp/users/get_user.php?numero=%2B21612345678
```

#### Lister Tous les Utilisateurs
```bash
curl http://192.168.56.1/servicephp/users/get_all_users.php
```

#### Ajouter une Connexion
```bash
curl -X POST http://192.168.56.1/servicephp/connections/add_connection.php \
  -H "Content-Type: application/json" \
  -d '{
    "user1_id": 1,
    "user2_id": 2
  }'
```

#### Récupérer les Amis
```bash
curl http://192.168.56.1/servicephp/connections/get_connections.php?user_id=1
```

#### Calculer la Distance
```bash
curl http://192.168.56.1/servicephp/connections/get_distance.php?user1_id=1&user2_id=2
```

#### Vérifier la Connexion
```bash
curl http://192.168.56.1/servicephp/verify_connection.php
```

---

## 📊 Logs & Débogage

### Afficher les Logs
```bash
# Tous les logs
adb logcat

# Logs filtrés par tag
adb logcat | grep "FyourF"

# Logs en temps réel
adb logcat -f /path/to/logfile.txt

# Logs avec timestamps
adb logcat -v time

# Logs avec niveau
adb logcat -v threadtime
```

### Nettoyer les Logs
```bash
# Effacer les logs
adb logcat -c

# Afficher les logs depuis le dernier démarrage
adb logcat -d
```

---

## 🔍 Inspection & Débogage

### Inspecter l'Appareil
```bash
# Lister les appareils
adb devices

# Infos de l'appareil
adb shell getprop

# Infos spécifiques
adb shell getprop ro.build.version.release
adb shell getprop ro.product.model
```

### Fichiers & Répertoires
```bash
# Lister les fichiers
adb shell ls -la /data/data/yasminemassaoudi.grp3.fyourf/

# Copier un fichier
adb pull /data/data/yasminemassaoudi.grp3.fyourf/file.txt

# Pousser un fichier
adb push file.txt /data/data/yasminemassaoudi.grp3.fyourf/
```

---

## 🧪 Tests

### Exécuter les Tests
```bash
# Tests unitaires
.\gradlew.bat test

# Tests d'intégration
.\gradlew.bat connectedAndroidTest

# Tests spécifiques
.\gradlew.bat test --tests "TestClass"
```

### Générer un Rapport de Couverture
```bash
# Couverture de code
.\gradlew.bat jacocoTestReport

# Voir le rapport
# Ouvrir: app/build/reports/jacoco/index.html
```

---

## 📦 Dépendances

### Mettre à Jour les Dépendances
```bash
# Vérifier les dépendances
.\gradlew.bat dependencies

# Mettre à jour Gradle
.\gradlew.bat wrapper --gradle-version=latest

# Mettre à jour les plugins
# Éditer build.gradle.kts
```

---

## 🚀 Déploiement

### Générer un APK de Release
```bash
# Build release
.\gradlew.bat assembleRelease

# APK généré
# app/build/outputs/apk/release/app-release.apk

# Signer l'APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore my-release-key.keystore \
  app-release-unsigned.apk alias_name
```

### Générer un Bundle
```bash
# Build bundle
.\gradlew.bat bundleRelease

# Bundle généré
# app/build/outputs/bundle/release/app-release.aab
```

---

## 🔐 Sécurité

### Générer une Clé de Signature
```bash
# Générer une clé
keytool -genkey -v -keystore my-release-key.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias alias_name

# Lister les clés
keytool -list -v -keystore my-release-key.keystore
```

---

## 📝 Fichiers Importants

### Fichiers de Configuration
```
app/build.gradle.kts - Configuration du build
AndroidManifest.xml - Manifest de l'app
gradle.properties - Propriétés Gradle
local.properties - Propriétés locales
```

### Fichiers de Ressources
```
res/values/strings.xml - Chaînes de caractères
res/values/colors.xml - Couleurs
res/values/themes.xml - Thèmes
res/layout/ - Layouts
res/drawable/ - Drawables
```

---

## 💡 Astuces

### Accélérer la Compilation
```bash
# Utiliser le daemon Gradle
org.gradle.daemon=true

# Paralléliser les tâches
org.gradle.parallel=true

# Augmenter la mémoire
org.gradle.jvmargs=-Xmx2048m
```

### Déboguer Efficacement
```bash
# Ajouter des logs
Log.d("TAG", "Message");
Log.e("TAG", "Error", exception);

# Utiliser le debugger Android Studio
# Run > Debug 'app'
```

---

## 🆘 Dépannage

### Erreurs Courantes

#### Build Failed
```bash
# Nettoyer et reconstruire
.\gradlew.bat clean build

# Invalider le cache
.\gradlew.bat build --refresh-dependencies
```

#### Erreur de Compilation
```bash
# Vérifier la syntaxe
.\gradlew.bat compileDebugJavaWithJavac

# Voir les erreurs détaillées
.\gradlew.bat build --stacktrace
```

#### Erreur de Connexion PHP
```bash
# Vérifier la connexion
curl http://192.168.56.1/servicephp/verify_connection.php

# Vérifier les logs PHP
tail -f /var/log/apache2/error.log
```

---

## 📞 Support

Pour plus d'aide:
1. Consulter la documentation
2. Vérifier les logs
3. Tester les endpoints
4. Vérifier la base de données

---

**Status**: 📋 Référence
**Dernière Mise à Jour**: 2025-11-07

