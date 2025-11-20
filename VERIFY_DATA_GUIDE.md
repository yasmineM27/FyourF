# 📊 Guide de Vérification des Données MySQL

## 🎯 Objectif
Vérifier que toutes les données de test sont correctement stockées dans MySQL sur le serveur `192.168.178.115`

---

## ✅ Étape 1: Vérifier la Connectivité

### Test Ping
```bash
ping 192.168.178.115
```
✅ **Résultat**: Réponse reçue (0% perte)

### Test HTTP
```bash
# Vérifier que le serveur web répond
curl http://192.168.178.115/
```

---

## 🔧 Étape 2: Accéder à MySQL

### Option A: Via phpMyAdmin (Interface Web)
```
URL: http://192.168.178.115/phpmyadmin
Utilisateur: root
Mot de passe: (vide)
```

**Étapes:**
1. Ouvrir le navigateur
2. Aller à `http://192.168.178.115/phpmyadmin`
3. Se connecter avec `root` / (pas de mot de passe)
4. Sélectionner la base de données `fyourf_db`

---

### Option B: Via MySQL Command Line

#### Sur Windows (CMD)
```bash
# Installer MySQL Client si nécessaire
# Puis exécuter:
mysql -h 192.168.178.115 -u root -p fyourf_db

# Laisser le mot de passe vide (appuyer sur Entrée)
```

#### Sur Linux/Mac
```bash
mysql -h 192.168.178.115 -u root fyourf_db
```

---

## 📋 Étape 3: Vérifier les Données

### 1️⃣ Vérifier les POSITIONS (Historique de localisation)

```sql
-- Compter les positions
SELECT COUNT(*) as total_positions FROM positions;

-- Voir les 10 dernières positions
SELECT * FROM positions ORDER BY timestamp DESC LIMIT 10;

-- Voir les positions par utilisateur
SELECT numero, pseudo, COUNT(*) as count FROM positions GROUP BY numero;
```

**Résultat attendu:**
- Au moins 6 positions de test en Tunisie
- Colonnes: `idposition`, `longitude`, `latitude`, `numero`, `pseudo`, `timestamp`

---

### 2️⃣ Vérifier les QUESTIONS DU QUIZ

```sql
-- Compter les questions
SELECT COUNT(*) as total_questions FROM geoquiz_questions;

-- Voir les 10 dernières questions
SELECT id, user_id, region, category, difficulty, correct_answer 
FROM geoquiz_questions 
ORDER BY id DESC LIMIT 10;

-- Voir les questions par région
SELECT region, COUNT(*) as count FROM geoquiz_questions GROUP BY region;
```

**Résultat attendu:**
- Questions générées à partir des positions
- Colonnes: `id`, `user_id`, `latitude`, `longitude`, `region`, `category`, `difficulty`, `correct_answer`, `options`

---

### 3️⃣ Vérifier les SCORES

```sql
-- Compter les scores
SELECT COUNT(*) as total_scores FROM geoquiz_scores;

-- Voir les 10 derniers scores
SELECT id, user_id, total_points, correct_answers, total_questions, accuracy 
FROM geoquiz_scores 
ORDER BY score_date DESC LIMIT 10;

-- Voir les statistiques par utilisateur
SELECT user_id, COUNT(*) as games, AVG(total_points) as avg_points, AVG(accuracy) as avg_accuracy
FROM geoquiz_scores 
GROUP BY user_id;
```

**Résultat attendu:**
- Scores des parties jouées
- Colonnes: `id`, `user_id`, `total_points`, `correct_answers`, `total_questions`, `accuracy`, `max_streak`

---

### 4️⃣ Vérifier les BADGES

```sql
-- Compter les badges
SELECT COUNT(*) as total_badges FROM geoquiz_badges;

-- Voir tous les badges
SELECT id, user_id, badge_id, unlocked, progress 
FROM geoquiz_badges 
ORDER BY id DESC;

-- Voir les badges déverrouillés
SELECT * FROM geoquiz_badges WHERE unlocked = 1;
```

**Résultat attendu:**
- Badges déverrouillés par les utilisateurs
- Colonnes: `id`, `user_id`, `badge_id`, `unlocked`, `progress`

---

## 📊 Étape 4: Résumé Complet

```sql
-- Résumé de toutes les données
SELECT 
    (SELECT COUNT(*) FROM positions) as total_positions,
    (SELECT COUNT(*) FROM geoquiz_questions) as total_questions,
    (SELECT COUNT(*) FROM geoquiz_scores) as total_scores,
    (SELECT COUNT(*) FROM geoquiz_badges) as total_badges;
```

---

## 🚀 Étape 5: Copier les Fichiers PHP

Pour que le script `verify_data.php` fonctionne:

### Sur XAMPP (Windows)
```bash
# Copier le dossier servicephp vers:
C:\xampp\htdocs\servicephp\

# Puis accéder à:
http://192.168.178.115/servicephp/verify_data.php
```

### Sur WAMP (Windows)
```bash
# Copier le dossier servicephp vers:
C:\wamp64\www\servicephp\

# Puis accéder à:
http://192.168.178.115/servicephp/verify_data.php
```

### Sur LAMP (Linux)
```bash
# Copier le dossier servicephp vers:
/var/www/html/servicephp/

# Puis accéder à:
http://192.168.178.115/servicephp/verify_data.php
```

---

## 🔍 Dépannage

### Erreur: "Connection refused"
- Vérifier que MySQL est en cours d'exécution
- Vérifier l'IP: `192.168.178.115`
- Vérifier le port: `3306`

### Erreur: "Access denied for user 'root'"
- Vérifier le mot de passe MySQL
- Modifier `servicephp/config.php` si nécessaire

### Erreur: "Unknown database 'fyourf_db'"
- Créer la base de données: `CREATE DATABASE fyourf_db;`
- Exécuter le script SQL: `geoquiz_mysql_setup.sql`

---

## 📝 Notes

- **IP du serveur**: `192.168.178.115`
- **Port MySQL**: `3306`
- **Base de données**: `fyourf_db`
- **Utilisateur**: `root`
- **Mot de passe**: (vide)

---

**Créé le**: 2025-11-20
**Dernière mise à jour**: 2025-11-20

