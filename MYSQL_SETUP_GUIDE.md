# 🗄️ Guide d'Installation MySQL - Multi-Utilisateurs

**Date**: 2025-11-07
**Status**: ✅ PRÊT À UTILISER

---

## 📋 Prérequis

- ✅ MySQL Server installé
- ✅ PhpMyAdmin ou MySQL CLI
- ✅ Base de données `fyourf_db` créée
- ✅ Accès root ou utilisateur avec permissions

---

## 🚀 Installation Rapide

### Étape 1: Ouvrir PhpMyAdmin

```
URL: http://192.168.56.1/phpmyadmin/
Utilisateur: root
Mot de passe: (vide par défaut)
```

### Étape 2: Sélectionner la Base de Données

```
Cliquer sur: fyourf_db
```

### Étape 3: Importer le Script SQL

1. Aller à l'onglet **SQL**
2. Copier le contenu de `database_multi_users_setup.sql`
3. Coller dans la zone de texte
4. Cliquer sur **Exécuter**

---

## 🔧 Installation via MySQL CLI

### Étape 1: Ouvrir Terminal

```bash
# Windows
mysql -h 192.168.56.1 -u root -p

# Linux/Mac
mysql -h localhost -u root -p
```

### Étape 2: Exécuter le Script

```bash
USE fyourf_db;
SOURCE /chemin/vers/database_multi_users_setup.sql;
```

---

## ✅ Vérifier l'Installation

### Voir les Tables

```sql
USE fyourf_db;
SHOW TABLES;
```

**Résultat attendu:**
```
geofences
group_members
meeting_history
notifications
positions
trajectories
user_connections
user_distances
user_groups
users
```

### Voir les Utilisateurs de Test

```sql
SELECT * FROM users;
```

**Résultat attendu:**
```
id | numero          | pseudo | email              | phone          | status
1  | +21612345678    | User1  | user1@example.com  | +21612345678   | online
2  | +21687654321    | User2  | user2@example.com  | +21687654321   | online
3  | +21698765432    | User3  | user3@example.com  | +21698765432   | offline
```

### Voir les Connexions

```sql
SELECT * FROM user_connections;
```

**Résultat attendu:**
```
id | user1_id | user2_id | status    | created_at
1  | 1        | 2        | connected | 2025-11-07 ...
2  | 1        | 3        | pending   | 2025-11-07 ...
```

---

## 📊 Structure des Tables

### Table: users
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) UNIQUE NOT NULL,
    pseudo VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    status ENUM('online', 'offline', 'away') DEFAULT 'offline',
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Table: user_connections
```sql
CREATE TABLE user_connections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    status ENUM('pending', 'connected', 'blocked') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_connection (user1_id, user2_id)
);
```

### Table: user_distances
```sql
CREATE TABLE user_distances (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    distance_meters DOUBLE NOT NULL,
    time_remaining_seconds INT,
    direction_degrees DOUBLE,
    user1_lat DOUBLE,
    user1_lon DOUBLE,
    user2_lat DOUBLE,
    user2_lon DOUBLE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_distance (user1_id, user2_id)
);
```

---

## 🧪 Tester les Endpoints PHP

### 1. Créer un Utilisateur

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

### 2. Récupérer un Utilisateur

```bash
curl http://192.168.56.1/servicephp/users/get_user.php?numero=%2B21612345678
```

### 3. Lister Tous les Utilisateurs

```bash
curl http://192.168.56.1/servicephp/users/get_all_users.php
```

### 4. Ajouter une Connexion

```bash
curl -X POST http://192.168.56.1/servicephp/connections/add_connection.php \
  -H "Content-Type: application/json" \
  -d '{
    "user1_id": 1,
    "user2_id": 2
  }'
```

### 5. Récupérer les Amis

```bash
curl http://192.168.56.1/servicephp/connections/get_connections.php?user_id=1
```

### 6. Calculer Distance

```bash
curl http://192.168.56.1/servicephp/connections/get_distance.php?user1_id=1&user2_id=2
```

---

## 🔍 Dépannage

### Erreur: "Table already exists"

**Solution**: Supprimer les tables existantes
```sql
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS user_connections;
-- etc...
```

### Erreur: "Foreign key constraint fails"

**Solution**: Vérifier l'ordre de création des tables
```sql
-- Créer d'abord les tables sans FK
-- Puis les tables avec FK
```

### Erreur: "Access denied"

**Solution**: Vérifier les permissions
```sql
GRANT ALL PRIVILEGES ON fyourf_db.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

---

## 📈 Optimisations

### Ajouter des Indexes

```sql
-- Index sur les recherches fréquentes
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_connection_status ON user_connections(status);
CREATE INDEX idx_distance_meters ON user_distances(distance_meters);
```

### Archiver les Anciennes Données

```sql
-- Archiver les notifications de plus de 30 jours
DELETE FROM notifications 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

---

## 🔐 Sécurité

### Créer un Utilisateur Dédié

```sql
CREATE USER 'fyourf_user'@'%' IDENTIFIED BY 'secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON fyourf_db.* TO 'fyourf_user'@'%';
FLUSH PRIVILEGES;
```

### Sauvegarder la Base de Données

```bash
mysqldump -h 192.168.56.1 -u root -p fyourf_db > backup.sql
```

### Restaurer la Base de Données

```bash
mysql -h 192.168.56.1 -u root -p fyourf_db < backup.sql
```

---

## ✨ Prochaines Étapes

1. ✅ Exécuter le script SQL
2. ✅ Vérifier les tables
3. ✅ Tester les endpoints PHP
4. ✅ Intégrer dans l'app Android
5. ✅ Créer les Fragments UI

---

**Status**: ✅ PRÊT À UTILISER

