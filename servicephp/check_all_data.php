<?php
/**
 * Script de Vérification Complète des Données MySQL
 * URL: http://localhost/servicephp/check_all_data.php
 */

require_once 'config.php';

header('Content-Type: application/json; charset=utf-8');

$response = array(
    'status' => 'success',
    'timestamp' => date('Y-m-d H:i:s'),
    'data' => array()
);

try {
    // Vérifier la connexion
    if ($conn->connect_error) {
        throw new Exception("Erreur de connexion: " . $conn->connect_error);
    }

    // ============================================
    // 1. VÉRIFIER LES POSITIONS
    // ============================================
    
    $sql_positions_count = "SELECT COUNT(*) as total FROM positions";
    $result = $conn->query($sql_positions_count);
    $row = $result->fetch_assoc();
    $total_positions = $row['total'];
    
    $sql_positions = "SELECT * FROM positions ORDER BY timestamp DESC LIMIT 10";
    $result = $conn->query($sql_positions);
    $positions = array();
    while ($row = $result->fetch_assoc()) {
        $positions[] = $row;
    }
    
    $response['data']['positions'] = array(
        'total' => $total_positions,
        'last_10' => $positions
    );

    // ============================================
    // 2. VÉRIFIER LES QUESTIONS
    // ============================================
    
    $sql_questions_count = "SELECT COUNT(*) as total FROM geoquiz_questions";
    $result = $conn->query($sql_questions_count);
    $row = $result->fetch_assoc();
    $total_questions = $row['total'];
    
    $sql_questions_by_region = "SELECT region, COUNT(*) as count FROM geoquiz_questions GROUP BY region";
    $result = $conn->query($sql_questions_by_region);
    $questions_by_region = array();
    while ($row = $result->fetch_assoc()) {
        $questions_by_region[] = $row;
    }
    
    $response['data']['questions'] = array(
        'total' => $total_questions,
        'by_region' => $questions_by_region
    );

    // ============================================
    // 3. VÉRIFIER LES SCORES
    // ============================================
    
    $sql_scores_count = "SELECT COUNT(*) as total FROM geoquiz_scores";
    $result = $conn->query($sql_scores_count);
    $row = $result->fetch_assoc();
    $total_scores = $row['total'];
    
    $sql_scores_stats = "SELECT 
        AVG(total_points) as avg_points,
        MAX(total_points) as max_points,
        MIN(total_points) as min_points,
        AVG(accuracy) as avg_accuracy
    FROM geoquiz_scores";
    $result = $conn->query($sql_scores_stats);
    $scores_stats = $result->fetch_assoc();
    
    $response['data']['scores'] = array(
        'total' => $total_scores,
        'statistics' => $scores_stats
    );

    // ============================================
    // 4. VÉRIFIER LES BADGES
    // ============================================
    
    $sql_badges_count = "SELECT COUNT(*) as total FROM geoquiz_badges";
    $result = $conn->query($sql_badges_count);
    $row = $result->fetch_assoc();
    $total_badges = $row['total'];
    
    $sql_badges_unlocked = "SELECT COUNT(*) as total FROM geoquiz_badges WHERE unlocked = 1";
    $result = $conn->query($sql_badges_unlocked);
    $row = $result->fetch_assoc();
    $unlocked_badges = $row['total'];
    
    $response['data']['badges'] = array(
        'total' => $total_badges,
        'unlocked' => $unlocked_badges
    );

    // ============================================
    // 5. RÉSUMÉ COMPLET
    // ============================================
    
    $response['data']['summary'] = array(
        'total_positions' => $total_positions,
        'total_questions' => $total_questions,
        'total_scores' => $total_scores,
        'total_badges' => $total_badges,
        'unlocked_badges' => $unlocked_badges
    );

    // ============================================
    // 6. VÉRIFIER L'INTÉGRITÉ DES DONNÉES
    // ============================================
    
    $integrity = array();
    
    // Vérifier les positions invalides
    $sql = "SELECT COUNT(*) as count FROM positions WHERE latitude < -90 OR latitude > 90 OR longitude < -180 OR longitude > 180";
    $result = $conn->query($sql);
    $row = $result->fetch_assoc();
    $integrity['invalid_positions'] = $row['count'];
    
    // Vérifier les questions sans réponse
    $sql = "SELECT COUNT(*) as count FROM geoquiz_questions WHERE correct_answer IS NULL OR correct_answer = ''";
    $result = $conn->query($sql);
    $row = $result->fetch_assoc();
    $integrity['questions_without_answer'] = $row['count'];
    
    // Vérifier les scores invalides
    $sql = "SELECT COUNT(*) as count FROM geoquiz_scores WHERE total_points < 0 OR correct_answers < 0 OR total_questions < 0";
    $result = $conn->query($sql);
    $row = $result->fetch_assoc();
    $integrity['invalid_scores'] = $row['count'];
    
    $response['data']['integrity'] = $integrity;

} catch (Exception $e) {
    $response['status'] = 'error';
    $response['error'] = $e->getMessage();
}

// Fermer la connexion
$conn->close();

// Retourner la réponse JSON
echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
?>

