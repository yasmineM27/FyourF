# 📱 GUIDE DE MIGRATION MYSQL - FyourF Application

## 🎯 OBJECTIF
Migrer l'application FyourF de SQLite local vers une base de données MySQL distante avec serveur PHP.

---

## 📦 ÉTAPE 1: CONFIGURATION DE LA BASE DE DONNÉES MYSQL

### 1.1 Créer la base de données
```sql
CREATE DATABASE locationdatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE locationdatabase;
```

### 1.2 Créer la table Position
```sql
CREATE TABLE Position (
    idposition INT AUTO_INCREMENT PRIMARY KEY,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    numero VARCHAR(30) NOT NULL,
    pseudo VARCHAR(30),
    timestamp BIGINT,
    INDEX idx_numero (numero),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 1.3 Vérifier la création
```sql
DESCRIBE Position;
SELECT * FROM Position;
```

---

## 📂 ÉTAPE 2: CONFIGURATION DU SERVEUR PHP

### 2.1 Créer le dossier servicephp
**Emplacement selon votre SGBD:**
- **XAMPP (Windows):** `C:\xampp\htdocs\servicephp\`
- **WAMP (Windows):** `C:\wamp64\www\servicephp\`
- **LAMP (Linux):** `/var/www/html/servicephp/`
- **MAMP (Mac):** `/Applications/MAMP/htdocs/servicephp/`

### 2.2 Créer config.php
Fichier: `servicephp/config.php`
```php
<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

// Configuration de la connexion MySQL
$user = "root";
$mp = "";  // Mot de passe (vide par défaut pour XAMPP/WAMP)
$database = "locationdatabase";
$server = "localhost";
$port = "3306";

// Connexion à la base de données
try {
    $conn = new mysqli($server, $user, $mp, $database, $port);
    
    if ($conn->connect_error) {
        die(json_encode([
            "success" => false,
            "message" => "Erreur de connexion: " . $conn->connect_error
        ]));
    }
    
    $conn->set_charset("utf8mb4");
    
} catch (Exception $e) {
    die(json_encode([
        "success" => false,
        "message" => "Exception: " . $e->getMessage()
    ]));
}
?>
```

### 2.3 Créer get_all.php
Fichier: `servicephp/get_all.php`
```php
<?php
require_once 'config.php';

try {
    $sql = "SELECT * FROM Position ORDER BY timestamp DESC";
    $result = $conn->query($sql);
    
    $positions = array();
    
    if ($result->num_rows > 0) {
        while($row = $result->fetch_assoc()) {
            $positions[] = array(
                "idposition" => $row["idposition"],
                "longitude" => floatval($row["longitude"]),
                "latitude" => floatval($row["latitude"]),
                "numero" => $row["numero"],
                "pseudo" => $row["pseudo"],
                "timestamp" => $row["timestamp"]
            );
        }
    }
    
    echo json_encode([
        "success" => true,
        "count" => count($positions),
        "data" => $positions
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}

$conn->close();
?>
```

### 2.4 Créer add_position.php
Fichier: `servicephp/add_position.php`
```php
<?php
require_once 'config.php';

// Récupérer les paramètres (GET ou POST)
$longitude = isset($_POST['longitude']) ? $_POST['longitude'] : (isset($_GET['longitude']) ? $_GET['longitude'] : null);
$latitude = isset($_POST['latitude']) ? $_POST['latitude'] : (isset($_GET['latitude']) ? $_GET['latitude'] : null);
$numero = isset($_POST['numero']) ? $_POST['numero'] : (isset($_GET['numero']) ? $_GET['numero'] : null);
$pseudo = isset($_POST['pseudo']) ? $_POST['pseudo'] : (isset($_GET['pseudo']) ? $_GET['pseudo'] : '');
$timestamp = isset($_POST['timestamp']) ? $_POST['timestamp'] : (isset($_GET['timestamp']) ? $_GET['timestamp'] : time() * 1000);

// Validation
if ($longitude === null || $latitude === null || $numero === null) {
    echo json_encode([
        "success" => false,
        "message" => "Paramètres manquants: longitude, latitude, numero sont requis"
    ]);
    exit;
}

try {
    // Vérifier si la position existe déjà pour ce numéro
    $check_sql = "SELECT idposition FROM Position WHERE numero = ?";
    $stmt_check = $conn->prepare($check_sql);
    $stmt_check->bind_param("s", $numero);
    $stmt_check->execute();
    $result = $stmt_check->get_result();
    
    if ($result->num_rows > 0) {
        // UPDATE
        $sql = "UPDATE Position SET longitude = ?, latitude = ?, pseudo = ?, timestamp = ? WHERE numero = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ddsss", $longitude, $latitude, $pseudo, $timestamp, $numero);
        
        if ($stmt->execute()) {
            echo json_encode([
                "success" => true,
                "message" => "Position mise à jour avec succès",
                "action" => "update",
                "numero" => $numero
            ]);
        } else {
            echo json_encode([
                "success" => false,
                "message" => "Erreur lors de la mise à jour: " . $stmt->error
            ]);
        }
    } else {
        // INSERT
        $sql = "INSERT INTO Position (longitude, latitude, numero, pseudo, timestamp) VALUES (?, ?, ?, ?, ?)";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ddsss", $longitude, $latitude, $numero, $pseudo, $timestamp);
        
        if ($stmt->execute()) {
            echo json_encode([
                "success" => true,
                "message" => "Position ajoutée avec succès",
                "action" => "insert",
                "id" => $conn->insert_id,
                "numero" => $numero
            ]);
        } else {
            echo json_encode([
                "success" => false,
                "message" => "Erreur lors de l'insertion: " . $stmt->error
            ]);
        }
    }
    
    $stmt->close();
    $stmt_check->close();
    
} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Exception: " . $e->getMessage()
    ]);
}

