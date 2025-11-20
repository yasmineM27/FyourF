# 🔍 Vérification des Données via phpMyAdmin

## ✅ phpMyAdmin est Accessible!

```
URL: http://localhost/phpmyadmin/index.php?route=/database/sql&db=fyourf_db
Utilisateur: root
Mot de passe: (vide)
```

---

## 📊 Vérifier les Données

### 1. **Vérifier les Positions**

Copier-coller cette requête dans phpMyAdmin:

```sql
SELECT COUNT(*) as total_positions FROM positions;
```

**Résultat attendu:** 6 positions

```sql
SELECT * FROM positions ORDER BY timestamp DESC;
```

**Résultat attendu:**
- Tunis: 36.8065, 10.1815
- Sousse: 35.8256, 10.6369
- Sfax: 35.7595, 10.5671
- Kairouan: 35.6781, 9.9197
- Gafsa: 34.4269, 8.7869
- Tozeur: 33.9197, 8.1339

---

### 2. **Vérifier les Questions du Quiz**

```sql
SELECT COUNT(*) as total_questions FROM geoquiz_questions;
```

**Résultat attendu:** N questions (générées à partir des positions)

```sql
SELECT id, region, category, difficulty, correct_answer 
FROM geoquiz_questions 
LIMIT 10;
```

**Résultat attendu:** Questions avec régions, catégories, difficultés

---

### 3. **Vérifier les Scores**

```sql
SELECT COUNT(*) as total_scores FROM geoquiz_scores;
```

**Résultat attendu:** N scores (des parties jouées)

```sql
SELECT * FROM geoquiz_scores 
ORDER BY score_date DESC 
LIMIT 10;
```

**Résultat attendu:** Scores avec points, réponses correctes, précision

---

### 4. **Vérifier les Badges**

```sql
SELECT COUNT(*) as total_badges FROM geoquiz_badges;
```

**Résultat attendu:** N badges

```sql
SELECT * FROM geoquiz_badges 
WHERE unlocked = 1;
```

**Résultat attendu:** Badges déverrouillés

---

## 🎯 Résumé Complet

Copier-coller cette requête pour voir un résumé complet:

```sql
SELECT 
    'Positions' as table_name,
    COUNT(*) as total_records
FROM positions
UNION ALL
SELECT 
    'Questions' as table_name,
    COUNT(*) as total_records
FROM geoquiz_questions
UNION ALL
SELECT 
    'Scores' as table_name,
    COUNT(*) as total_records
FROM geoquiz_scores
UNION ALL
SELECT 
    'Badges' as table_name,
    COUNT(*) as total_records
FROM geoquiz_badges;
```

---

## 📋 Étapes pour Vérifier

### Étape 1: Ouvrir phpMyAdmin
```
http://localhost/phpmyadmin/index.php?route=/database/sql&db=fyourf_db
```

### Étape 2: Aller à l'onglet "SQL"
- Cliquer sur "SQL" en haut
- Ou aller à la base de données `fyourf_db` → onglet "SQL"

### Étape 3: Copier-coller les requêtes
- Copier une requête ci-dessus
- Coller dans la zone de texte
- Cliquer sur "Exécuter"

### Étape 4: Analyser les résultats
- Vérifier le nombre de lignes
- Vérifier les données

---

## 🔍 Requêtes Utiles

### Voir toutes les positions avec dates formatées
```sql
SELECT idposition, numero, pseudo, latitude, longitude, 
       FROM_UNIXTIME(timestamp/1000) as date
FROM positions 
ORDER BY timestamp DESC;
```

### Voir les questions par région
```sql
SELECT region, COUNT(*) as count, 
       AVG(difficulty) as avg_difficulty
FROM geoquiz_questions 
GROUP BY region;
```

### Voir les statistiques des scores
```sql
SELECT 
    COUNT(*) as total_games,
    AVG(total_points) as avg_points,
    MAX(total_points) as max_points,
    MIN(total_points) as min_points,
    AVG(accuracy) as avg_accuracy
FROM geoquiz_scores;
```

### Voir les badges déverrouillés par utilisateur
```sql
SELECT user_id, COUNT(*) as unlocked_badges
FROM geoquiz_badges 
WHERE unlocked = 1
GROUP BY user_id;
```

---

## ⚠️ Problèmes Courants

### Erreur: "Table doesn't exist"
- Vérifier que la base de données `fyourf_db` est sélectionnée
- Vérifier que les tables existent

### Erreur: "Access denied"
- Vérifier le mot de passe (vide par défaut)
- Vérifier l'utilisateur (root par défaut)

### Pas de données
- Vérifier que les données ont été insérées
- Vérifier que l'application a synchronisé les données

---

## 📞 Support

Pour plus d'aide:
1. Consulter `README_DATA_VERIFICATION.md`
2. Consulter `SETUP_MYSQL_NETWORK.md`
3. Consulter `SQL_QUERIES_VERIFICATION.sql`

---

**Créé le**: 2025-11-20
**Statut**: ✅ Prêt à l'emploi

