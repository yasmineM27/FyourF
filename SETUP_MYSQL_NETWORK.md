# 🔧 Configuration MySQL pour Accès Réseau

## ⚠️ Problème Détecté

MySQL n'est pas accessible sur `192.168.178.115:3306` depuis le réseau.

**Causes possibles:**
1. MySQL n'est pas en cours d'exécution
2. MySQL écoute uniquement sur `localhost` (127.0.0.1)
3. Le pare-feu bloque le port 3306
4. MySQL n'est pas configuré pour accepter les connexions réseau

---

## ✅ Solution: Configurer MySQL pour Accès Réseau

### Étape 1: Vérifier que MySQL est en cours d'exécution

#### Sur Windows (XAMPP)
```bash
# Ouvrir XAMPP Control Panel
# Vérifier que MySQL est "Running" (vert)
# Si non, cliquer sur "Start"
```

#### Sur Windows (WAMP)
```bash
# Ouvrir WAMP Control Panel
# Vérifier que MySQL est "Running" (vert)
# Si non, cliquer sur "Start"
```

#### Sur Linux
```bash
sudo systemctl status mysql
# ou
sudo service mysql status

# Si arrêté, démarrer:
sudo systemctl start mysql
```

---

### Étape 2: Configurer MySQL pour Écouter sur le Réseau

#### Sur Windows (XAMPP)

1. **Localiser le fichier de configuration:**
   ```
   C:\xampp\mysql\bin\my.ini
   ```

2. **Ouvrir le fichier avec un éditeur de texte**

3. **Trouver la ligne:**
   ```ini
   bind-address = 127.0.0.1
   ```

4. **Remplacer par:**
   ```ini
   bind-address = 0.0.0.0
   ```
   
   Ou spécifier l'IP:
   ```ini
   bind-address = 192.168.178.115
   ```

5. **Sauvegarder le fichier**

6. **Redémarrer MySQL:**
   - Ouvrir XAMPP Control Panel
   - Cliquer sur "Stop" pour MySQL
   - Attendre 2 secondes
   - Cliquer sur "Start"

---

#### Sur Windows (WAMP)

1. **Localiser le fichier de configuration:**
   ```
   C:\wamp64\bin\mysql\mysql8.0.x\my.ini
   ```

2. **Ouvrir le fichier avec un éditeur de texte**

3. **Trouver la ligne:**
   ```ini
   bind-address = 127.0.0.1
   ```

4. **Remplacer par:**
   ```ini
   bind-address = 0.0.0.0
   ```

5. **Sauvegarder le fichier**

6. **Redémarrer MySQL:**
   - Ouvrir WAMP Control Panel
   - Cliquer sur "Stop" pour MySQL
   - Attendre 2 secondes
   - Cliquer sur "Start"

---

#### Sur Linux

1. **Localiser le fichier de configuration:**
   ```bash
   sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
   ```

2. **Trouver la ligne:**
   ```ini
   bind-address = 127.0.0.1
   ```

3. **Remplacer par:**
   ```ini
   bind-address = 0.0.0.0
   ```

4. **Sauvegarder (Ctrl+O, Entrée, Ctrl+X)**

5. **Redémarrer MySQL:**
   ```bash
   sudo systemctl restart mysql
   ```

---

### Étape 3: Vérifier la Configuration

```bash
# Vérifier que MySQL écoute sur le port 3306
netstat -an | findstr 3306  # Windows
netstat -an | grep 3306     # Linux/Mac

# Résultat attendu:
# LISTENING 0.0.0.0:3306
```

---

### Étape 4: Tester la Connexion

#### Test 1: Depuis la même machine
```bash
mysql -h 192.168.178.115 -u root -p fyourf_db
# Laisser le mot de passe vide (appuyer sur Entrée)
```

#### Test 2: Depuis une autre machine du réseau
```bash
mysql -h 192.168.178.115 -u root -p fyourf_db
```

#### Test 3: Via PowerShell
```powershell
# Exécuter le script de vérification
.\verify_mysql_data.ps1
```

---

## 🔐 Sécurité: Créer un Utilisateur Réseau

Pour plus de sécurité, créer un utilisateur MySQL spécifique pour les connexions réseau:

```sql
-- Se connecter à MySQL en tant que root
mysql -u root

-- Créer un nouvel utilisateur
CREATE USER 'fyourf_user'@'192.168.178.%' IDENTIFIED BY 'password123';

-- Donner les permissions
GRANT ALL PRIVILEGES ON fyourf_db.* TO 'fyourf_user'@'192.168.178.%';

-- Appliquer les changements
FLUSH PRIVILEGES;

-- Vérifier
SELECT user, host FROM mysql.user WHERE user='fyourf_user';
```

---

## 🚀 Après Configuration

Une fois MySQL configuré:

1. **Mettre à jour la configuration Android:**
   ```java
   // app/src/main/java/yasminemassaoudi/grp3/fyourf/MySQLConfig.java
   public static final String MYSQL_SERVER_IP = "192.168.178.115";
   ```

2. **Mettre à jour la configuration PHP:**
   ```php
   // servicephp/config.php
   $server = "192.168.178.115";  // Au lieu de "localhost"
   ```

3. **Copier les fichiers PHP:**
   ```bash
   # Copier le dossier servicephp vers:
   # XAMPP: C:\xampp\htdocs\servicephp\
   # WAMP: C:\wamp64\www\servicephp\
   ```

4. **Tester l'accès web:**
   ```
   http://192.168.178.115/servicephp/verify_data.php
   ```

---

## 📋 Checklist

- [ ] MySQL est en cours d'exécution
- [ ] `bind-address` est configuré à `0.0.0.0` ou `192.168.178.115`
- [ ] MySQL a été redémarré après la modification
- [ ] Test de connexion réussi: `mysql -h 192.168.178.115 -u root`
- [ ] Fichiers PHP copiés dans le répertoire web
- [ ] Configuration Android mise à jour
- [ ] Configuration PHP mise à jour

---

## 🆘 Dépannage

### Erreur: "Can't connect to MySQL server"
```bash
# Vérifier que MySQL écoute sur le port 3306
netstat -an | findstr 3306

# Vérifier la configuration bind-address
grep bind-address /etc/mysql/mysql.conf.d/mysqld.cnf
```

### Erreur: "Access denied for user 'root'"
```bash
# Vérifier le mot de passe
mysql -h 192.168.178.115 -u root -p

# Réinitialiser le mot de passe si nécessaire
```

### Erreur: "Connection refused"
```bash
# Vérifier que le pare-feu n'est pas bloquant
# Windows: Ouvrir Windows Defender Firewall
# Ajouter une règle pour MySQL port 3306
```

---

**Créé le**: 2025-11-20
**Dernière mise à jour**: 2025-11-20

