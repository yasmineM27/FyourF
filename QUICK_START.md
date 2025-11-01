# 🚀 Guide de Démarrage Rapide - FyourF

## ⚡ Installation en 5 Minutes

### 1️⃣ Base de Données MySQL (2 min)

```bash
# Ouvrir MySQL
mysql -u root -p

# Exécuter le script
source database_simple.sql

# Ou copier-coller directement:
CREATE DATABASE IF NOT EXISTS fyourf_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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

### 2️⃣ Configuration PHP (1 min)

**Modifier `servicephp/config.php` :**

```php
<?php
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');  // Votre mot de passe MySQL
define('DB_NAME', 'fyourf_db');
?>
```

**Copier les fichiers PHP :**

```bash
# XAMPP (Windows)
Copier le dossier servicephp/ vers: C:\xampp\htdocs\

# WAMP (Windows)
Copier le dossier servicephp/ vers: C:\wamp64\www\

# Linux/Mac
sudo cp -r servicephp/ /var/www/html/
```

### 3️⃣ Configuration Android (1 min)

**Obtenir votre IP locale :**

```cmd
# Windows
ipconfig
# Chercher "Adresse IPv4" (ex: 192.168.1.100)

# Linux/Mac
ifconfig
# ou
ip addr show
```

**Modifier `app/src/main/java/yasminemassaoudi/grp3/fyourf/Config.java` :**

```java
public class Config {
    public static final boolean USE_MYSQL = true;
    public static final String MYSQL_SERVER_IP = "192.168.1.100"; // ⚠️ VOTRE IP ICI
    public static final String MYSQL_SERVER_PORT = "80";
    public static final String MYSQL_SERVICE_FOLDER = "servicephp";
}
```

### 4️⃣ Tester les Endpoints (1 min)

**Ouvrir dans un navigateur :**

```
http://192.168.1.100/servicephp/get_all.php
```

**Vous devriez voir :**

```json
{
  "success": true,
  "positions": [...]
}
```

### 5️⃣ Compiler et Installer (30 sec)

1. Ouvrir le projet dans Android Studio
2. Cliquer sur "Run" (▶️)
3. Sélectionner votre appareil/émulateur
4. Attendre la compilation et l'installation

---

## 📱 Utilisation

### Démarrer un Tracking

1. **Ouvrir l'app** → Cliquer sur le bouton vert 📍 (en bas à droite)
2. **Entrer votre numéro** : +21612345678
3. **Configurer l'intervalle** : 30 (secondes)
4. **Cliquer "Démarrer"** ▶️
5. **Voir le trajet** en temps réel sur la carte

### Voir l'Historique

1. **Onglet "History"** (en bas)
2. **Cliquer "Refresh"** pour charger depuis MySQL
3. **Trier** par date ou numéro

---

## 🔧 Dépannage Express

### ❌ Erreur "Connection failed"

**Vérifier :**
1. Apache/MySQL sont démarrés (XAMPP/WAMP)
2. L'IP dans Config.java est correcte
3. Le téléphone et PC sont sur le même réseau WiFi
4. Tester l'URL dans le navigateur du téléphone

### ❌ Le tracking ne démarre pas

**Vérifier :**
1. GPS activé sur le téléphone
2. Permissions accordées (Localisation "Toujours autoriser")
3. Optimisation batterie désactivée pour l'app

### ❌ Pas de positions dans l'historique

**Vérifier :**
1. Config.USE_MYSQL = true
2. Les endpoints PHP fonctionnent
3. Cliquer sur "Refresh" dans l'onglet History

---

## 📊 Structure des Fichiers

```
FyourF/
├── servicephp/              ← À copier dans htdocs/www
│   ├── config.php          ← Configurer ici
│   ├── get_all.php
│   ├── add_position.php
│   ├── delete_position.php
│   └── get_trajectory.php
│
├── app/src/main/java/.../
│   ├── Config.java         ← Configurer ici (IP)
│   ├── TrackingActivity.java
│   ├── TrackingService.java
│   └── ...
│
├── database_simple.sql     ← Script MySQL simple
└── QUICK_START.md         ← Ce fichier
```

---

## ✅ Checklist Rapide

- [ ] MySQL installé et démarré
- [ ] Base de données `fyourf_db` créée
- [ ] Table `positions` créée
- [ ] Fichiers PHP copiés dans htdocs/www
- [ ] config.php configuré (mot de passe MySQL)
- [ ] Apache démarré
- [ ] IP locale obtenue (ipconfig)
- [ ] Config.java configuré (IP)
- [ ] Endpoint testé dans navigateur
- [ ] App compilée et installée
- [ ] Permissions accordées
- [ ] GPS activé
- [ ] Tracking testé ✅

---

## 🎯 Commandes Utiles

### Démarrer XAMPP (Windows)
```cmd
# Démarrer Apache et MySQL
C:\xampp\xampp-control.exe
```

### Tester MySQL
```bash
mysql -u root -p
USE fyourf_db;
SELECT * FROM positions;
```

### Tester les Endpoints
```bash
# Get all
curl http://192.168.1.100/servicephp/get_all.php

# Add position
curl -X POST http://192.168.1.100/servicephp/add_position.php \
  -d "longitude=10.1815&latitude=36.8065&numero=+21612345678&pseudo=Test&timestamp=1234567890000"
```

### Voir les Logs Android
```bash
# Dans Android Studio
View → Tool Windows → Logcat
# Filtrer par "TrackingService" ou "MySQLLocationService"
```

---

## 📞 Support Rapide

### Erreur de connexion MySQL
```
✅ Vérifier que MySQL est démarré
✅ Vérifier config.php (user, password)
✅ Tester: mysql -u root -p
```

### Erreur de connexion HTTP
```
✅ Vérifier que Apache est démarré
✅ Vérifier l'IP dans Config.java
✅ Tester l'URL dans le navigateur
✅ Même réseau WiFi (PC et téléphone)
```

### Permissions Android
```
✅ Paramètres → Apps → FyourF → Permissions
✅ Localisation: "Toujours autoriser"
✅ Notifications: Activé
✅ Batterie: Optimisation désactivée
```

---

## 🎉 C'est Prêt !

Votre application de tracking GPS est maintenant fonctionnelle !

**Prochaines étapes :**
- Tester le tracking pendant 5 minutes
- Vérifier que les positions apparaissent dans MySQL
- Consulter l'historique
- Partager avec vos amis ! 🚀

---

## 📚 Documentation Complète

Pour plus de détails, consultez :
- **README_SETUP.md** - Guide complet d'installation
- **IMPLEMENTATION_SUMMARY.md** - Résumé technique
- **database_setup.sql** - Script SQL complet avec procédures

---

**Bon tracking ! 📍🗺️**

