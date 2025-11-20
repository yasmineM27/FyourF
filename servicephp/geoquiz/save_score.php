<?php
/**
 * API: Sauvegarder un score de GeoQuiz
 * Endpoint: POST /servicephp/geoquiz/save_score.php
 * 
 * Paramètres:
 * - user_id: ID de l'utilisateur
 * - total_points: Points totaux
 * - correct_answers: Nombre de réponses correctes
 * - total_questions: Nombre total de questions
 * - max_streak: Meilleur streak
 * - session_duration: Durée de la session en secondes
 */

header('Content-Type: application/json');
require_once '../config.php';

try {
    // Récupérer les données POST
    $data = json_decode(file_get_contents("php://input"), true);
    
    if (!$data) {
        throw new Exception("Données invalides");
    }

    $user_id = intval($data['user_id'] ?? 0);
    $total_points = intval($data['total_points'] ?? 0);
    $correct_answers = intval($data['correct_answers'] ?? 0);
    $total_questions = intval($data['total_questions'] ?? 0);
    $max_streak = intval($data['max_streak'] ?? 0);
    $session_duration = intval($data['session_duration'] ?? 0);

    if ($user_id <= 0 || $total_questions <= 0) {
        throw new Exception("Paramètres invalides");
    }

    // Calculer la précision
    $accuracy = ($total_questions > 0) ? ($correct_answers * 100.0) / $total_questions : 0;

    // Préparer la requête
    $sql = "INSERT INTO geoquiz_scores 
            (user_id, total_points, correct_answers, total_questions, accuracy, max_streak, session_duration, score_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Erreur de préparation: " . $conn->error);
    }

    $score_date = time() * 1000; // Timestamp en millisecondes
    
    $stmt->bind_param(
        "iiiiidii",
        $user_id,
        $total_points,
        $correct_answers,
        $total_questions,
        $accuracy,
        $max_streak,
        $session_duration,
        $score_date
    );

    if (!$stmt->execute()) {
        throw new Exception("Erreur d'exécution: " . $stmt->error);
    }

    $score_id = $stmt->insert_id;

    // Mettre à jour le leaderboard
    $update_sql = "CALL update_geoquiz_leaderboard()";
    $conn->query($update_sql);

    echo json_encode([
        'success' => true,
        'message' => 'Score sauvegardé avec succès',
        'score_id' => $score_id,
        'accuracy' => round($accuracy, 2)
    ]);

} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage()
    ]);
} finally {
    if (isset($stmt)) {
        $stmt->close();
    }
    $conn->close();
}
?>

