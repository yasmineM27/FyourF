-- ============================================
-- Script Simple de Création de la Base de Données
-- pour l'application FyourF - Tracking GPS
-- ============================================

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS fyourf_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE fyourf_db;

-- ============================================
-- Table: positions
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
    
    INDEX idx_numero (numero),
    INDEX idx_timestamp (timestamp),
    INDEX idx_numero_timestamp (numero, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Données de test
-- ============================================

INSERT INTO positions (longitude, latitude, numero, pseudo, timestamp) VALUES
(10.1815, 36.8065, '+21612345678', 'TestUser', UNIX_TIMESTAMP() * 1000),
(10.1820, 36.8070, '+21612345678', 'TestUser', UNIX_TIMESTAMP() * 1000 + 30000),
(10.1825, 36.8075, '+21612345678', 'TestUser', UNIX_TIMESTAMP() * 1000 + 60000);

-- ============================================
-- Vérification
-- ============================================

SELECT 'Database created successfully!' AS status;
SELECT COUNT(*) as total_positions FROM positions;
SELECT * FROM positions;

