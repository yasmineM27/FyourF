# 🔧 Commandes Utiles - GeoQuiz Challenge

**Date**: 2025-11-07
**Status**: 📋 Référence

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

### Exécuter le Script SQL
```bash
# Exécuter le script complet
mysql -h 192.168.56.1 -u root -p fyourf_db < geoquiz_mysql_setup.sql

# Exécuter depuis PhpMyAdmin
# 1. Aller à http://192.168.56.1/phpmyadmin/
# 2. Sélectionner fyourf_db
# 3. Onglet SQL
# 4. Copier-coller le script
# 5. Exécuter
```

### Requêtes MySQL Utiles
```sql
-- Voir les tables GeoQuiz
SHOW TABLES LIKE 'geoquiz%';

-- Voir les questions
SELECT * FROM geoquiz_questions LIMIT 10;

-- Voir les badges d'un utilisateur
SELECT * FROM geoquiz_badges WHERE user_id = 1;

-- Voir les scores
SELECT * FROM geoquiz_scores ORDER BY total_points DESC LIMIT 10;

-- Voir le leaderboard
SELECT * FROM geoquiz_leaderboard ORDER BY rank ASC LIMIT 10;

-- Compter les questions par région
SELECT region, COUNT(*) FROM geoquiz_questions GROUP BY region;

-- Voir les badges déverrouillés
SELECT * FROM geoquiz_badges WHERE unlocked = TRUE;

-- Voir les statistiques utilisateur
SELECT * FROM geoquiz_user_stats WHERE id = 1;

-- Voir les statistiques régionales
SELECT * FROM geoquiz_regional_stats;

-- Mettre à jour le leaderboard
CALL update_geoquiz_leaderboard();

-- Créer un défi quotidien
CALL create_daily_challenge('Tunis', 2, 5, 100);
```

---

## 🌐 PHP & API

### Tester les Endpoints

#### Sauvegarder un Score
```bash
curl -X POST http://192.168.56.1/servicephp/geoquiz/save_score.php \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": 1,
    "total_points": 250,
    "correct_answers": 8,
    "total_questions": 10,
    "max_streak": 5,
    "session_duration": 300
  }'
```

#### Récupérer les Badges
```bash
# Tous les badges
curl http://192.168.56.1/servicephp/geoquiz/get_badges.php?user_id=1

# Seulement les badges déverrouillés
curl http://192.168.56.1/servicephp/geoquiz/get_badges.php?user_id=1&unlocked_only=true
```

#### Récupérer le Leaderboard
```bash
# Top 10 joueurs
curl http://192.168.56.1/servicephp/geoquiz/get_leaderboard.php?limit=10

# Top 10 avec le rang de l'utilisateur
curl http://192.168.56.1/servicephp/geoquiz/get_leaderboard.php?limit=10&user_id=1

# Top 50 joueurs
curl http://192.168.56.1/servicephp/geoquiz/get_leaderboard.php?limit=50
```

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
adb logcat | grep "GeoQuiz"

# Lancer les tests
.\gradlew.bat connectedAndroidTest
```

---

## 📊 Logs & Débogage

### Afficher les Logs
```bash
# Tous les logs
adb logcat

# Logs filtrés par tag
adb logcat | grep "GeoQuiz"

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
adb pull /data/data/yasminemassaoudi.grp3.fyourf/geoquiz.db

# Pousser un fichier
adb push geoquiz.db /data/data/yasminemassaoudi.grp3.fyourf/
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
.\gradlew.bat test --tests "GeoQuizTest"
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
Log.d("GeoQuiz", "Message");
Log.e("GeoQuiz", "Error", exception);

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
curl http://192.168.56.1/servicephp/geoquiz/get_leaderboard.php

# Vérifier les logs PHP
tail -f /var/log/apache2/error.log
```

#### Base de Données Vide
```bash
# Vérifier les tables
mysql -h 192.168.56.1 -u root -p fyourf_db -e "SHOW TABLES LIKE 'geoquiz%';"

# Réexécuter le script SQL
mysql -h 192.168.56.1 -u root -p fyourf_db < geoquiz_mysql_setup.sql
```

---

## 📞 Support

Pour plus d'aide:
1. Consulter GEOQUIZ_IMPLEMENTATION_GUIDE.md
2. Vérifier les logs
3. Tester les endpoints
4. Vérifier la base de données

---

**Status**: 📋 Référence
**Dernière Mise à Jour**: 2025-11-07

