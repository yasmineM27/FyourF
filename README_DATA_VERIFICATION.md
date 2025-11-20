# 📊 Vérification des Données MySQL - FyourF

## 🎯 Objectif

Vérifier que toutes les données de test (positions, questions du quiz, scores, badges) sont correctement stockées dans MySQL sur le serveur `192.168.178.115`.

---

## 📍 Configuration

```
Serveur: 192.168.178.115
Port: 3306
Base de données: fyourf_db
Utilisateur: root
Mot de passe: (vide)
```

---

## 📊 Données Attendues

### 1. **Positions** (6 positions de test en Tunisie)
```
Tunis: 36.8065, 10.1815
Sousse: 35.8256, 10.6369
Sfax: 35.7595, 10.5671
Kairouan: 35.6781, 9.9197
Gafsa: 34.4269, 8.7869
Tozeur: 33.9197, 8.1339
```

### 2. **Questions du Quiz**
- Générées à partir des positions
- Régions: Tunis, Sousse, Sfax, Kairouan, Gafsa, Tozeur
- Catégories: Plage, Montagne, Ville, Désert, Oasis, Historique, Moderne
- Difficultés: 1, 2, 3

### 3. **Scores**
- Historique des parties jouées
- Points, réponses correctes, précision

### 4. **Badges**
- Badges déverrouillés par région
- Progression et statut

---

## 🚀 Comment Vérifier les Données

### Option 1: Via phpMyAdmin (Interface Web)
```
URL: http://192.168.178.115/phpmyadmin
Utilisateur: root
Mot de passe: (vide)
```

### Option 2: Via MySQL Command Line
```bash
mysql -h 192.168.178.115 -u root fyourf_db

# Voir les positions
SELECT * FROM positions;

# Voir les questions
SELECT * FROM geoquiz_questions;

# Voir les scores
SELECT * FROM geoquiz_scores;

# Voir les badges
SELECT * FROM geoquiz_badges;
```

### Option 3: Via Script Batch
```bash
.\verify_mysql_data.bat
```

### Option 4: Via Script PowerShell
```powershell
.\verify_mysql_data.ps1
```

### Option 5: Via Script PHP
```
http://192.168.178.115/servicephp/verify_data.php
```

---

## 📁 Fichiers Créés

| Fichier | Description |
|---------|-------------|
| `servicephp/verify_data.php` | Script PHP pour vérifier les données |
| `verify_mysql_data.bat` | Script Batch pour vérifier les données |
| `verify_mysql_data.ps1` | Script PowerShell pour vérifier les données |
| `VERIFY_DATA_GUIDE.md` | Guide complet de vérification |
| `SETUP_MYSQL_NETWORK.md` | Guide de configuration MySQL pour accès réseau |
| `DATA_VERIFICATION_SUMMARY.md` | Résumé de la vérification |
| `SQL_QUERIES_VERIFICATION.sql` | Requêtes SQL pour vérifier les données |
| `README_DATA_VERIFICATION.md` | Ce fichier |

---

## ⚠️ Problème Détecté

**MySQL n'est pas accessible sur le port 3306 de `192.168.178.115`**

### Causes Possibles:
1. MySQL n'est pas en cours d'exécution
2. MySQL écoute uniquement sur `localhost` (127.0.0.1)
3. Le pare-feu bloque le port 3306
4. MySQL n'est pas configuré pour accepter les connexions réseau

### Solution:
Voir le fichier `SETUP_MYSQL_NETWORK.md` pour les instructions de configuration

---

## 🔧 Configuration MySQL pour Accès Réseau

### Étape 1: Localiser le fichier de configuration

**XAMPP (Windows):**
```
C:\xampp\mysql\bin\my.ini
```

**WAMP (Windows):**
```
C:\wamp64\bin\mysql\mysql8.0.x\my.ini
```

**Linux:**
```
/etc/mysql/mysql.conf.d/mysqld.cnf
```

### Étape 2: Modifier la configuration

Trouver la ligne:
```ini
bind-address = 127.0.0.1
```

Remplacer par:
```ini
bind-address = 0.0.0.0
```

### Étape 3: Redémarrer MySQL

**XAMPP:** Ouvrir XAMPP Control Panel → Stop MySQL → Start MySQL

**WAMP:** Ouvrir WAMP Control Panel → Stop MySQL → Start MySQL

**Linux:**
```bash
sudo systemctl restart mysql
```

### Étape 4: Vérifier la configuration

```bash
mysql -h 192.168.178.115 -u root fyourf_db
```

---

## 📋 Checklist

- [ ] MySQL est en cours d'exécution
- [ ] `bind-address` est configuré à `0.0.0.0`
- [ ] MySQL a été redémarré
- [ ] Test de connexion réussi
- [ ] Fichiers PHP copiés dans le répertoire web
- [ ] Configuration Android mise à jour
- [ ] Données vérifiées avec succès

---

## 🔍 Requêtes SQL Utiles

### Compter les données
```sql
SELECT 
    (SELECT COUNT(*) FROM positions) as positions,
    (SELECT COUNT(*) FROM geoquiz_questions) as questions,
    (SELECT COUNT(*) FROM geoquiz_scores) as scores,
    (SELECT COUNT(*) FROM geoquiz_badges) as badges;
```

### Voir les positions
```sql
SELECT * FROM positions ORDER BY timestamp DESC LIMIT 10;
```

### Voir les questions
```sql
SELECT id, region, category, difficulty, correct_answer FROM geoquiz_questions LIMIT 10;
```

### Voir les scores
```sql
SELECT * FROM geoquiz_scores ORDER BY score_date DESC LIMIT 10;
```

### Voir les badges
```sql
SELECT * FROM geoquiz_badges WHERE unlocked = 1;
```

---

## 📚 Documentation Complète

- **VERIFY_DATA_GUIDE.md** - Guide complet avec toutes les options
- **SETUP_MYSQL_NETWORK.md** - Instructions détaillées de configuration
- **SQL_QUERIES_VERIFICATION.sql** - Toutes les requêtes SQL
- **DATA_VERIFICATION_SUMMARY.md** - Résumé technique

---

## 🆘 Dépannage

### Erreur: "Can't connect to MySQL server"
```bash
# Vérifier que MySQL écoute sur le port 3306
netstat -an | findstr 3306
```

### Erreur: "Access denied for user 'root'"
```bash
# Vérifier le mot de passe
mysql -h 192.168.178.115 -u root -p
```

### Erreur: "Connection refused"
```bash
# Vérifier que le pare-feu n'est pas bloquant
# Ajouter une règle pour MySQL port 3306
```

---

## 📞 Support

Pour plus d'aide:
1. Consulter `SETUP_MYSQL_NETWORK.md`
2. Consulter `VERIFY_DATA_GUIDE.md`
3. Exécuter les requêtes SQL dans `SQL_QUERIES_VERIFICATION.sql`

---

**Créé le**: 2025-11-20
**Dernière mise à jour**: 2025-11-20
**Statut**: ⏳ En attente de configuration MySQL

