-- ============================================
-- GeoQuiz Challenge - MySQL Setup Script
-- ============================================
-- Date: 2025-11-07
-- Description: Tables pour stocker les données du GeoQuiz Challenge

-- ============================================
-- 1. TABLE: geoquiz_questions
-- ============================================
CREATE TABLE IF NOT EXISTS geoquiz_questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    location_name VARCHAR(255),
    correct_answer VARCHAR(100) NOT NULL,
    options JSON,
    region VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    difficulty INT DEFAULT 2,
    map_image_url TEXT,
    street_view_url TEXT,
    timestamp BIGINT,
    answered BOOLEAN DEFAULT FALSE,
    correct BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_region (user_id, region),
    INDEX idx_difficulty (difficulty)
);

-- ============================================
-- 2. TABLE: geoquiz_badges
-- ============================================
CREATE TABLE IF NOT EXISTS geoquiz_badges (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    badge_id INT NOT NULL,
    badge_name VARCHAR(100) NOT NULL,
    badge_description TEXT,
    region VARCHAR(100),
    category VARCHAR(100),
    unlocked BOOLEAN DEFAULT FALSE,
    unlocked_date BIGINT,
    progress INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_badge (user_id, badge_id),
    INDEX idx_unlocked (unlocked)
);

-- ============================================
-- 3. TABLE: geoquiz_scores
-- ============================================
CREATE TABLE IF NOT EXISTS geoquiz_scores (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    total_points INT DEFAULT 0,
    correct_answers INT DEFAULT 0,
    total_questions INT DEFAULT 0,
    accuracy DECIMAL(5, 2) DEFAULT 0,
    max_streak INT DEFAULT 0,
    session_duration INT DEFAULT 0,
    score_date BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, created_at),
    INDEX idx_total_points (total_points)
);

-- ============================================
-- 4. TABLE: geoquiz_leaderboard
-- ============================================
CREATE TABLE IF NOT EXISTS geoquiz_leaderboard (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    pseudo VARCHAR(100),
    total_points INT DEFAULT 0,
    total_badges INT DEFAULT 0,
    total_games INT DEFAULT 0,
    average_accuracy DECIMAL(5, 2) DEFAULT 0,
    rank INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user (user_id),
    INDEX idx_rank (rank),
    INDEX idx_total_points (total_points)
);

-- ============================================
-- 5. TABLE: geoquiz_daily_challenges
-- ============================================
CREATE TABLE IF NOT EXISTS geoquiz_daily_challenges (
    id INT PRIMARY KEY AUTO_INCREMENT,
    challenge_date DATE NOT NULL,
    region VARCHAR(100) NOT NULL,
    difficulty INT DEFAULT 2,
    required_correct INT DEFAULT 5,
    reward_points INT DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_date_region (challenge_date, region)
);

