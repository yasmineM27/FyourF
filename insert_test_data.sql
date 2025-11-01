-- ============================================
-- Script d'insertion de données de test
-- pour l'application FyourF
-- ============================================

USE fyourf_db;

-- Supprimer les anciennes données de test (optionnel)
-- DELETE FROM positions WHERE pseudo LIKE 'Test%';

-- Insérer des positions de test pour un trajet
INSERT INTO positions (longitude, latitude, numero, pseudo, timestamp) VALUES
-- Point de départ (Tunis - Avenue Habib Bourguiba)
(10.1815, 36.8065, '+21612345678', 'TestUser', UNIX_TIMESTAMP() * 1000),

-- Point 2 (30 secondes plus tard)
(10.1820, 36.8070, '+21612345678', 'TestUser', UNIX_TIMESTAMP() * 1000 + 30000),

-- Point 3 (1 minute plus tard)
(10.1825, 36.8075, '+21612345678', 'TestUser', UNIX_TIMESTAMP() * 1000 + 60000),

-- Point 4 (1 minute 30 plus tard)
(10.1830, 36.8080, '+21612345678', 'TestUser', UNIX_TIMESTAMP() * 1000 + 90000),

-- Point 5 (2 minutes plus tard)
(10.1835, 36.8085, '+21612345678', 'TestUser', UNIX_TIMESTAMP() * 1000 + 120000);

-- Insérer des positions pour un autre utilisateur
INSERT INTO positions (longitude, latitude, numero, pseudo, timestamp) VALUES
(10.1650, 36.8100, '+21698765432', 'TestUser2', UNIX_TIMESTAMP() * 1000),
(10.1655, 36.8105, '+21698765432', 'TestUser2', UNIX_TIMESTAMP() * 1000 + 30000);

-- Afficher les résultats
SELECT 
    idposition,
    longitude,
    latitude,
    numero,
    pseudo,
    FROM_UNIXTIME(timestamp/1000) as date_position,
    created_at
FROM positions
ORDER BY timestamp DESC;

-- Statistiques
SELECT 
    numero,
    COUNT(*) as nombre_positions,
    MIN(FROM_UNIXTIME(timestamp/1000)) as premiere_position,
    MAX(FROM_UNIXTIME(timestamp/1000)) as derniere_position
FROM positions
GROUP BY numero;

SELECT 'Données de test insérées avec succès!' as status;

