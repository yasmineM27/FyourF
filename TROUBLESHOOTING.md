# 🔧 Guide de Dépannage - FyourF

## ❌ Erreur: 404 Not Found

### Symptôme
```
http://192.168.1.18/servicephp/get_all.php : Not Found
The requested URL was not found on this server.
```

### ✅ Solution

#### Étape 1 : Vérifier que Apache est démarré

1. Ouvrez **XAMPP Control Panel** ou **WAMP**
2. Vérifiez que **Apache** est démarré (bouton vert)
3. Si non, cliquez sur "Start"

#### Étape 2 : Copier les fichiers PHP

**Option A : Script Automatique**
```cmd
1. Double-cliquez sur deploy_php.bat
2. Attendez "Fichiers copiés avec succès!"
```

**Option B : Copie Manuelle**
```
1. Ouvrez: C:\Users\yasmi\AndroidStudioProjects\FyourF\
2. Copiez le dossier: servicephp
3. Collez dans: C:\xampp\htdocs\
   (ou C:\wamp64\www\)
```

#### Étape 3 : Vérifier l'emplacement

Les fichiers doivent être ici :
```
C:\xampp\htdocs\servicephp\
├── config.php
├── get_all.php
├── add_position.php
├── delete_position.php
└── get_trajectory.php
```

#### Étape 4 : Tester

Ouvrez dans votre navigateur :
```
http://192.168.1.18/servicephp/get_all.php
```

Ou exécutez :
```cmd
test_server.bat
```

---

## ❌ Erreur: Database connection failed

### Symptôme
```json
{
  "success": false,
  "error": "Database connection failed",
  "message": "Access denied for user 'root'@'localhost'"
}
```

### ✅ Solution

#### Étape 1 : Vérifier que MySQL est démarré

1. Ouvrez **XAMPP Control Panel** ou **WAMP**
2. Vérifiez que **MySQL** est démarré (bouton vert)
3. Si non, cliquez sur "Start"

#### Étape 2 : Créer la base de données

```cmd
1. Ouvrez XAMPP Shell ou CMD
2. Tapez: mysql -u root -p
3. Appuyez sur Entrée (pas de mot de passe par défaut)
4. Tapez: source C:\Users\yasmi\AndroidStudioProjects\FyourF\database_simple.sql
```

Ou utilisez **phpMyAdmin** :
```
1. Ouvrez: http://localhost/phpmyadmin
2. Cliquez sur "Importer"
3. Sélectionnez: database_simple.sql
4. Cliquez sur "Exécuter"
```

#### Étape 3 : Vérifier config.php

Ouvrez `C:\xampp\htdocs\servicephp\config.php` et vérifiez :

```php
$user = "root";           // ✅ Correct
$mp = "";                 // ✅ Vide pour XAMPP par défaut
$database = "fyourf_db";  // ✅ Nom de la BDD
$server = "localhost";    // ✅ Correct
$port = "3306";           // ✅ Port par défaut
```

Si vous avez un mot de passe MySQL, modifiez :
```php
$mp = "votre_mot_de_passe";
```

---

## ❌ Erreur: Table 'positions' doesn't exist

### Symptôme
```json
{
  "success": false,
  "error": "Table 'fyourf_db.positions' doesn't exist"
}
```

### ✅ Solution

La base de données existe mais la table n'a pas été créée.

#### Méthode 1 : MySQL Command Line