$conn->close();
?>
```

---

## 🧪 ÉTAPE 3: TESTS

### 3.1 Tester depuis Windows (navigateur local)
1. Démarrer XAMPP/WAMP
2. Ouvrir le navigateur
3. Tester get_all.php:
   ```
   http://localhost/servicephp/get_all.php
   ```
4. Tester add_position.php:
   ```
   http://localhost/servicephp/add_position.php?longitude=10.123&latitude=36.456&numero=+21612345678&pseudo=Test
   ```

### 3.2 Trouver votre adresse IPv4
**Windows:**
```cmd
ipconfig
```
Cherchez "Adresse IPv4" (ex: 192.168.1.100)

**Linux/Mac:**
```bash
ifconfig
```

### 3.3 Configurer le serveur pour accepter les connexions externes

#### Pour XAMPP:
1. Ouvrir `C:\xampp\apache\conf\httpd.conf`
2. Chercher `Listen 80` et vérifier qu'il n'y a pas `127.0.0.1:80`
3. Redémarrer Apache

#### Pour WAMP:
1. Clic gauche sur l'icône WAMP
2. Aller dans "Apache" → "httpd.conf"
3. Chercher `Listen 80` et modifier si nécessaire
4. Redémarrer tous les services

### 3.4 Configurer le pare-feu Windows
1. Ouvrir "Pare-feu Windows Defender"
2. Cliquer sur "Paramètres avancés"
3. Règles de trafic entrant → Nouvelle règle
4. Type: Port → TCP → Port 80
5. Autoriser la connexion
6. Appliquer à tous les profils

### 3.5 Tester depuis le smartphone
1. Connecter le smartphone au même réseau WiFi que votre PC
2. Ouvrir le navigateur du smartphone
3. Tester avec votre IPv4:
   ```
   http://192.168.1.100/servicephp/get_all.php
   http://192.168.1.100/servicephp/add_position.php?longitude=10.123&latitude=36.456&numero=+21612345678
   ```

---

## 📱 ÉTAPE 4: MODIFICATION DE L'APPLICATION ANDROID

Les fichiers suivants ont été créés/modifiés:

### 4.1 Fichiers créés:
- ✅ `MySQLConfig.java` - Configuration MySQL
- ✅ `Download.java` - AsyncTask pour télécharger les positions
- ✅ `Loading.java` - Thread pour charger les données
- ✅ `MySQLLocationService.java` - Service de gestion MySQL

### 4.2 Fichiers modifiés:
- ✅ `Config.java` - Ajout configuration MySQL
- ✅ `HomeFragment.java` - Intégration MySQL
- ✅ `SmsReceiver.java` - Sauvegarde dans MySQL

---

## ⚙️ CONFIGURATION FINALE

### Dans Config.java, modifier:
```java
public static String MYSQL_SERVER_IP = "192.168.1.100"; // VOTRE IPv4
```

---

## ✅ CHECKLIST DE VÉRIFICATION

- [ ] Base de données `locationdatabase` créée
- [ ] Table `Position` créée avec les bons champs
- [ ] Dossier `servicephp` créé dans www/htdocs
- [ ] Fichier `config.php` créé et configuré
- [ ] Fichier `get_all.php` créé
- [ ] Fichier `add_position.php` créé
- [ ] Tests locaux réussis (localhost)
- [ ] IPv4 identifiée
- [ ] Pare-feu configuré
- [ ] Tests depuis smartphone réussis
- [ ] Application Android modifiée
- [ ] IP serveur configurée dans Config.java

---

## 🐛 DÉPANNAGE

### Erreur "Connection refused"
- Vérifier que Apache est démarré
- Vérifier le pare-feu
- Vérifier que le smartphone est sur le même réseau WiFi

### Erreur "Access denied for user"
- Vérifier les identifiants dans config.php
- Vérifier que MySQL est démarré

### Erreur "Table doesn't exist"
- Vérifier que la base de données est sélectionnée
- Re-créer la table Position

### Pas de données retournées
- Vérifier que des données existent: `SELECT * FROM Position;`
- Vérifier les logs PHP dans XAMPP

---

## 📞 SUPPORT
Pour toute question, vérifier les logs:
- **Android:** Logcat dans Android Studio
- **PHP:** `C:\xampp\apache\logs\error.log`
- **MySQL:** `C:\xampp\mysql\data\*.err`

