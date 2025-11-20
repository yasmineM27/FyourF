-- ============================================
-- Script COMPLET de création de la base de données MySQL
-- pour l'application FyourF - Tracking GPS Multi-Utilisateurs
-- ============================================
-- Date: 2025-11-07
-- Version: 2.0.0
-- ============================================

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS fyourf_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE fyourf_db;

-- ============================================
-- TABLES ORIGINALES - Tracking GPS
-- ============================================

-- Table: positions
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
    INDEX idx_created_at (created_at),
    INDEX idx_numero_timestamp (numero, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: trajectories
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
-- TABLES NOUVELLES - Multi-Utilisateurs
-- ============================================

-- Table: users
DROP TABLE IF EXISTS users;
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_numero (numero),
    INDEX idx_pseudo (pseudo),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_connections
DROP TABLE IF EXISTS user_connections;
CREATE TABLE user_connections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    status ENUM('pending', 'connected', 'blocked') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_connection (user1_id, user2_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_distances
DROP TABLE IF EXISTS user_distances;
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
    UNIQUE KEY unique_distance (user1_id, user2_id),
    INDEX idx_distance_meters (distance_meters)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_groups
DROP TABLE IF EXISTS user_groups;
CREATE TABLE user_groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id INT NOT NULL,
    icon_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: group_members
DROP TABLE IF EXISTS group_members;
CREATE TABLE group_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    role ENUM('admin', 'member') DEFAULT 'member',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member (group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: geofences
DROP TABLE IF EXISTS geofences;
CREATE TABLE geofences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius_meters INT DEFAULT 500,
    alert_on_exit BOOLEAN DEFAULT TRUE,
    alert_on_enter BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: notifications
DROP TABLE IF EXISTS notifications;
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(50),
    title VARCHAR(255),
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: meeting_history
DROP TABLE IF EXISTS meeting_history;
CREATE TABLE meeting_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    meeting_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location_lat DOUBLE,
    location_lon DOUBLE,
    duration_seconds INT,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_meeting_date (meeting_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- DONNÉES DE TEST
-- ============================================

-- Insérer des utilisateurs de test
INSERT INTO users (numero, pseudo, email, phone, status) VALUES
('+21612345678', 'User1', 'user1@example.com', '+21612345678', 'online'),
('+21687654321', 'User2', 'user2@example.com', '+21687654321', 'online'),
('+21698765432', 'User3', 'user3@example.com', '+21698765432', 'offline');

-- Insérer des connexions de test
INSERT INTO user_connections (user1_id, user2_id, status) VALUES
(1, 2, 'connected'),
(1, 3, 'pending');

-- Insérer des positions de test
INSERT INTO positions (longitude, latitude, numero, pseudo, timestamp) VALUES
(10.1815, 36.8065, '+21612345678', 'User1', UNIX_TIMESTAMP() * 1000),
(10.1820, 36.8070, '+21612345678', 'User1', UNIX_TIMESTAMP() * 1000 + 30000),
(10.1825, 36.8075, '+21612345678', 'User1', UNIX_TIMESTAMP() * 1000 + 60000);

-- ============================================
-- VUES UTILES
-- ============================================

-- Vue: Dernières positions par utilisateur
CREATE OR REPLACE VIEW latest_positions AS
SELECT p1.*
FROM positions p1
INNER JOIN (
    SELECT numero, MAX(timestamp) as max_timestamp
    FROM positions
    GROUP BY numero
) p2 ON p1.numero = p2.numero AND p1.timestamp = p2.max_timestamp;

-- Vue: Statistiques par utilisateur
CREATE OR REPLACE VIEW user_statistics AS
SELECT 
    numero,
    pseudo,
    COUNT(*) as total_positions,
    MIN(timestamp) as first_position_time,
    MAX(timestamp) as last_position_time
FROM positions
GROUP BY numero, pseudo;

-- Vue: Amis connectés
CREATE OR REPLACE VIEW connected_friends AS
SELECT 
    u1.id as user_id,
    u1.pseudo as user_pseudo,
    u2.id as friend_id,
    u2.pseudo as friend_pseudo,
    u2.status as friend_status,
    uc.created_at as connected_since
FROM user_connections uc
JOIN users u1 ON uc.user1_id = u1.id
JOIN users u2 ON uc.user2_id = u2.id
WHERE uc.status = 'connected';

-- Vue: Statistiques sociales
CREATE OR REPLACE VIEW social_statistics AS
SELECT 
    u.id,
    u.pseudo,
    COUNT(DISTINCT uc.user2_id) as total_connections,
    COUNT(DISTINCT gm.group_id) as total_groups,
    COUNT(DISTINCT mh.id) as total_meetings
FROM users u
LEFT JOIN user_connections uc ON u.id = uc.user1_id AND uc.status = 'connected'
LEFT JOIN group_members gm ON u.id = gm.user_id
LEFT JOIN meeting_history mh ON u.id = mh.user1_id OR u.id = mh.user2_id
GROUP BY u.id, u.pseudo;

-- ============================================
-- AFFICHER LES INFORMATIONS
-- ============================================

SELECT '✅ Database setup completed successfully!' AS status;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_positions FROM positions;
SELECT COUNT(*) as total_connections FROM user_connections WHERE status = 'connected';

