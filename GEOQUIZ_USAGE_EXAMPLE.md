# 📖 GeoQuiz Challenge - Exemples d'Utilisation

**Date**: 2025-11-07
**Status**: 📋 Tutoriel

---

## 🎮 Scénario 1: Joueur Casual

### Étape 1: Lancer l'Application
```
1. Ouvrir FyourF
2. Cliquer sur l'onglet "GeoQuiz"
3. L'app charge les questions depuis l'historique
```

### Étape 2: Jouer au Quiz
```
Question 1: "Où a été prise cette photo?"
- Image de la carte affichée
- Options: Tunis, Sfax, Sousse, Kairouan
- Utilisateur sélectionne "Tunis"
- Cliquer "Valider"
- Résultat: ✅ Correct! +25 points
```

### Étape 3: Continuer
```
Question 2: "Où a été prise cette photo?"
- Image de la carte affichée
- Options: Sfax, Sousse, Gafsa, Tozeur
- Utilisateur sélectionne "Gafsa"
- Cliquer "Valider"
- Résultat: ❌ Incorrect! La réponse est: Sfax
```

### Étape 4: Voir les Badges
```
1. Cliquer sur l'onglet "Badges"
2. Voir les badges déverrouillés
3. Voir la progression des autres badges
```

---

## 🏆 Scénario 2: Compétiteur

### Étape 1: Jouer Quotidiennement
```
Jour 1:
- Joue 10 questions
- Score: 250 points
- Badges déverrouillés: 1

Jour 2:
- Joue 10 questions
- Score: 300 points
- Badges déverrouillés: 2

Jour 3:
- Joue 10 questions
- Score: 280 points
- Badges déverrouillés: 3
```

### Étape 2: Vérifier le Leaderboard
```
API: GET /servicephp/geoquiz/get_leaderboard.php?user_id=1

Réponse:
{
  "success": true,
  "leaderboard": [
    {
      "rank": 1,
      "pseudo": "Ahmed",
      "total_points": 5000,
      "total_badges": 10,
      "average_accuracy": 92.5
    },
    {
      "rank": 2,
      "pseudo": "Yasmina",
      "total_points": 4500,
      "total_badges": 9,
      "average_accuracy": 90.0
    }
  ],
  "user_rank": 2,
  "user_points": 4500
}
```

### Étape 3: Déverrouiller Tous les Badges
```
Badges déverrouillés:
✅ 🏙️ Tunis Explorer
✅ 🏖️ Sfax Voyageur
✅ 🏜️ Sahara Voyageur
✅ 🏔️ Montagne Alpiniste
✅ 🌊 Côte Marin
✅ ⭐ Quiz Master
✅ 🔥 Streak Champion
✅ 🎯 Perfectionist
✅ 🏛️ Historien
✅ 🌳 Naturaliste
```

---

## 📱 Scénario 3: Utilisateur Hors Ligne

### Étape 1: Jouer Hors Ligne
```
1. Désactiver Internet
2. Ouvrir FyourF
3. Cliquer sur "GeoQuiz"
4. Les questions en cache s'affichent
5. Jouer normalement
```

### Étape 2: Réactiver Internet
```
1. Réactiver Internet
2. L'app synchronise automatiquement
3. Les scores sont envoyés au serveur
4. Le leaderboard est mis à jour
```

### Étape 3: Vérifier la Synchronisation
```
Avant synchronisation:
- Score local: 250 points
- Leaderboard: Non mis à jour

Après synchronisation:
- Score serveur: 250 points
- Leaderboard: Mis à jour
- Rang: Calculé
```

---

## 💻 Scénario 4: Développeur

### Étape 1: Générer des Questions
```java
// Dans GeoQuizManager.java
List<Position> positions = locationDatabase.getAllPositions();
List<GeoQuizQuestion> questions = 
    quizManager.generateQuestionsFromHistory(positions, 10);

// Résultat:
// - 10 questions générées
// - Basées sur l'historique
// - Stockées dans GeoQuizDatabase
```