-- ============================================
-- 6. TABLE: geoquiz_user_challenges
-- ============================================
CREATE TABLE IF NOT EXISTS geoquiz_user_challenges (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    challenge_id INT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    correct_answers INT DEFAULT 0,
    completed_date BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (challenge_id) REFERENCES geoquiz_daily_challenges(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_challenge (user_id, challenge_id)
);

-- ============================================
-- 7. VIEWS
-- ============================================

-- Vue: Top Players
CREATE OR REPLACE VIEW geoquiz_top_players AS
SELECT 
    u.id,
    u.pseudo,
    COUNT(DISTINCT gs.id) as total_games,
    SUM(gs.total_points) as total_points,
    AVG(gs.accuracy) as average_accuracy,
    COUNT(DISTINCT gb.id) as total_badges,
    ROW_NUMBER() OVER (ORDER BY SUM(gs.total_points) DESC) as rank
FROM users u
LEFT JOIN geoquiz_scores gs ON u.id = gs.user_id
LEFT JOIN geoquiz_badges gb ON u.id = gb.user_id AND gb.unlocked = TRUE
GROUP BY u.id, u.pseudo
ORDER BY total_points DESC;

-- Vue: User Statistics
CREATE OR REPLACE VIEW geoquiz_user_stats AS
SELECT 
    u.id,
    u.pseudo,
    COUNT(DISTINCT gq.id) as total_questions_answered,
    SUM(CASE WHEN gq.correct = TRUE THEN 1 ELSE 0 END) as correct_answers,
    ROUND(SUM(CASE WHEN gq.correct = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT gq.id), 2) as accuracy,
    COUNT(DISTINCT gb.id) as unlocked_badges,
    SUM(gs.total_points) as total_points
FROM users u
LEFT JOIN geoquiz_questions gq ON u.id = gq.user_id
LEFT JOIN geoquiz_badges gb ON u.id = gb.user_id AND gb.unlocked = TRUE
LEFT JOIN geoquiz_scores gs ON u.id = gs.user_id
GROUP BY u.id, u.pseudo;

-- Vue: Regional Statistics
CREATE OR REPLACE VIEW geoquiz_regional_stats AS
SELECT 
    region,
    COUNT(*) as total_questions,
    SUM(CASE WHEN correct = TRUE THEN 1 ELSE 0 END) as correct_answers,
    ROUND(SUM(CASE WHEN correct = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as accuracy,
    AVG(difficulty) as average_difficulty
FROM geoquiz_questions
GROUP BY region
ORDER BY total_questions DESC;

-- ============================================
-- 8. STORED PROCEDURES
-- ============================================

-- Procédure: Mettre à jour le leaderboard
DELIMITER //
CREATE PROCEDURE update_geoquiz_leaderboard()
BEGIN
    INSERT INTO geoquiz_leaderboard (user_id, pseudo, total_points, total_badges, total_games, average_accuracy)
    SELECT 
        u.id,
        u.pseudo,
        COALESCE(SUM(gs.total_points), 0),
        COALESCE(COUNT(DISTINCT gb.id), 0),
        COALESCE(COUNT(DISTINCT gs.id), 0),
        COALESCE(AVG(gs.accuracy), 0)
    FROM users u
    LEFT JOIN geoquiz_scores gs ON u.id = gs.user_id
    LEFT JOIN geoquiz_badges gb ON u.id = gb.user_id AND gb.unlocked = TRUE
    GROUP BY u.id, u.pseudo
    ON DUPLICATE KEY UPDATE
        total_points = VALUES(total_points),
        total_badges = VALUES(total_badges),
        total_games = VALUES(total_games),
        average_accuracy = VALUES(average_accuracy);
END //
DELIMITER ;

-- Procédure: Créer un défi quotidien
DELIMITER //
CREATE PROCEDURE create_daily_challenge(
    IN p_region VARCHAR(100),
    IN p_difficulty INT,
    IN p_required_correct INT,
    IN p_reward_points INT
)
BEGIN
    INSERT INTO geoquiz_daily_challenges (challenge_date, region, difficulty, required_correct, reward_points)
    VALUES (CURDATE(), p_region, p_difficulty, p_required_correct, p_reward_points)
    ON DUPLICATE KEY UPDATE
        difficulty = p_difficulty,
        required_correct = p_required_correct,
        reward_points = p_reward_points;
END //
DELIMITER ;

-- ============================================
-- 9. DONNÉES DE TEST
-- ============================================

-- Insérer des défis quotidiens
INSERT INTO geoquiz_daily_challenges (challenge_date, region, difficulty, required_correct, reward_points)
VALUES 
    (CURDATE(), 'Tunis', 2, 5, 100),
    (CURDATE(), 'Sfax', 2, 5, 100),
    (CURDATE(), 'Sousse', 2, 5, 100);

-- ============================================
-- 10. INDEXES SUPPLÉMENTAIRES
-- ============================================

CREATE INDEX idx_geoquiz_questions_user_date ON geoquiz_questions(user_id, created_at);
CREATE INDEX idx_geoquiz_scores_user_date ON geoquiz_scores(user_id, created_at);
CREATE INDEX idx_geoquiz_badges_user_unlocked ON geoquiz_badges(user_id, unlocked);

-- ============================================
-- FIN DU SCRIPT
-- ============================================
-- Status: ✅ COMPLET
-- Date: 2025-11-07

