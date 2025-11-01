-- ============================================
-- Script de création de la base de données MySQL
-- pour l'application FyourF - Tracking GPS
-- ============================================

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS fyourf_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE fyourf_db;

-- ============================================
-- Table: positions
-- Stocke toutes les positions GPS enregistrées
-- ============================================

DROP TABLE IF EXISTS positions;

CREATE TABLE positions (
    idposition INT AUTO_INCREMENT PRIMARY KEY,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    numero VARCHAR(20) NOT NULL,
    pseudo VARCHAR(100) DEFAULT NULL,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Index pour améliorer les performances
    INDEX idx_numero (numero),
    INDEX idx_timestamp (timestamp),
    INDEX idx_created_at (created_at),
    INDEX idx_numero_timestamp (numero, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: trajectories (optionnel)
-- Stocke les informations sur les trajets
-- ============================================

DROP TABLE IF EXISTS trajectories;

CREATE TABLE trajectories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) NOT NULL,
    pseudo VARCHAR(100) DEFAULT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT DEFAULT NULL,
    position_count INT DEFAULT 0,
    total_distance DOUBLE DEFAULT 0,
    status ENUM('active', 'completed', 'cancelled') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_numero (numero),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Données de test (optionnel)
-- ============================================

-- Insérer quelques positions de test
INSERT INTO positions (longitude, latitude, numero, pseudo, timestamp) VALUES
(10.1815, 36.8065, '+21612345678', 'User1', UNIX_TIMESTAMP() * 1000),
(10.1820, 36.8070, '+21612345678', 'User1', UNIX_TIMESTAMP() * 1000 + 30000),
(10.1825, 36.8075, '+21612345678', 'User1', UNIX_TIMESTAMP() * 1000 + 60000),
(10.1830, 36.8080, '+21612345678', 'User1', UNIX_TIMESTAMP() * 1000 + 90000),
(10.1835, 36.8085, '+21612345678', 'User1', UNIX_TIMESTAMP() * 1000 + 120000);

-- ============================================
-- Vues utiles
-- ============================================

-- Vue pour obtenir les dernières positions par utilisateur
CREATE OR REPLACE VIEW latest_positions AS
SELECT p1.*
FROM positions p1
INNER JOIN (
    SELECT numero, MAX(timestamp) as max_timestamp
    FROM positions
    GROUP BY numero
) p2 ON p1.numero = p2.numero AND p1.timestamp = p2.max_timestamp;

-- Vue pour les statistiques par utilisateur
CREATE OR REPLACE VIEW user_statistics AS
SELECT 
    numero,
    pseudo,
    COUNT(*) as total_positions,
    MIN(timestamp) as first_position_time,
    MAX(timestamp) as last_position_time,
    MIN(created_at) as first_created,
    MAX(created_at) as last_created
FROM positions
GROUP BY numero, pseudo;

-- ============================================
-- Procédures stockées
-- ============================================

-- Procédure pour nettoyer les anciennes positions (plus de 30 jours)
DELIMITER //

DROP PROCEDURE IF EXISTS clean_old_positions//

CREATE PROCEDURE clean_old_positions(IN days_to_keep INT)
BEGIN
    DECLARE deleted_count INT;
    
    DELETE FROM positions 
    WHERE created_at < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    SET deleted_count = ROW_COUNT();
    
    SELECT CONCAT('Deleted ', deleted_count, ' positions older than ', days_to_keep, ' days') AS result;
END//

-- Procédure pour obtenir les positions d'un trajet
DROP PROCEDURE IF EXISTS get_trajectory//

CREATE PROCEDURE get_trajectory(
    IN p_numero VARCHAR(20),
    IN p_start_time BIGINT,
    IN p_end_time BIGINT
)
BEGIN
    IF p_start_time > 0 AND p_end_time > 0 THEN
        SELECT * FROM positions
        WHERE numero = p_numero
        AND timestamp BETWEEN p_start_time AND p_end_time
        ORDER BY timestamp ASC;
    ELSEIF p_start_time > 0 THEN
        SELECT * FROM positions
        WHERE numero = p_numero
        AND timestamp >= p_start_time
        ORDER BY timestamp ASC;
    ELSE
        SELECT * FROM positions
        WHERE numero = p_numero
        ORDER BY timestamp ASC;
    END IF;
END//

-- Procédure pour calculer la distance d'un trajet
DROP PROCEDURE IF EXISTS calculate_trajectory_distance//

CREATE PROCEDURE calculate_trajectory_distance(
    IN p_numero VARCHAR(20),
    IN p_start_time BIGINT,
    IN p_end_time BIGINT
)
BEGIN
    DECLARE total_distance DOUBLE DEFAULT 0;
    
    -- Utilise la formule de Haversine pour calculer la distance
    SELECT SUM(
        6371 * ACOS(
            COS(RADIANS(p1.latitude)) * COS(RADIANS(p2.latitude)) *
            COS(RADIANS(p2.longitude) - RADIANS(p1.longitude)) +
            SIN(RADIANS(p1.latitude)) * SIN(RADIANS(p2.latitude))
        )
    ) INTO total_distance
    FROM positions p1
    INNER JOIN positions p2 ON p1.idposition + 1 = p2.idposition
    WHERE p1.numero = p_numero
    AND p1.timestamp BETWEEN p_start_time AND p_end_time
    AND p2.timestamp BETWEEN p_start_time AND p_end_time;
    
    SELECT IFNULL(total_distance, 0) as distance_km;
END//

DELIMITER ;

-- ============================================
-- Triggers
-- ============================================

-- Trigger pour mettre à jour automatiquement updated_at
DELIMITER //

DROP TRIGGER IF EXISTS before_position_update//

CREATE TRIGGER before_position_update
BEFORE UPDATE ON positions
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

DELIMITER ;

-- ============================================
-- Utilisateur et permissions
-- ============================================

-- Créer un utilisateur pour l'application (optionnel)
-- Remplacez 'your_password' par un mot de passe sécurisé
-- CREATE USER IF NOT EXISTS 'fyourf_user'@'%' IDENTIFIED BY 'your_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON fyourf_db.* TO 'fyourf_user'@'%';
-- FLUSH PRIVILEGES;

-- ============================================
-- Afficher les informations
-- ============================================

SELECT 'Database setup completed successfully!' AS status;
SELECT COUNT(*) as total_positions FROM positions;
SELECT * FROM user_statistics;

-- ============================================
-- Requêtes utiles pour tester
-- ============================================

-- Obtenir toutes les positions
-- SELECT * FROM positions ORDER BY timestamp DESC;

-- Obtenir les positions d'un utilisateur
-- SELECT * FROM positions WHERE numero = '+21612345678' ORDER BY timestamp DESC;

-- Obtenir les dernières positions de chaque utilisateur
-- SELECT * FROM latest_positions;

-- Supprimer une position
-- DELETE FROM positions WHERE idposition = 1;

-- Nettoyer les positions de plus de 30 jours
-- CALL clean_old_positions(30);

-- Obtenir un trajet
-- CALL get_trajectory('+21612345678', 0, 0);

-- Calculer la distance d'un trajet
-- CALL calculate_trajectory_distance('+21612345678', 1609459200000, 1609545600000);

