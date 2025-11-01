# 🧪 Test MySQL depuis le Smartphone

## 📱 Méthode 1 : Test Navigateur (Le Plus Simple)

### Sur votre smartphone, ouvrez Chrome et testez ces URLs :

#### Test 1 : Vérifier la connexion au serveur
```
http://192.168.1.18/
```
**Résultat attendu :** Page d'accueil XAMPP ou liste de dossiers

---

#### Test 2 : Vérifier le dossier servicephp
```
http://192.168.1.18/servicephp/
```
**Résultat attendu :** Liste des fichiers PHP ou erreur 403 (normal)

---

#### Test 3 : Tester get_all.php
```
http://192.168.1.18/servicephp/get_all.php
```
**Résultat attendu :**
```json
{
  "success": true,
  "count": 7,
  "data": [
    {
      "id": "1",
      "pseudo": "+1234567890",
      "numero": "+1234567890",
      "latitude": "36.8065",
      "longitude": "10.1815",
      "timestamp": "2025-11-01 12:00:00"
    },
    ...
  ],
  "message": "Positions récupérées avec succès",
  "timestamp": 1730467935
}
```

---

#### Test 4 : Tester config.php
```
http://192.168.1.18/servicephp/config.php
```
**Résultat attendu :**
```json
{
  "status": "success",
  "message": "MySQL connection successful",
  "server": "localhost",
  "database": "fyourf_db"
}
```

---

## 🔥 Si vous voyez une erreur

### Erreur : "Ce site est inaccessible"
**Cause :** Le smartphone ne peut pas atteindre le PC
**Solutions :**
1. Vérifiez que PC et smartphone sont sur le **même WiFi**
2. Vérifiez l'IP du PC :
   - Ouvrez `check_ip.bat` sur le PC
   - Notez l'IP (ex: 192.168.1.18)
   - Utilisez cette IP exacte dans l'URL

---

### Erreur : "404 Not Found"
**Cause :** Les fichiers PHP ne sont pas dans `C:\xampp\htdocs\servicephp\`
**Solution :**
```
Double-cliquez sur : fix_and_deploy.bat
```

---

### Erreur : "Connection refused" ou "ERR_CONNECTION_REFUSED"
**Cause :** Apache n'est pas démarré
**Solution :**
1. Ouvrez XAMPP Control Panel
2. Cliquez sur "Start" pour Apache
3. Vérifiez que le bouton devient vert

---

### Erreur JSON : `{"success":false,"error":"La table 'fyourf_db.positions' n'existe pas"}`
**Cause :** La base de données n'est pas créée
**Solution :**
```
Double-cliquez sur : setup_complet.bat
```

---

### Erreur : "Access denied for user 'root'@'localhost'"
**Cause :** Mot de passe MySQL incorrect
**Solution :**
1. Ouvrez `servicephp/config.php`
2. Vérifiez :
   ```php
   $servername = "localhost";
   $username = "root";
   $password = "";  // Vide par défaut sur XAMPP
   $dbname = "fyourf_db";
   ```

---

## 🔧 Pare-feu Windows

### Si le navigateur du smartphone ne peut pas se connecter :

**Étape 1 : Autoriser Apache dans le pare-feu**
```
1. Panneau de configuration → Système et sécurité → Pare-feu Windows Defender
2. Paramètres avancés
3. Règles de trafic entrant
4. Nouvelle règle...
5. Type : Port
6. TCP, Port 80
7. Autoriser la connexion
8. Cocher : Domaine, Privé, Public
9. Nom : Apache HTTP Server
10. Terminer
```

**Étape 2 : Vérifier que le port 80 est ouvert**
```
Sur le PC, ouvrez CMD et tapez :
netstat -an | findstr :80

