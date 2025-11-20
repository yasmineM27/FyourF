-- ============================================
-- Script d'ajout des tables Multi-Utilisateurs
-- pour l'application FyourF - Tracking GPS
-- ============================================

USE fyourf_db;

-- ============================================
-- Table: users
-- Stocke les informations des utilisateurs
-- ============================================

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

-- ============================================
-- Table: user_connections
-- Stocke les connexions entre utilisateurs
-- ============================================

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
    INDEX idx_status (status),
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: user_distances
-- Stocke les distances en temps réel entre users
-- ============================================

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
    INDEX idx_distance (distance_meters),
    INDEX idx_updated (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: user_groups
-- Stocke les groupes/équipes
-- ============================================

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
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: group_members
-- Stocke les membres des groupes
-- ============================================

DROP TABLE IF EXISTS group_members;

CREATE TABLE group_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    role ENUM('admin', 'member') DEFAULT 'member',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member (group_id, user_id),
    INDEX idx_group (group_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: geofences
-- Stocke les zones de sécurité
-- ============================================

DROP TABLE IF EXISTS geofences;

CREATE TABLE geofences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius_meters INT NOT NULL,
    alert_on_exit BOOLEAN DEFAULT TRUE,
    alert_on_enter BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: notifications
-- Stocke les notifications
-- ============================================

DROP TABLE IF EXISTS notifications;

CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type ENUM('friend_nearby', 'friend_very_close', 'geofence_exit', 'geofence_enter', 'connection_request', 'group_invite') DEFAULT 'friend_nearby',
    title VARCHAR(100),
    message TEXT,
    related_user_id INT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (related_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_read (is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: meeting_history
-- Stocke l'historique des rencontres
-- ============================================

DROP TABLE IF EXISTS meeting_history;

CREATE TABLE meeting_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    meeting_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location_lat DOUBLE,
    location_lon DOUBLE,
    duration_seconds INT,
    distance_at_closest DOUBLE,
    
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id),
    INDEX idx_date (meeting_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Vues utiles
-- ============================================

-- Vue pour obtenir les amis connectés
CREATE OR REPLACE VIEW connected_friends AS
SELECT 
    u1.id as user_id,
    u2.id as friend_id,
    u2.pseudo as friend_pseudo,
    u2.status as friend_status,
    uc.created_at as connected_since
FROM user_connections uc
JOIN users u1 ON uc.user1_id = u1.id
JOIN users u2 ON uc.user2_id = u2.id
WHERE uc.status = 'connected'
UNION ALL
SELECT 
    u2.id as user_id,
    u1.id as friend_id,
    u1.pseudo as friend_pseudo,
    u1.status as friend_status,
    uc.created_at as connected_since
FROM user_connections uc
JOIN users u1 ON uc.user1_id = u1.id
JOIN users u2 ON uc.user2_id = u2.id
WHERE uc.status = 'connected';

-- Vue pour les statistiques sociales
CREATE OR REPLACE VIEW social_statistics AS
SELECT 
    u.id,
    u.pseudo,
    COUNT(DISTINCT uc.id) as total_connections,
    COUNT(DISTINCT gm.id) as total_groups,
    COUNT(DISTINCT mh.id) as total_meetings
FROM users u
LEFT JOIN user_connections uc ON (u.id = uc.user1_id OR u.id = uc.user2_id) AND uc.status = 'connected'
LEFT JOIN group_members gm ON u.id = gm.user_id
LEFT JOIN meeting_history mh ON u.id = mh.user1_id OR u.id = mh.user2_id
GROUP BY u.id, u.pseudo;

-- ============================================
-- Données de test
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

-- ============================================
-- Afficher les informations
-- ============================================

SELECT 'Multi-user tables created successfully!' AS status;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_connections FROM user_connections;

