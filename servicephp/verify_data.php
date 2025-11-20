<?php
/**
 * verify_data.php
 * Script pour vérifier toutes les données stockées dans MySQL
 * 
 * URL: http://192.168.178.115/servicephp/verify_data.php
 */

require_once 'config.php';

header('Content-Type: application/json; charset=utf-8');

$response = [
    'success' => false,
    'timestamp' => date('Y-m-d H:i:s'),
    'data' => []
];

try {
    // ============================================
    // 1. VÉRIFIER LA CONNEXION
    // ============================================
    $response['data']['connection'] = [
        'status' => 'connected',
        'server' => $server,
        'database' => $database,
        'message' => 'Connexion MySQL réussie'
    ];

    // ============================================
    // 2. COMPTER LES POSITIONS
    // ============================================
    $sql_positions = "SELECT COUNT(*) as total FROM positions";
    $result = $conn->query($sql_positions);
    $row = $result->fetch_assoc();
    $total_positions = $row['total'];

    $response['data']['positions'] = [
        'total' => $total_positions,
        'table' => 'positions'
    ];

    // Récupérer les positions
    if ($total_positions > 0) {
        $sql = "SELECT * FROM positions ORDER BY timestamp DESC LIMIT 10";
        $result = $conn->query($sql);
        $positions = [];
        while ($row = $result->fetch_assoc()) {
            $positions[] = $row;
        }
        $response['data']['positions']['recent'] = $positions;
    }

    // ============================================
    // 3. COMPTER LES QUESTIONS DU QUIZ
    // ============================================
    $sql_questions = "SELECT COUNT(*) as total FROM geoquiz_questions";
    $result = $conn->query($sql_questions);
    $row = $result->fetch_assoc();
    $total_questions = $row['total'];

    $response['data']['geoquiz_questions'] = [
        'total' => $total_questions,
        'table' => 'geoquiz_questions'
    ];

    // Récupérer les questions
    if ($total_questions > 0) {
        $sql = "SELECT id, user_id, region, category, difficulty, correct_answer FROM geoquiz_questions ORDER BY id DESC LIMIT 10";
        $result = $conn->query($sql);
        $questions = [];
        while ($row = $result->fetch_assoc()) {
            $questions[] = $row;
        }
        $response['data']['geoquiz_questions']['recent'] = $questions;
    }

    // ============================================
    // 4. COMPTER LES SCORES
    // ============================================
    $sql_scores = "SELECT COUNT(*) as total FROM geoquiz_scores";
    $result = $conn->query($sql_scores);
    $row = $result->fetch_assoc();
    $total_scores = $row['total'];

    $response['data']['geoquiz_scores'] = [
        'total' => $total_scores,
        'table' => 'geoquiz_scores'
    ];

    // Récupérer les scores
    if ($total_scores > 0) {
        $sql = "SELECT id, user_id, total_points, correct_answers, total_questions, accuracy FROM geoquiz_scores ORDER BY score_date DESC LIMIT 10";
        $result = $conn->query($sql);
        $scores = [];
        while ($row = $result->fetch_assoc()) {
            $scores[] = $row;
        }
        $response['data']['geoquiz_scores']['recent'] = $scores;
    }

    // ============================================
    // 5. COMPTER LES BADGES
    // ============================================
    $sql_badges = "SELECT COUNT(*) as total FROM geoquiz_badges";
    $result = $conn->query($sql_badges);
    $row = $result->fetch_assoc();
    $total_badges = $row['total'];

    $response['data']['geoquiz_badges'] = [
        'total' => $total_badges,
        'table' => 'geoquiz_badges'
    ];

    // Récupérer les badges
    if ($total_badges > 0) {
        $sql = "SELECT id, user_id, badge_id, unlocked, progress FROM geoquiz_badges ORDER BY id DESC LIMIT 10";
        $result = $conn->query($sql);
        $badges = [];
        while ($row = $result->fetch_assoc()) {
            $badges[] = $row;
        }
        $response['data']['geoquiz_badges']['recent'] = $badges;
    }

    // ============================================
    // 6. RÉSUMÉ STATISTIQUE
    // ============================================
    $response['data']['summary'] = [
        'total_positions' => $total_positions,
        'total_questions' => $total_questions,
        'total_scores' => $total_scores,
        'total_badges' => $total_badges,
        'all_tables_exist' => true
    ];

    $response['success'] = true;
    $response['message'] = 'Vérification des données réussie';

} catch (Exception $e) {
    $response['success'] = false;
    $response['error'] = $e->getMessage();
    http_response_code(500);
}

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
?>