Résultat attendu :
TCP    0.0.0.0:80    0.0.0.0:0    LISTENING
```

---

## 📊 Test depuis l'Application Android

### Méthode 2 : Vérifier les logs Logcat

**Dans Android Studio :**
```
1. View → Tool Windows → Logcat
2. Filtrer par : MySQLLocationService
3. Dans l'app, allez dans History
4. Cliquez sur "Refresh"
5. Observez les logs
```

### Logs attendus (SUCCÈS) :
```
D/MySQLLocationService: Chargement de toutes les positions...
D/MySQLLocationService: URL: http://192.168.1.18/servicephp/get_all.php
D/MySQLLocationService: Réponse reçue: {"success":true,"count":7,...}
D/Loading: ✓ Positions chargées: 7
D/HistoryFragment: Positions chargées depuis MySQL
```

### Logs d'erreur possibles :

#### Erreur 1 : Connection refused
```
E/MySQLLocationService: ✗ Erreur: Connection refused
```
**Cause :** Apache non démarré ou pare-feu bloque
**Solution :** Démarrez Apache + Configurez pare-feu (voir ci-dessus)

---

#### Erreur 2 : 404 Not Found
```
E/MySQLLocationService: ✗ Erreur: 404 Not Found
```
**Cause :** Fichiers PHP pas dans htdocs
**Solution :** Exécutez `fix_and_deploy.bat`

---

#### Erreur 3 : UnknownHostException
```
E/MySQLLocationService: ✗ Erreur: Unable to resolve host "192.168.1.18"
```
**Cause :** Smartphone pas sur le même réseau WiFi
**Solution :** Connectez le smartphone au même WiFi que le PC

---

#### Erreur 4 : SocketTimeoutException
```
E/MySQLLocationService: ✗ Erreur: timeout
```
**Cause :** Pare-feu bloque ou serveur trop lent
**Solution :** 
1. Configurez le pare-feu
2. Augmentez le timeout dans MySQLConfig.java

---

#### Erreur 5 : JSONException
```
E/MySQLLocationService: ✗ Erreur parsing JSON
```
**Cause :** PHP retourne du HTML au lieu de JSON (erreur PHP)
**Solution :** Testez l'URL dans le navigateur du PC pour voir l'erreur exacte

---

## 🎯 Checklist Complète

### Sur le PC :
- [ ] XAMPP installé
- [ ] Apache démarré (bouton vert dans XAMPP)
- [ ] MySQL démarré (bouton vert dans XAMPP)
- [ ] Fichiers PHP dans `C:\xampp\htdocs\servicephp\`
- [ ] Base de données `fyourf_db` créée
- [ ] Table `positions` créée avec données de test
- [ ] Pare-feu autorise le port 80
- [ ] PC et smartphone sur le même WiFi

### Test Navigateur PC :
- [ ] `http://localhost/servicephp/get_all.php` → JSON avec success:true
- [ ] `http://192.168.1.18/servicephp/get_all.php` → Même résultat

### Test Navigateur Smartphone :
- [ ] `http://192.168.1.18/` → Page XAMPP
- [ ] `http://192.168.1.18/servicephp/get_all.php` → JSON avec success:true

### Dans l'Application :
- [ ] Config.java → IP = 192.168.1.18
- [ ] MySQLConfig.java → IP = 192.168.1.18
- [ ] Application recompilée (Build → Rebuild Project)
- [ ] History → Refresh → Logs montrent succès
- [ ] Dashboard → Send Request → Sauvegarde dans MySQL

---

## 🆘 Script de Test Automatique

Créez ce fichier pour tester automatiquement :

**test_mysql_connection.bat**
```batch
@echo off
echo ========================================
echo TEST CONNEXION MYSQL - FyourF
echo ========================================
echo.

echo 1. Verification Apache...
tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] Apache est demarre
) else (
    echo [ERREUR] Apache n'est pas demarre !
    echo Solution : Ouvrez XAMPP et cliquez sur Start pour Apache
    pause
    exit
)

echo.
echo 2. Verification MySQL...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] MySQL est demarre
) else (
    echo [ERREUR] MySQL n'est pas demarre !
    echo Solution : Ouvrez XAMPP et cliquez sur Start pour MySQL
    pause
    exit
)

echo.
echo 3. Verification fichiers PHP...
if exist "C:\xampp\htdocs\servicephp\get_all.php" (
    echo [OK] get_all.php existe
) else (
    echo [ERREUR] get_all.php n'existe pas !
    echo Solution : Executez fix_and_deploy.bat
    pause
    exit
)

echo.
echo 4. Test connexion HTTP...
curl -s http://localhost/servicephp/get_all.php > test_response.json
if %ERRORLEVEL% EQU 0 (
    echo [OK] Connexion HTTP reussie
    echo.
    echo Reponse :
    type test_response.json
    del test_response.json
) else (
    echo [ERREUR] Connexion HTTP echouee !
)

echo.
echo 5. Votre adresse IP :
ipconfig | findstr /i "IPv4"

echo.
echo ========================================
echo TESTEZ DEPUIS VOTRE SMARTPHONE :
echo http://[VOTRE_IP]/servicephp/get_all.php
echo ========================================
pause
```

---

## 📱 Test Final

### Commande curl depuis le smartphone (si vous avez Termux) :
```bash
curl http://192.168.1.18/servicephp/get_all.php
```

### Ou utilisez une app Android :
- **HTTP Request Tester** (Google Play)
- **REST API Client** (Google Play)

**URL à tester :**
```
http://192.168.1.18/servicephp/get_all.php
```

**Méthode :** GET

**Résultat attendu :** JSON avec `"success":true`

---

**Testez d'abord dans le navigateur du smartphone, puis dites-moi ce que vous voyez ! 📱**

