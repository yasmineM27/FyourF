-- ============================================
-- SQL Queries pour Vérifier les Données
-- ============================================
-- Base de données: fyourf_db
-- Serveur: 192.168.178.115
-- Port: 3306
-- Utilisateur: root
-- ============================================

-- ============================================
-- 1. VÉRIFIER LES POSITIONS
-- ============================================

-- Compter le total de positions
SELECT COUNT(*) as total_positions FROM positions;

-- Voir toutes les positions
SELECT * FROM positions;

-- Voir les 10 dernières positions
SELECT * FROM positions ORDER BY timestamp DESC LIMIT 10;

-- Voir les positions par utilisateur
SELECT numero, pseudo, COUNT(*) as count, 
       MIN(latitude) as min_lat, MAX(latitude) as max_lat,
       MIN(longitude) as min_lon, MAX(longitude) as max_lon
FROM positions 
GROUP BY numero, pseudo;

-- Voir les positions avec date formatée
SELECT idposition, numero, pseudo, latitude, longitude, 
       FROM_UNIXTIME(timestamp/1000) as date
FROM positions 
ORDER BY timestamp DESC;

-- ============================================
-- 2. VÉRIFIER LES QUESTIONS DU QUIZ
-- ============================================

-- Compter le total de questions
SELECT COUNT(*) as total_questions FROM geoquiz_questions;

-- Voir toutes les questions
SELECT * FROM geoquiz_questions;

-- Voir les 10 dernières questions
SELECT id, user_id, region, category, difficulty, correct_answer 
FROM geoquiz_questions 
ORDER BY id DESC LIMIT 10;

-- Voir les questions par région
SELECT region, COUNT(*) as count, 
       AVG(difficulty) as avg_difficulty
FROM geoquiz_questions 
GROUP BY region;

-- Voir les questions par catégorie
SELECT category, COUNT(*) as count 
FROM geoquiz_questions 
GROUP BY category;

-- Voir les questions par difficulté
SELECT difficulty, COUNT(*) as count 
FROM geoquiz_questions 
GROUP BY difficulty;

-- Voir les questions correctement répondues
SELECT COUNT(*) as correct_answers 
FROM geoquiz_questions 
WHERE correct = TRUE;

-- ============================================
-- 3. VÉRIFIER LES SCORES
-- ============================================

-- Compter le total de scores
SELECT COUNT(*) as total_scores FROM geoquiz_scores;

-- Voir tous les scores
SELECT * FROM geoquiz_scores;

-- Voir les 10 derniers scores
SELECT id, user_id, total_points, correct_answers, total_questions, accuracy 
FROM geoquiz_scores 
ORDER BY score_date DESC LIMIT 10;

-- Voir les statistiques par utilisateur
SELECT user_id, 
       COUNT(*) as games_played,
       AVG(total_points) as avg_points,
       MAX(total_points) as max_points,
       MIN(total_points) as min_points,
       AVG(accuracy) as avg_accuracy,
       MAX(accuracy) as max_accuracy
FROM geoquiz_scores 
GROUP BY user_id;

-- Voir les scores avec date formatée
SELECT id, user_id, total_points, correct_answers, total_questions, accuracy,
       FROM_UNIXTIME(score_date/1000) as date
FROM geoquiz_scores 
ORDER BY score_date DESC;

-- Voir le meilleur score
SELECT * FROM geoquiz_scores 
ORDER BY total_points DESC LIMIT 1;

-- Voir le score moyen
SELECT AVG(total_points) as avg_score,
       AVG(accuracy) as avg_accuracy,
       AVG(correct_answers) as avg_correct
FROM geoquiz_scores;

-- ============================================
-- 4. VÉRIFIER LES BADGES
-- ============================================

-- Compter le total de badges
SELECT COUNT(*) as total_badges FROM geoquiz_badges;

-- Voir tous les badges
SELECT * FROM geoquiz_badges;

-- Voir les badges déverrouillés
SELECT * FROM geoquiz_badges WHERE unlocked = 1;

-- Voir les badges verrouillés
SELECT * FROM geoquiz_badges WHERE unlocked = 0;

-- Voir les badges par utilisateur
SELECT user_id, COUNT(*) as total_badges,
       SUM(CASE WHEN unlocked = 1 THEN 1 ELSE 0 END) as unlocked_badges
FROM geoquiz_badges 
GROUP BY user_id;

-- Voir la progression des badges
SELECT id, user_id, badge_id, progress, unlocked 
FROM geoquiz_badges 
ORDER BY progress DESC;

-- ============================================
-- 5. RÉSUMÉ STATISTIQUE COMPLET
-- ============================================

-- Résumé de toutes les données
SELECT 
    (SELECT COUNT(*) FROM positions) as total_positions,
    (SELECT COUNT(*) FROM geoquiz_questions) as total_questions,
    (SELECT COUNT(*) FROM geoquiz_scores) as total_scores,
    (SELECT COUNT(*) FROM geoquiz_badges) as total_badges;

-- Résumé détaillé
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

-- ============================================
-- 6. VÉRIFIER L'INTÉGRITÉ DES DONNÉES
-- ============================================

-- Vérifier les positions avec coordonnées invalides
SELECT * FROM positions 
WHERE latitude < -90 OR latitude > 90 
   OR longitude < -180 OR longitude > 180;

-- Vérifier les questions sans réponse correcte
SELECT * FROM geoquiz_questions 
WHERE correct_answer IS NULL OR correct_answer = '';

-- Vérifier les scores avec des valeurs négatives
SELECT * FROM geoquiz_scores 
WHERE total_points < 0 OR correct_answers < 0 OR total_questions < 0;

-- Vérifier les badges avec progression > 100
SELECT * FROM geoquiz_badges 
WHERE progress > 100;

-- ============================================
-- 7. REQUÊTES DE NETTOYAGE (À UTILISER AVEC PRUDENCE)
-- ============================================

-- Supprimer toutes les positions
-- DELETE FROM positions;

-- Supprimer toutes les questions
-- DELETE FROM geoquiz_questions;

-- Supprimer tous les scores
-- DELETE FROM geoquiz_scores;

-- Supprimer tous les badges
-- DELETE FROM geoquiz_badges;

-- Réinitialiser les auto-increment
-- ALTER TABLE positions AUTO_INCREMENT = 1;
-- ALTER TABLE geoquiz_questions AUTO_INCREMENT = 1;
-- ALTER TABLE geoquiz_scores AUTO_INCREMENT = 1;
-- ALTER TABLE geoquiz_badges AUTO_INCREMENT = 1;

-- ============================================
-- FIN DES REQUÊTES
-- ============================================