```bash
mysql -u root -p
# Appuyez sur Entrée (pas de mot de passe)

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

#### Méthode 2 : phpMyAdmin

```
1. http://localhost/phpmyadmin
2. Cliquez sur "fyourf_db" à gauche
3. Onglet "SQL"
4. Copiez-collez le CREATE TABLE ci-dessus
5. Cliquez "Exécuter"
```

---

## ❌ Erreur: MySQL - erreur lors de chargement !

### Symptôme
Dans l'application Android :
```
Toast: "MySQL : erreur lors de chargement !"
```

### ✅ Solution

#### Étape 1 : Vérifier la connexion réseau

1. Le téléphone et le PC doivent être sur le **même réseau WiFi**
2. Désactivez le pare-feu Windows temporairement pour tester

#### Étape 2 : Vérifier l'IP dans Config.java

Ouvrez `app/src/main/java/yasminemassaoudi/grp3/fyourf/Config.java` :

```java
public static final String MYSQL_SERVER_IP = "192.168.1.18"; // ⚠️ Votre IP
```

Vérifiez votre IP actuelle :
```cmd
ipconfig
# Cherchez "Adresse IPv4"
```

Si l'IP a changé, mettez à jour Config.java et recompilez.

#### Étape 3 : Tester depuis le téléphone

Ouvrez le navigateur du téléphone et allez sur :
```
http://192.168.1.18/servicephp/get_all.php
```

Si ça ne fonctionne pas :
- Vérifiez que le téléphone est sur le même WiFi
- Désactivez le pare-feu Windows
- Vérifiez que Apache est démarré

#### Étape 4 : Vérifier les logs Android

Dans Android Studio :
```
View → Tool Windows → Logcat
Filtrer par: MySQLLocationService
```

Cherchez les erreurs :
```
E/MySQLLocationService: ✗ Erreur: Connection refused
E/MySQLLocationService: ✗ Erreur: 404 Not Found
```

---

## ❌ Le tracking ne démarre pas

### Symptôme
Cliquer sur "Démarrer" ne fait rien, ou l'app crash.

### ✅ Solution

#### Étape 1 : Vérifier les permissions

```
Paramètres → Applications → FyourF → Autorisations
```

Accordez :
- ✅ Localisation : "Toujours autoriser"
- ✅ Notifications : Activé

#### Étape 2 : Activer le GPS

```
Paramètres → Localisation → Activé
Mode : Haute précision
```

#### Étape 3 : Désactiver l'optimisation de batterie

```
Paramètres → Batterie → Optimisation de la batterie
Cherchez "FyourF" → Désactiver
```

#### Étape 4 : Vérifier les logs

```
Logcat → Filtrer par: TrackingService
```

Cherchez :
```
E/TrackingService: Permission denied
E/TrackingService: Location provider not available
```

---

## ❌ Les positions ne s'affichent pas sur la carte

### Symptôme
La carte est vide, pas de marqueurs.

### ✅ Solution

#### Étape 1 : Vérifier la clé Google Maps API

Ouvrez `app/src/main/AndroidManifest.xml` :

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="VOTRE_CLE_API_ICI" />
```

Si la clé est manquante ou invalide :
1. Allez sur : https://console.cloud.google.com/
2. Créez un projet
3. Activez "Maps SDK for Android"
4. Créez une clé API
5. Ajoutez-la dans AndroidManifest.xml

#### Étape 2 : Vérifier que les positions existent

```sql
mysql -u root -p
USE fyourf_db;
SELECT * FROM positions;
```

Si vide, ajoutez des données de test :
```sql
INSERT INTO positions (longitude, latitude, numero, pseudo, timestamp) VALUES
(10.1815, 36.8065, '+21612345678', 'Test', UNIX_TIMESTAMP() * 1000);
```

---

## ❌ Erreur de compilation Android

### Symptôme
```
Error: Cannot resolve symbol 'Position'
Error: Cannot find symbol method addPosition()
```

### ✅ Solution

#### Étape 1 : Sync Gradle

```
File → Sync Project with Gradle Files
```

#### Étape 2 : Clean & Rebuild

```
Build → Clean Project
Build → Rebuild Project
```

#### Étape 3 : Invalidate Caches

```
File → Invalidate Caches / Restart...
Invalidate and Restart
```

---

## 🔍 Commandes de Diagnostic

### Vérifier Apache
```cmd
netstat -ano | findstr :80
# Si rien, Apache n'est pas démarré
```

### Vérifier MySQL
```cmd
netstat -ano | findstr :3306
# Si rien, MySQL n'est pas démarré
```

### Tester la connexion
```cmd
curl http://192.168.1.18/servicephp/get_all.php
```

### Voir les logs PHP
```
C:\xampp\htdocs\servicephp\php_errors.log
```

### Voir les logs Apache
```
C:\xampp\apache\logs\error.log
```

---

## 📞 Checklist Complète

### Serveur
- [ ] Apache démarré (port 80)
- [ ] MySQL démarré (port 3306)
- [ ] Fichiers PHP dans htdocs/servicephp/
- [ ] Base de données fyourf_db créée
- [ ] Table positions créée
- [ ] config.php configuré
- [ ] URL testée dans navigateur

### Android
- [ ] Config.java avec bonne IP
- [ ] Permissions accordées
- [ ] GPS activé
- [ ] Même réseau WiFi
- [ ] Pare-feu désactivé (test)
- [ ] App compilée et installée

### Test Final
- [ ] http://192.168.1.18/servicephp/get_all.php fonctionne
- [ ] L'app peut charger l'historique
- [ ] Le tracking démarre
- [ ] Les positions s'enregistrent
- [ ] La carte affiche les marqueurs

---

## 🆘 Besoin d'Aide ?

Si le problème persiste :

1. **Vérifiez les logs** :
   - Logcat Android
   - php_errors.log
   - Apache error.log

2. **Testez étape par étape** :
   - Serveur seul (navigateur)
   - Téléphone seul (navigateur mobile)
   - Application Android

3. **Informations à fournir** :
   - Message d'erreur exact
   - Logs Logcat
   - Version de XAMPP/WAMP
   - Version d'Android

---

**Bonne chance ! 🚀**

