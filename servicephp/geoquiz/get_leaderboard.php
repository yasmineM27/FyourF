<?php
/**
 * API: Récupérer le leaderboard du GeoQuiz
 * Endpoint: GET /servicephp/geoquiz/get_leaderboard.php?limit=10&user_id=1
 * 
 * Paramètres:
 * - limit: Nombre de joueurs à retourner (défaut: 10)
 * - user_id: (optionnel) ID de l'utilisateur pour voir son rang
 */

header('Content-Type: application/json');
require_once '../config.php';

try {
    $limit = intval($_GET['limit'] ?? 10);
    $user_id = intval($_GET['user_id'] ?? 0);

    if ($limit <= 0 || $limit > 100) {
        $limit = 10;
    }

    // Récupérer le top des joueurs
    $sql = "SELECT 
                user_id,
                pseudo,
                total_points,
                total_badges,
                total_games,
                average_accuracy,
                rank,
                updated_at
            FROM geoquiz_leaderboard
            ORDER BY rank ASC
            LIMIT ?";

    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Erreur de préparation: " . $conn->error);
    }

    $stmt->bind_param("i", $limit);
    
    if (!$stmt->execute()) {
        throw new Exception("Erreur d'exécution: " . $stmt->error);
    }

    $result = $stmt->get_result();
    $leaderboard = [];

    while ($row = $result->fetch_assoc()) {
        $leaderboard[] = [
            'rank' => intval($row['rank']),
            'user_id' => intval($row['user_id']),
            'pseudo' => $row['pseudo'],
            'total_points' => intval($row['total_points']),
            'total_badges' => intval($row['total_badges']),
            'total_games' => intval($row['total_games']),
            'average_accuracy' => floatval($row['average_accuracy']),
            'updated_at' => $row['updated_at']
        ];
    }

    $response = [
        'success' => true,
        'leaderboard' => $leaderboard
    ];

    // Si user_id est fourni, ajouter son rang
    if ($user_id > 0) {
        $user_sql = "SELECT rank, total_points, pseudo FROM geoquiz_leaderboard WHERE user_id = ?";
        $user_stmt = $conn->prepare($user_sql);
        if ($user_stmt) {
            $user_stmt->bind_param("i", $user_id);
            $user_stmt->execute();
            $user_result = $user_stmt->get_result();
            
            if ($user_row = $user_result->fetch_assoc()) {
                $response['user_rank'] = intval($user_row['rank']);
                $response['user_points'] = intval($user_row['total_points']);
                $response['user_pseudo'] = $user_row['pseudo'];
            }
            $user_stmt->close();
        }
    }

    echo json_encode($response);

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

