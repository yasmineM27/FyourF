<?php
/**
 * API: Récupérer les badges d'un utilisateur
 * Endpoint: GET /servicephp/geoquiz/get_badges.php?user_id=1
 * 
 * Paramètres:
 * - user_id: ID de l'utilisateur
 * - unlocked_only: (optionnel) true pour afficher seulement les badges déverrouillés
 */

header('Content-Type: application/json');
require_once '../config.php';

try {
    $user_id = intval($_GET['user_id'] ?? 0);
    $unlocked_only = isset($_GET['unlocked_only']) && $_GET['unlocked_only'] === 'true';

    if ($user_id <= 0) {
        throw new Exception("user_id invalide");
    }

    // Construire la requête
    $sql = "SELECT 
                id,
                badge_id,
                badge_name,
                badge_description,
                region,
                category,
                unlocked,
                unlocked_date,
                progress,
                created_at
            FROM geoquiz_badges
            WHERE user_id = ?";
    
    if ($unlocked_only) {
        $sql .= " AND unlocked = TRUE";
    }
    
    $sql .= " ORDER BY unlocked DESC, progress DESC";

    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Erreur de préparation: " . $conn->error);
    }

    $stmt->bind_param("i", $user_id);
    
    if (!$stmt->execute()) {
        throw new Exception("Erreur d'exécution: " . $stmt->error);
    }

    $result = $stmt->get_result();
    $badges = [];

    while ($row = $result->fetch_assoc()) {
        $badges[] = [
            'id' => intval($row['badge_id']),
            'name' => $row['badge_name'],
            'description' => $row['badge_description'],
            'region' => $row['region'],
            'category' => $row['category'],
            'unlocked' => boolval($row['unlocked']),
            'unlocked_date' => $row['unlocked_date'],
            'progress' => intval($row['progress']),
            'created_at' => $row['created_at']
        ];
    }

    // Compter les badges
    $total_badges = count($badges);
    $unlocked_badges = count(array_filter($badges, function($b) { return $b['unlocked']; }));

    echo json_encode([
        'success' => true,
        'total_badges' => $total_badges,
        'unlocked_badges' => $unlocked_badges,
        'badges' => $badges
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