### Étape 2: Traiter une Réponse
```java
GeoQuizQuestion question = questions.get(0);
boolean isCorrect = quizManager.answerQuestion(question, "Tunis");

// Résultat:
// - isCorrect = true
// - Score augmente de 25 points
// - Streak augmente de 1
// - Badges vérifiés
```

### Étape 3: Sauvegarder le Score
```java
quizManager.endSession();

// Appel API:
POST /servicephp/geoquiz/save_score.php
{
  "user_id": 1,
  "total_points": 250,
  "correct_answers": 8,
  "total_questions": 10,
  "max_streak": 5,
  "session_duration": 300
}

// Réponse:
{
  "success": true,
  "score_id": 123,
  "accuracy": 80.0
}
```

### Étape 4: Récupérer les Badges
```java
List<Badge> badges = quizManager.getBadges();

// Résultat:
// - 10 badges
// - 3 déverrouillés
// - Progression affichée
```

---

## 🔧 Scénario 5: Admin

### Étape 1: Mettre à Jour le Leaderboard
```sql
CALL update_geoquiz_leaderboard();

-- Résultat:
-- - Leaderboard mis à jour
-- - Rangs recalculés
-- - Statistiques mises à jour
```

### Étape 2: Créer un Défi Quotidien
```sql
CALL create_daily_challenge('Tunis', 2, 5, 100);

-- Résultat:
-- - Défi créé pour aujourd'hui
-- - Région: Tunis
-- - Difficulté: Moyen
-- - 5 correctes requises
-- - 100 points de récompense
```

### Étape 3: Voir les Statistiques
```sql
SELECT * FROM geoquiz_regional_stats;

-- Résultat:
-- region | total_questions | correct_answers | accuracy | avg_difficulty
-- Tunis  | 150             | 120             | 80.0     | 2.1
-- Sfax   | 100             | 75              | 75.0     | 2.0
-- Sousse | 80              | 60              | 75.0     | 1.9
```

---

## 📊 Scénario 6: Analyse

### Étape 1: Voir les Statistiques Utilisateur
```sql
SELECT * FROM geoquiz_user_stats WHERE id = 1;

-- Résultat:
-- id | pseudo  | total_questions | correct_answers | accuracy | unlocked_badges | total_points
-- 1  | Yasmina | 100             | 80              | 80.0     | 5               | 2000
```

### Étape 2: Voir le Top 10
```sql
SELECT * FROM geoquiz_top_players LIMIT 10;

-- Résultat:
-- rank | pseudo  | total_games | total_points | average_accuracy | total_badges
-- 1    | Ahmed   | 50          | 5000         | 92.5             | 10
-- 2    | Yasmina | 40          | 4500         | 90.0             | 9
-- 3    | Fatima  | 35          | 4000         | 88.0             | 8
```

### Étape 3: Voir les Statistiques Régionales
```sql
SELECT * FROM geoquiz_regional_stats;

-- Résultat:
-- region | total_questions | correct_answers | accuracy | avg_difficulty
-- Tunis  | 500             | 400             | 80.0     | 2.1
-- Sfax   | 400             | 300             | 75.0     | 2.0
-- Sousse | 300             | 225             | 75.0     | 1.9
```

---

## 🎯 Résumé des Cas d'Usage

| Utilisateur | Action | Résultat |
|-------------|--------|----------|
| Casual | Joue 10 questions | Gagne des points |
| Compétiteur | Joue quotidiennement | Grimpe le leaderboard |
| Hors Ligne | Joue sans connexion | Données cachées |
| Développeur | Génère des questions | Questions créées |
| Admin | Met à jour leaderboard | Statistiques mises à jour |
| Analyste | Voit les statistiques | Données détaillées |

---

## 🚀 Prochaines Étapes

1. Intégrer dans MainActivity
2. Compiler et tester
3. Ajouter animations
4. Implémenter leaderboard UI
5. Ajouter défis quotidiens
6. Implémenter multiplayer

---

**Status**: 📋 Tutoriel
**Date**: 2025-11-07

